package com.vikash.projects.lovableclone.dto.member;

import com.vikash.projects.lovableclone.enums.ProjectRole;

import java.time.Instant;

public record MemberResponse(
        Long userId,
        String username,
        String name,
        ProjectRole projectRole,
        Instant invitedAt
) {
}
