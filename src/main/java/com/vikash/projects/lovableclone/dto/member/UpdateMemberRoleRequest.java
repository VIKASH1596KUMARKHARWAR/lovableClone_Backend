package com.vikash.projects.lovableclone.dto.member;

import com.vikash.projects.lovableclone.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record UpdateMemberRoleRequest(
        @NotNull ProjectRole role) {
}
