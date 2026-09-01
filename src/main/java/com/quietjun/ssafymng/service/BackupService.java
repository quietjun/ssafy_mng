package com.quietjun.ssafymng.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BackupService {

    @Value("${spring.datasource.url}")
    private String localDbUrl;

    @Value("${spring.datasource.username}")
    private String localDbUsername;

    @Value("${spring.datasource.password}")
    private String localDbPassword;

    @Value("${app.backup.mysqldump-path:}")
    private String customMysqldumpPath;

    @Value("${app.backup.mysql-path:}")
    private String customMysqlPath;

    public Map<String, Object> syncCloudToLocal(
            String remoteHost, 
            int remotePort, 
            String remoteDb, 
            String remoteUser, 
            String remotePassword) throws Exception {

        long startTime = System.currentTimeMillis();
        log.info("Starting Cloud DB to Local DB backup from {}:{}/{}", remoteHost, remotePort, remoteDb);

        // 1. 임시 덤프 파일 경로 설정
        File tempSqlFile = File.createTempFile("cloud_db_backup_", ".sql");
        tempSqlFile.deleteOnExit();

        try {
            // 2. mysqldump 실행 (stdout만 sql 파일로 리다이렉트, stderr는 분리)
            executeMysqldump(remoteHost, remotePort, remoteDb, remoteUser, remotePassword, tempSqlFile);

            if (tempSqlFile.length() == 0) {
                throw new RuntimeException("생성된 SQL 덤프 파일이 비어 있습니다.");
            }

            // 3. 로컬 MySQL로 덤프 파일 임포트
            String mysqlCmd = findExecutable("mysql");
            String localHost = extractHostFromUrl(localDbUrl, "127.0.0.1");
            int localPort = extractPortFromUrl(localDbUrl, 3306);
            String localDatabase = extractDbNameFromUrl(localDbUrl, "ssafy_db");

            ProcessBuilder importPb = new ProcessBuilder(
                mysqlCmd,
                "-h", localHost,
                "-P", String.valueOf(localPort),
                "-u", localDbUsername,
                localDatabase
            );
            importPb.environment().put("MYSQL_PWD", localDbPassword);
            importPb.redirectInput(tempSqlFile);
            importPb.redirectErrorStream(true);

            Process importProcess;
            try {
                importProcess = importPb.start();
            } catch (Exception e) {
                throw new RuntimeException("로컬 DB 복원 도구('" + mysqlCmd + "')를 실행할 수 없습니다. MySQL 이 설치되어 있는지 확인하거나 application.properties 에 app.backup.mysql-path 경로를 설정해 주세요.", e);
            }

            StringBuilder importLog = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(importProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    importLog.append(line).append("\n");
                }
            }
            int importExit = importProcess.waitFor();
            if (importExit != 0) {
                throw new RuntimeException("로컬 DB 복원 실패 (종료 코드: " + importExit + "): " + importLog);
            }

            // 4. 로컬 DB 동기화 결과 통계 조회
            Map<String, Long> tableCounts = getTableCounts(localDbUrl, localDbUsername, localDbPassword);

            long duration = System.currentTimeMillis() - startTime;
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("message", "클라우드 DB가 로컬 DB로 성공적으로 백업 및 동기화되었습니다.");
            result.put("durationMs", duration);
            result.put("fileSizeBytes", tempSqlFile.length());
            result.put("tableCounts", tableCounts);
            return result;

        } finally {
            if (tempSqlFile.exists()) {
                tempSqlFile.delete();
            }
        }
    }

    public File dumpCloudDbToSqlFile(
            String remoteHost, 
            int remotePort, 
            String remoteDb, 
            String remoteUser, 
            String remotePassword) throws Exception {

        File tempSqlFile = File.createTempFile("ssafy_db_oracle_backup_", ".sql");
        executeMysqldump(remoteHost, remotePort, remoteDb, remoteUser, remotePassword, tempSqlFile);
        if (tempSqlFile.length() == 0) {
            throw new RuntimeException("SQL 덤프 생성 실패: 파일이 비어 있습니다.");
        }
        return tempSqlFile;
    }

    private void executeMysqldump(
            String remoteHost, 
            int remotePort, 
            String remoteDb, 
            String remoteUser, 
            String remotePassword, 
            File outputSqlFile) throws Exception {

        String mysqldumpCmd = findExecutable("mysqldump");
        List<String> command = new ArrayList<>();
        command.add(mysqldumpCmd);
        command.add("-h");
        command.add(remoteHost);
        command.add("-P");
        command.add(String.valueOf(remotePort));
        command.add("-u");
        command.add(remoteUser);
        command.add("--single-transaction");
        command.add("--routines");
        command.add("--triggers");
        command.add("--set-gtid-purged=OFF");
        command.add("--no-tablespaces");
        command.add("--skip-column-statistics");
        command.add("--databases");
        command.add(remoteDb);

        ProcessBuilder dumpPb = new ProcessBuilder(command);
        dumpPb.environment().put("MYSQL_PWD", remotePassword);
        dumpPb.redirectOutput(outputSqlFile);
        dumpPb.redirectErrorStream(false); // CRITICAL: stderr가 stdout(SQL 파일)에 섞이지 않도록 분리

        Process dumpProcess;
        try {
            dumpProcess = dumpPb.start();
        } catch (Exception e) {
            throw new RuntimeException("mysqldump 실행 프로그램('" + mysqldumpCmd + "')을 찾을 수 없거나 실행하지 못했습니다. MySQL이 설치되어 있는지 확인하거나 application.properties 에 app.backup.mysqldump-path 경로를 설정해 주세요.", e);
        }

        // stderr는 별도로 읽어서 로그에만 기록
        StringBuilder stderrLog = new StringBuilder();
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(dumpProcess.getErrorStream()))) {
            String line;
            while ((line = errorReader.readLine()) != null) {
                stderrLog.append(line).append("\n");
            }
        }

        int dumpExit = dumpProcess.waitFor();
        if (stderrLog.length() > 0) {
            log.warn("mysqldump stderr output:\n{}", stderrLog);
        }

        if (dumpExit != 0 && outputSqlFile.length() == 0) {
            throw new RuntimeException("mysqldump 실패 (종료 코드: " + dumpExit + "): " + stderrLog);
        }
    }

    private Map<String, Long> getTableCounts(String url, String user, String pass) {
        Map<String, Long> counts = new LinkedHashMap<>();
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {

            String[] tables = {
                "config_metadata",
                "students",
                "exams",
                "exam_scores",
                "platform_sites",
                "problems",
                "submissions",
                "pair_histories",
                "admin_scripts"
            };

            for (String table : tables) {
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table)) {
                    if (rs.next()) {
                        counts.put(table, rs.getLong(1));
                    }
                } catch (Exception ignored) {
                    counts.put(table, -1L);
                }
            }
        } catch (Exception e) {
            log.error("Failed to query table counts", e);
        }
        return counts;
    }

    private String findExecutable(String name) {
        if ("mysqldump".equals(name) && customMysqldumpPath != null && !customMysqldumpPath.trim().isEmpty()) {
            File f = new File(customMysqldumpPath);
            if (f.exists()) return f.getAbsolutePath();
        }
        if ("mysql".equals(name) && customMysqlPath != null && !customMysqlPath.trim().isEmpty()) {
            File f = new File(customMysqlPath);
            if (f.exists()) return f.getAbsolutePath();
        }

        List<String> candidates = new ArrayList<>();

        // Linux / macOS standard binary paths
        candidates.add("/opt/homebrew/bin/" + name);
        candidates.add("/usr/local/bin/" + name);
        candidates.add("/usr/bin/" + name);

        // Windows common MySQL / MariaDB installation paths
        String ext = isWindows() ? ".exe" : "";
        String exeName = name + ext;

        candidates.add("C:\\Program Files\\MySQL\\MySQL Server 8.4\\bin\\" + exeName);
        candidates.add("C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\" + exeName);
        candidates.add("C:\\Program Files\\MySQL\\MySQL Server 5.7\\bin\\" + exeName);
        candidates.add("C:\\Program Files\\MySQL\\MySQL Workbench 8.0\\" + exeName);
        candidates.add("C:\\Program Files\\MySQL\\MySQL Workbench 8.0 CE\\" + exeName);
        candidates.add("C:\\Program Files\\MariaDB 10.11\\bin\\" + exeName);
        candidates.add("C:\\Program Files\\MariaDB 10.6\\bin\\" + exeName);
        candidates.add("C:\\xampp\\mysql\\bin\\" + exeName);

        // System PATH environment directories
        String sysPath = System.getenv("PATH");
        if (sysPath != null) {
            for (String dir : sysPath.split(File.pathSeparator)) {
                if (dir != null && !dir.trim().isEmpty()) {
                    candidates.add(new File(dir.trim(), exeName).getAbsolutePath());
                    candidates.add(new File(dir.trim(), name).getAbsolutePath());
                }
            }
        }

        for (String path : candidates) {
            File f = new File(path);
            if (f.exists() && f.isFile()) {
                return f.getAbsolutePath();
            }
        }

        return name;
    }

    private boolean isWindows() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().contains("win");
    }

    private String extractHostFromUrl(String url, String defaultHost) {
        try {
            String clean = url.replace("jdbc:mysql://", "");
            int slashIdx = clean.indexOf("/");
            String hostPort = (slashIdx != -1) ? clean.substring(0, slashIdx) : clean;
            if (hostPort.contains(":")) {
                return hostPort.split(":")[0];
            }
            return hostPort;
        } catch (Exception e) {
            return defaultHost;
        }
    }

    private int extractPortFromUrl(String url, int defaultPort) {
        try {
            String clean = url.replace("jdbc:mysql://", "");
            int slashIdx = clean.indexOf("/");
            String hostPort = (slashIdx != -1) ? clean.substring(0, slashIdx) : clean;
            if (hostPort.contains(":")) {
                return Integer.parseInt(hostPort.split(":")[1]);
            }
            return defaultPort;
        } catch (Exception e) {
            return defaultPort;
        }
    }

    private String extractDbNameFromUrl(String url, String defaultDb) {
        try {
            String clean = url.replace("jdbc:mysql://", "");
            int slashIdx = clean.indexOf("/");
            if (slashIdx != -1) {
                String dbPart = clean.substring(slashIdx + 1);
                int qIdx = dbPart.indexOf("?");
                return (qIdx != -1) ? dbPart.substring(0, qIdx) : dbPart;
            }
            return defaultDb;
        } catch (Exception e) {
            return defaultDb;
        }
    }
}
