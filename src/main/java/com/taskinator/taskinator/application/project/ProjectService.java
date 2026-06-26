package com.taskinator.taskinator.application.project;

import com.taskinator.taskinator.application.ProjectValidationService;
import com.taskinator.taskinator.exception.NotFoundException;
import com.taskinator.taskinator.domain.entity.Project;
import com.taskinator.taskinator.domain.entity.User;
import com.taskinator.taskinator.domain.repository.ProjectRepository;
import com.taskinator.taskinator.domain.repository.UserRepository;
import com.taskinator.taskinator.infrastructure.security.CurrentUserDetails;
import com.taskinator.taskinator.web.dto.CreateProjectRequest;
import com.taskinator.taskinator.web.dto.UpdateProjectRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    private final UserRepository userRepository;

    private final ProjectValidationService projectValidationService;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository,
        ProjectValidationService projectValidationService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectValidationService = projectValidationService;
    }

    public List<ProjectDTO> findAllProjects(CurrentUserDetails  currentUserDetails) {

        List<Project> projects = projectRepository.findAllByUserId(currentUserDetails.id());

        List<ProjectDTO> projectDTOs = new ArrayList<>();
        for (Project project : projects) {
            projectDTOs.add(new ProjectDTO(project));
        }

        return projectDTOs;
    }

    @Transactional
    public ProjectDTO createProject(CurrentUserDetails  currentUserDetails, CreateProjectRequest createProjectRequest) {

        User user = userRepository.findById(currentUserDetails.id())
            .orElseThrow(() -> new NotFoundException("User not found"));

        Project newProject = new Project(
            createProjectRequest.name(), createProjectRequest.description(), user
        );

        projectRepository.save(newProject);

        return new ProjectDTO(newProject);
    }

    public List<ProjectDTO> findProjectByName(String projectName, CurrentUserDetails  currentUserDetails) {

        if (!projectRepository.findAllByNameAndUserId(projectName, currentUserDetails.id()).isEmpty()) {
            List<Project> projects = projectRepository.findAllByNameAndUserId(projectName, currentUserDetails.id());

            List<ProjectDTO> projectDTOs = new ArrayList<>();

            for(Project project : projects) {
                projectDTOs.add(new ProjectDTO(project));
            }

            return projectDTOs;
        } else {
            throw new NotFoundException("Project not found");
        }
    }

    @Transactional
    public ProjectDTO updateProject(CurrentUserDetails  currentUserDetails, UpdateProjectRequest request) {

        projectValidationService.validateProjectBelongsToUser(request.projectId(), currentUserDetails.id());

        Project project = projectRepository.findByIdAndUserId(request.projectId(), currentUserDetails.id());

        project.setName(request.name());
        project.setDescription(request.description());
        projectRepository.save(project);

        return new ProjectDTO(project);
    }

    @Transactional
    public void deleteProject(CurrentUserDetails  currentUserDetails, UUID projectId) {

        projectValidationService.validateProjectBelongsToUser(projectId, currentUserDetails.id());

        projectRepository.deleteById(projectId);
    }

}