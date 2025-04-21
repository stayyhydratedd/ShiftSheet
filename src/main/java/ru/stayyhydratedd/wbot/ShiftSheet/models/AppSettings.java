package ru.stayyhydratedd.wbot.ShiftSheet.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "app_settings")
@NoArgsConstructor
@ToString
public class AppSettings {

    @Id
    private int id;

    @Column(name = "pay_rate")
    @Getter
    @Setter
    private Double payRate;

    @OneToOne
    @JoinColumn(name = "last_root_folder", referencedColumnName = "id")
    @Getter
    @Setter
    private RootFolder lastRootFolder;

    @OneToOne
    @JoinColumn(name = "last_pwz", referencedColumnName = "id")
    @Getter
    @Setter
    private Pwz lastPwz;

    @OneToOne
    @JoinColumn(name = "last_month_sheet", referencedColumnName = "id")
    @Getter
    @Setter
    private MonthSheet lastMonthSheet;

    @OneToMany(mappedBy = "appSettings", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Getter
    @Setter
    private List<RootFolder> rootFolders;
}
