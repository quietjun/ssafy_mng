package com.quietjun.ssafymng.controller;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quietjun.ssafymng.service.BackupService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/backup")
@RequiredArgsConstructor
public class BackupController {

    private final BackupService backupService;

    public static final String DEFAULT_REMOTE_HOST = "150.230.206.18";
    public static final int DEFAULT_REMOTE_PORT = 3306;
    public static final String DEFAULT_REMOTE_DB = "ssafy_db";
    public static final String DEFAULT_REMOTE_USER = "quietjun";
    public static final String DEFAULT_REMOTE_PASS = "dmstj@0205M";

    @PostMapping("/cloud-to-local")
    public ResponseEntity<?> syncCloudToLocal(@RequestBody(required = false) Map<String, Object> params) {
        String host = (params != null && params.containsKey("remoteHost")) 
                ? (String) params.get("remoteHost") : DEFAULT_REMOTE_HOST;
        int port = (params != null && params.containsKey("remotePort")) 
                ? Integer.parseInt(String.valueOf(params.get("remotePort"))) : DEFAULT_REMOTE_PORT;
        String db = (params != null && params.containsKey("remoteDb")) 
                ? (String) params.get("remoteDb") : DEFAULT_REMOTE_DB;
        String user = (params != null && params.containsKey("remoteUser")) 
                ? (String) params.get("remoteUser") : DEFAULT_REMOTE_USER;
        String pass = (params != null && params.containsKey("remotePassword")) 
                ? (String) params.get("remotePassword") : DEFAULT_REMOTE_PASS;

        try {
            Map<String, Object> result = backupService.syncCloudToLocal(host, port, db, user, pass);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Cloud DB to Local sync failed", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "클라우드 DB 백업 실패: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/export-sql")
    public ResponseEntity<?> exportSql(
            @RequestParam(required = false, defaultValue = DEFAULT_REMOTE_HOST) String remoteHost,
            @RequestParam(required = false, defaultValue = "3306") int remotePort,
            @RequestParam(required = false, defaultValue = DEFAULT_REMOTE_DB) String remoteDb,
            @RequestParam(required = false, defaultValue = DEFAULT_REMOTE_USER) String remoteUser,
            @RequestParam(required = false, defaultValue = DEFAULT_REMOTE_PASS) String remotePassword) {

        try {
            File sqlFile = backupService.dumpCloudDbToSqlFile(remoteHost, remotePort, remoteDb, remoteUser, remotePassword);
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = "ssafy_db_oracle_backup_" + timestamp + ".sql";

            InputStreamResource resource = new InputStreamResource(new FileInputStream(sqlFile) {
                @Override
                public void close() throws java.io.IOException {
                    super.close();
                    sqlFile.delete(); // 다운로드 완료 후 임시 파일 삭제
                }
            });

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(sqlFile.length())
                .body(resource);

        } catch (Exception e) {
            log.error("SQL Export failed", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "SQL 덤프 생성 실패: " + e.getMessage()));
        }
    }
}
