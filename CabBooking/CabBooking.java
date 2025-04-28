//Ride sharing/Cab bookin app

package cabbookinghandson;


import java.util.*;

class Driver {
    String name;
    String vehicleType;
    Double rating;
    Double distanceFromCustomer;

    public Driver(String name, String vehicleType, Double rating, Double distanceFromCustomer) {
        this.name = name;
        this.vehicleType = vehicleType;
        this.rating = rating;
        this.distanceFromCustomer = distanceFromCustomer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Double getDistanceFromCustomer() {
        return distanceFromCustomer;
    }

    public void setDistanceFromCustomer(Double distanceFromCustomer) {
        this.distanceFromCustomer = distanceFromCustomer;
    }
}
class RentService {
    static final double rate_per_km = 8.0;
    public Driver findDriver(List<Driver> drivers, String vehicleType){
        return drivers.stream().filter(driver -> driver.rating >=4.0)
                .sorted(Comparator.comparingDouble(Driver::getDistanceFromCustomer))
                .filter(driver -> vehicleType == null || driver.vehicleType.equalsIgnoreCase(vehicleType))
                .findFirst()
                .orElse(null);
    }
    public Double calculateFare(Double distance) {
        return rate_per_km * distance;

    }
}
public class CabBookingApp {

    public static void main(String[] args) {
        List<Driver> drivers = Arrays.asList(
                new Driver ("Ashwath", "Swift", 4.0, 1.2),
                new Driver ("Kumar", "Innova", 3.7, 0.5),
                new Driver ("Anil", "i20", 4.5, 2.0),
                new Driver ("Ashok", "Kia", 3.9, 0.8),
                new Driver ("Kiran", "Innova", 4.3, 1.0)
        );

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter preferred car model (or press Enter to skip): ");
        String inputCar = scanner.nextLine();
        String selectedCar = inputCar.isBlank() ? null : inputCar.trim();
        RentService service = new RentService();
        Driver matchedDriver = service.findDriver(drivers, selectedCar);
        if (matchedDriver != null) {
            System.out.println("\nDriver Found: " + matchedDriver);
            System.out.print("Enter ride distance (km): ");
            double rideDistance = scanner.nextDouble();
            double fare = service.calculateFare(rideDistance);
            System.out.println("Fare: Rs. " + fare);
        } else {
            if (selectedCar != null) {
                System.out.println("\nNo drivers available with the selected car model. Please try another.");
            } else {
                System.out.println("\nNo drivers available at the moment. Please try again later.");
            }
        }
    }
}
