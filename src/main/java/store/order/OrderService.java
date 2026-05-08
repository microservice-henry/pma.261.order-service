package store.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import feign.FeignException;

@Service
public class OrderService {

    private static final String DEFAULT_CURRENCY = "USD";

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductClient productClient;

    @Autowired
    private ExchangeClient exchangeClient;

    public Order create(String idAccount, OrderIn in) {
        validateAccount(idAccount);
        validateOrder(in);

        List<OrderItem> items = in.items().stream()
            .map(this::buildItem)
            .toList();

        BigDecimal total = items.stream()
            .map(OrderItem::total)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        Order order = Order.builder()
            .idAccount(idAccount)
            .date(LocalDateTime.now())
            .total(total)
            .build();

        Order saved = orderRepository.save(new OrderModel(order)).to();

        List<OrderItem> savedItems = items.stream()
            .map(item -> orderItemRepository.save(new OrderItemModel(saved.id(), item)).to())
            .toList();

        return saved.items(savedItems);
    }

    public List<Order> findAll(String idAccount) {
        validateAccount(idAccount);
        return orderRepository.findByIdAccountOrderByDateDesc(idAccount)
            .stream()
            .map(OrderModel::to)
            .toList();
    }

    public Order findById(String idAccount, String id, String currency) {
        validateAccount(idAccount);
        Order order = orderRepository.findByIdAndIdAccount(id, idAccount)
            .map(OrderModel::to)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        order.items(
            orderItemRepository.findByIdOrder(order.id())
                .stream()
                .map(OrderItemModel::to)
                .toList()
        );

        BigDecimal rate = resolveRate(currency);
        if (rate.compareTo(BigDecimal.ONE) != 0) {
            order.total(convert(order.total(), rate));
            order.items(
                order.items().stream()
                    .map(item -> item.total(convert(item.total(), rate)))
                    .toList()
            );
        }
        return order;
    }

    public String normalizeCurrency(String currency) {
        return currency == null || currency.trim().isEmpty()
            ? DEFAULT_CURRENCY
            : currency.trim().toUpperCase();
    }

    private OrderItem buildItem(OrderItemIn in) {
        validateItem(in);
        ProductOut product = findProduct(in.idProduct());
        BigDecimal total = product.price()
            .multiply(BigDecimal.valueOf(in.quantity()))
            .setScale(2, RoundingMode.HALF_UP);
        return OrderParser.to(in, total);
    }

    private ProductOut findProduct(String idProduct) {
        try {
            ResponseEntity<ProductOut> response = productClient.findById(idProduct);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product does not exist");
            }
            return response.getBody();
        } catch (ProductNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product does not exist");
        } catch (ProductApiUnavailableException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Product service unavailable");
        }
    }

    private BigDecimal resolveRate(String currency) {
        String normalized = normalizeCurrency(currency);
        if (DEFAULT_CURRENCY.equals(normalized)) {
            return BigDecimal.ONE;
        }
        try {
            ResponseEntity<ExchangeOut> response = exchangeClient.findByCurrency(normalized);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Unsupported currency");
            }
            return response.getBody().rate();
        } catch (FeignException.UnprocessableEntity e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Unsupported currency");
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Unsupported currency");
        } catch (FeignException e) {
            // Fallback to storage currency (USD) when exchange service is unavailable.
            return BigDecimal.ONE;
        }
    }

    private BigDecimal convert(BigDecimal value, BigDecimal rate) {
        return value.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private void validateOrder(OrderIn in) {
        if (in == null || in.items() == null || in.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order items are required");
        }
    }

    private void validateItem(OrderItemIn in) {
        if (in == null || in.idProduct() == null || in.idProduct().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product is required");
        }
        if (in.quantity() == null || in.quantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be positive");
        }
    }

    private void validateAccount(String idAccount) {
        if (idAccount == null || idAccount.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated account is required");
        }
    }

}
