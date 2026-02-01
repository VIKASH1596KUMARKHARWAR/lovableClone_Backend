package com.vikash.projects.lovableclone.service;

import com.vikash.projects.lovableclone.dto.project.ProjectRequest;
import com.vikash.projects.lovableclone.dto.project.ProjectResponse;
import com.vikash.projects.lovableclone.dto.project.ProjectSummaryResponse;

import java.util.List;

public interface ProjectService {
    List<ProjectSummaryResponse> getUserProjects();

    ProjectResponse getUserProjectById(Long id);

    ProjectResponse createProject(ProjectRequest request);

    ProjectResponse updateProject(Long id, ProjectRequest request);

    void softDelete(Long id);
}
