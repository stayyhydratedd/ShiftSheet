package ru.stayyhydratedd.wbot.ShiftSheet.models;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "month_sheet")
@NoArgsConstructor
public class MonthSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "google_id")
    @Getter
    private String googleId;

    @ManyToOne
    @JoinColumn(name = "pwz_id")
    @Getter
    private Pwz pwz;

    @Column(name = "month_num")
    @Getter
    @Setter
    private Integer month;

    @Column(name = "year_num")
    @Getter
    @Setter
    private Integer year;

    @OneToMany(mappedBy = "monthSheet", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Getter
    @Setter
    private List<SheetInfo> sheetInfo;

    @Column(name = "pay_rate")
    @Getter
    @Setter
    private Double payRate;

    @Builder
    public MonthSheet(String googleId, int month, int year, double payRate, Pwz pwz) {
        this.googleId = googleId;
        this.month = month;
        this.year = year;
        this.payRate = payRate;
        this.pwz = pwz;
    }
}
