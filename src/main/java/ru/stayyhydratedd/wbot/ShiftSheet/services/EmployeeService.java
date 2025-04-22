package ru.stayyhydratedd.wbot.ShiftSheet.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.stayyhydratedd.wbot.ShiftSheet.models.Employee;
import ru.stayyhydratedd.wbot.ShiftSheet.repositories.EmployeeRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public void save(Employee employee) {
        employeeRepository.save(employee);
    }

    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> findEmployeeByName(String name) {
        return employeeRepository.findEmployeeByName(name);
    }
    public Optional<Employee> findEmployeeByGmail(String gmail) {
        return employeeRepository.findEmployeeByGmail(gmail);
    }
    public Optional<Employee> findEmployeeByPhoneNumber(String phoneNumber) {
        return employeeRepository.findEmployeeByPhoneNumber(phoneNumber);
    }
}
