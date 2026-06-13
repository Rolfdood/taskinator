package com.taskinator.taskinator.application.project;

import com.taskinator.taskinator.application.exception.NotFoundException;
import com.taskinator.taskinator.domain.entity.Project;
import com.taskinator.taskinator.domain.entity.User;
import com.taskinator.taskinator.domain.repository.ProjectRepository;
import com.taskinator.taskinator.domain.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * TO DO: Validate the currently logged-in user before using services.
 *  - Only show the currently logged-in user's project.
 *  - Projects of other users cannot be seen by the logged-in user.
 *
 */

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public List<ProjectDTO> findAllProjects() {

        List<Project> projects = projectRepository.findAll();

        List<ProjectDTO> projectDTOs = new ArrayList<>();
        for (Project project : projects) {
            projectDTOs.add(new ProjectDTO(project));
        }

        return projectDTOs;

    }

    public ProjectDTO findProjectById(Long projectId) {
        if (projectRepository.findById(projectId).isPresent()) {
            return new ProjectDTO(projectRepository.findById(projectId).get());
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
    public ProjectDTO updateProject(Long projectId, String newName, String newDescription) {

        if (projectRepository.findById(projectId).isPresent()) {
            Project project = projectRepository.findById(projectId).get();
            project.setName(newName);
            project.setDescription(newDescription);
            projectRepository.save(project);
            return new ProjectDTO(project);
        } else {
            throw new NotFoundException("Project not found");
        }

    }

    @Transactional
    public void deleteProject(Long projectId) {
        if (projectRepository.findById(projectId).isPresent()) {
            projectRepository.deleteById(projectId);
        } else {
            throw new NotFoundException("Project not found");
        }
    }
}
