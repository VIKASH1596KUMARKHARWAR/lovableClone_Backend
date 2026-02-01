package com.vikash.projects.lovableclone.mapper;

import com.vikash.projects.lovableclone.dto.project.ProjectResponse;
import com.vikash.projects.lovableclone.dto.project.ProjectSummaryResponse;
import com.vikash.projects.lovableclone.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    ProjectResponse toProjectResponse(Project project);

    @Mapping(target = "projectName", source = "name")
    ProjectSummaryResponse toProjectSummaryResponse(Project project);

    List<ProjectSummaryResponse> toListOfProjectSummaryResponse(List<Project> projects);

}
