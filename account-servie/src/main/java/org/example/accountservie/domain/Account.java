package org.example.accountservie.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String customerReference;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountStatus status;
    @Column(nullable = false)
    private String currency;
    @Column(nullable = false)
    private double balance;
}
