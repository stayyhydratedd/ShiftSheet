package ru.stayyhydratedd.wbot.ShiftSheet.models;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "month_sheet")
@NoArgsConstructor
public class MonthSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "google_id")
    @Getter
    private String googleId;

    @ManyToOne
    @JoinColumn(name = "pwz_id")
    @Getter
    private Pwz pwz;

    @Column(name = "month_num")
    @Getter
    private Integer month;

    @Column(name = "year_num")
    @Getter
    private Integer year;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "sheet_info_id", referencedColumnName = "id")
    @Getter
    private SheetInfo sheetInfo;

    @Column(name = "pay_rate")
    @Getter
    private Double payRate;

    @Builder
    public MonthSheet(String googleId, Integer month, Integer year, Double payRate, Pwz pwz) {
        this.googleId = googleId;
        this.month = month;
        this.year = year;
        this.payRate = payRate;
        this.pwz = pwz;
    }
}
