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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
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
