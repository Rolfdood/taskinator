package com.taskinator.taskinator.application.project;

import com.taskinator.taskinator.application.ProjectValidationService;
import com.taskinator.taskinator.application.exception.NotFoundException;
import com.taskinator.taskinator.domain.entity.Project;
import com.taskinator.taskinator.domain.entity.User;
import com.taskinator.taskinator.domain.repository.ProjectRepository;
import com.taskinator.taskinator.domain.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {
    /**
     * TODO: Validate the currently logged-in user before using services.
     *  - Only show the currently logged-in user's project.
     *  - Projects of other users cannot be seen by the logged-in user.
     *
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

    public List<ProjectDTO> findAllProjects(Long userId) {

        List<Project> projects = projectRepository.findAllByUserId(userId);

        List<ProjectDTO> projectDTOs = new ArrayList<>();
        for (Project project : projects) {
            projectDTOs.add(new ProjectDTO(project));
        }

        return projectDTOs;
    }

    public ProjectDTO findProjectById(Long projectId, Long userId) {
        if (projectRepository.existsByIdAndUserId(projectId, userId)) {
            return new ProjectDTO(projectRepository.findByIdAndUserId(projectId, userId));
        } else {
            throw new NotFoundException("Project not found");
        }
    }

    @Transactional
    public ProjectDTO createProject(CreateProjectRequest createProjectRequest) {

        if (userRepository.findById(createProjectRequest.getUserId()).isPresent()) {
            User user = userRepository.findById(createProjectRequest.getUserId()).get();

            Project newProject = new Project(
                createProjectRequest.getName(), createProjectRequest.getDescription(), user
            );

            projectRepository.save(newProject);

            return new ProjectDTO(newProject);

        } else {
            throw new NotFoundException("User not found");
        }

    }

    @Transactional
    public ProjectDTO updateProject(Long userId, Long projectId, String newName, String newDescription) {
        projectValidationService.validateProjectBelongsToUser(projectId, userId);

        Project project = projectRepository.findByIdAndUserId(projectId, userId);

        project.setName(newName);
        project.setDescription(newDescription);
        projectRepository.save(project);

        return new ProjectDTO(project);
    }

    @Transactional
    public void deleteProject(Long userId, Long projectId) {
        projectValidationService.validateProjectBelongsToUser(projectId, userId);

        projectRepository.deleteById(projectId);
    }
}
