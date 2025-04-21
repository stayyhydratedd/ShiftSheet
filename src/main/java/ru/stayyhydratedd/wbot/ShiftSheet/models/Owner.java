package ru.stayyhydratedd.wbot.ShiftSheet.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Entity
@Table(name = "owner")
@ToString
@NoArgsConstructor
public class Owner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    @Getter
    @Setter
    private String name;

    @Column(name = "password")
    @Getter
    @Setter
    private String password;

    @ManyToMany
    @JoinTable(
            name = "owner_pwz",
            joinColumns = @JoinColumn(name = "owner_id"),
            inverseJoinColumns = @JoinColumn(name = "pwz_id")
    )
    @Getter
    @ToString.Exclude
    private Set<Pwz> pwzs;

    public Owner(String name, String password) {
        this.name = name;
        this.password = password;
    }
}
