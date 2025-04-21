package ru.stayyhydratedd.wbot.ShiftSheet.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.stayyhydratedd.wbot.ShiftSheet.models.Owner;

import java.util.Optional;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Integer> {

    Optional<Owner> findByName(String name);
}
