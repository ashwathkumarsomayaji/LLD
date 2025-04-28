package parkinglot;


import java.util.ArrayList;
import java.util.List;

enum VehicleType {
    CAR, BIKE, TRUCK;
}
class Vehicle {
    String licenceNumber;
    VehicleType vehicleType;

    public Vehicle(String licenceNumber, VehicleType vehicleType) {
        this.licenceNumber = licenceNumber;
        this.vehicleType = vehicleType;
    }

    public String getLicenceNumber() {
        return licenceNumber;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

}

class Car extends Vehicle {

    public Car(String licenceNumber) {
        super(licenceNumber, VehicleType.CAR);
    }
}

class Bike extends Vehicle {

    public Bike(String licenceNumber) {
        super(licenceNumber, VehicleType.BIKE);
    }
}

class Truck extends Vehicle {

    public Truck(String licenceNumber) {
        super(licenceNumber, VehicleType.TRUCK);
    }
}

class ParkingSlot {
    private String slotNumber;
    private VehicleType supportedVehcileType;
    private boolean isOccupied;
    private Vehicle parkedVehicle;

    public String getSlotNumber() {
        return slotNumber;
    }

    public VehicleType getSupportedVehcileType() {
        return supportedVehcileType;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public Vehicle getParkedVehicle() {
        return parkedVehicle;
    }

    public ParkingSlot(String slotNumber, VehicleType supportedVehcileType) {
        this.slotNumber = slotNumber;
        this.supportedVehcileType = supportedVehcileType;
    }

    public void parkVehicleInTheSlot(Vehicle vehicle) {
        this.isOccupied = true;
        this.parkedVehicle =  vehicle;
    }
    public  void unparkVehicleInTheSlot() {
        this.isOccupied = false;
        this.parkedVehicle = null;
    }
}

class ParkingFloor {
    private List<ParkingSlot> slots;
    private int floorNumber;

    public ParkingFloor(int floorNumber) {
        this.floorNumber = floorNumber; //will initialise object with floorNumber but we need slots to be initialised
                                        //during constructor call.
        this.slots = new ArrayList<>();
    }

    public void addSlot(ParkingSlot slot) {
            slots.add(slot);
    }
    public List<ParkingSlot> getSlots() {
        return  slots;
    }
    public int getFloorNumber() {
        return floorNumber;
    }
//    public void displayAvailableSlots() {
//        slots.stream().filter(ParkingSlot::isAvailable).forEach(
//                slot -> System.out.println("Available Slot: " + slot.slotNumber)
//        );
//    }

}
public class ParkingLot {
    private List<ParkingFloor> floors;

    public ParkingLot() {
        this.floors = new ArrayList<>(); //Parking lot object will not need any floor to be passed
        // but we need to initialise floorNumber bcz we will add floors to parking lot later
    }

    public List<ParkingFloor> getFloors() {
        return floors;
    }

    public void addFloor(ParkingFloor floor) {
        floors.add(floor);
    }

    public Ticket parkVehicle(Vehicle vehicle) {
        for (ParkingFloor floor : floors) {
            for (ParkingSlot slot : floor.getSlots()) {
                if (!slot.isOccupied() && slot.getSupportedVehcileType() == vehicle.getVehicleType()) {
                    slot.parkVehicleInTheSlot(vehicle);
                    return ParkingFactory.createTicket(vehicle, slot);

                }
            }
        }
        throw new RuntimeException("No available slot for vehicle type: " + vehicle.getVehicleType());
    }
    public void unparkVehicle (String slotNumber) {
        for(ParkingFloor floor : floors) {
            for(ParkingSlot slot : floor.getSlots()) {
                if(slot.getSlotNumber() == slotNumber && slot.isOccupied() == true) {
                    slot.unparkVehicleInTheSlot();
                    return;
                }
            }
        }
        throw new RuntimeException("Slot not found or already empty");

    }
}
class ParkingFactory {
        public static Ticket createTicket(Vehicle vehicle, ParkingSlot slot){
            return  new Ticket(vehicle, slot);

        }
        public static PricingStratergy getPricingStratergy(VehicleType type){
            switch(type) {
                case CAR : return new Carpricingstratergy();
                case BIKE : return new Bikepricingstratergy();
                case TRUCK : return new Truckpricingstratergy();
                default: throw new IllegalArgumentException("Unsupported vehicle type");
            }

        }
    }

    interface PricingStratergy {
        double calculateFee(long durationInHours);

    }
    class Carpricingstratergy implements PricingStratergy  {
        public double calculateFee(long duration) {
            return 20*duration;
        }
    }
    class Bikepricingstratergy implements PricingStratergy {
        public double calculateFee(long duration) {
            return 10*duration;
        }
    }
    class Truckpricingstratergy implements PricingStratergy {
        public double calculateFee(long duration) {
            return 50 * duration;
        }
    }

class Ticket {
    private Vehicle vehicle;
    private ParkingSlot slot;
    private long startTime;

    public Ticket(Vehicle vehicle, ParkingSlot slot) {
        this.vehicle = vehicle;
        this.slot = slot;
        this.startTime = System.currentTimeMillis();// I wont send this args during obj initialisation
        // but keep the track of entry time
    }

    public double calculateFare() { //called when  customer leaves
        long hours = System.currentTimeMillis() - startTime;
        PricingStratergy stratergy = ParkingFactory.getPricingStratergy(vehicle.vehicleType);
        return stratergy.calculateFee(hours == 0 ? 1 : hours);
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public ParkingSlot getSlot() {
        return slot;
    }
}
    class Main {
        public static void main(String[] args) throws InterruptedException {
            /* ---------- 1. bootstrap parking-lot ------------ */
            ParkingLot lot = new ParkingLot();

            ParkingFloor floor0 = new ParkingFloor(0);

            ParkingSlot carSlot = new ParkingSlot("C-1", VehicleType.CAR);
            ParkingSlot bikeSlot = new ParkingSlot("B-1", VehicleType.BIKE);
            ParkingSlot truckSlot = new ParkingSlot("T-1", VehicleType.TRUCK);

            /* attach a display-board observer to every slot so we can see changes */
//            SlotObserver board = new DisplayBoard();
//            carSlot.addObserver(board);
//            bikeSlot.addObserver(board);
//            truckSlot.addObserver(board);

            floor0.addSlot(carSlot);
            floor0.addSlot(bikeSlot);
            floor0.addSlot(truckSlot);

            lot.addFloor(floor0);
            System.out.println("\n=== initial availability ===");

            // floor0.displayAvailableSlots();
            /* ---------- 2. customer drives in --------------- */
            System.out.println("\n=== CAR arrives ===");
            Ticket ticket = lot.parkVehicle(new Car("KA-01-AB-4321"));
            /* stay ~2 sec → rounded to 1 hour fee */
            Thread.sleep(2_000);
            /* ---------- 3. customer leaves and pays --------- */
            double amountDue = ticket.calculateFare();     // uses selected strategy
            System.out.printf("\nAmount to pay: $%.2f%n", amountDue);

            lot.unparkVehicle(ticket.getSlot().getSlotNumber());
            /* ---------- 4. inventory after exit ------------- */
            System.out.println("\n=== availability after exit ===");
            //floor0.displayAvailableSlots();
        }
    }
