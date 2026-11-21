package com.wellsfargo.counselor.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Wells Fargo Investment Counselor Entity Model Tests")
class InvestmentModelTest {

    @Test
    @DisplayName("FinancialAdvisor should initialize with valid credentials and client collection")
    void testFinancialAdvisorCreation() {
        FinancialAdvisor advisor = new FinancialAdvisor(
                "Jane",
                "Doe",
                "jane.doe@wellsfargo.com",
                "555-0199"
        );

        assertEquals("Jane", advisor.getFirstName());
        assertEquals("Doe", advisor.getLastName());
        assertEquals("jane.doe@wellsfargo.com", advisor.getEmail());
        assertEquals("555-0199", advisor.getPhone());
        assertNull(advisor.getAdvisorId(), "Id should be unassigned before DB persistence");

        List<Client> clients = new ArrayList<>();
        advisor.setClients(clients);
        assertSame(clients, advisor.getClients());
    }

    @Test
    @DisplayName("Client should map to FinancialAdvisor and have one Portfolio")
    void testClientEntityAndRelationships() {
        FinancialAdvisor advisor = new FinancialAdvisor(
                "Jane", "Doe", "jane.doe@wellsfargo.com", "555-0199"
        );
        LocalDate dob = LocalDate.of(1990, 5, 15);

        Client client = new Client(
                advisor,
                "Robert",
                "Smith",
                "robert.smith@example.com",
                "555-0142",
                dob
        );

        assertEquals("Robert", client.getFirstName());
        assertEquals("Smith", client.getLastName());
        assertEquals("robert.smith@example.com", client.getEmail());
        assertEquals("555-0142", client.getPhone());
        assertEquals(dob, client.getDateOfBirth());
        assertSame(advisor, client.getAdvisor());

        Portfolio portfolio = new Portfolio(client, "Growth & Retirement");
        client.setPortfolio(portfolio);
        assertSame(portfolio, client.getPortfolio());
    }

    @Test
    @DisplayName("Portfolio should map to Client and manage Security assets")
    void testPortfolioAndSecurities() {
        FinancialAdvisor advisor = new FinancialAdvisor("A", "B", "ab@wf.com", "123");
        Client client = new Client(advisor, "C", "D", "cd@wf.com", "456", LocalDate.of(1985, 1, 1));
        Portfolio portfolio = new Portfolio(client, "High-Yield Tech");

        assertEquals("High-Yield Tech", portfolio.getName());
        assertSame(client, portfolio.getClient());

        Security apple = new Security(
                portfolio,
                "Apple Inc. (AAPL)",
                "Equities",
                LocalDate.of(2023, 10, 1),
                new BigDecimal("175.50"),
                50
        );

        Security usTreasury = new Security(
                portfolio,
                "10-Year US Treasury Note",
                "Fixed Income",
                LocalDate.of(2023, 11, 15),
                new BigDecimal("98.25"),
                100
        );

        List<Security> securities = new ArrayList<>();
        securities.add(apple);
        securities.add(usTreasury);
        portfolio.setSecurities(securities);

        assertEquals(2, portfolio.getSecurities().size());
        assertEquals("Apple Inc. (AAPL)", portfolio.getSecurities().get(0).getName());
        assertEquals(new BigDecimal("175.50"), portfolio.getSecurities().get(0).getPurchasePrice());
        assertEquals(50, portfolio.getSecurities().get(0).getQuantity());
        assertSame(portfolio, apple.getPortfolio());
    }

    @Test
    @DisplayName("Security should maintain exact financial BigDecimal precision")
    void testSecurityPricePrecision() {
        Portfolio portfolio = new Portfolio();
        BigDecimal price = new BigDecimal("1234.5678");

        Security sec = new Security(
                portfolio,
                "Alphabet Class A",
                "Equities",
                LocalDate.now(),
                price,
                25
        );

        assertEquals(price, sec.getPurchasePrice());
        assertEquals(25, sec.getQuantity());
        assertEquals("Equities", sec.getCategory());
    }

    @Test
    @DisplayName("Advisor entity setters and getters should work consistently")
    void testAdvisorLegacyEntity() {
        Advisor advisor = new Advisor("John", "Miller", "100 Pine St, SF", "555-9000", "john@wf.com");
        assertEquals("John", advisor.getFirstName());
        assertEquals("Miller", advisor.getLastName());
        assertEquals("100 Pine St, SF", advisor.getAddress());
        assertEquals("555-9000", advisor.getPhone());
        assertEquals("john@wf.com", advisor.getEmail());

        advisor.setAddress("200 California St, SF");
        assertEquals("200 California St, SF", advisor.getAddress());
    }
}
