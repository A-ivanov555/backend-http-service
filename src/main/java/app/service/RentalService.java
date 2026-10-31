package app.service;
import java.util.ArrayList;
import app.exception.InvalidOperationException;
import app.exception.NotFoundException;
import app.model.Car;
import app.model.Customer;
import app.model.Scooter;
import app.model.Vehicle;

import java.util.HashMap;

public class RentalService {
    private HashMap<Integer, Vehicle> vehicles=new HashMap<>();
    private HashMap<Integer, Customer>  customers=new HashMap<>();
    public void addVehicle(Vehicle vehicle){
        if (vehicles.containsKey(vehicle.getId())) {
            throw new InvalidOperationException("Транспорт с таким id уже существует");
        }
        vehicles.put(vehicle.getId(), vehicle);

    }
    public void addCustomer(Customer customer){
        if (customers.containsKey(customer.getId())) {
            throw new InvalidOperationException("Клиент с таким id уже существует");
        }
        customers.put(customer.getId(), customer);
    }
    public Vehicle getVehicle(int id){
        Vehicle vehicle=vehicles.get(id);
        if (vehicle==null){
            throw new NotFoundException("Транспорт не найден");
        }
        return vehicle;
    }
    public Customer getCustomer(int id){
        Customer customer=customers.get(id);
        if (customer==null){
            throw new NotFoundException("Клиент не найден");
        }
        return customer;
    }
    public Vehicle deleteVehicle(int id){
        Vehicle vehicle=getVehicle(id);
        if(!vehicle.isAvailable()){
            throw new InvalidOperationException("Транспорт сейчас занят");
        }
        return vehicles.remove(id);
    }
    public Customer deleteCustomer(int id){
        Customer customer=getCustomer(id);
        for(Vehicle vehicle: vehicles.values()){
            if(vehicle.getCurrentRenter()==customer){
                throw new InvalidOperationException("Клиент сейчас арендует транспорт");
            }

        }
        return customers.remove(id);
    }
    public void rentVehicle(int vehicleId, int customerId) {
        Vehicle vehicle = getVehicle(vehicleId);
        Customer customer = getCustomer(customerId);

        if (!vehicle.isAvailable()) {
            throw new InvalidOperationException("Транспорт уже арендован");
        }

        if (!vehicle.canRent(customer)) {
            throw new InvalidOperationException("Клиент не подходит по возрасту");
        }

        vehicle.rentTo(customer);
    }
    public void returnVehicle(int vehicleId){
        Vehicle vehicle=getVehicle(vehicleId);

        if (vehicle.isAvailable()) {
            throw new InvalidOperationException("Транспорт уже свободен");
        }
        vehicle.returnVehicle();
    }
    public ArrayList<Vehicle> getAllVehicles(){
        return new ArrayList<>(vehicles.values());
    }
    public ArrayList<Customer> getAllCustomers(){
        return new ArrayList<>(customers.values());
    }
    public ArrayList<Vehicle> getAvailableVehicles(){
        ArrayList<Vehicle> availableVehicles=new ArrayList<>();
        for(Vehicle vehicle:getAllVehicles()){
            if(vehicle.isAvailable()){
                availableVehicles.add(vehicle);

            }
        }
        return availableVehicles;
    }
    public ArrayList<Vehicle> getCars(){
        ArrayList<Vehicle> cars=new ArrayList<>();
        for(Vehicle vehicle:getAllVehicles()){
            if (vehicle instanceof Car){
                cars.add(vehicle);
            }
        }
        return cars;
    }
    public ArrayList<Vehicle> getScooters(){
        ArrayList<Vehicle> scooters=new ArrayList<>();
        for(Vehicle vehicle:getAllVehicles()){
            if (vehicle instanceof Scooter){
                scooters.add(vehicle);
            }
        }
            return scooters;
    }
    public void updateVehicle(int id, String model, double pricePerHour){
        Vehicle vehicle=getVehicle(id);

        vehicle.updateDetails(model, pricePerHour);
    }
    public void updateCustomer(int id, String name,int age){
        Customer customer=getCustomer(id);

        customer.updateDetails(name,age);
    }
}
