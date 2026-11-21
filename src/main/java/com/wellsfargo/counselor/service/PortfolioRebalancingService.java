package com.wellsfargo.counselor.service;

import com.wellsfargo.counselor.entity.Security;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Portfolio Rebalancing & Asset Allocation Service
 * Evaluates portfolio drift against advisor target asset allocations
 * and computes recommended buy/sell rebalancing orders.
 */
public class PortfolioRebalancingService {

    public static class RebalanceRecommendation {
        private final String category;
        private final double currentPercentage;
        private final double targetPercentage;
        private final double driftPercentage;
        private final double dollarAdjustment; // positive = BUY, negative = SELL
        private final String action;

        public RebalanceRecommendation(String category, double currentPct, double targetPct, double driftPct, double adjustment, String action) {
            this.category = category;
            this.currentPercentage = currentPct;
            this.targetPercentage = targetPct;
            this.driftPercentage = driftPct;
            this.dollarAdjustment = adjustment;
            this.action = action;
        }

        public String getCategory() { return category; }
        public double getCurrentPercentage() { return currentPercentage; }
        public double getTargetPercentage() { return targetPercentage; }
        public double getDriftPercentage() { return driftPercentage; }
        public double getDollarAdjustment() { return dollarAdjustment; }
        public String getAction() { return action; }
    }

    /**
     * Calculates total market value of securities.
     */
    public double calculateTotalPortfolioValue(List<Security> securities) {
        if (securities == null || securities.isEmpty()) {
            return 0.0;
        }
        double total = 0.0;
        for (Security s : securities) {
            if (s.getPurchasePrice() != null) {
                total += s.getPurchasePrice().doubleValue() * s.getQuantity();
            }
        }
        return Math.round(total * 100.0) / 100.0;
    }

    /**
     * Computes allocation percentages by category.
     */
    public Map<String, Double> computeCategoryAllocations(List<Security> securities) {
        Map<String, Double> categoryTotals = new HashMap<>();
        double grandTotal = calculateTotalPortfolioValue(securities);

        if (grandTotal <= 0.0) {
            return categoryTotals;
        }

        for (Security s : securities) {
            String cat = s.getCategory() != null ? s.getCategory().toUpperCase() : "OTHER";
            double val = s.getPurchasePrice() != null ? s.getPurchasePrice().doubleValue() * s.getQuantity() : 0.0;
            categoryTotals.put(cat, categoryTotals.getOrDefault(cat, 0.0) + val);
        }

        Map<String, Double> percentages = new HashMap<>();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            double pct = (entry.getValue() / grandTotal) * 100.0;
            percentages.put(entry.getKey(), Math.round(pct * 10.0) / 10.0);
        }

        return percentages;
    }

    /**
     * Generates rebalancing recommendations given target allocations (e.g. {"EQUITY": 60.0, "FIXED_INCOME": 40.0}).
     */
    public Map<String, RebalanceRecommendation> evaluateRebalance(List<Security> securities, Map<String, Double> targetAllocations) {
        Map<String, RebalanceRecommendation> recommendations = new HashMap<>();
        double totalValue = calculateTotalPortfolioValue(securities);
        Map<String, Double> currentAllocations = computeCategoryAllocations(securities);

        for (Map.Entry<String, Double> targetEntry : targetAllocations.entrySet()) {
            String cat = targetEntry.getKey().toUpperCase();
            double targetPct = targetEntry.getValue();
            double currentPct = currentAllocations.getOrDefault(cat, 0.0);
            double drift = Math.round((currentPct - targetPct) * 10.0) / 10.0;

            double targetDollar = (targetPct / 100.0) * totalValue;
            double currentDollar = (currentPct / 100.0) * totalValue;
            double adjustment = Math.round((targetDollar - currentDollar) * 100.0) / 100.0;

            String action = "HOLD";
            if (drift < -2.0) {
                action = "BUY";
            } else if (drift > 2.0) {
                action = "SELL";
            }

            recommendations.put(cat, new RebalanceRecommendation(cat, currentPct, targetPct, drift, adjustment, action));
        }

        return recommendations;
    }
}
