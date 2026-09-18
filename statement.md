# Problem Statement & System Scope - Campus Event Resource Manager
---

## Problem Statement
College campuses host a multitude of concurrent events such as technical workshops, hackathons, academic seminars, cultural festivals, and sports competitions. Managing shared physical resources,such as lecture auditoriums, seminar halls, high-definition projectors, wireless microphones, sound systems, and event seating—presents significant operational challenges:

1. **Double Booking & Overlapping Schedules**: Shared venues are frequently booked by different departments for overlapping time windows, resulting in venue unavailability and event disruption.
2. **Resource Over-Allocation**: Quantity-based equipment (e.g., microphones, projectors, chairs) is often assigned beyond physical inventory capacity during peak activity windows.
3. **Lack of Transparency & Auditability**: Manual or paper-based reservation systems lack real-time visibility into resource status (e.g., active vs. under maintenance) and allocation timelines.
4. **Data Loss & Fragmented Records**: Absence of persistent storage leads to lost reservation records when systems restart or personnel change.

## Project Objectives
The **Campus Event Resource Manager** is a pure Core Java console application built to provide a centralized, resilient, and beginner-friendly resource management system.

---

## System Scope
The application is scoped for single-node command-line deployment suitable for college administration, student council heads, and event coordinators.

### In Scope:
* **Event Management:** Creating, updating, canceling, and tracking events across different types (e.g., academic, cultural, sports) and statuses (e.g., scheduled, completed, canceled).
* **Resource Management:** Categorizing and tracking campus assets (venues, equipment, personnel) along with their availability statuses.
* **Allocation & Scheduling Logic:** Reserving resources for specific events while actively validating time schedules to prevent double-booking or over-allocation.
* **Data Persistence:** Storing and restoring event, resource, and allocation records locally using file-based storage (`.csv` files).
* **Reporting & Reporting Utilities:** Generating detailed event summary reports, resource utilization lists, and formatted tabular console outputs.
* **Console-Based Interaction:** Providing a robust interactive terminal interface with input validation and exception handling.

---

### Out of Scope:
* Web or mobile graphical user interfaces (GUI).
* Real-time multi-user concurrent networking or remote database integration.
* Automated email/SMS notification dispatch systems.
* Payment gateway or fee processing integration.

---

## Target Users
* **Campus Event Organizers & Student Committees:** Individuals responsible for planning workshops, hackathons, cultural fests, or sports meets who need to reserve venues and necessary technical assets.
* **Facility & Resource Managers:** Staff members tasked with keeping track of campus inventory, approving asset usage, and maintaining facility schedules.
* **Campus Administrators:** Authorities who oversee overall schedule clearance, monitor resource utilization, and review activity reports across various departments.

## High-Level Features
1. **Event Management Module**: Create, view, update, delete, and search campus events.
2. **Resource Inventory Management**: Register equipment, update quantities, set maintenance flags, and search inventory.
3. **Resource Allocation Module**: Allocate venues and quantities to events with instant conflict validation; release allocations on demand or upon event deletion.
4. **Conflict Detection Engine**: Real-time validation preventing venue double bookings and over-allocation across overlapping date/time slots.
5. **Reporting & Analytics Engine**: Generate upcoming event schedules, inventory usage summaries, financial budget reports, and full combined audit logs.
6. **CSV Persistence Engine**: Auto-save on modification, background auto-load on start, and malformed row recovery.
