package ru.stayyhydratedd.wbot.ShiftSheet.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthUserDTO {

    private String username;

    private String password;
}
