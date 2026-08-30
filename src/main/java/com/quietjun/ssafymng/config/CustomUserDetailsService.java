package com.quietjun.ssafymng.config;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.quietjun.ssafymng.entity.Student;
import com.quietjun.ssafymng.repository.StudentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final StudentRepository studentRepository;

    @Override
    public UserDetails loadUserByUsername(String sno) throws UsernameNotFoundException {
        Student student = studentRepository.findBySnoAndEscapeFalse(sno)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + sno));

        return new User(
                student.getSno(),
                student.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(student.getRole().name()))
        );
    }
}
