package com.taskinator.taskinator.application;

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

    @Transactional
    public ProjectDTO save(CreateProjectRequest createProjectRequest) {

        if (userRepository.findById(createProjectRequest.getUserId()).isPresent()) {
            User user = userRepository.findById(createProjectRequest.getUserId()).get();

            Project newProject = new Project(
                createProjectRequest.getName(), createProjectRequest.getDescription(), user
            );

            projectRepository.save(newProject);

            return new ProjectDTO(newProject);

        } else {
            return null; // To throw exception
        }

    }
}
