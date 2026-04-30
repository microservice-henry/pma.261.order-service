package store.order;

import java.math.BigDecimal;
import java.util.List;

public class OrderParser {

    public static OrderItem to(OrderItemIn in, BigDecimal total) {
        return OrderItem.builder()
            .idProduct(in.idProduct())
            .quantity(in.quantity())
            .total(total)
            .build();
    }

    public static OrderSummaryOut toSummary(Order order) {
        return OrderSummaryOut.builder()
            .id(order.id())
            .date(order.date())
            .total(order.total())
            .build();
    }

    public static List<OrderSummaryOut> toSummary(List<Order> orders) {
        return orders.stream().map(OrderParser::toSummary).toList();
    }

    public static OrderOut toOut(Order order, String currency) {
        return OrderOut.builder()
            .id(order.id())
            .date(order.date())
            .currency(currency)
            .items(order.items().stream().map(OrderParser::toOut).toList())
            .total(order.total())
            .build();
    }

    private static OrderItemOut toOut(OrderItem item) {
        return OrderItemOut.builder()
            .id(item.id())
            .product(ProductRefOut.builder().id(item.idProduct()).build())
            .quantity(item.quantity())
            .total(item.total())
            .build();
    }

}
