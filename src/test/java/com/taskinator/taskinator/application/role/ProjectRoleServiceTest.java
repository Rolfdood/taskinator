package com.taskinator.taskinator.application.role;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taskinator.taskinator.application.ProjectValidationService;
import com.taskinator.taskinator.domain.ProjectPermission;
import com.taskinator.taskinator.domain.entity.Project;
import com.taskinator.taskinator.domain.entity.ProjectRole;
import com.taskinator.taskinator.domain.repository.ProjectMemberRepository;
import com.taskinator.taskinator.domain.repository.ProjectRepository;
import com.taskinator.taskinator.domain.repository.ProjectRoleRepository;
import com.taskinator.taskinator.exception.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectRoleServiceTest {

    @Mock
    private ProjectRoleRepository projectRoleRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectValidationService projectValidationService;

    @InjectMocks
    private ProjectRoleService projectRoleService;

    @Test
    void listRoles_validatesPermissionAndReturnsRoles() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Project project = org.mockito.Mockito.mock(Project.class);
        ProjectRole role = new ProjectRole("Manager", project,
            Set.of(ProjectPermission.PROJECT_VIEW, ProjectPermission.TASK_CREATE));

        when(projectRoleRepository.findAllByProjectId(projectId)).thenReturn(List.of(role));

        List<ProjectRoleDTO> result = projectRoleService.listRoles(projectId, userId);

        verify(projectValidationService).validatePermission(projectId, userId, ProjectPermission.PROJECT_VIEW);
        assertEquals(1, result.size());
        assertEquals("Manager", result.get(0).name());
    }

    @Test
    void listRoles_noPermission_throwsNotFoundException() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        doThrow(new NotFoundException("Project not found."))
            .when(projectValidationService).validatePermission(projectId, userId, ProjectPermission.PROJECT_VIEW);

        assertThrows(NotFoundException.class,
            () -> projectRoleService.listRoles(projectId, userId));
    }

    @Test
    void createRole_createsSuccessfully() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Project project = org.mockito.Mockito.mock(Project.class);
        Set<ProjectPermission> permissions = Set.of(ProjectPermission.PROJECT_VIEW);

        when(projectRoleRepository.existsByProjectIdAndName(projectId, "Viewer")).thenReturn(false);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        ProjectRoleDTO result = projectRoleService.createRole(projectId, userId, "Viewer", permissions);

        verify(projectValidationService).validatePermission(projectId, userId, ProjectPermission.MEMBER_MANAGE);
        assertEquals("Viewer", result.name());
        assertEquals(1, result.permissions().size());
    }

    @Test
    void createRole_duplicateName_throwsIllegalArgumentException() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Set<ProjectPermission> permissions = Set.of(ProjectPermission.PROJECT_VIEW);

        when(projectRoleRepository.existsByProjectIdAndName(projectId, "Viewer")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
            () -> projectRoleService.createRole(projectId, userId, "Viewer", permissions));
    }

    @Test
    void createRole_noPermission_throwsNotFoundException() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Set<ProjectPermission> permissions = Set.of(ProjectPermission.PROJECT_VIEW);

        doThrow(new NotFoundException("Project not found."))
            .when(projectValidationService).validatePermission(projectId, userId, ProjectPermission.MEMBER_MANAGE);

        assertThrows(NotFoundException.class,
            () -> projectRoleService.createRole(projectId, userId, "Viewer", permissions));
    }

    @Test
    void updateRole_updatesSuccessfully() {
        UUID projectId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Project project = org.mockito.Mockito.mock(Project.class);
        ProjectRole role = new ProjectRole("OldName", project, Set.of(ProjectPermission.PROJECT_VIEW));
        Set<ProjectPermission> newPermissions = Set.of(ProjectPermission.PROJECT_VIEW, ProjectPermission.TASK_CREATE);

        when(projectRoleRepository.findByIdAndProjectId(roleId, projectId)).thenReturn(Optional.of(role));

        ProjectRoleDTO result = projectRoleService.updateRole(projectId, roleId, userId, "NewName", newPermissions);

        verify(projectValidationService).validatePermission(projectId, userId, ProjectPermission.MEMBER_MANAGE);
        assertEquals("NewName", result.name());
        assertEquals(2, result.permissions().size());
    }

    @Test
    void updateRole_roleNotFound_throwsNotFoundException() {
        UUID projectId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Set<ProjectPermission> permissions = Set.of(ProjectPermission.PROJECT_VIEW);

        when(projectRoleRepository.findByIdAndProjectId(roleId, projectId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> projectRoleService.updateRole(projectId, roleId, userId, "Name", permissions));
    }

    @Test
    void updateRole_duplicateName_throwsIllegalArgumentException() {
        UUID projectId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Project project = org.mockito.Mockito.mock(Project.class);
        ProjectRole role = new ProjectRole("OldName", project, Set.of(ProjectPermission.PROJECT_VIEW));
        Set<ProjectPermission> permissions = Set.of(ProjectPermission.PROJECT_VIEW);

        when(projectRoleRepository.findByIdAndProjectId(roleId, projectId)).thenReturn(Optional.of(role));
        when(projectRoleRepository.existsByProjectIdAndName(projectId, "NewName")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
            () -> projectRoleService.updateRole(projectId, roleId, userId, "NewName", permissions));
    }

    @Test
    void deleteRole_deletesSuccessfully() {
        UUID projectId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(projectRoleRepository.findByIdAndProjectId(roleId, projectId))
            .thenReturn(Optional.of(org.mockito.Mockito.mock(ProjectRole.class)));
        when(projectMemberRepository.existsByRoleId(roleId)).thenReturn(false);

        projectRoleService.deleteRole(projectId, roleId, userId);

        verify(projectValidationService).validatePermission(projectId, userId, ProjectPermission.MEMBER_MANAGE);
        verify(projectRoleRepository).deleteByIdAndProjectId(roleId, projectId);
    }

    @Test
    void deleteRole_roleNotFound_throwsNotFoundException() {
        UUID projectId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(projectRoleRepository.findByIdAndProjectId(roleId, projectId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> projectRoleService.deleteRole(projectId, roleId, userId));
    }

    @Test
    void deleteRole_roleInUse_throwsIllegalArgumentException() {
        UUID projectId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(projectRoleRepository.findByIdAndProjectId(roleId, projectId))
            .thenReturn(Optional.of(org.mockito.Mockito.mock(ProjectRole.class)));
        when(projectMemberRepository.existsByRoleId(roleId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
            () -> projectRoleService.deleteRole(projectId, roleId, userId));
    }
}
