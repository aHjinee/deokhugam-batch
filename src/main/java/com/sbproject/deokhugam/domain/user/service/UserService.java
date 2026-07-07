package com.sbproject.deokhugam.domain.user.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sbproject.deokhugam.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public int deleteExpiredUsers() {
        Instant threshold = Instant.now().minus(1, ChronoUnit.DAYS);
        return userRepository.deleteExpiredUsers(threshold);
    }
}