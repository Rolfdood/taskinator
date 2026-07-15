package com.taskinator.taskinator.web.controller;

import com.taskinator.taskinator.application.user.UserDTO;
import com.taskinator.taskinator.application.user.UserService;
import com.taskinator.taskinator.infrastructure.security.CurrentUser;
import com.taskinator.taskinator.infrastructure.security.CurrentUserDetails;
import com.taskinator.taskinator.web.dto.ChangeEmailRequest;
import com.taskinator.taskinator.web.dto.ChangePasswordRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserDTO> getCurrentUserInformation(@CurrentUser CurrentUserDetails currentUserDetails) {
        return ResponseEntity.ok(userService.getCurrentUserInfo(currentUserDetails));
    }

    @PostMapping("/email")
    public ResponseEntity<UserDTO> changeEmail(@CurrentUser CurrentUserDetails currentUserDetails,
        @Valid @RequestBody ChangeEmailRequest request) {
        return ResponseEntity.ok(userService.changeUserEmail(currentUserDetails, request.newEmail()));
    }

    @PostMapping("/password")
    public ResponseEntity<UserDTO> changePassword(@CurrentUser CurrentUserDetails currentUserDetails,
        @Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(
            userService.changeUserPassword(currentUserDetails, request.currentPassword(),  request.newPassword())
        );
    }
}
