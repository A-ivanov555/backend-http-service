package app.model;

public class Scooter extends Vehicle {
    public Scooter(int id, String model, double  pricePerHour) {
        super(id, model, pricePerHour);
    }
    @Override
    public double getPrice(int hours){
        return getPricePerHour() * hours +100;
    }
}
