package app.service;

import app.exception.InvalidOperationException;
import app.exception.NotFoundException;
import app.model.Car;
import app.model.Customer;
import app.model.Scooter;
import app.model.Vehicle;

import java.util.ArrayList;
import java.util.HashMap;

public class RentalService {
    private final HashMap<Integer, Vehicle> vehicles = new HashMap<>();
    private final HashMap<Integer, Customer> customers = new HashMap<>();

    public void addVehicle(Vehicle vehicle) {
        validateVehicleData(
                vehicle.getId(),
                vehicle.getModel(),
                vehicle.getPricePerHour()
        );

        if (vehicles.containsKey(vehicle.getId())) {
            throw new InvalidOperationException(
                    "Транспорт с таким id уже существует"
            );
        }

        vehicles.put(vehicle.getId(), vehicle);
    }

    public void addCustomer(Customer customer) {
        validateCustomerData(
                customer.getId(),
                customer.getName(),
                customer.getAge()
        );

        if (customers.containsKey(customer.getId())) {
            throw new InvalidOperationException(
                    "Клиент с таким id уже существует"
            );
        }

        customers.put(customer.getId(), customer);
    }

    public Vehicle getVehicle(int id) {
        Vehicle vehicle = vehicles.get(id);

        if (vehicle == null) {
            throw new NotFoundException("Транспорт не найден");
        }

        return vehicle;
    }

    public Customer getCustomer(int id) {
        Customer customer = customers.get(id);

        if (customer == null) {
            throw new NotFoundException("Клиент не найден");
        }

        return customer;
    }

    public ArrayList<Vehicle> getAllVehicles() {
        return new ArrayList<>(vehicles.values());
    }

    public ArrayList<Customer> getAllCustomers() {
        return new ArrayList<>(customers.values());
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

        for (Vehicle vehicle : vehicles.values()) {
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

    public ArrayList<Vehicle> getAvailableVehicles() {
        return getVehicles(true, null);
    }

    public ArrayList<Vehicle> getCars() {
        ArrayList<Vehicle> cars = new ArrayList<>();

        for (Vehicle vehicle : vehicles.values()) {
            if (vehicle instanceof Car) {
                cars.add(vehicle);
            }
        }

        return cars;
    }

    public ArrayList<Vehicle> getScooters() {
        ArrayList<Vehicle> scooters = new ArrayList<>();

        for (Vehicle vehicle : vehicles.values()) {
            if (vehicle instanceof Scooter) {
                scooters.add(vehicle);
            }
        }

        return scooters;
    }

    public void updateVehicle(
            int id,
            String model,
            double pricePerHour
    ) {
        Vehicle vehicle = getVehicle(id);

        validateVehicleData(id, model, pricePerHour);

        vehicle.updateDetails(model, pricePerHour);
    }

    public void updateCustomer(
            int id,
            String name,
            int age
    ) {
        Customer customer = getCustomer(id);

        validateCustomerData(id, name, age);

        customer.updateDetails(name, age);
    }

    public Vehicle deleteVehicle(int id) {
        Vehicle vehicle = getVehicle(id);

        if (!vehicle.isAvailable()) {
            throw new InvalidOperationException(
                    "Нельзя удалить арендованный транспорт"
            );
        }

        return vehicles.remove(id);
    }

    public Customer deleteCustomer(int id) {
        Customer customer = getCustomer(id);

        for (Vehicle vehicle : vehicles.values()) {
            if (vehicle.getCurrentRenter() == customer) {
                throw new InvalidOperationException(
                        "Клиент сейчас арендует транспорт"
                );
            }
        }

        return customers.remove(id);
    }

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
                    "Клиент не подходит по возрасту"
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

    private void validateVehicleData(
            int id,
            String model,
            double pricePerHour
    ) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Id транспорта должен быть положительным"
            );
        }

        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException(
                    "Модель транспорта не указана"
            );
        }

        if (!Double.isFinite(pricePerHour) || pricePerHour <= 0) {
            throw new IllegalArgumentException(
                    "Цена должна быть положительной"
            );
        }
    }

    private void validateCustomerData(
            int id,
            String name,
            int age
    ) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Id клиента должен быть положительным"
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Имя клиента не указано"
            );
        }

        if (age < 0) {
            throw new IllegalArgumentException(
                    "Возраст не может быть отрицательным"
            );
        }
    }
}