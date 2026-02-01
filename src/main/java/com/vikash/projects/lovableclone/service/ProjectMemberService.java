package com.vikash.projects.lovableclone.service;

import com.vikash.projects.lovableclone.dto.member.InviteMemberRequest;
import com.vikash.projects.lovableclone.dto.member.MemberResponse;
import com.vikash.projects.lovableclone.dto.member.UpdateMemberRoleRequest;

import java.util.List;

public interface ProjectMemberService {
    List<MemberResponse> getProjectMembers(Long projectId);

    MemberResponse inviteMember(Long projectId, InviteMemberRequest request);

    MemberResponse updateMemberRole(Long projectId, Long memberId, UpdateMemberRoleRequest request);

    void removeProjectMember(Long projectId, Long memberId);
}
