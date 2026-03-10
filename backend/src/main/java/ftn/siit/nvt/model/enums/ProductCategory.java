package ftn.siit.nvt.model.enums;

import java.io.Serializable;

public enum ProductCategory implements Serializable  {
    SOFT_DRINKS("Soft Drinks", "Carbonated drinks like Coca-Cola, Sprite", "SOFT", "seq_sku_soft_drinks"),
    JUICES("Juices", "Fruit juices and nectars", "JUICE", "seq_sku_juices"),
    WATER("Water", "Various types of water", "WATER", "seq_sku_water"),
    SPORTS_DRINKS("Sports & Energy Drinks", "Energy and sports drinks", "SPORT", "seq_sku_sports_drinks"),
    COFFEE("Coffee", "Coffee and coffee products", "COFFEE", "seq_sku_coffee"),
    TEA("Tea", "Various teas", "TEA", "seq_sku_tea"),
    PLANT_BASED("Plant-Based Drinks", "Plant-based and healthy beverages", "PLANT", "seq_sku_plant_based"),
    DAIRY("Dairy Products", "Yogurt, milk, etc.", "DAIRY", "seq_sku_dairy");

    private final String displayName;
    private final String description;
    private final String skuPrefix;
    private final String sequenceName;

    ProductCategory(String displayName, String description, String skuPrefix, String sequenceName) {
        this.displayName = displayName;
        this.description = description;
        this.skuPrefix = skuPrefix;
        this.sequenceName = sequenceName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getSkuPrefix() {
        return skuPrefix;
    }

    public String getSequenceName() {
        return sequenceName;
    }
}
