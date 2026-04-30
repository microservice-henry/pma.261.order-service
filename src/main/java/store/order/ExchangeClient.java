package store.order;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "exchange",
    url = "${EXCHANGE_API_URL:http://exchange:8080}"
)
public interface ExchangeClient {

    @GetMapping("/exchange/{currency}")
    public ResponseEntity<ExchangeOut> findByCurrency(
        @PathVariable String currency
    );

}
