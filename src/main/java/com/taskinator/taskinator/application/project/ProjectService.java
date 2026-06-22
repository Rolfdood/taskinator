package com.taskinator.taskinator.application.project;

import com.taskinator.taskinator.application.ProjectValidationService;
import com.taskinator.taskinator.exception.NotFoundException;
import com.taskinator.taskinator.domain.entity.Project;
import com.taskinator.taskinator.domain.entity.User;
import com.taskinator.taskinator.domain.repository.ProjectRepository;
import com.taskinator.taskinator.domain.repository.UserRepository;
import com.taskinator.taskinator.web.dto.CreateProjectRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {
    /**
     * TODO: Replace getCurrentUserId() with a proper currently-logged-in user resolver.
     *  - Wire getCurrentUserId() to Spring Security's SecurityContextHolder or a dedicated
     *    CurrentUserResolver component once authentication is in place.
     */
    private final ProjectRepository projectRepository;

    private final UserRepository userRepository;

    private final ProjectValidationService projectValidationService;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository,
        ProjectValidationService projectValidationService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectValidationService = projectValidationService;
    }

    public List<ProjectDTO> findAllProjects() {
        UUID userId = getCurrentUserId();

        List<Project> projects = projectRepository.findAllByUserId(userId);

        List<ProjectDTO> projectDTOs = new ArrayList<>();
        for (Project project : projects) {
            projectDTOs.add(new ProjectDTO(project));
        }

        return projectDTOs;
    }

    public ProjectDTO findProjectById(UUID projectId) {
        UUID userId = getCurrentUserId();

        if (projectRepository.existsByIdAndUserId(projectId, userId)) {
            return new ProjectDTO(projectRepository.findByIdAndUserId(projectId, userId));
        } else {
            throw new NotFoundException("Project not found");
        }
    }

    @Transactional
    public ProjectDTO createProject(CreateProjectRequest createProjectRequest) {
        UUID userId = getCurrentUserId();

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));

        Project newProject = new Project(
            createProjectRequest.getName(), createProjectRequest.getDescription(), user
        );

        projectRepository.save(newProject);

        return new ProjectDTO(newProject);
    }

    @Transactional
    public ProjectDTO updateProject(UUID projectId, String newName, String newDescription) {
        UUID userId = getCurrentUserId();

        projectValidationService.validateProjectBelongsToUser(projectId, userId);

        Project project = projectRepository.findByIdAndUserId(projectId, userId);

        project.setName(newName);
        project.setDescription(newDescription);
        projectRepository.save(project);

        return new ProjectDTO(project);
    }

    @Transactional
    public void deleteProject(UUID projectId) {
        UUID userId = getCurrentUserId();

        projectValidationService.validateProjectBelongsToUser(projectId, userId);

        projectRepository.deleteById(projectId);
    }

    /**
     * Placeholder for the currently logged-in user resolver.
     * TODO: Replace with Spring Security context lookup, e.g.:
     *   return ((YourUserDetails) SecurityContextHolder.getContext()
     *       .getAuthentication().getPrincipal()).getId();
     */
    private UUID getCurrentUserId() {
        throw new UnsupportedOperationException("Current user resolver not yet implemented");
    }
}