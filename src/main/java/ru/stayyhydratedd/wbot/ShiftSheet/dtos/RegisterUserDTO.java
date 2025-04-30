package ru.stayyhydratedd.wbot.ShiftSheet.dtos;

import lombok.Data;

@Data
public class RegisterUserDTO {

    private String username;

    private String password;

    private String confirmPassword;
}
