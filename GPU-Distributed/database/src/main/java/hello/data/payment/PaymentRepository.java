package hello.data.payment;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

//@Repository
public interface PaymentRepository extends CrudRepository<Payment, Long> {
}
