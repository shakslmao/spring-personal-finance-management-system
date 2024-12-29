package com.devshaks.personal_finance.transactions;

import java.util.*;

public class TransactionsCategoryMapper {
    private static final Map<String, List<String>> CATEGORY_TAGS = new HashMap<>();

    static {
        CATEGORY_TAGS.put("Groceries", Arrays.asList("food", "essentials", "shopping", "daily needs"));
        CATEGORY_TAGS.put("Entertainment", Arrays.asList("leisure", "fun", "movies", "music", "games"));
        CATEGORY_TAGS.put("Bills", Arrays.asList("utilities", "monthly", "electricity", "water", "internet"));
        CATEGORY_TAGS.put("Transportation", Arrays.asList("commute", "fuel", "public transport", "travel"));
        CATEGORY_TAGS.put("Dining Out", Arrays.asList("restaurants", "food", "social", "luxury"));
        CATEGORY_TAGS.put("Health", Arrays.asList("medical", "pharmacy", "insurance", "wellness", "fitness"));
        CATEGORY_TAGS.put("Education", Arrays.asList("tuition", "books", "learning", "courses", "training"));
        CATEGORY_TAGS.put("Savings", Arrays.asList("goals", "investments", "future", "security", "finance"));
        CATEGORY_TAGS.put("Shopping", Arrays.asList("clothing", "electronics", "accessories", "luxury", "online"));
        CATEGORY_TAGS.put("Travel", Arrays.asList("vacation", "flights", "hotels", "adventure", "tourism"));
        CATEGORY_TAGS.put("Subscriptions", Arrays.asList("streaming", "software", "monthly", "services"));
        CATEGORY_TAGS.put("Charity", Arrays.asList("donation", "non-profit", "help", "community"));
        CATEGORY_TAGS.put("Home", Arrays.asList("rent", "mortgage", "furniture", "appliances", "repairs"));
        CATEGORY_TAGS.put("Fitness", Arrays.asList("gym", "sports", "workout", "equipment", "health"));
        CATEGORY_TAGS.put("Pets", Arrays.asList("food", "vet", "supplies", "care", "toys"));
        CATEGORY_TAGS.put("Insurance", Arrays.asList("health", "auto", "home", "life", "protection"));
        CATEGORY_TAGS.put("Investments", Arrays.asList("stocks", "bonds", "mutual funds", "retirement", "portfolio"));
        CATEGORY_TAGS.put("Taxes", Arrays.asList("income tax", "property tax", "deductions", "returns"));
        CATEGORY_TAGS.put("Loans", Arrays.asList("personal", "auto", "education", "repayment", "interest"));
        CATEGORY_TAGS.put("Gifts", Arrays.asList("presents", "special occasions", "holidays", "celebrations"));
        CATEGORY_TAGS.put("Miscellaneous", Arrays.asList("other", "unplanned", "one-time", "extra"));
    }

    public static List<String> getTagsForCategory(String category) {
        return CATEGORY_TAGS.getOrDefault(category, Collections.emptyList());
    }

}
