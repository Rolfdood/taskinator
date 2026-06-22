package com.taskinator.taskinator.application.user;

import com.taskinator.taskinator.domain.entity.Name;

public class UserDTO {
    private Name name;

    private String email;

    public UserDTO(Name name, String email) {
        this.name = name;
        this.email = email;
    }

    public UserDTO(String firstName, String middleName, String lastName, String suffix, String email) {
        this.name = new Name(firstName, middleName, lastName, suffix);
        this.email = email;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
