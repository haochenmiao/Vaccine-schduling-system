# COVID-19 Vaccine Reservation Scheduling Application

## Overview

This application provides a platform for scheduling COVID-19 vaccine appointments. It allows patients to create accounts, search for available caregiver schedules, reserve vaccination appointments, and manage their bookings. Caregivers can also register, upload their availability, and manage vaccine doses.

## Getting Started

### Prerequisites

- Java JDK 8 or later
- MySQL Server
- Git (for cloning the repository)

### Installation

1. **Clone the repository**

```bash
git clone https://github.com/<your-github-username>/covid-vaccine-scheduler.git
cd covid-vaccine-scheduler
```

1. Set up the database

Import the create.sql script into your MySQL Server to set up the required database and tables.

2. Configure database connection

Update the environment variables in ConnectionManager.java with your database credentials:

```sh
private final String connectionUrl = "jdbc:sqlserver://<Server>.database.windows.net:1433;database=<DBName>";
private final String userName = "<UserID>";
private final String userPass = "<Password>";
```

3. Compile and run the application
``` bash
javac -d . *.java
java scheduler.Scheduler
```

### Features
* ***Account Management***: Users can create and manage patient or caregiver accounts.
* ***Appointment Scheduling***: Patients can search for available slots and book vaccination appointments.
* ****Availability Management***: Caregivers can upload their available times for appointments.
* ***Vaccine Management***: Caregivers can manage the inventory of vaccine doses.

### Source Files
* `ConnectionManager.java`: Manages database connections.
* `Util.java`: Provides utility functions, including password hashing.
* `Patient.java`: Represents a patient, handling account creation and appointment bookings.
* `Caregiver.java`: Represents a caregiver, handling account creation, availability uploads, and vaccine dose management.
* `Vaccine.java`: Manages vaccine inventory, including adding and subtracting doses.
* `Scheduler.java`: The main application logic, handling user input and coordinating the actions of patients and caregivers.
* `create.sql`: SQL script for creating the database schema.
* ER Diagrams: Visual representations of the database schema, provided in PDF format.

### ER Diagram
![ER Diagram with Figma](https://github.com/haochenmiao/Vaccine-schduling-system/blob/main/diagrams/er_diagram_figma.png)

![ER Diagram using code](https://github.com/haochenmiao/Vaccine-schduling-system/blob/main/diagrams/er_diagram_code.png)


### License
This project is licensed under the MIT License - see the LICENSE.md file for details.
