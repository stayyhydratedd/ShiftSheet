package ru.stayyhydratedd.wbot.ShiftSheet.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.stayyhydratedd.wbot.ShiftSheet.dtos.OwnerDTO;
import ru.stayyhydratedd.wbot.ShiftSheet.mappers.OwnerMapper;
import ru.stayyhydratedd.wbot.ShiftSheet.models.Owner;
import ru.stayyhydratedd.wbot.ShiftSheet.repositories.OwnerRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OwnerService {

    private final OwnerRepository ownerRepository;
    private final OwnerMapper ownerMapper;
    private final PasswordEncoder passwordEncoder;

    public List<Owner> findAll() {
        return ownerRepository.findAll();
    }

    public Optional<Owner> findByName(String name) {
        return ownerRepository.findByName(name);
    }

    public void save(Owner owner) {
        owner.setPassword(passwordEncoder.encode(owner.getPassword()));
        ownerRepository.save(owner);
    }

    public Owner ownerDtoToOwner(OwnerDTO ownerDTO) {
        return ownerMapper.ownerDtoToOwner(ownerDTO);
    }
}
