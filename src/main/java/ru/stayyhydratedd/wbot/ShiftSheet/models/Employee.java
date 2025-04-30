package ru.stayyhydratedd.wbot.ShiftSheet.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Entity
@Table(name = "employee")
@NoArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Getter
    private int id;

    @Column(name = "name")
    @Setter
    @Getter
    private String name;

    @Column(name = "gmail")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@gmail\\.com$", message = "Некорректный gmail")
    @Setter
    private String gmail;

    @Column(name = "phone_number")
    @Pattern(regexp = "^[87]\\d{10}$|^\\d{10}$", message = "Неправильный номер телефона")
    @Setter
    private String phoneNumber;

    @Column(name = "pay_rate")
    @Setter
    private Double payRate;

    @OneToMany(mappedBy = "employee")
    @Setter
    @Getter
    private List<SheetInfo> sheets;

    @Builder
    public Employee(int id, String name, String gmail, String phoneNumber, double payRate) {
        this.id = id;
        this.name = name;
        this.gmail = gmail;
        this.phoneNumber = phoneNumber;
        this.payRate = payRate;
    }

    public Optional<String> getGmail() {
        return Optional.ofNullable(gmail);
    }

    public Optional<String> getPhoneNumber() {
        return Optional.ofNullable(phoneNumber);
    }

    public Optional<Double> getPayRate() {
        if (payRate == 0)
            return Optional.empty();
        return Optional.of(payRate);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return id == employee.id && Objects.equals(name, employee.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
