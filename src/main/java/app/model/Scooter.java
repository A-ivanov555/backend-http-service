package app.model;

public class Scooter extends Vehicle {

    public Scooter(int id, String model, double pricePerHour) {
        super(id, model, pricePerHour);
    }

    @Override
    public String getType() {
        return "scooter";
    }

    @Override
    public double getPrice(int hours) {
        return getPricePerHour() * hours + 100;
    }

    @Override
    public boolean canRent(Customer customer) {
        return customer.getAge() >= 16;
    }
}