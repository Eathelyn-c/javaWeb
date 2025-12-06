package org.example.shopping_group.model;

import java.math.BigDecimal;

public class Product {
    private Integer id;          // 商品ID（对应id）
    private String name;         // 商品名称（对应name）
    private String category;     // 类别（对应category）
    private BigDecimal price;    // 价格（对应price）
    private String description;  // 描述（对应description）
    private String imageUrl;     // 图片文件名（对应image_url）
    private Integer stock;       // 库存（对应stock）

    public Product() {}

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