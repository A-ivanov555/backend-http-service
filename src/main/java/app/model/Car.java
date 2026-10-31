package app.model;

public class Car extends Vehicle {
    public Car(int id, String model, double  pricePerHour) {
        super(id, model, pricePerHour);

    }
    @Override
    public double getPrice(int hours) {
        return getPricePerHour() * hours;
    }
    @Override
    public boolean canRent(Customer customer) {
        return customer.getAge()>=18;
    }
}
