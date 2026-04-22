package com.yuequge.service;

import com.yuequge.dto.LoginDTO;
import com.yuequge.dto.LoginVO;
import com.yuequge.dto.RegisterDTO;
import com.yuequge.entity.User;

public interface AuthService {
    LoginVO login(LoginDTO dto);

    User register(RegisterDTO dto);

    void logout(String token);
}
