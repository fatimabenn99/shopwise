package com.shopwise.app.service;

import com.shopwise.app.dto.request.LoginRequest;
import com.shopwise.app.dto.request.RegisterRequest;
import com.shopwise.app.dto.response.LoginResponse;
import com.shopwise.app.dto.response.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}