package com.tdd.booking;
import java.util.List;
import java.util.Objects;

 class BookingValidation{
    public boolean validate(Slot slot) throws InvalidBookingTimeException {
        boolean validSlot = true;
        String startTime = slot.getStartDate();
        String endTime = slot.getEndDate();
        String[] startTimeArray = startTime.split(":");
        if(startTimeArray.length != 2) {
            return false;
        } else {
            int hoursInStartTime = Integer.parseInt(startTimeArray[0]);
            if(hoursInStartTime < 1 || hoursInStartTime > 12) {
                validSlot = false;
            }
            String[] minutesAndTimeFormatInStartTime = startTimeArray[1].split("\\s");

	This line splits the second part of the time string (which contains minutes and AM/PM) by whitespace (\\s+), to separate:


            if(!minutesAndTimeFormatInStartTime[0].equals("00"))
                validSlot = false;
            else if(!(minutesAndTimeFormatInStartTime[1].equals("AM") || minutesAndTimeFormatInStartTime[1].equals("PM")))
                validSlot = false;
        }
        String[] endTimeArray = endTime.split(":");
        if(endTimeArray.length != 2) {
            validSlot = false;
        } else {
            int hoursInEndTime = Integer.parseInt(endTimeArray[0]);
            if(hoursInEndTime < 1 || hoursInEndTime > 12) {
                validSlot = false;
            }
            String[] minutesAndTimeFormatInEndTime = endTimeArray[1].split("\\s");
            if(!minutesAndTimeFormatInEndTime[0].equals("00"))
                validSlot = false;
            else if(!(minutesAndTimeFormatInEndTime[1].equals("AM") || minutesAndTimeFormatInEndTime[1].equals("PM")))
                validSlot = false;
        }
        if(!validSlot)
            throw new InvalidBookingTimeException("There is invalid time format in slot" + slot.toString() + "Please check the guidelines");
        return true;
    }
}


public class NonOverlappingSlotBooking {
    List<Slot> slots;
    IBookingValidation bookingValidation;
    String bookingType;
    public BookingManagerImpl(String bookingType, List<Slot> slots, IBookingValidation bookingValidation) {
        this.bookingType = bookingType;
        this.bookingValidation = bookingValidation;
        this.slots = slots;
    }
    @Override
    public void bookTime(Slot slot) throws BookingConflictException, InvalidBookingTimeException {
        Objects.requireNonNull(slot);
        bookingValidation.validate(slot);
        int newStart = convertTo24HourFormat(slot.getStartDate());
        int newEnd = convertTo24HourFormat(slot.getEndDate());
        for (Slot s : slots) {
            int existingStart = convertTo24HourFormat(s.getStartDate());
            int existingEnd = convertTo24HourFormat(s.getEndDate());
            // Check for overlapping intervals
            if (!(newEnd <= existingStart || newStart >= existingEnd)) {
                throw new BookingConflictException("Conflict with slot: " + s);
            }
        }
        slots.add(slot);
    }
    private int convertTo24HourFormat(String time) {
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        String[] minMeridian = parts[1].split("\\s+");

// 		This line splits the second part of the time string (which contains minutes and AM/PM) by whitespace (\\s+), to separate:
	

// 		"00 AM" → ["00", "AM"]
// 		🔍 Step-by-step example:
// 		Given this input:
		
	
// 		String time = "10:00 AM";
// String[] parts = time.split(":");  // ["10", "00 AM"]
// String[] minMeridian = parts[1].split("\\s+"); // ["00", "AM"]
// 		Now:
// 			§ minMeridian[0] is "00" → the minutes part.
// 			§ minMeridian[1] is "AM" → the meridian (AM/PM).
// 		❓ Why use "\\s+"?
// 			§ \\s means "whitespace character" (space, tab, etc).
// 			§ + means "one or more".
// 		So split("\\s+") safely splits even if there are extra spaces.


        String meridian = minMeridian[1];
        if (meridian.equalsIgnoreCase("AM")) {
            return hour == 12 ? 0 : hour;
        } else {
            return hour == 12 ? 12 : hour + 12;
        }
    }
    @Override
    public List<Slot> bookings() {
        return slots;
    }
}

// ✅ What This Fixes
// 	• "10:00 AM" → 10
// 	• "10:00 PM" → 22
// 	• "12:00 AM" → 0
// 	• "12:00 PM" → 12
// Now it avoids the mistake where "10:00 AM" and "10:00 PM" both yielded 10.

// ✅ You Don't Need to Change BookingValidation Class
// The validation logic already ensures:
// 	• Hour is 1–12
// 	• Minutes are 00
// 	• AM/PM is valid



//Flow

// Overlapping (conflict):


// new:        |------|
// existing:     |------|
// newStart < existingEnd && newEnd > existingStart
// This will enter the if block — i.e., there’s a conflict.

// Not overlapping (safe):
// Case 1: new ends before existing starts


// new:     |----|
// existing:        |----|
// newEnd <= existingStart
// Case 2: new starts after existing ends


// existing: |----|
// new:             |----|
// newStart >= existingEnd
// Both are non-overlapping, so the if block will be skipped.



// 📍 Step 2: bookingValidation.validate(slot) — Format Checking
// Let’s say the slot is "10:00 AM" to "11:00 AM".
// Inside BookingValidation.validate():
// 	• Splits "10:00 AM" → ["10", "00 AM"]
// 		○ Verifies that hour is between 1–12 ✅
// 		○ Minutes must be "00" ✅
// 		○ Time format must be "AM" or "PM" ✅
// 	• Same checks for "11:00 AM" ✅
// If all pass → returns true.
// If anything fails → throws InvalidBookingTimeException.

// 📍 Step 3: Check for conflicts with already booked slots


// int givenSlotStartTime = Integer.parseInt(slot.getStartDate().split(":")[0]);
// int givenSlotEndDate = Integer.parseInt(slot.getEndDate().split(":")[0]);
// ⚠️ This part extracts only the hour part. So "10:00 AM" → 10, "11:00 AM" → 11.
// 🧠 Caution: This logic doesn't distinguish between AM/PM — so "10:00 AM" and "10:00 PM" will be treated the same. That’s a flaw in this implementation.


// ✅ Example Walkthrough
// Book slot "10:00 AM" to "11:00 AM":
// 	• No existing slots → ✅
// 	• Passes format validation → ✅
// 	• No conflict → ✅
// 	• Added to slots

// Book slot "10:00 AM" to "11:00 AM" again:
// 	• Format valid ✅
// 	• But conflict found in existing slots → ❌
// 	• Throws BookingConflictException

// Book slot "13:00 AM" to "14:00 AM":
// 	• Fails format validation (13 is not between 1–12) → ❌
// 	• Throws InvalidBookingTimeException

