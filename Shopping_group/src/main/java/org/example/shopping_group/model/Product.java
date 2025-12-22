package org.example.shopping_group.model;

import java.math.BigDecimal;

public class Product {
    private Integer id;          // 商品ID（对应数据库id字段）
    private String name;         // 商品名称（对应name字段）
    private String category;     // 商品类别（对应category字段）
    private BigDecimal price;    // 商品价格（对应price字段）
    private String description;  // 商品描述（对应description字段）
    private String imageUrl;     // 图片文件名（对应image_url字段）
    private Integer stock;       // 商品库存（对应stock字段）

    public Product() {}

    // Getter & Setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}