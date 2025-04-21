package ru.stayyhydratedd.wbot.ShiftSheet.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "root_folder")
@NoArgsConstructor
@ToString
public class RootFolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "google_id", nullable = false, unique = true)
    @Getter
    private String googleId;

    @Column(name = "title", nullable = false)
    @Getter
    private String title;

    @Column(name = "pay_rate")
    @Getter
    @Setter
    private Double payRate;

    @ManyToOne
    @JoinColumn(name = "app_settings", referencedColumnName = "id")
    @ToString.Exclude
    private AppSettings appSettings;


    @Builder
    public RootFolder(String googleId, String title, Double payRate, AppSettings appSettings) {
        this.googleId = googleId;
        this.title = title;
        this.payRate = payRate;
        this.appSettings = appSettings;
    }
}
