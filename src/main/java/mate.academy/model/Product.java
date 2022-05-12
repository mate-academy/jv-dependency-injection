package mate.academy.model;

import java.math.BigDecimal;

public class Product {
    private Long id;
    private String name;
    private String category;
    private String description;
    private BigDecimal price;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() { 
        return id; 
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "Product{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", category='" + category + '\''
                + ", description='" + description + '\''
                + ", price=" + price
                + '}';
    }
}
