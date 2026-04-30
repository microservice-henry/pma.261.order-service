package store.order;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderResource implements OrderController {

    @Autowired
    private OrderService orderService;

    @Override
    public ResponseEntity<OrderOut> create(String idAccount, OrderIn in) {
        Order order = orderService.create(idAccount, in);
        return ResponseEntity.status(201)
            .body(OrderParser.toOut(order, orderService.normalizeCurrency(null)));
    }

    @Override
    public ResponseEntity<List<OrderSummaryOut>> findAll(String idAccount) {
        return ResponseEntity.ok(
            OrderParser.toSummary(orderService.findAll(idAccount))
        );
    }

    @Override
    public ResponseEntity<OrderOut> findById(String idAccount, String id, String currency) {
        Order order = orderService.findById(idAccount, id, currency);
        return ResponseEntity.ok(
            OrderParser.toOut(order, orderService.normalizeCurrency(currency))
        );
    }

    @Override
    public ResponseEntity<Void> healthCheck() {
        return ResponseEntity.ok().build();
    }

}
