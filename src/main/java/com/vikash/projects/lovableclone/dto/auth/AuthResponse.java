package com.vikash.projects.lovableclone.dto.auth;

public record AuthResponse(
        String token,
        UserProfileResponse user
) {

}
