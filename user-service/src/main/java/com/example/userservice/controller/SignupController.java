package com.example.userservice.controller;

import com.example.userservice.dto.MemberSign;
import com.example.userservice.dto.SuggestFastRequestDto;
import com.example.userservice.dto.SuggestFastResponseDto;
import com.example.userservice.entity.Member;
import com.example.userservice.security.JwtUtil;
import com.example.userservice.service.MemberService;
import com.example.userservice.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Slf4j
public class SignupController {
    private final MemberService memberService;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final RestTemplate restTemplate;

    @Value("${fastapi-svc}")
    private String fastApiUrl;

    /**
     * 일반 사용자 회원가입 API
     */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@RequestBody MemberSign dto) {
        try {
            log.debug("회원가입 요청: userId={}", dto.getUserId());

            Member member = memberService.registerUser(dto);

            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority(member.getRole())
            );

            UserDetails userDetails = new User(member.getUserId(), member.getPassword(), authorities);
            String accessToken = jwtUtil.generateToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            tokenService.saveRefreshToken(member.getMemberId(), refreshToken);

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("member_id", String.valueOf(member.getMemberId()));
            response.put("user_id", member.getUserId());
            response.put("nickname", member.getNickname());
            response.put("role", member.getRole());

            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "회원가입 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 회원 탈퇴 (본인 인증 포함)
     */
    @DeleteMapping("/signout/{memberId}")
    public ResponseEntity<String> signout(
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 현재 로그인한 사용자 정보 조회
        String userId = userDetails.getUsername();
        Member loginMember = memberService.findByUserId(userId);

        // 본인인지 확인
        if (!loginMember.getMemberId().equals(memberId)) {
            return ResponseEntity.status(403).body("본인만 탈퇴할 수 있습니다.");
        }

        // fast-api 서버에 데이터 삭제 요청
        try {
            ResponseEntity<Void> responseEntity = restTemplate.getForEntity(fastApiUrl + "/suggest/" + userId + "/delete", Void.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("탈퇴 전 AI 여행지 추천 내역 삭제 실패 : " + e.getMessage());
        }
        log.debug("AI 여행지 추천 내역 삭제 완료");

        try {
            memberService.deleteMemberById(memberId);
            log.debug("회원탈퇴 완료: memberId={}", memberId);
            return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("회원 탈퇴 실패: " + e.getMessage());
        }
    }
}
