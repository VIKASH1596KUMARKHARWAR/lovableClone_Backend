package com.vikash.projects.lovableclone.service;

import com.vikash.projects.lovableclone.dto.auth.UserProfileResponse;

public interface UserService {
    UserProfileResponse getProfile(Long userId);
}
