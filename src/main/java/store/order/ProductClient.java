package store.order;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    name = "product",
    url = "${PRODUCT_API_URL:http://product:8080}",
    configuration = ProductFeignConfig.class
)
public interface ProductClient {

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductOut> findById(
        @RequestHeader("id-account") String idAccount,
        @PathVariable String id
    );

}
