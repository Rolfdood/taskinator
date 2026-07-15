package com.taskinator.taskinator.application;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.taskinator.taskinator.domain.ProjectPermission;
import com.taskinator.taskinator.domain.entity.ProjectMember;
import com.taskinator.taskinator.domain.entity.ProjectRole;
import com.taskinator.taskinator.domain.repository.ProjectMemberRepository;
import com.taskinator.taskinator.domain.repository.ProjectRepository;
import com.taskinator.taskinator.domain.repository.TaskRepository;
import com.taskinator.taskinator.exception.NotFoundException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectValidationServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @InjectMocks
    private ProjectValidationService validationService;

    @Test
    void validateProjectAccess_admin_passes() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(projectRepository.existsByIdAndAccessibleByUserId(projectId, userId)).thenReturn(true);

        assertDoesNotThrow(() -> validationService.validateProjectAccess(projectId, userId));
    }

    @Test
    void validateProjectAccess_notAccessible_throwsNotFoundException() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(projectRepository.existsByIdAndAccessibleByUserId(projectId, userId)).thenReturn(false);

        assertThrows(NotFoundException.class,
            () -> validationService.validateProjectAccess(projectId, userId));
    }

    @Test
    void validatePermission_admin_passes() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(projectRepository.existsByIdAndUserId(projectId, userId)).thenReturn(true);

        assertDoesNotThrow(
            () -> validationService.validatePermission(projectId, userId, ProjectPermission.TASK_CREATE));
    }

    @Test
    void validatePermission_memberWithPermission_passes() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ProjectMember member = org.mockito.Mockito.mock(ProjectMember.class);
        ProjectRole role = org.mockito.Mockito.mock(ProjectRole.class);

        when(projectRepository.existsByIdAndUserId(projectId, userId)).thenReturn(false);
        when(projectMemberRepository.findByUserIdAndProjectId(userId, projectId))
            .thenReturn(Optional.of(member));
        when(member.getRole()).thenReturn(role);
        when(role.getPermissions()).thenReturn(Set.of(ProjectPermission.TASK_CREATE));

        assertDoesNotThrow(
            () -> validationService.validatePermission(projectId, userId, ProjectPermission.TASK_CREATE));
    }

    @Test
    void validatePermission_memberWithoutPermission_throwsNotFoundException() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ProjectMember member = org.mockito.Mockito.mock(ProjectMember.class);
        ProjectRole role = org.mockito.Mockito.mock(ProjectRole.class);

        when(projectRepository.existsByIdAndUserId(projectId, userId)).thenReturn(false);
        when(projectMemberRepository.findByUserIdAndProjectId(userId, projectId))
            .thenReturn(Optional.of(member));
        when(member.getRole()).thenReturn(role);
        when(role.getPermissions()).thenReturn(Set.of(ProjectPermission.PROJECT_VIEW));

        assertThrows(NotFoundException.class,
            () -> validationService.validatePermission(projectId, userId, ProjectPermission.TASK_CREATE));
    }

    @Test
    void validatePermission_notMember_throwsNotFoundException() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(projectRepository.existsByIdAndUserId(projectId, userId)).thenReturn(false);
        when(projectMemberRepository.findByUserIdAndProjectId(userId, projectId))
            .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> validationService.validatePermission(projectId, userId, ProjectPermission.TASK_CREATE));
    }

    @Test
    void validateTaskBelongsToProject_exists_passes() {
        UUID taskId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        when(taskRepository.existsByIdAndProjectId(taskId, projectId)).thenReturn(true);

        assertDoesNotThrow(() -> validationService.validateTaskBelongsToProject(taskId, projectId));
    }

    @Test
    void validateTaskBelongsToProject_notExists_throwsNotFoundException() {
        UUID taskId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        when(taskRepository.existsByIdAndProjectId(taskId, projectId)).thenReturn(false);

        assertThrows(NotFoundException.class,
            () -> validationService.validateTaskBelongsToProject(taskId, projectId));
    }

    @Test
    void validateTaskAccess_hasPermissionAndTaskExists_passes() {
        UUID taskId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(projectRepository.existsByIdAndUserId(projectId, userId)).thenReturn(true);
        when(taskRepository.existsByIdAndProjectId(taskId, projectId)).thenReturn(true);

        assertDoesNotThrow(
            () -> validationService.validateTaskAccess(taskId, projectId, userId, ProjectPermission.TASK_EDIT));
    }

    @Test
    void validateTaskAccess_noPermission_throwsNotFoundException() {
        UUID taskId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(projectRepository.existsByIdAndUserId(projectId, userId)).thenReturn(false);
        when(projectMemberRepository.findByUserIdAndProjectId(userId, projectId))
            .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> validationService.validateTaskAccess(taskId, projectId, userId, ProjectPermission.TASK_EDIT));
    }

    @Test
    void validateTaskAccess_taskNotInProject_throwsNotFoundException() {
        UUID taskId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(projectRepository.existsByIdAndUserId(projectId, userId)).thenReturn(true);
        when(taskRepository.existsByIdAndProjectId(taskId, projectId)).thenReturn(false);

        assertThrows(NotFoundException.class,
            () -> validationService.validateTaskAccess(taskId, projectId, userId, ProjectPermission.TASK_EDIT));
    }

    @Test
    void validateProjectBelongsToUser_delegatesToValidateProjectAccess() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(projectRepository.existsByIdAndAccessibleByUserId(projectId, userId)).thenReturn(true);

        assertDoesNotThrow(() -> validationService.validateProjectBelongsToUser(projectId, userId));
    }

    @Test
    void validateTaskOwnership_delegatesToValidateTaskAccess() {
        UUID taskId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(projectRepository.existsByIdAndUserId(projectId, userId)).thenReturn(true);
        when(taskRepository.existsByIdAndProjectId(taskId, projectId)).thenReturn(true);

        assertDoesNotThrow(() -> validationService.validateTaskOwnership(taskId, projectId, userId));
    }
}
