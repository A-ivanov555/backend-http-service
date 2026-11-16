
package app.repository;

import app.model.Customer;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CustomerRepository {

    private final Map<Integer, Customer> customers = new HashMap<>();

    public Customer findById(int id) {
        return customers.get(id);
    }

    public List<Customer> findAll() {
        return new ArrayList<>(customers.values());
    }

    public boolean existsById(int id) {
        return customers.containsKey(id);
    }

    public void save(Customer customer) {
        customers.put(customer.getId(), customer);
    }

    public void deleteById(int id) {
        customers.remove(id);
    }
}
