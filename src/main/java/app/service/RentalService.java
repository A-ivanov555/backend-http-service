
package app.service;

import app.exception.InvalidOperationException;
import app.exception.NotFoundException;
import app.model.Customer;
import app.model.Vehicle;
import app.repository.CustomerRepository;
import app.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class RentalService {

    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;

    public RentalService(
            VehicleRepository vehicleRepository,
            CustomerRepository customerRepository
    ) {
        this.vehicleRepository = vehicleRepository;
        this.customerRepository = customerRepository;
    }

    // Транспорт

    public void addVehicle(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Транспорт не указан");
        }

        validateVehicleDetails(
                vehicle.getId(),
                vehicle.getModel(),
                vehicle.getPricePerHour()
        );

        if (vehicleRepository.existsById(vehicle.getId())) {
            throw new InvalidOperationException(
                    "Транспорт с таким id уже существует"
            );
        }

        vehicleRepository.save(vehicle);
    }

    public Vehicle getVehicle(int id) {
        validateId(id);

        Vehicle vehicle = vehicleRepository.findById(id);

        if (vehicle == null) {
            throw new NotFoundException("Транспорт не найден");
        }

        return vehicle;
    }

    public ArrayList<Vehicle> getVehicles(Boolean available, String type) {
        if (type != null
                && !type.equalsIgnoreCase("car")
                && !type.equalsIgnoreCase("scooter")) {
            throw new IllegalArgumentException(
                    "Неизвестный тип транспорта"
            );
        }

        ArrayList<Vehicle> result = new ArrayList<>();

        for (Vehicle vehicle : vehicleRepository.findAll()) {
            if (available != null
                    && vehicle.isAvailable() != available) {
                continue;
            }

            if (type != null
                    && !vehicle.getType().equalsIgnoreCase(type)) {
                continue;
            }

            result.add(vehicle);
        }

        return result;
    }

    public Vehicle updateVehicle(
            int id,
            String model,
            double pricePerHour
    ) {
        Vehicle vehicle = getVehicle(id);
        validateVehicleDetails(id, model, pricePerHour);

        vehicle.updateDetails(model, pricePerHour);
        return vehicle;
    }

    public void deleteVehicle(int id) {
        Vehicle vehicle = getVehicle(id);

        if (!vehicle.isAvailable()) {
            throw new InvalidOperationException(
                    "Нельзя удалить арендованный транспорт"
            );
        }

        vehicleRepository.deleteById(id);
    }

    // Клиенты

    public void addCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Клиент не указан");
        }

        validateCustomerDetails(
                customer.getId(),
                customer.getName(),
                customer.getAge()
        );

        if (customerRepository.existsById(customer.getId())) {
            throw new InvalidOperationException(
                    "Клиент с таким id уже существует"
            );
        }

        customerRepository.save(customer);
    }

    public Customer getCustomer(int id) {
        validateId(id);

        Customer customer = customerRepository.findById(id);

        if (customer == null) {
            throw new NotFoundException("Клиент не найден");
        }

        return customer;
    }

    public ArrayList<Customer> getCustomers() {
        return new ArrayList<>(customerRepository.findAll());
    }

    public Customer updateCustomer(int id, String name, int age) {
        Customer customer = getCustomer(id);
        validateCustomerDetails(id, name, age);

        Customer updatedCustomer = new Customer(id, name, age);

        for (Vehicle vehicle : vehicleRepository.findAll()) {
            Customer renter = vehicle.getCurrentRenter();

            if (renter != null
                    && renter.getId() == id
                    && !vehicle.canRent(updatedCustomer)) {
                throw new InvalidOperationException(
                        "Новый возраст не позволяет продолжить аренду"
                );
            }
        }

        customer.updateDetails(name, age);
        return customer;
    }

    public void deleteCustomer(int id) {
        Customer customer = getCustomer(id);

        for (Vehicle vehicle : vehicleRepository.findAll()) {
            if (vehicle.getCurrentRenter() == customer) {
                throw new InvalidOperationException(
                        "Нельзя удалить клиента с активной арендой"
                );
            }
        }

        customerRepository.deleteById(id);
    }

    // Аренда и возврат

    public void rentVehicle(int vehicleId, int customerId) {
        Vehicle vehicle = getVehicle(vehicleId);
        Customer customer = getCustomer(customerId);

        if (!vehicle.isAvailable()) {
            throw new InvalidOperationException(
                    "Транспорт уже арендован"
            );
        }

        if (!vehicle.canRent(customer)) {
            throw new InvalidOperationException(
                    "Возраст клиента не подходит для аренды"
            );
        }

        vehicle.rentTo(customer);
    }

    public void returnVehicle(int vehicleId) {
        Vehicle vehicle = getVehicle(vehicleId);

        if (vehicle.isAvailable()) {
            throw new InvalidOperationException(
                    "Транспорт уже свободен"
            );
        }

        vehicle.returnVehicle();
    }

    public double getRentalPrice(int vehicleId, int hours) {
        Vehicle vehicle = getVehicle(vehicleId);

        if (hours <= 0) {
            throw new IllegalArgumentException(
                    "Количество часов должно быть положительным"
            );
        }

        return vehicle.getPrice(hours);
    }

    // Проверка входных данных

    private void validateId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "id должен быть положительным"
            );
        }
    }

    private void validateVehicleDetails(
            int id,
            String model,
            double pricePerHour
    ) {
        validateId(id);

        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException(
                    "Модель транспорта не должна быть пустой"
            );
        }

        if (!Double.isFinite(pricePerHour) || pricePerHour <= 0) {
            throw new IllegalArgumentException(
                    "Стоимость часа должна быть положительным числом"
            );
        }
    }

    private void validateCustomerDetails(
            int id,
            String name,
            int age
    ) {
        validateId(id);

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Имя клиента не должно быть пустым"
            );
        }

        if (age < 0) {
            throw new IllegalArgumentException(
                    "Возраст не может быть отрицательным"
            );
        }
    }
}
