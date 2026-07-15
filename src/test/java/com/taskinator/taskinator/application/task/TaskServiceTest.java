package com.taskinator.taskinator.application.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taskinator.taskinator.application.ProjectValidationService;
import com.taskinator.taskinator.domain.TaskStatus;
import com.taskinator.taskinator.domain.entity.Project;
import com.taskinator.taskinator.domain.entity.Task;
import com.taskinator.taskinator.domain.repository.ProjectRepository;
import com.taskinator.taskinator.domain.repository.TaskRepository;
import com.taskinator.taskinator.exception.NotFoundException;
import com.taskinator.taskinator.web.dto.CreateTaskRequest;
import com.taskinator.taskinator.web.dto.UpdateTaskRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectValidationService projectValidationService;

    @InjectMocks
    private TaskService taskService;

    @Test
    void findAllByProject_tasksFound_returnsListOfTaskDTOs() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Project project = mock(Project.class);
        Task task = new Task("Task 1", "Description", TaskStatus.TODO, null, project);

        when(taskRepository.findAllByProjectId(projectId)).thenReturn(List.of(task));

        List<TaskDTO> result = taskService.findAllByProject(projectId, userId);

        verify(projectValidationService).validateProjectBelongsToUser(projectId, userId);
        assertEquals(1, result.size());
        assertEquals("Task 1", result.get(0).getTitle());
    }

    @Test
    void findAllByProject_projectNotOwned_throwsNotFoundException() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        doThrow(new NotFoundException("Project not found."))
            .when(projectValidationService).validateProjectBelongsToUser(projectId, userId);

        assertThrows(NotFoundException.class,
            () -> taskService.findAllByProject(projectId, userId));
    }

    @Test
    void findTaskById_taskFound_returnsTaskDTO() {
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Project project = mock(Project.class);
        Task task = new Task("Task 1", "Description", TaskStatus.TODO, null, project);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        TaskDTO result = taskService.findTaskById(projectId, taskId, userId);

        verify(projectValidationService).validateTaskOwnership(taskId, projectId, userId);
        assertNotNull(result);
        assertEquals("Task 1", result.getTitle());
    }

    @Test
    void findTaskById_taskNotFound_throwsNotFoundException() {
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> taskService.findTaskById(projectId, taskId, userId));
    }

    @Test
    void createTask_createsSuccessfully_returnsTaskDTO() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Project project = mock(Project.class);

        CreateTaskRequest request = mock(CreateTaskRequest.class);
        when(request.title()).thenReturn("New Task");
        when(request.description()).thenReturn("Task description");
        when(request.status()).thenReturn("TODO");
        when(request.dueDate()).thenReturn(LocalDateTime.of(2026, 12, 31, 0, 0));

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        TaskDTO result = taskService.createTask(projectId, request, userId);

        verify(projectValidationService).validateProjectBelongsToUser(projectId, userId);
        verify(taskRepository).save(org.mockito.ArgumentMatchers.any(Task.class));
        assertEquals("New Task", result.getTitle());
        assertEquals("Task description", result.getDescription());
        assertEquals(TaskStatus.TODO, result.getStatus());
        assertEquals(LocalDate.of(2026, 12, 31), result.getDueDate());
    }

    @Test
    void createTask_projectNotFound_throwsNotFoundException() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> taskService.createTask(projectId, mock(CreateTaskRequest.class), userId));
    }

    @Test
    void updateTask_updatesSuccessfully_returnsTaskDTO() {
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Project project = mock(Project.class);
        Task task = new Task("Old Title", "Old Desc", TaskStatus.TODO, null, project);

        UpdateTaskRequest request = mock(UpdateTaskRequest.class);
        when(request.title()).thenReturn("Updated Title");
        when(request.description()).thenReturn("Updated Desc");
        when(request.status()).thenReturn("DONE");
        when(request.dueDate()).thenReturn(null);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        TaskDTO result = taskService.updateTask(projectId, taskId, request, userId);

        verify(projectValidationService).validateTaskOwnership(taskId, projectId, userId);
        verify(taskRepository).save(task);
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Desc", result.getDescription());
        assertEquals(TaskStatus.DONE, result.getStatus());
    }

    @Test
    void updateTask_taskNotFound_throwsNotFoundException() {
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> taskService.updateTask(projectId, taskId, mock(UpdateTaskRequest.class), userId));
    }

    @Test
    void deleteTask_deletesSuccessfully() {
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Project project = mock(Project.class);
        Task task = new Task("Task 1", "Description", TaskStatus.TODO, null, project);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        taskService.deleteTask(projectId, taskId, userId);

        verify(projectValidationService).validateTaskOwnership(taskId, projectId, userId);
        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_taskNotFound_throwsNotFoundException() {
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> taskService.deleteTask(projectId, taskId, userId));
    }
}
