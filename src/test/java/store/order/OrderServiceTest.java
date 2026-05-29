package store.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import feign.FeignException;
import feign.Request;
import feign.Request.HttpMethod;
import feign.Response;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductClient productClient;

    @Mock
    private ExchangeClient exchangeClient;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldReturn400WhenProductDoesNotExist() {
        when(productClient.findById("account-1", "product-1")).thenThrow(new ProductNotFoundException());

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> orderService.create(
                "account-1",
                OrderIn.builder().items(List.of(OrderItemIn.builder().idProduct("product-1").quantity(1).build())).build()
            )
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Product does not exist", ex.getReason());
    }

    @Test
    void shouldReturn502WhenProductServiceIsUnavailable() {
        when(productClient.findById("account-1", "product-1")).thenThrow(new ProductApiUnavailableException());

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> orderService.create(
                "account-1",
                OrderIn.builder().items(List.of(OrderItemIn.builder().idProduct("product-1").quantity(1).build())).build()
            )
        );

        assertEquals(HttpStatus.BAD_GATEWAY, ex.getStatusCode());
        assertEquals("Product service unavailable", ex.getReason());
    }

    @Test
    void shouldFilterOrdersByAuthenticatedAccount() {
        when(orderRepository.findByIdAccountOrderByDateDesc("account-1")).thenReturn(List.of());

        orderService.findAll("account-1");

        verify(orderRepository).findByIdAccountOrderByDateDesc("account-1");
    }

    @Test
    void shouldReturn404WhenOrderDoesNotBelongToAccount() {
        when(orderRepository.findByIdAndIdAccount("order-1", "account-1")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> orderService.findById("account-1", "order-1", null)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void shouldReturn422WhenCurrencyIsInvalid() {
        mockOrderWithItems();
        when(exchangeClient.findByCurrency("USD", "BRL")).thenThrow(feignExceptionWithStatus(422));

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> orderService.findById("account-1", "order-1", "BRL")
        );

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatusCode());
        assertEquals("Unsupported currency", ex.getReason());
    }

    @Test
    void shouldKeepUsdValuesWhenExchangeServiceIsUnavailable() {
        mockOrderWithItems();
        when(exchangeClient.findByCurrency("USD", "BRL")).thenThrow(feignExceptionWithStatus(503));

        Order order = orderService.findById("account-1", "order-1", "BRL");

        assertEquals(new BigDecimal("26.44"), order.total());
        assertEquals(new BigDecimal("20.24"), order.items().get(0).total());
        assertEquals(new BigDecimal("6.20"), order.items().get(1).total());
    }

    private void mockOrderWithItems() {
        OrderModel orderModel = new OrderModel(
            "order-1",
            "account-1",
            LocalDateTime.of(2025, 9, 1, 12, 30, 0),
            new BigDecimal("26.44")
        );
        OrderItemModel item1 = new OrderItemModel(
            "item-1",
            "order-1",
            "product-1",
            2,
            new BigDecimal("20.24")
        );
        OrderItemModel item2 = new OrderItemModel(
            "item-2",
            "order-1",
            "product-2",
            10,
            new BigDecimal("6.20")
        );

        when(orderRepository.findByIdAndIdAccount("order-1", "account-1")).thenReturn(Optional.of(orderModel));
        when(orderItemRepository.findByIdOrder("order-1")).thenReturn(List.of(item1, item2));
    }

    private FeignException feignExceptionWithStatus(int status) {
        Request request = Request.create(
            HttpMethod.GET,
            "/exchange/BRL",
            Map.of(),
            null,
            null,
            null
        );
        Response response = Response.builder()
            .status(status)
            .reason("error")
            .request(request)
            .headers(Map.of())
            .build();
        return FeignException.errorStatus("ExchangeClient#findByCurrency", response);
    }

}
