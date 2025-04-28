package interviewscheduling;

import java.util.*;


class Timeslot {
        int startTime;
        int endTime;

        public Timeslot(int startTime, int endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

class Interview {
    int attendee;
    String interviewer;
    String room;
    Timeslot slot;

    public Interview(int attendee, String interviewer, String room, Timeslot slot) {
        this.attendee = attendee;
        this.interviewer = interviewer;
        this.room = room;
        this.slot = slot;
    }
}

public class InterviewSchedule {
    public static final List<Timeslot> availableSlots = List.of(
            new Timeslot(9, 11),
            new Timeslot(11, 13),
            new Timeslot(15, 17)
    );

    public static void main(String[] args) {
        List<Integer> attendees = List.of(1, 2, 3, 4, 5);
        List<String> rooms = new ArrayList<>(List.of("R1", "R2"));
        List<String> interviewers = new ArrayList<>(List.of("A", "B"));

        List<Interview> schedule = new ArrayList<>();
        Set<Integer> unscheduledAttendees = new HashSet<>(attendees);
        Map<String, List<Timeslot>> interviewerSchedule = new HashMap<>();
        Map<String, List<Timeslot>> roomSchedule = new HashMap<>();

        for (Integer attendee : attendees) {
            boolean scheduled = false;
            for (Timeslot availableSlot : availableSlots) {
                for (String interviewer : interviewers) {
                    for (String room : rooms) {
                        if (isAvailable(interviewerSchedule, interviewer, availableSlot) && isAvailable(roomSchedule, room, availableSlot)) {
                            schedule.add(new Interview(attendee, interviewer, room, availableSlot));
                            interviewerSchedule.computeIfAbsent(interviewer, s -> new ArrayList<>()).add(availableSlot);
                            roomSchedule.computeIfAbsent(room, s -> new ArrayList<>()).add(availableSlot);
                            unscheduledAttendees.remove(attendee);
                            scheduled = true;
                            break;
                        }
                    }
                    if (scheduled == true) break;
                }
                if (scheduled == true) break;
            }
        }
        // Print the schedule
        System.out.println("Scheduled Interviews:");
        for (Interview interview : schedule) {
            System.out.println(interview);
        }
        // Print unscheduled attendees
        if (!unscheduledAttendees.isEmpty()) {
            System.out.println("\nCould not schedule interviews for the following attendees:");
            for (int id : unscheduledAttendees) {
                System.out.println("Attendee " + id);
            }
        }
    }

        private static boolean isAvailable (Map<String, List <Timeslot>> scheduledMap, String key, Timeslot slot){
        return scheduledMap.getOrDefault(key, new ArrayList<>()).stream()
                .noneMatch(timeslot -> timeslot.startTime == slot.startTime && timeslot.endTime ==  slot.endTime);
        }
}



