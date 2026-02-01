package com.vikash.projects.lovableclone.service;

import com.vikash.projects.lovableclone.dto.project.FileContentResponse;
import com.vikash.projects.lovableclone.dto.project.FileNode;

import java.util.List;

public interface ProjectFileService {
    List<FileNode> getFileTree(Long projectId);

    FileContentResponse getFileContent(Long projectId, String path);

    void saveFile(Long projectId, String filePath, String fileContent);
}
