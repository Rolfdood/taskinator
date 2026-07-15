package com.taskinator.taskinator.application.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taskinator.taskinator.application.ProjectValidationService;
import com.taskinator.taskinator.domain.ProjectPermission;
import com.taskinator.taskinator.domain.entity.Name;
import com.taskinator.taskinator.domain.entity.Project;
import com.taskinator.taskinator.domain.entity.ProjectMember;
import com.taskinator.taskinator.domain.entity.ProjectRole;
import com.taskinator.taskinator.domain.entity.User;
import com.taskinator.taskinator.domain.repository.ProjectMemberRepository;
import com.taskinator.taskinator.domain.repository.ProjectRepository;
import com.taskinator.taskinator.domain.repository.ProjectRoleRepository;
import com.taskinator.taskinator.domain.repository.TaskRepository;
import com.taskinator.taskinator.domain.repository.UserRepository;
import com.taskinator.taskinator.exception.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProjectMemberServiceTest {

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private ProjectRoleRepository projectRoleRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectValidationService projectValidationService;

    @InjectMocks
    private ProjectMemberService projectMemberService;

    private User createMockUser(UUID id, String email, String firstName, String lastName) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getEmail()).thenReturn(email);
        Name name = mock(Name.class);
        when(name.getFirstName()).thenReturn(firstName);
        when(name.getLastName()).thenReturn(lastName);
        when(user.getName()).thenReturn(name);
        return user;
    }

    @Test
    void listMembers_validatesPermissionAndReturnsMembers() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ProjectRole role = mock(ProjectRole.class);
        User memberUser = createMockUser(UUID.randomUUID(), "member@test.com", "Jane", "Doe");
        ProjectMember member = mock(ProjectMember.class);

        when(member.getUser()).thenReturn(memberUser);
        when(member.getRole()).thenReturn(role);
        when(role.getId()).thenReturn(UUID.randomUUID());
        when(role.getName()).thenReturn("Manager");
        when(member.getJoinedAt()).thenReturn(java.time.LocalDateTime.now());
        when(projectMemberRepository.findAllByProjectId(projectId)).thenReturn(List.of(member));

        List<ProjectMemberDTO> result = projectMemberService.listMembers(projectId, userId);

        verify(projectValidationService).validatePermission(projectId, userId, ProjectPermission.PROJECT_VIEW);
        assertEquals(1, result.size());
        assertEquals("member@test.com", result.get(0).email());
    }

    @Test
    void listMembers_noPermission_throwsNotFoundException() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        doThrow(new NotFoundException("Project not found."))
            .when(projectValidationService).validatePermission(projectId, userId, ProjectPermission.PROJECT_VIEW);

        assertThrows(NotFoundException.class,
            () -> projectMemberService.listMembers(projectId, userId));
    }

    @Test
    void addMember_addsSuccessfully() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        String email = "newmember@test.com";

        User admin = createMockUser(UUID.randomUUID(), "admin@test.com", "Admin", "User");
        User targetUser = createMockUser(UUID.randomUUID(), email, "New", "Member");
        Project project = mock(Project.class);
        ProjectRole role = mock(ProjectRole.class);

        when(project.getUser()).thenReturn(admin);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(targetUser));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByUserIdAndProjectId(targetUser.getId(), projectId)).thenReturn(false);
        when(projectRoleRepository.findByIdAndProjectId(roleId, projectId)).thenReturn(Optional.of(role));
        when(role.getName()).thenReturn("Member");

        ProjectMemberDTO result = projectMemberService.addMember(projectId, userId, email, roleId);

        verify(projectValidationService).validatePermission(projectId, userId, ProjectPermission.MEMBER_MANAGE);
        assertEquals(email, result.email());
        assertEquals("Member", result.roleName());
    }

    @Test
    void addMember_emailNotFound_throwsNotFoundException() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        String email = "nonexistent@test.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> projectMemberService.addMember(projectId, userId, email, roleId));
    }

    @Test
    void addMember_isAdmin_throwsIllegalArgumentException() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        String email = "admin@test.com";

        User admin = createMockUser(UUID.randomUUID(), email, "Admin", "User");
        Project project = mock(Project.class);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(admin));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(project.getUser()).thenReturn(admin);

        assertThrows(IllegalArgumentException.class,
            () -> projectMemberService.addMember(projectId, userId, email, roleId));
    }

    @Test
    void addMember_alreadyMember_throwsIllegalArgumentException() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        String email = "existing@test.com";

        User admin = createMockUser(UUID.randomUUID(), "admin@test.com", "Admin", "User");
        User targetUser = createMockUser(UUID.randomUUID(), email, "Existing", "Member");
        Project project = mock(Project.class);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(targetUser));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(project.getUser()).thenReturn(admin);
        when(projectMemberRepository.existsByUserIdAndProjectId(targetUser.getId(), projectId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
            () -> projectMemberService.addMember(projectId, userId, email, roleId));
    }

    @Test
    void addMember_roleNotInProject_throwsNotFoundException() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        String email = "newmember@test.com";

        User admin = createMockUser(UUID.randomUUID(), "admin@test.com", "Admin", "User");
        User targetUser = createMockUser(UUID.randomUUID(), email, "New", "Member");
        Project project = mock(Project.class);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(targetUser));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(project.getUser()).thenReturn(admin);
        when(projectMemberRepository.existsByUserIdAndProjectId(targetUser.getId(), projectId)).thenReturn(false);
        when(projectRoleRepository.findByIdAndProjectId(roleId, projectId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> projectMemberService.addMember(projectId, userId, email, roleId));
    }

    @Test
    void addMember_noPermission_throwsNotFoundException() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        String email = "newmember@test.com";

        doThrow(new NotFoundException("Project not found."))
            .when(projectValidationService).validatePermission(projectId, userId, ProjectPermission.MEMBER_MANAGE);

        assertThrows(NotFoundException.class,
            () -> projectMemberService.addMember(projectId, userId, email, roleId));
    }

    @Test
    void updateMemberRole_updatesSuccessfully() {
        UUID projectId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID newRoleId = UUID.randomUUID();

        ProjectRole newRole = mock(ProjectRole.class);
        User memberUser = createMockUser(targetUserId, "member@test.com", "Jane", "Doe");
        ProjectMember member = mock(ProjectMember.class);

        when(projectMemberRepository.findByUserIdAndProjectId(targetUserId, projectId))
            .thenReturn(Optional.of(member));
        when(projectRoleRepository.findByIdAndProjectId(newRoleId, projectId)).thenReturn(Optional.of(newRole));
        when(member.getUser()).thenReturn(memberUser);
        when(member.getRole()).thenReturn(newRole);
        when(newRole.getId()).thenReturn(newRoleId);
        when(newRole.getName()).thenReturn("NewRole");
        when(member.getJoinedAt()).thenReturn(java.time.LocalDateTime.now());

        ProjectMemberDTO result = projectMemberService.updateMemberRole(projectId, targetUserId, userId, newRoleId);

        verify(projectValidationService).validatePermission(projectId, userId, ProjectPermission.MEMBER_MANAGE);
        assertEquals("NewRole", result.roleName());
    }

    @Test
    void updateMemberRole_memberNotFound_throwsNotFoundException() {
        UUID projectId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        when(projectMemberRepository.findByUserIdAndProjectId(targetUserId, projectId))
            .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> projectMemberService.updateMemberRole(projectId, targetUserId, userId, roleId));
    }

    @Test
    void updateMemberRole_roleNotInProject_throwsNotFoundException() {
        UUID projectId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        ProjectMember member = mock(ProjectMember.class);

        when(projectMemberRepository.findByUserIdAndProjectId(targetUserId, projectId))
            .thenReturn(Optional.of(member));
        when(projectRoleRepository.findByIdAndProjectId(roleId, projectId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> projectMemberService.updateMemberRole(projectId, targetUserId, userId, roleId));
    }

    @Test
    void removeMember_removesSuccessfully() {
        UUID projectId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(projectMemberRepository.existsByUserIdAndProjectId(targetUserId, projectId)).thenReturn(true);

        projectMemberService.removeMember(projectId, targetUserId, userId);

        verify(projectValidationService).validatePermission(projectId, userId, ProjectPermission.MEMBER_MANAGE);
        verify(taskRepository).unassignTasksByProjectIdAndUserId(projectId, targetUserId);
        verify(projectMemberRepository).deleteByUserIdAndProjectId(targetUserId, projectId);
    }

    @Test
    void removeMember_memberNotFound_throwsNotFoundException() {
        UUID projectId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(projectMemberRepository.existsByUserIdAndProjectId(targetUserId, projectId)).thenReturn(false);

        assertThrows(NotFoundException.class,
            () -> projectMemberService.removeMember(projectId, targetUserId, userId));
    }

    @Test
    void removeMember_noPermission_throwsNotFoundException() {
        UUID projectId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        doThrow(new NotFoundException("Project not found."))
            .when(projectValidationService).validatePermission(projectId, userId, ProjectPermission.MEMBER_MANAGE);

        assertThrows(NotFoundException.class,
            () -> projectMemberService.removeMember(projectId, targetUserId, userId));
    }
}
