package ru.stayyhydratedd.wbot.ShiftSheet.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employee")
@NoArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    @Size(min = 2, message = "Минимальная длина имени - 2 символа")
    private String name;

    @Column(name = "pay_rate")
    @Min(value = 1, message = "Ставка оплаты не может быть отрицательной")
    private Integer payRate;

    @Column(name = "email")
    @Email(message = "Некорректный email-адрес")
    private String email;

    @Column(name = "phone_number")
    @Pattern(regexp = "^[87]\\d{10}$|^\\d{10}$", message = "Неправильный номер телефона")
    private String phoneNumber;

    @Builder
    public Employee(String name, String email, String phoneNumber, Integer payRate) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.payRate = payRate;
    }
}
