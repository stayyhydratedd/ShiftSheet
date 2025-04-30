package ru.stayyhydratedd.wbot.ShiftSheet.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.stayyhydratedd.wbot.ShiftSheet.models.Employee;
import ru.stayyhydratedd.wbot.ShiftSheet.services.EmployeeService;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmployeeValidator implements Validator {

    private final EmployeeService employeeService;

    @Override
    public boolean supports(Class<?> clazz) {
        return Employee.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Employee employee = (Employee) target;

        Optional<Employee> foundEmployee = employeeService.findEmployeeByName(employee.getName());
        if(foundEmployee.isPresent()) {
            errors.rejectValue("name", null,
                    "Сотрудник с таким именем уже существует");
        }
        foundEmployee = employeeService.findEmployeeByGmail(employee.getGmail().get());
        if(foundEmployee.isPresent()) {
            errors.rejectValue("gmail", null,
                    "Сотрудник с таким gmail уже существует");
        }
        foundEmployee = employeeService.findEmployeeByPhoneNumber(employee.getPhoneNumber().get());
        if(foundEmployee.isPresent()) {
            errors.rejectValue("phoneNumber", null,
                    "Сотрудник с таким номером телефона уже существует");
        }
    }
}
