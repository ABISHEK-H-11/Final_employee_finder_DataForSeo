package com.employeeFinderByDataForSEO.service;

import com.employeeFinderByDataForSEO.Entity.Account;
import com.employeeFinderByDataForSEO.dto.LoginRequest;
import com.employeeFinderByDataForSEO.dto.RegisterRequest;
import com.employeeFinderByDataForSEO.repository.AccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AccountRepository accountRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }


    public void register(RegisterRequest registerRequest) {
        if(accountRepository.existsByEmail(registerRequest.getEmail())){
            throw new RuntimeException("Email already register");
        }

        accountRepository.save(new Account(registerRequest.getUsername()
                ,registerRequest.getEmail()
                ,passwordEncoder.encode(registerRequest.getPassword())
                , LocalDateTime.now()));
    }

    public String login(LoginRequest loginRequest) {
        Account account = accountRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(()-> new RuntimeException("Invalid email or password"));

            if(!passwordEncoder.matches(loginRequest.getPassword(), account.getPassword())) {
                throw new RuntimeException("Invalid email or password");
            }

        return jwtService.generateToken(account.getEmail());

    }
}
