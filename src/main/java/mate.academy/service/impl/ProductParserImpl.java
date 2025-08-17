package mate.academy.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import mate.academy.lib.Component;
import mate.academy.model.Product;
import mate.academy.service.ProductParser;

@Component
public class ProductParserImpl implements ProductParser {
    public static final int ID_POSITION = 0;
    public static final int NAME_POSITION = 1;
    public static final int CATEGORY_POSITION = 2;
    public static final int DESCRIPTION_POSITION = 3;
    public static final int PRICE_POSITION = 4;

    @Override
    public List<Product> parse(List<String> lines) {
        return lines.stream()
                .map(line -> {
                    String[] parts = line.split(",");
                    return new Product(
                            Long.parseLong(parts[ID_POSITION]),
                            parts[NAME_POSITION],
                            parts[CATEGORY_POSITION],
                            parts[DESCRIPTION_POSITION],
                            Double.parseDouble(parts[PRICE_POSITION])
                    );
                })
                .collect(Collectors.toList());
    }
}
