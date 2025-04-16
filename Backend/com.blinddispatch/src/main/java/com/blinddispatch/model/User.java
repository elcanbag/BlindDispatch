package com.blinddispatch.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
@JsonIgnoreProperties({
        "password", "email", "active", "enabled",
        "accountNonLocked", "authorities",
        "credentialsNonExpired", "accountNonExpired"
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String email;

    private String password;

    private boolean active;

    private boolean verified;
}
