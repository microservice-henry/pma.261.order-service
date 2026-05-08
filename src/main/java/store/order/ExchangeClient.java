package store.order;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "exchange",
    url = "${EXCHANGE_API_URL:http://exchange:8080}"
)
public interface ExchangeClient {

    @GetMapping("/exchange")
    public ResponseEntity<ExchangeOut> findByCurrency(
        @RequestParam String from,
        @RequestParam String to
    );

}
