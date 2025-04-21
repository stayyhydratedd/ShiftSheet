package ru.stayyhydratedd.wbot.ShiftSheet.mappers;

import org.mapstruct.Mapper;
import ru.stayyhydratedd.wbot.ShiftSheet.dtos.OwnerDTO;
import ru.stayyhydratedd.wbot.ShiftSheet.models.Owner;

@Mapper(componentModel = "spring")
public interface OwnerMapper {

    Owner ownerDtoToOwner(OwnerDTO ownerDTO);
}
