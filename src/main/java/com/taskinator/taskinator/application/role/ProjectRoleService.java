package com.taskinator.taskinator.application.role;

import com.taskinator.taskinator.application.ProjectValidationService;
import com.taskinator.taskinator.domain.ProjectPermission;
import com.taskinator.taskinator.domain.entity.Project;
import com.taskinator.taskinator.domain.entity.ProjectRole;
import com.taskinator.taskinator.domain.repository.ProjectMemberRepository;
import com.taskinator.taskinator.domain.repository.ProjectRepository;
import com.taskinator.taskinator.domain.repository.ProjectRoleRepository;
import com.taskinator.taskinator.exception.NotFoundException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectRoleService {

    private final ProjectRoleRepository projectRoleRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final ProjectValidationService projectValidationService;

    public ProjectRoleService(ProjectRoleRepository projectRoleRepository,
        ProjectMemberRepository projectMemberRepository,
        ProjectRepository projectRepository,
        ProjectValidationService projectValidationService) {
        this.projectRoleRepository = projectRoleRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectRepository = projectRepository;
        this.projectValidationService = projectValidationService;
    }

    public List<ProjectRoleDTO> listRoles(UUID projectId, UUID userId) {
        projectValidationService.validatePermission(projectId, userId, ProjectPermission.PROJECT_VIEW);
        return projectRoleRepository.findAllByProjectId(projectId)
            .stream()
            .map(ProjectRoleDTO::new)
            .toList();
    }

    @Transactional
    public ProjectRoleDTO createRole(UUID projectId, UUID userId, String name, Set<ProjectPermission> permissions) {
        projectValidationService.validatePermission(projectId, userId, ProjectPermission.MEMBER_MANAGE);

        if (projectRoleRepository.existsByProjectIdAndName(projectId, name)) {
            throw new IllegalArgumentException("A role with the name '" + name + "' already exists in this project.");
        }

        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new NotFoundException("Project not found."));

        ProjectRole role = new ProjectRole(name, project, permissions);
        projectRoleRepository.save(role);

        return new ProjectRoleDTO(role);
    }

    @Transactional
    public ProjectRoleDTO updateRole(UUID projectId, UUID roleId, UUID userId, String name,
        Set<ProjectPermission> permissions) {
        projectValidationService.validatePermission(projectId, userId, ProjectPermission.MEMBER_MANAGE);

        ProjectRole role = projectRoleRepository.findByIdAndProjectId(roleId, projectId)
            .orElseThrow(() -> new NotFoundException("Role not found."));

        if (!role.getName().equals(name)
            && projectRoleRepository.existsByProjectIdAndName(projectId, name)) {
            throw new IllegalArgumentException("A role with the name '" + name + "' already exists in this project.");
        }

        role.setName(name);
        role.setPermissions(permissions);
        projectRoleRepository.save(role);

        return new ProjectRoleDTO(role);
    }

    @Transactional
    public void deleteRole(UUID projectId, UUID roleId, UUID userId) {
        projectValidationService.validatePermission(projectId, userId, ProjectPermission.MEMBER_MANAGE);

        if (projectRoleRepository.findByIdAndProjectId(roleId, projectId).isEmpty()) {
            throw new NotFoundException("Role not found.");
        }

        if (projectMemberRepository.existsByRoleId(roleId)) {
            throw new IllegalArgumentException("Cannot delete a role that is assigned to members.");
        }

        projectRoleRepository.deleteByIdAndProjectId(roleId, projectId);
    }
}
