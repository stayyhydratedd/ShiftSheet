package ru.stayyhydratedd.wbot.ShiftSheet.models;

import com.google.api.client.util.DateTime;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Table(name = "root_folder")
@NoArgsConstructor
@Getter
@ToString
public class RootFolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "google_id", nullable = false, unique = true)
    private String googleId;

    @Column(name = "title", nullable = false)
    @Setter
    private String title;

    @Column(name = "pay_rate")
    @Setter
    private Double payRate;

    @Column(name = "created_time")
    @Setter
    private DateTime createdTime;

    @OneToMany(mappedBy = "rootFolder", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @ToString.Exclude
    private List<Pwz> pwzs;

    @ManyToOne
    @JoinColumn(name = "app_settings", referencedColumnName = "id")
    @ToString.Exclude
    private AppSettings appSettings;

    @ManyToMany(mappedBy = "rootFolders")
    @ToString.Exclude
    private Set<User> users;

    @Builder
    public RootFolder(int id, String googleId, String title, Double payRate, DateTime createdTime, AppSettings appSettings) {
        this.id = id;
        this.googleId = googleId;
        this.title = title;
        this.payRate = payRate;
        this.createdTime = createdTime;
        this.appSettings = appSettings;
        this.pwzs = new ArrayList<>();
        this.users = new HashSet<>();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RootFolder that = (RootFolder) o;
        return Objects.equals(googleId, that.googleId) && Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(googleId, title);
    }
}
