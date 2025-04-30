package ru.stayyhydratedd.wbot.ShiftSheet.models;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "pwz")
@NoArgsConstructor
public class Pwz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "google_id")
    private String googleId;

    @Column(name = "address")
    private String address;

    @Column(name = "pay_rate")
    private Double payRate;

    @OneToMany(mappedBy = "pwz", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<MonthSheet> monthSheets;

    @ManyToOne
    @JoinColumn(name = "root_folder_id", referencedColumnName = "id")
    private RootFolder rootFolder;

    @Builder
    public Pwz(String googleId, String address, double payRate, RootFolder rootFolder) {
        this.googleId = googleId;
        this.address = address;
        this.payRate = payRate;
        this.rootFolder = rootFolder;
    }
}
