package com.example.jewellery_backend.service;

import com.example.jewellery_backend.dto.*;

public interface AdminUserService {

    AdminUserResponse register(AdminUserRegisterRequest request);

    AdminUserResponse login(AdminUserLoginRequest request);

    AdminUserResponse getById(Long id);
}
