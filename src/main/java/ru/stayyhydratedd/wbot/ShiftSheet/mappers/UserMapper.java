package ru.stayyhydratedd.wbot.ShiftSheet.mappers;

import org.mapstruct.Mapper;
import ru.stayyhydratedd.wbot.ShiftSheet.dtos.AuthUserDTO;
import ru.stayyhydratedd.wbot.ShiftSheet.dtos.RegisterUserDTO;
import ru.stayyhydratedd.wbot.ShiftSheet.models.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User registerUserDtoToUser(RegisterUserDTO userDTO);

    User authUserDtoToUser(AuthUserDTO authUserDTO);

    AuthUserDTO registerUserDtoToAuthUserDto(RegisterUserDTO userDTO);
}
