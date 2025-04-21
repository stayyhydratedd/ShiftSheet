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
    private Integer id;

    @Column(name = "google_id")
    @Setter
    private String googleId;

    @Column(name = "address")
    @Setter
    private String address;

    @Column(name = "pay_rate")
    @Setter
    private Double payRate;

    @ManyToMany(mappedBy = "pwzs")
    @Setter
    private Set<Owner> owners;

    @OneToMany(mappedBy = "pwz")
    @Setter
    private List<MonthSheet> monthSheets;

    @Builder
    public Pwz(String googleId, String address, Double payRate) {
        this.googleId = googleId;
        this.address = address;
        this.payRate = payRate;
    }
}
