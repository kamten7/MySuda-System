package com.suda.service;

import com.suda.dto.UserLoginDTO;
import com.suda.entity.User;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    User wxlogin(UserLoginDTO userLoginDTO);
}
