package store.order;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Response;
import feign.codec.ErrorDecoder;

@Configuration
public class ProductFeignConfig {

    @Bean
    public ErrorDecoder productErrorDecoder() {
        return (String methodKey, Response response) -> {
            if (response.status() == 404) {
                return new ProductNotFoundException();
            }
            return new ProductApiUnavailableException();
        };
    }

}
