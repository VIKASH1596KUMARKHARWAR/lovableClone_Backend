package com.vikash.projects.lovableclone.service;

import com.vikash.projects.lovableclone.dto.auth.AuthResponse;
import com.vikash.projects.lovableclone.dto.auth.LoginRequest;
import com.vikash.projects.lovableclone.dto.auth.SignupRequest;

public interface AuthService {
    AuthResponse signup(SignupRequest request);

    AuthResponse login(LoginRequest request);
}
