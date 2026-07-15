package com.taskinator.taskinator.application.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taskinator.taskinator.application.ProjectValidationService;
import com.taskinator.taskinator.domain.ProjectPermission;
import com.taskinator.taskinator.domain.entity.Project;
import com.taskinator.taskinator.domain.entity.User;
import com.taskinator.taskinator.domain.repository.ProjectRepository;
import com.taskinator.taskinator.domain.repository.ProjectRoleRepository;
import com.taskinator.taskinator.domain.repository.UserRepository;
import com.taskinator.taskinator.exception.NotFoundException;
import com.taskinator.taskinator.web.dto.CreateProjectRequest;
import com.taskinator.taskinator.web.dto.UpdateProjectRequest;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRoleRepository projectRoleRepository;

    @Mock
    private ProjectValidationService projectValidationService;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void findAllProjects_projectsFound_returnsListOfProjectDTOs() {
        User user = mock(User.class);
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        Project project = mock(Project.class);

        when(projectRepository.findAllAccessibleByUserId(userId)).thenReturn(List.of(project));
        when(project.getId()).thenReturn(projectId);
        when(project.getName()).thenReturn("Project 1");
        when(project.getDescription()).thenReturn("Project Description 1");
        when(project.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(project.getTasks()).thenReturn(Collections.emptyList());
        when(project.getRoles()).thenReturn(Collections.emptyList());
        when(project.getMembers()).thenReturn(Collections.emptyList());

        List<ProjectDTO> result = projectService.findAllProjects(userId);

        assertEquals(projectId, result.get(0).id());
    }


    @Test
    void findProjectsByName_projectsNotFound_throwsNotFoundException() {
        String projectName = "Project 1";
        UUID userId = UUID.randomUUID();

        when(projectRepository.findAllByNameAndUserId(projectName, userId)).thenReturn(List.of());

        assertThrows(NotFoundException.class, () -> projectService.findProjectsByName(projectName, userId));
    }

    @Test
    void findProjectsByName_projectsFound_returnsListOfProjectDTOs() {
        User user = mock(User.class);
        String projectName = "Project 1";
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        Project project = mock(Project.class);

        when(projectRepository.findAllByNameAndUserId(projectName, userId)).thenReturn(List.of(project));
        when(project.getId()).thenReturn(projectId);
        when(project.getName()).thenReturn(projectName);
        when(project.getDescription()).thenReturn("Project Description 1");
        when(project.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(project.getTasks()).thenReturn(Collections.emptyList());
        when(project.getRoles()).thenReturn(Collections.emptyList());
        when(project.getMembers()).thenReturn(Collections.emptyList());

        List<ProjectDTO> result = projectService.findProjectsByName(projectName, userId);

        assertEquals(projectId, result.get(0).id());
    }

    @Test
    void createProject_userNotFound_throwsNotFoundException() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> projectService.createProject(userId, mock(CreateProjectRequest.class)));
    }

    @Test
    void createProject_createSuccessfully_returnsProjectDTO() {
        CreateProjectRequest request = mock(CreateProjectRequest.class);
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(request.name()).thenReturn("Project 1");
        when(request.description()).thenReturn("Project Description 1");

        ProjectDTO result = projectService.createProject(userId, request);

        assertEquals("Project 1", result.name());
        assertEquals("Project Description 1", result.description());
        assertEquals(2, result.roles().size());
        assertEquals("Manager", result.roles().get(0).name());
        assertEquals("Member", result.roles().get(1).name());
    }

    @Test
    void updateProject_projectNotFound_throwsNotFoundException() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UpdateProjectRequest request = mock(UpdateProjectRequest.class);

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> projectService.updateProject(projectId, userId, request));
    }

    @Test
    void updateProject_updateSuccessfully_returnsProjectDTO() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UpdateProjectRequest request = mock(UpdateProjectRequest.class);
        User user = mock(User.class);
        Project project = new Project("Project 1", "Description 1", user);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(user.getId()).thenReturn(userId);
        when(request.name()).thenReturn("Project 2");
        when(request.description()).thenReturn("Project Description 2");

        ProjectDTO result = projectService.updateProject(projectId, userId, request);

        verify(projectValidationService).validatePermission(projectId, userId, ProjectPermission.PROJECT_EDIT);
        assertEquals("Project 2", result.name());
        assertEquals("Project Description 2", result.description());
    }

    @Test
    void deleteProject_deletesSuccessfully() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        projectService.deleteProject(projectId, userId);

        verify(projectValidationService).validatePermission(projectId, userId, ProjectPermission.PROJECT_DELETE);
        verify(projectRepository).deleteById(projectId);
    }
}
