package ru.stayyhydratedd.wbot.ShiftSheet.models;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Entity
@Table(name = "pwz")
@NoArgsConstructor
public class Pwz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "google_id")
    @Setter
    private String googleId;

    @Column(name = "address")
    @Setter
    private String address;

    @Column(name = "pay_rate")
    @Setter
    private Double payRate;

    @OneToMany(mappedBy = "pwz")
    @Setter
    private List<MonthSheet> monthSheets;

    @Builder
    public Pwz(String googleId, String address, double payRate) {
        this.googleId = googleId;
        this.address = address;
        this.payRate = payRate;
    }
}
