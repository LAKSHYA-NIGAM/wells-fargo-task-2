package com.wellsfargo.counselor.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "portfolio")
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long portfolioId;

    @OneToOne
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    private Client client;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Security> securities;

    // Required no-arg constructor for JPA
    public Portfolio() {}

    public Portfolio(Client client, String name) {
        this.client = client;
        this.name   = name;
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public Long getPortfolioId() {
        return portfolioId;
    }

    public Client getClient() {
        return client;
    }

    public String getName() {
        return name;
    }

    public List<Security> getSecurities() {
        return securities;
    }

    // ── Setters (no setter for id) ─────────────────────────────────────────────

    public void setClient(Client client) {
        this.client = client;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSecurities(List<Security> securities) {
        this.securities = securities;
    }
}