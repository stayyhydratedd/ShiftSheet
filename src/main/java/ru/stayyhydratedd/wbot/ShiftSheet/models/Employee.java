package ru.stayyhydratedd.wbot.ShiftSheet.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "employee")
@NoArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    @Getter
    private String name;

    @Column(name = "pay_rate")
    @Setter
    @Getter
    private Double payRate;

    @Column(name = "gmail")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@gmail\\.com$", message = "Некорректный gmail")
    @Setter
    @Getter
    private String gmail;

    @Column(name = "phone_number")
    @Pattern(regexp = "^[87]\\d{10}$|^\\d{10}$", message = "Неправильный номер телефона")
    @Setter
    @Getter
    private String phoneNumber;

    @OneToMany(mappedBy = "employee")
    @Setter
    @Getter
    private List<SheetInfo> sheets;

    @Builder
    public Employee(String name, String gmail, String phoneNumber, double payRate) {
        this.name = name;
        this.gmail = gmail;
        this.phoneNumber = phoneNumber;
        this.payRate = payRate;
    }
}
