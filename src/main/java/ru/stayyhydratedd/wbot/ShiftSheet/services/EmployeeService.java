package ru.stayyhydratedd.wbot.ShiftSheet.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.stayyhydratedd.wbot.ShiftSheet.models.Employee;
import ru.stayyhydratedd.wbot.ShiftSheet.repositories.EmployeeRepository;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public void save(Employee employee) {
        employeeRepository.save(employee);
    }
}
