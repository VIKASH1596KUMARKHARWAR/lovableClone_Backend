package com.vikash.projects.lovableclone.mapper;

import com.vikash.projects.lovableclone.dto.auth.SignupRequest;
import com.vikash.projects.lovableclone.dto.auth.UserProfileResponse;
import com.vikash.projects.lovableclone.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(SignupRequest signupRequest);

    UserProfileResponse toUserProfileResponse(User user);

}
