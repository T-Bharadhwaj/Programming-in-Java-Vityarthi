# Programming in JAVA Project

## Campus Event Resource Manager

---

## Student Details

__Name__: Thondavarapu Bharadhwaj <br>
__Registration Number__: 25BAI10897 <br>
__Course Code__: CSE2006 <br>

---

## About the Project
The **Campus Event Resource Manager** is a simple Java-based application designed to make managing college events and shared resources easier. <br>
Instead of keeping everything in separate manual records, the system brings event and resource information together in one place.<br>

The application allows users to create and manage events, keep track of resources, allocate them to events, and check for scheduling conflicts.<br>
It is built using **"Core Java"** and uses **"CSV files"** to store data.

---

## Main Features

- **Event Lifecycle Management**: Add, update, view, delete, and search events (`WORKSHOP`, `SEMINAR`, `HACKATHON`, `CULTURAL`, `SPORTS`, `OTHER`).

- **Resource Inventory Management**: Manage venues, projectors, microphones, chairs, and sound systems with status tracking (`ACTIVE`, `UNDER_MAINTENANCE`, `DECOMMISSIONED`).

- **Real-Time Conflict Detection Engine**:
  - **Venue Double-Booking Locking**: Blocks booking the same auditorium/classroom for overlapping time windows.
  - **Resource Capacity Validation**: Computes unallocated quantity across overlapping time slots to prevent over-allocation.
  - **Back-to-Back Slot Support**: Allows Event B to start exactly when Event A ends (eg., 09:00–11:00 and 11:00–13:00).

- **Financial & Budget Estimation**: Tracks cost rates per unit resource and computes event resource costs against event budgets.

- **CSV Data Persistence & Fault Recovery**: Automatically saves to and loads from `data/events.csv`, `data/resources.csv`, and `data/allocations.csv`. Malformed rows are safely skipped without crashing valid records.

- **Reporting & Analytics**: Formatted ASCII table reports for upcoming events, inventory status, event-resource allocations, and full system audit logs.

- **Automated Verification Suite**: Includes a standalone `TestRunner` executing 14 automated test assertions.

---

## Technologies Used

**Language:** Java

**JDK:** 17 or later recommended

**Interface:** Command-Line Interface (CLI)

**Storage:** CSV files

**Testing:** Java TestRunner

**Concepts:** OOP, Collections, Exception Handling, File Handling, Validation, Date and Time APIs

---

## Project Structure
---
Campus_Event_Resource_Manager/
├── src/com/campusevent/
│   ├── Main.java
│   ├── model/
│   ├── service/
│   ├── persistence/
│   ├── util/
│   ├── exception/
│   └── test/
└── data/
    ├── events.csv
    ├── resources.csv
    └── allocations.csv

---

## Compilation & Execution Instructions

### Windows Execution Guide

#### Option A: Using Windows Command Prompt (CMD)

1. Open **Command Prompt** (`cmd.exe`).
2. Navigate to the project directory.
   ```
3. Compile all Java source files into the `bin` directory.
   ```
4. Run the interactive CLI Application.
   ```
5. Run the Automated Test Suite (14 Test Assertions).
   ```

---

#### Option B: Using Windows PowerShell

1. Open **PowerShell**.
2. Navigate to the project directory.
   ```
3. Compile all Java source files into the `bin` directory.
   ```
4. Run the interactive CLI Application.
   ```
5. Run the Automated Test Suite.
   ```

---


## Sample CLI Menu Walkthrough

When you start `Main`, the application presents an interactive menu:

```
==================================================================
       WELCOME TO CAMPUS EVENT RESOURCE MANAGER (CORE JAVA)       
==================================================================

------------------------------------------------------------------
                        MAIN MENU                                 
------------------------------------------------------------------
 1. Manage Events (Add, View, Update, Delete, Search)
 2. Manage Resources (Add, View, Update, Status, Delete)
 3. Allocate Resources (Allocate, Release Allocation)
 4. Check Resource & Venue Availability (By Date & Time)
 5. View Event Schedule (Date Filter)
 6. Generate Reports & Analytics
 7. Restore Sample Data
 8. Save & Exit
------------------------------------------------------------------
Enter option (1-8) [8]: 
```

### Example: Booking Allocation & Conflict Detection
If an administrator attempts to book `Seminar Hall A` (`R101`) for `Event 2` during a time window that overlaps with `Event 1` at the same venue, the application outputs a clear diagnostic warning:

```
[ALLOCATION REJECTED] VENUE CONFLICT DETECTED!
   Venue 'Seminar Hall A' [R101] is already booked on 2026-10-15 for Event 'AI & ML Workshop' [E101] (09:00 - 12:00).
   Your requested event 'Overlap Seminar' [E999] (10:00 - 13:00) overlaps with this booking.
   Note: Back-to-back events ending exactly when the next starts are allowed.
```

---

## Testing Verification Summary
All 14 unit test assertions pass successfully when running `TestRunner`:
- `[PASS] Create Event` & `Update Event`
- `[PASS] Add Resource` & `Update Resource`
- `[PASS] Successful Allocation`
- `[PASS] Prevent Venue Double Booking`
- `[PASS] Prevent Over-Allocation Quantity`
- `[PASS] Allow Back-to-Back Events`
- `[PASS] Release Allocations After Deletion`
- `[PASS] Missing Event ID Error Handling`
- `[PASS] CSV Persistence Loaded Events / Resources / Allocations`
