package mate.academy.model;

public class Product {
    private Long id;
    private String name;
    private String category;
    private String description;
    private Double price;

    public Product(Long id, String name, String category, String description, Double price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.price = price;
    }

    @Override
    public String toString() {
        return id + " | " + name + " | " + category + " | " + description + " | $" + price;
    }
}
