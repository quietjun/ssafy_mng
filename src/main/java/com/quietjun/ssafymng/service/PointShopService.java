package com.quietjun.ssafymng.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quietjun.ssafymng.dto.ShopItemDto;
import com.quietjun.ssafymng.dto.ShopProfileDto;
import com.quietjun.ssafymng.entity.Role;
import com.quietjun.ssafymng.entity.Student;
import com.quietjun.ssafymng.repository.StudentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointShopService {

    private final StudentRepository studentRepository;

    // 카탈로그 데이터 정의
    private static final List<ShopItemDto> CATALOG = List.of(
            // === 1. 아바타 (AVATAR) ===
            ShopItemDto.builder()
                    .id("robot")
                    .name("싸피봇")
                    .category("AVATAR")
                    .icon("🤖")
                    .description("싸피의 믿음직한 기본 코딩 로봇 아바타")
                    .price(0)
                    .defaultOwned(true)
                    .build(),
            ShopItemDto.builder()
                    .id("cat")
                    .name("자바 고양이")
                    .category("AVATAR")
                    .icon("🐱")
                    .description("밤샘 코딩을 지켜보는 귀여운 자바 고양이")
                    .price(30)
                    .defaultOwned(false)
                    .build(),
            ShopItemDto.builder()
                    .id("wizard")
                    .name("알고리즘 마법사")
                    .category("AVATAR")
                    .icon("🧙‍♂️")
                    .description("복잡한 시간복잡도를 마법처럼 단축시키는 마법사")
                    .price(50)
                    .defaultOwned(false)
                    .build(),
            ShopItemDto.builder()
                    .id("ninja")
                    .name("버그 슬레이어 닌자")
                    .category("AVATAR")
                    .icon("🥷")
                    .description("눈 깜짝할 사이에 런타임 에러를 베어버리는 닌자")
                    .price(60)
                    .defaultOwned(false)
                    .build(),
            ShopItemDto.builder()
                    .id("alchemist")
                    .name("카페인 연금술사")
                    .category("AVATAR")
                    .icon("☕")
                    .description("아메리카노를 코드로 변환하는 연금술사")
                    .price(40)
                    .defaultOwned(false)
                    .build(),
            ShopItemDto.builder()
                    .id("dragon")
                    .name("골드 드래곤")
                    .category("AVATAR")
                    .icon("🐉")
                    .description("알고리즘 탑의 정점에 도달한 전설의 드래곤")
                    .price(100)
                    .defaultOwned(false)
                    .build(),

            // === 2. 프로필 프레임/오라 (FRAME) ===
            ShopItemDto.builder()
                    .id("none")
                    .name("기본 테두리")
                    .category("FRAME")
                    .icon("⭕")
                    .description("깔끔하고 단정한 기본 프레임")
                    .price(0)
                    .defaultOwned(true)
                    .build(),
            ShopItemDto.builder()
                    .id("frame-cyan")
                    .name("사이버 시안 네온")
                    .category("FRAME")
                    .icon("💫")
                    .description("푸른 네온 빛으로 빛나는 사이버 테두리")
                    .price(30)
                    .previewColor("#38bdf8")
                    .previewClass("frame-cyan")
                    .build(),
            ShopItemDto.builder()
                    .id("frame-purple")
                    .name("RGB 오로라 글로우")
                    .category("FRAME")
                    .icon("✨")
                    .description("신비로운 보랏빛 오로라가 감도는 테두리")
                    .price(40)
                    .previewColor("#c084fc")
                    .previewClass("frame-purple")
                    .build(),
            ShopItemDto.builder()
                    .id("frame-gold")
                    .name("챔피언 골드 플레임")
                    .category("FRAME")
                    .icon("🔥")
                    .description("타오르는 금빛 불꽃 아우라")
                    .price(70)
                    .previewColor("#fbbf24")
                    .previewClass("frame-gold")
                    .build(),

            // === 3. 화면 테마 (THEME) ===
            ShopItemDto.builder()
                    .id("default")
                    .name("다크 슬레이트 (기본)")
                    .category("THEME")
                    .icon("🌑")
                    .description("안정적이고 편안한 기본 다크 모드")
                    .price(0)
                    .defaultOwned(true)
                    .previewColor("#0f172a")
                    .build(),
            ShopItemDto.builder()
                    .id("cyberpunk")
                    .name("사이버펑크 네온")
                    .category("THEME")
                    .icon("🌌")
                    .description("시안 & 네온 핑크의 화려하고 미래지향적인 감성")
                    .price(50)
                    .previewColor("#090d16")
                    .build(),
            ShopItemDto.builder()
                    .id("matrix")
                    .name("매트릭스 해커 터미널")
                    .category("THEME")
                    .icon("📟")
                    .description("녹색 디지털 글리치와 정통 해커의 감성")
                    .price(50)
                    .previewColor("#051608")
                    .build(),
            ShopItemDto.builder()
                    .id("midnight")
                    .name("미드나잇 코지 카페")
                    .category("THEME")
                    .icon("☕")
                    .description("따뜻한 앰버 톤과 포근한 심야 카페 분위기")
                    .price(50)
                    .previewColor("#181412")
                    .build(),
            ShopItemDto.builder()
                    .id("retro")
                    .name("8비트 아케이드")
                    .category("THEME")
                    .icon("🎮")
                    .description("픽셀 레트로 게임 콘솔 분위기")
                    .price(50)
                    .previewColor("#1a102f")
                    .build(),
            ShopItemDto.builder()
                    .id("aurora")
                    .name("딥 오로라 퍼플")
                    .category("THEME")
                    .icon("🔮")
                    .description("몽환적이고 감각적인 딥 인디고 & 바이올렛 테마")
                    .price(50)
                    .previewColor("#130e26")
                    .build(),

            // === 4. 명예 칭호 (TITLE) ===
            ShopItemDto.builder()
                    .id("새싹 개발자")
                    .name("새싹 개발자")
                    .category("TITLE")
                    .icon("🌱")
                    .description("성장 가능성이 무궁무진한 기본 칭호")
                    .price(0)
                    .defaultOwned(true)
                    .build(),
            ShopItemDto.builder()
                    .id("D1 학살자")
                    .name("D1 학살자")
                    .category("TITLE")
                    .icon("⚔️")
                    .description("D1 난이도는 3분 컷하는 당당한 칭호")
                    .price(20)
                    .build(),
            ShopItemDto.builder()
                    .id("백준 골드 원정대")
                    .name("백준 골드 원정대")
                    .category("TITLE")
                    .icon("🧭")
                    .description("골드 문제를 두려워하지 않는 알고리즘 원정대원")
                    .price(40)
                    .build(),
            ShopItemDto.builder()
                    .id("메모리 최적화 장인")
                    .name("메모리 최적화 장인")
                    .category("TITLE")
                    .icon("🧠")
                    .description("단 1KB의 낭비도 허용하지 않는 엄격한 최적화 장인")
                    .price(50)
                    .build(),
            ShopItemDto.builder()
                    .id("칼퇴의 요정")
                    .name("칼퇴의 요정")
                    .category("TITLE")
                    .icon("🧚")
                    .description("과제가 나오자마자 빛의 속도로 제출하고 칼퇴하는 요정")
                    .price(60)
                    .build(),
            ShopItemDto.builder()
                    .id("알고리즘 연금술사")
                    .name("알고리즘 연금술사")
                    .category("TITLE")
                    .icon("🧪")
                    .description("에러를 금빛 정답으로 연성해내는 자")
                    .price(80)
                    .build(),

            // === 5. 명품 패션 배너 스킨 (BANNER) ===
            ShopItemDto.builder()
                    .id("banner-default")
                    .name("클래식 다크 미니멀")
                    .category("BANNER")
                    .icon("🖤")
                    .description("깔끔하고 정돈된 기본 다크 슬레이트 배너")
                    .price(0)
                    .defaultOwned(true)
                    .previewClass("banner-default")
                    .build(),
            ShopItemDto.builder()
                    .id("banner-louis-monogram")
                    .name("클래식 모노그램 르 브라운 (LV)")
                    .category("BANNER")
                    .icon("💼")
                    .description("다크 초콜릿 바탕에 빛나는 골드 플로럴 모노그램 시그니처")
                    .price(60)
                    .previewClass("banner-louis-monogram")
                    .build(),
            ShopItemDto.builder()
                    .id("banner-gucci-stripe")
                    .name("시그니처 레드&그린 웹 (GUCCI)")
                    .category("BANNER")
                    .icon("🐍")
                    .description("매트 블랙 레더 & 아이코닉 그린/레드/그린 3선 웹 스트라이프")
                    .price(60)
                    .previewClass("banner-gucci-stripe")
                    .build(),
            ShopItemDto.builder()
                    .id("banner-burberry-check")
                    .name("헤리티지 런던 노바 체크 (BURBERRY)")
                    .category("BANNER")
                    .icon("🧣")
                    .description("카멜 베이지 톤과 블랙/화이트/레드 교차 클래식 체크 패턴")
                    .price(50)
                    .previewClass("banner-burberry-check")
                    .build(),
            ShopItemDto.builder()
                    .id("banner-goyard-chevron")
                    .name("파리지앵 쉐브론 Y-도트 (GOYARD)")
                    .category("BANNER")
                    .icon("⛵")
                    .description("장인의 손길이 담긴 3D 점묘 쉐브론 Y자 입체 패턴")
                    .price(60)
                    .previewClass("banner-goyard-chevron")
                    .build(),
            ShopItemDto.builder()
                    .id("banner-chanel-quilted")
                    .name("타임리스 블랙 매트 퀼팅 (CHANEL)")
                    .category("BANNER")
                    .icon("💎")
                    .description("럭셔리 블랙 가죽 다이아몬드 퀼팅 엠보싱과 골드 스티치")
                    .price(70)
                    .previewClass("banner-chanel-quilted")
                    .build(),
            ShopItemDto.builder()
                    .id("banner-dior-oblique")
                    .name("오블리크 럭셔리 네이비 (DIOR)")
                    .category("BANNER")
                    .icon("🌟")
                    .description("딥 네이비 & 베이지 사선 자카드 타이포그래피 모노그램")
                    .price(60)
                    .previewClass("banner-dior-oblique")
                    .build(),
            ShopItemDto.builder()
                    .id("banner-louis-azur")
                    .name("다미에 아주르 화이트 (LV)")
                    .category("BANNER")
                    .icon("🏖️")
                    .description("휴양지의 햇살을 담은 아이보리 & 리비에라 블루 체크 패턴")
                    .price(60)
                    .previewClass("banner-louis-azur")
                    .build(),
            ShopItemDto.builder()
                    .id("banner-hermes-orange")
                    .name("시그니처 오렌지 & 골드 (HERMÈS)")
                    .category("BANNER")
                    .icon("🍊")
                    .description("화사하고 에너제틱한 시그니처 탠저린 오렌지와 샴페인 골드 스티치")
                    .price(70)
                    .previewClass("banner-hermes-orange")
                    .build(),
            ShopItemDto.builder()
                    .id("banner-tiffany-blue")
                    .name("로빈스에그 아쿠아 민트 (TIFFANY)")
                    .category("BANNER")
                    .icon("🩵")
                    .description("맑고 청량한 티파니 민트 블루와 실키 화이트 리본 패턴")
                    .price(60)
                    .previewClass("banner-tiffany-blue")
                    .build(),
            ShopItemDto.builder()
                    .id("banner-dior-toile")
                    .name("트왈 드 주이 크림 블랑 (DIOR)")
                    .category("BANNER")
                    .icon("🕊️")
                    .description("우아한 크림 아이보리 바탕에 세룰리안 블루 식물/자연 일러스트 패턴")
                    .price(70)
                    .previewClass("banner-dior-toile")
                    .build(),
            ShopItemDto.builder()
                    .id("banner-chanel-tweed")
                    .name("크루즈 파스텔 핑크 트위드 (CHANEL)")
                    .category("BANNER")
                    .icon("🌸")
                    .description("화이트, 베이비 핑크, 골드 펄 스레드가 어우러진 화사한 오뜨 꾸뛰르 트위드")
                    .price(70)
                    .previewClass("banner-chanel-tweed")
                    .build(),
            ShopItemDto.builder()
                    .id("banner-goyard-blanc")
                    .name("고야딘 블랑 퓨어 화이트 (GOYARD)")
                    .category("BANNER")
                    .icon("🤍")
                    .description("가장 희소성 높은 화이트 캔버스 위의 그레이 & 베이지 쉐브론 점묘 패턴")
                    .price(60)
                    .previewClass("banner-goyard-blanc")
                    .build(),
            ShopItemDto.builder()
                    .id("banner-gucci-flora")
                    .name("플로라 가든 블룸 (GUCCI)")
                    .category("BANNER")
                    .icon("🌺")
                    .description("그레이스 켈리를 기리는 로맨틱한 파스텔 플라워 & 아이보리 가든 패턴")
                    .price(60)
                    .previewClass("banner-gucci-flora")
                    .build()
    );

    public List<ShopItemDto> getCatalog() {
        return CATALOG;
    }

    @Transactional
    public ShopProfileDto getProfile(String sno) {
        Student student = studentRepository.findById(sno)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다: " + sno));

        // 관리자 테스트 지원: 관리자 초기 접속 시 1,000,000P 지급 (구매 차감 내역 정상 유지)
        if (student.getRole() == Role.ROLE_ADMIN && student.getPoints() == 0 && student.getTotalPointsEarned() == 0) {
            student.setPoints(1000000);
            student.setTotalPointsEarned(1000000);
            studentRepository.save(student);
        }

        Set<String> unlockedSet = parseUnlockedItems(student.getUnlockedItems());
        // 기본 제공 아이템은 항상 포함
        CATALOG.stream().filter(ShopItemDto::isDefaultOwned).forEach(i -> unlockedSet.add(i.getId()));

        return ShopProfileDto.builder()
                .sno(student.getSno())
                .name(student.getName())
                .points(student.getPoints())
                .totalPointsEarned(student.getTotalPointsEarned())
                .equippedAvatar(student.getEquippedAvatar() != null ? student.getEquippedAvatar() : "robot")
                .equippedFrame(student.getEquippedFrame() != null ? student.getEquippedFrame() : "none")
                .equippedTheme(student.getEquippedTheme() != null ? student.getEquippedTheme() : "default")
                .equippedTitle(student.getEquippedTitle() != null ? student.getEquippedTitle() : "새싹 개발자")
                .equippedBanner(student.getEquippedBanner() != null ? student.getEquippedBanner() : "banner-default")
                .unlockedItemIds(new ArrayList<>(unlockedSet))
                .build();
    }

    @Transactional
    public ShopProfileDto buyItem(String sno, String itemId) {
        Student student = studentRepository.findById(sno)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다: " + sno));

        ShopItemDto item = CATALOG.stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이템입니다: " + itemId));

        Set<String> unlockedSet = parseUnlockedItems(student.getUnlockedItems());
        if (unlockedSet.contains(itemId) || item.isDefaultOwned()) {
            throw new IllegalStateException("이미 보유하고 있는 아이템입니다.");
        }

        if (student.getPoints() < item.getPrice()) {
            throw new IllegalStateException(String.format("포인트가 부족합니다. (필요: %dP, 보유: %dP)", item.getPrice(), student.getPoints()));
        }

        student.setPoints(student.getPoints() - item.getPrice());
        unlockedSet.add(itemId);
        student.setUnlockedItems(String.join(",", unlockedSet));

        studentRepository.save(student);
        log.info("학생 [{}] 아이템 구매 완료: {} (차감: {}P, 잔여: {}P)", 
                student.getName(), item.getName(), item.getPrice(), student.getPoints());

        return getProfile(sno);
    }

    @Transactional
    public ShopProfileDto equipItem(String sno, String category, String itemId) {
        Student student = studentRepository.findById(sno)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다: " + sno));

        ShopItemDto item = CATALOG.stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이템입니다: " + itemId));

        Set<String> unlockedSet = parseUnlockedItems(student.getUnlockedItems());
        if (!item.isDefaultOwned() && !unlockedSet.contains(itemId)) {
            throw new IllegalStateException("보유하지 않은 아이템은 장착할 수 없습니다. 먼저 구매해 주세요.");
        }

        switch (category.toUpperCase()) {
            case "AVATAR" -> student.setEquippedAvatar(itemId);
            case "FRAME" -> student.setEquippedFrame(itemId);
            case "THEME" -> student.setEquippedTheme(itemId);
            case "TITLE" -> student.setEquippedTitle(itemId);
            case "BANNER" -> student.setEquippedBanner(itemId);
            default -> throw new IllegalArgumentException("알 수 없는 카테고리입니다: " + category);
        }

        studentRepository.save(student);
        log.info("학생 [{}] 아이템 장착 완료: [{} -> {}]", student.getName(), category, item.getName());

        return getProfile(sno);
    }

    @Transactional(readOnly = true)
    public List<ShopProfileDto> getRanking() {
        List<Student> students = studentRepository.findByRoleAndEscapeFalse(Role.ROLE_STUDENT);
        students.sort((a, b) -> Integer.compare(b.getTotalPointsEarned(), a.getTotalPointsEarned()));

        return students.stream()
                .map(s -> ShopProfileDto.builder()
                        .sno(s.getSno())
                        .name(s.getName())
                        .points(s.getPoints())
                        .totalPointsEarned(s.getTotalPointsEarned())
                        .equippedAvatar(s.getEquippedAvatar() != null ? s.getEquippedAvatar() : "robot")
                        .equippedFrame(s.getEquippedFrame() != null ? s.getEquippedFrame() : "none")
                        .equippedTheme(s.getEquippedTheme() != null ? s.getEquippedTheme() : "default")
                        .equippedTitle(s.getEquippedTitle() != null ? s.getEquippedTitle() : "새싹 개발자")
                        .equippedBanner(s.getEquippedBanner() != null ? s.getEquippedBanner() : "banner-default")
                        .build())
                .collect(Collectors.toList());
    }

    private Set<String> parseUnlockedItems(String unlockedItemsStr) {
        Set<String> set = new HashSet<>();
        if (unlockedItemsStr != null && !unlockedItemsStr.isBlank()) {
            Arrays.stream(unlockedItemsStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(set::add);
        }
        return set;
    }
}
