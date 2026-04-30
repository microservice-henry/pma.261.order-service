package store.order;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<OrderModel, String> {

    public List<OrderModel> findByIdAccountOrderByDateDesc(String idAccount);

    public Optional<OrderModel> findByIdAndIdAccount(String id, String idAccount);

}
