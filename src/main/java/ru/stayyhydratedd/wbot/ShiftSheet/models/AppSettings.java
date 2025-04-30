package ru.stayyhydratedd.wbot.ShiftSheet.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Optional;

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

    @Column(name = "pwsz_folder_id")
    @Getter
    @Setter
    private String pwzsFolderId;

    @OneToOne
    @JoinColumn(name = "last_root_folder", referencedColumnName = "id")
    @Setter
    private RootFolder lastRootFolder;

    @OneToOne
    @JoinColumn(name = "last_pwz", referencedColumnName = "id")
    @Setter
    private Pwz lastPwz;

    @OneToOne
    @JoinColumn(name = "last_month_sheet", referencedColumnName = "id")
    @Setter
    private MonthSheet lastMonthSheet;

    @OneToMany(mappedBy = "appSettings", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.EAGER)
    @Getter
    @Setter
    private List<RootFolder> rootFolders;

    public Optional<RootFolder> getLastRootFolder() {
        return Optional.ofNullable(lastRootFolder);
    }

    public Optional<Pwz> getLastPwz() {
        return Optional.ofNullable(lastPwz);
    }

    public Optional<MonthSheet> getLastMonthSheet() {
        return Optional.ofNullable(lastMonthSheet);
    }
}
