package com.example.jewellery_backend.service;

import com.example.jewellery_backend.dto.*;

public interface AdminUserService {

    AdminUserResponse register(AdminUserRegisterRequest request);

    AdminUserLoginResponse login(AdminUserLoginRequest request);

    AdminUserResponse getById(Long id);
}
