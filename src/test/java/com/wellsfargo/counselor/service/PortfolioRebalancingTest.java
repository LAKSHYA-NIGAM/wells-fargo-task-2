package com.wellsfargo.counselor.service;

import com.wellsfargo.counselor.entity.Client;
import com.wellsfargo.counselor.entity.FinancialAdvisor;
import com.wellsfargo.counselor.entity.Portfolio;
import com.wellsfargo.counselor.entity.Security;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class PortfolioRebalancingTest {

    @Test
    public void testTotalPortfolioValueAndAllocations() {
        PortfolioRebalancingService service = new PortfolioRebalancingService();

        FinancialAdvisor advisor = new FinancialAdvisor("Sarah", "Connor", "sconnor@wellsfargo.com", "555-1234");
        Client client = new Client(advisor, "John", "Doe", "jdoe@example.com", "555-5678", LocalDate.of(1985, 5, 20));
        Portfolio portfolio = new Portfolio(client, "Retirement Trust");

        // $10,000 Equity (100 shares * $100)
        Security eq = new Security(portfolio, "Apple Inc", "EQUITY", LocalDate.of(2023, 1, 1), new BigDecimal("100.00"), 100);
        // $5,000 Fixed Income (50 bonds * $100)
        Security fi = new Security(portfolio, "US Treasury 10Y", "FIXED_INCOME", LocalDate.of(2023, 1, 1), new BigDecimal("100.00"), 50);

        List<Security> securities = Arrays.asList(eq, fi);

        double totalValue = service.calculateTotalPortfolioValue(securities);
        assertEquals(15000.0, totalValue);

        Map<String, Double> allocations = service.computeCategoryAllocations(securities);
        assertEquals(66.7, allocations.get("EQUITY"));
        assertEquals(33.3, allocations.get("FIXED_INCOME"));
    }

    @Test
    public void testRebalanceRecommendationsDrift() {
        PortfolioRebalancingService service = new PortfolioRebalancingService();

        FinancialAdvisor advisor = new FinancialAdvisor("Sarah", "Connor", "sconnor@wellsfargo.com", "555-1234");
        Client client = new Client(advisor, "John", "Doe", "jdoe@example.com", "555-5678", LocalDate.of(1985, 5, 20));
        Portfolio portfolio = new Portfolio(client, "Growth Fund");

        // $8,000 Equity (80% of portfolio)
        Security eq = new Security(portfolio, "S&P 500 ETF", "EQUITY", LocalDate.of(2023, 1, 1), new BigDecimal("100.00"), 80);
        // $2,000 Fixed Income (20% of portfolio)
        Security fi = new Security(portfolio, "Agg Bond ETF", "FIXED_INCOME", LocalDate.of(2023, 1, 1), new BigDecimal("100.00"), 20);

        List<Security> securities = Arrays.asList(eq, fi);

        // Target allocation is 60% Equity / 40% Fixed Income
        Map<String, Double> target = new HashMap<>();
        target.put("EQUITY", 60.0);
        target.put("FIXED_INCOME", 40.0);

        Map<String, PortfolioRebalancingService.RebalanceRecommendation> recs = service.evaluateRebalance(securities, target);

        // Equity is 80%, target is 60% -> drift +20% -> SELL $2000
        PortfolioRebalancingService.RebalanceRecommendation eqRec = recs.get("EQUITY");
        assertEquals("SELL", eqRec.getAction());
        assertEquals(20.0, eqRec.getDriftPercentage());
        assertEquals(-2000.0, eqRec.getDollarAdjustment());

        // Fixed income is 20%, target is 40% -> drift -20% -> BUY $2000
        PortfolioRebalancingService.RebalanceRecommendation fiRec = recs.get("FIXED_INCOME");
        assertEquals("BUY", fiRec.getAction());
        assertEquals(-20.0, fiRec.getDriftPercentage());
        assertEquals(2000.0, fiRec.getDollarAdjustment());
    }
}
