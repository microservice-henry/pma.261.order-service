package store.order;

public class ProductApiUnavailableException extends ProductApiException {

    public ProductApiUnavailableException() {
        super("Product service unavailable");
    }

}
