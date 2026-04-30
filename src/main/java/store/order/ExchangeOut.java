package store.order;

import java.math.BigDecimal;

public record ExchangeOut(

    String currency,
    BigDecimal rate

) {

}
