package com.quietjun.ssafymng.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quietjun.ssafymng.dto.AdminScriptDto;
import com.quietjun.ssafymng.entity.AdminScript;
import com.quietjun.ssafymng.repository.AdminScriptRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminScriptService {

    private final AdminScriptRepository adminScriptRepository;

    @Transactional(readOnly = true)
    public List<AdminScriptDto> getAllScripts() {
        return adminScriptRepository.findAllByOrderByOrderIndexAscIdAsc()
                .stream()
                .map(AdminScript::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AdminScriptDto saveScript(AdminScriptDto dto) {
        AdminScript script;
        if (dto.getId() != null) {
            script = adminScriptRepository.findById(dto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스크립트 ID: " + dto.getId()));
            script.setTitle(dto.getTitle());
            script.setDescription(dto.getDescription());
            script.setScriptContent(dto.getScriptContent());
            script.setOrderIndex(dto.getOrderIndex());
        } else {
            script = AdminScript.builder()
                    .title(dto.getTitle())
                    .description(dto.getDescription())
                    .scriptContent(dto.getScriptContent())
                    .orderIndex(dto.getOrderIndex())
                    .build();
        }
        return adminScriptRepository.save(script).toDto();
    }

    @Transactional
    public void deleteScript(Long id) {
        adminScriptRepository.deleteById(id);
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initSeedScripts() {
        if (adminScriptRepository.count() > 0) {
            return;
        }
        log.info("Initializing Default Tutor Admin Scripts...");

        // 1. tutor에서 학생 정보 가져오기
        adminScriptRepository.save(AdminScript.builder()
                .title("1. Tutor 학생 정보(학번/이름) 가져오기")
                .description("학생 목록 그리드에서 학번과 이름을 탭(\\t) 구분 텍스트로 추출하여 콘솔에 출력")
                .scriptContent("""
let sn = "";

const trs1 = document.querySelectorAll("#gridList > tbody > tr > td:nth-child(2)");
const trs2 = document.querySelectorAll("#gridList > tbody > tr > td:nth-child(3)");
for(let i=0; i<trs1.length; i++){
    sn+=trs1[i].title+"\\t"+trs2[i].title+"\\n";
}
console.log(sn)
""".trim())
                .orderIndex(1)
                .build());

        // 2. tutor에서 재시험자 대상 원 점수 확인하기
        adminScriptRepository.save(AdminScript.builder()
                .title("2. Tutor 재시험자 대상 원 점수 확인하기")
                .description("재시험 응시자 채점 페이지에서 정답(Y)/오답(N) 집계 및 원래 점수 합산 계산")
                .scriptContent("""
let points = document.querySelectorAll("[name^='grdng']")
//console.log(points);
let score=0, ys=0, ns=0, no=1;
points.forEach(item =>{
   //console.log(no++, ":점수: ", item.value);
   let tr = item.parentNode.parentNode;
   const answerNode = tr.querySelector("td:nth-of-type(2)");
   const answerRadio = answerNode.querySelector("[checked='checked']"); // 수정부분
   //console.log("answerRadio:", answerRadio);
   const yn = answerRadio?answerRadio.value: answerNode.innerHTML.trim();
   //console.log("YN: ",yn);
   if(yn==='Y'){
      ys++;
      score+=parseInt(item.value);
   }else{
      ns++;      
   }   
})
console.log("점수:", score, ", 정답: ",ys, ", 오답: ",ns, ", 총 개수: ",ys+ns)
""".trim())
                .orderIndex(2)
                .build());

        // 3. tutor에서 시험점수 가져오기
        adminScriptRepository.save(AdminScript.builder()
                .title("3. Tutor 시험 점수 가져오기 (학번/이름/아이디/점수)")
                .description("그리드 목록에서 학번, 이름, 로그인아이디, 점수를 탭 구분 및 SQL 튜플 형식으로 추출")
                .scriptContent("""
let total = "";
let tscript = "";
let trs = document.querySelectorAll("#gridList > tbody > tr");
trs.forEach(function(tr){
    let tds = tr.querySelectorAll("td");
    let sno = tds[1].querySelector("a");
    if(sno){
        let name = tds[2].innerHTML;
        let loginid = tds[3].querySelector("a").innerHTML;
        loginid = loginid.substr(0, loginid.indexOf("@"));
        let score = tds[11].innerHTML;
        tscript += sno.innerHTML + "\\t" + name + "\\t" + loginid + "\\t" + score + "\\n";
        total += '("' + sno.innerHTML + '","' + name + '","' + loginid + '","' + score + '",""),\\n';
    }
});
console.log(tscript);
console.log(total);
""".trim())
                .orderIndex(3)
                .build());

        // 4. tutor에서 실기 평가 점수 가져오기
        adminScriptRepository.save(AdminScript.builder()
                .title("4. Tutor 실기 평가 점수 가져오기")
                .description("실기 평가 그리드에서 학번, 이름, 실기점수를 탭 구분 및 SQL 형식으로 추출")
                .scriptContent("""
let total = "";
let tscript = "";
let trs = document.querySelectorAll("#gridList > tbody > tr");
trs.forEach(function(tr){
    let tds = tr.querySelectorAll("td");
    let sno = tds[4].innerHTML;
    console.log(sno);
    if(sno){
        let name = tds[3].querySelector("a").innerHTML;
        let score = tds[13].innerHTML;
        tscript += sno + "\\t" + name + "\\t" + score + "\\n";
        total += '("' + sno + '","' + name + '","' + score + '",""),\\n';
    }
});
console.log(tscript);
console.log(total);
""".trim())
                .orderIndex(4)
                .build());

        log.info("Initialized 4 default Tutor Admin Scripts successfully.");
    }
}
