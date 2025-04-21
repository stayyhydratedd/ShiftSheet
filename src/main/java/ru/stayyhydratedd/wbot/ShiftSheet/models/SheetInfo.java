package ru.stayyhydratedd.wbot.ShiftSheet.models;

import jakarta.persistence.*;

@Entity
@Table(name = "sheet_info")
public class SheetInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @OneToOne
    private MonthSheet monthSheet;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "hours")
    private Double hours;

    @Column(name = "salary")
    private Double salary;
}
