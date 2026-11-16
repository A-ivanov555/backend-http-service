
package app.controller;

import app.dto.CustomerRequest;
import app.model.Customer;
import app.service.RentalService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final RentalService rentalService;

    public CustomerController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @GetMapping
    public List<Customer> getCustomers() {
        return rentalService.getCustomers();
    }

    @GetMapping("/{id}")
    public Customer getCustomer(@PathVariable int id) {
        return rentalService.getCustomer(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Customer createCustomer(@RequestBody CustomerRequest request) {
        if (request == null
                || request.id == null
                || request.name == null
                || request.age == null) {
            throw new IllegalArgumentException(
                    "Не указаны обязательные поля клиента"
            );
        }

        Customer customer = new Customer(
                request.id,
                request.name,
                request.age
        );

        rentalService.addCustomer(customer);
        return customer;
    }

    @PutMapping("/{id}")
    public Customer updateCustomer(
            @PathVariable int id,
            @RequestBody CustomerRequest request
    ) {
        if (request == null
                || request.name == null
                || request.age == null) {
            throw new IllegalArgumentException(
                    "Не указаны обязательные поля клиента"
            );
        }

        return rentalService.updateCustomer(
                id,
                request.name,
                request.age
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(@PathVariable int id) {
        rentalService.deleteCustomer(id);
    }
}
