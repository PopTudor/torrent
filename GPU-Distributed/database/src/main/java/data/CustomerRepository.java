package data;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface CustomerRepository extends CrudRepository<Account, Long> {

    List<Account> findByName(String name);
}