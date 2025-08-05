package com.sihe.emsbackend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "hosts")
public class Host {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String email;

    private String mobileNumber;

    private LocalDate dateOfBirth;

    private String eventCategory;

    private String password;
    public String getPassword() {
        return password;
    }
// <-- Add password field

}
