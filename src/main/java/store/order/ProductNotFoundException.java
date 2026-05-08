package store.order;

public class ProductNotFoundException extends ProductApiException {

    public ProductNotFoundException() {
        super("Product does not exist");
    }

}
