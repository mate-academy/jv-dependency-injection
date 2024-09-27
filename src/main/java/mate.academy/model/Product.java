package mate.academy.model;

import mate.academy.lib.Inject;

import java.math.BigDecimal;

public class Product {
    @Inject
    private Long id;
    @Inject
    private String name;
    @Inject
    private String category;
    @Inject
    private String description;
    @Inject
    private BigDecimal price;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
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
