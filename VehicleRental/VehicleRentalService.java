import java.io.*;
import java.util.*;
// ENUMS
enum VehicleType {
    CAR, BIKE, VAN
}
// MODELS
class Vehicle {
    String id;
    VehicleType type;
    int price;
    boolean[] timeSlots = new boolean[24];
    public Vehicle(String id, VehicleType type, int price) {
        this.id = id;
        this.type = type;
        this.price = price;
    }
    public boolean isAvailable(int start, int end) {
        for (int i = start; i < end; i++) if (timeSlots[i]) return false;
        return true;
    }
    public void book(int start, int end) {
        for (int i = start; i < end; i++) timeSlots[i] = true;
    }
}
class Branch {
    String name;
    Set<VehicleType> supportedTypes;
    Map<VehicleType, List<Vehicle>> vehiclesByType = new HashMap<>();
    public Branch(String name, Set<VehicleType> supportedTypes) {
        this.name = name;
        this.supportedTypes = supportedTypes;
    }
    public boolean addVehicle(Vehicle vehicle) {
        if (!supportedTypes.contains(vehicle.type)) return false;
        vehiclesByType.putIfAbsent(vehicle.type, new ArrayList<>());
        vehiclesByType.get(vehicle.type).add(vehicle);
        return true;
    }
    public List<Vehicle> getAvailableVehicles(VehicleType type, int start, int end) {
        List<Vehicle> result = new ArrayList<>();
        if (!vehiclesByType.containsKey(type)) return result;
        for (Vehicle v : vehiclesByType.get(type)) {
            if (v.isAvailable(start, end)) result.add(v);
        }
        return result;
    }
    public double getBookedPercentage(VehicleType type) {
        if (!vehiclesByType.containsKey(type)) return 0.0;
        int booked = 0, total = vehiclesByType.get(type).size();
        for (Vehicle v : vehiclesByType.get(type)) {
            for (boolean slot : v.timeSlots) {
                if (slot) {
                    booked++;
                    break;
                }
            }
        }
        return (booked * 1.0 / total);
    }
}
// SERVICE
class VehicleRentalService {
    Map<String, Branch> branches = new HashMap<>();
    public boolean addBranch(String name, List<String> types) {
        if (branches.containsKey(name)) return false;
        Set<VehicleType> supported = new HashSet<>();
        try {
            for (String type : types) supported.add(VehicleType.valueOf(type));
        } catch (IllegalArgumentException e) {
            return false;
        }
        branches.put(name, new Branch(name, supported));
        return true;
    }
    public boolean addVehicle(String branchName, String type, String id, int price) {
        Branch branch = branches.get(branchName);
        if (branch == null) return false;
        try {
            VehicleType vt = VehicleType.valueOf(type);
            double multiplier = branch.getBookedPercentage(vt) >= 0.8 ? 1.1 : 1.0;
            Vehicle vehicle = new Vehicle(id, vt, (int)(price * multiplier));
            return branch.addVehicle(vehicle);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    public int bookVehicle(String branchName, String type, int start, int end) {
        Branch branch = branches.get(branchName);
        if (branch == null) return -1;
        VehicleType vt;
        try {
            vt = VehicleType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return -1;
        }
        List<Vehicle> available = branch.getAvailableVehicles(vt, start, end);
        if (available.isEmpty()) return -1;
        available.sort(Comparator.comparingInt(v -> v.price));
        Vehicle toBook = available.get(0);
        toBook.book(start, end);
        return toBook.price * (end - start);
    }
    public List<String> displayVehicles(String branchName, int start, int end) {
        Branch branch = branches.get(branchName);
        if (branch == null) return Collections.emptyList();
        List<Vehicle> result = new ArrayList<>();
        for (VehicleType type : branch.supportedTypes) {
            result.addAll(branch.getAvailableVehicles(type, start, end));
        }
        result.sort(Comparator.comparingInt(v -> v.price));
        List<String> ids = new ArrayList<>();
        for (Vehicle v : result) ids.add(v.id);
        return ids;
    }
}
// DRIVER
public class MainDriver {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Please provide input file path.");
            return;
        }
        BufferedReader br = new BufferedReader(new FileReader(args[0]));
        VehicleRentalService service = new VehicleRentalService();
        String line;
        while ((line = br.readLine()) != null) {
            String[] tokens = line.split(" ");
            String command = tokens[0];
            switch (command) {
                case "ADD_BRANCH": {
                    String branch = tokens[1];
                    List<String> types = Arrays.asList(tokens[2].split(","));
                    System.out.println(service.addBranch(branch, types));
                    break;
                }
                case "ADD_VEHICLE": {
                    String branch = tokens[1];
                    String type = tokens[2];
                    String id = tokens[3];
                    int price = Integer.parseInt(tokens[4]);
                    System.out.println(service.addVehicle(branch, type, id, price));
                    break;
                }
                case "BOOK": {
                    String branch = tokens[1];
                    String type = tokens[2];
                    int start = Integer.parseInt(tokens[3]);
                    int end = Integer.parseInt(tokens[4]);
                    System.out.println(service.bookVehicle(branch, type, start, end));
                    break;
                }
                case "DISPLAY_VEHICLES": {
                    String branch = tokens[1];
                    int start = Integer.parseInt(tokens[2]);
                    int end = Integer.parseInt(tokens[3]);
                    System.out.println(String.join(",", service.displayVehicles(branch, start, end)));
                    break;
                }
            }
        }
        br.close();
    }
}
