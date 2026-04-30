package store.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "orders")
@Setter @Accessors(chain = true, fluent = true)
@NoArgsConstructor @AllArgsConstructor
public class OrderModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "id_account")
    private String idAccount;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "total")
    private BigDecimal total;

    public OrderModel(Order order) {
        this.id = order.id();
        this.idAccount = order.idAccount();
        this.date = order.date();
        this.total = order.total();
    }

    public Order to() {
        return Order.builder()
            .id(this.id)
            .idAccount(this.idAccount)
            .date(this.date)
            .total(this.total)
            .build();
    }

}
