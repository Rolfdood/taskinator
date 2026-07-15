package com.taskinator.taskinator.application.project;

import com.taskinator.taskinator.application.ProjectValidationService;
import com.taskinator.taskinator.domain.ProjectPermission;
import com.taskinator.taskinator.domain.entity.Project;
import com.taskinator.taskinator.domain.entity.ProjectRole;
import com.taskinator.taskinator.domain.entity.User;
import com.taskinator.taskinator.domain.repository.ProjectRepository;
import com.taskinator.taskinator.domain.repository.ProjectRoleRepository;
import com.taskinator.taskinator.domain.repository.UserRepository;
import com.taskinator.taskinator.exception.NotFoundException;
import com.taskinator.taskinator.web.dto.CreateProjectRequest;
import com.taskinator.taskinator.web.dto.UpdateProjectRequest;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final ProjectValidationService projectValidationService;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository,
        ProjectRoleRepository projectRoleRepository,
        ProjectValidationService projectValidationService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectRoleRepository = projectRoleRepository;
        this.projectValidationService = projectValidationService;
    }

    public List<ProjectDTO> findAllProjects(UUID userId) {
        return projectRepository.findAllAccessibleByUserId(userId)
            .stream()
            .map(ProjectDTO::new)
            .toList();
    }

    public List<ProjectDTO> findProjectsByName(String name, UUID userId) {
        List<Project> projects = projectRepository.findAllByNameAndUserId(name, userId);
        if (projects.isEmpty()) {
            throw new NotFoundException("Project not found");
        }
        return projects.stream().map(ProjectDTO::new).toList();
    }

    @Transactional
    public ProjectDTO createProject(UUID userId, CreateProjectRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));

        Project project = new Project(request.name(), request.description(), user);
        projectRepository.save(project);

        ProjectRole managerRole = new ProjectRole("Manager", project, Set.of(ProjectPermission.values()));
        ProjectRole memberRole = new ProjectRole("Member", project, Set.of(ProjectPermission.PROJECT_VIEW,
            ProjectPermission.TASK_CREATE, ProjectPermission.TASK_EDIT, ProjectPermission.TASK_DELETE));

        projectRoleRepository.save(managerRole);
        projectRoleRepository.save(memberRole);

        project.getRoles().add(managerRole);
        project.getRoles().add(memberRole);

        return new ProjectDTO(project);
    }

    @Transactional
    public ProjectDTO updateProject(UUID projectId, UUID userId, UpdateProjectRequest request) {
        projectValidationService.validatePermission(projectId, userId, ProjectPermission.PROJECT_EDIT);

        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new NotFoundException("Project not found"));

        project.setName(request.name());
        project.setDescription(request.description());
        projectRepository.save(project);

        return new ProjectDTO(project);
    }

    @Transactional
    public void deleteProject(UUID projectId, UUID userId) {
        projectValidationService.validatePermission(projectId, userId, ProjectPermission.PROJECT_DELETE);
        projectRepository.deleteById(projectId);
    }
}