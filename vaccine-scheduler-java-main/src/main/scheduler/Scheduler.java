package scheduler;

import scheduler.db.ConnectionManager;
import scheduler.model.Caregiver;
import scheduler.model.Patient;
import scheduler.model.Vaccine;
import scheduler.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

public class Scheduler {

    // objects to keep track of the currently logged-in user
    // Note: it is always true that at most one of currentCaregiver and currentPatient is not null
    //       since only one user can be logged-in at a time
    private static Caregiver currentCaregiver = null;
    private static Patient currentPatient = null;

    public static void main(String[] args) {
        // printing greetings text
        System.out.println();
        System.out.println("Welcome to the COVID-19 Vaccine Reservation Scheduling Application!");
        System.out.println("*** Please enter one of the following commands ***");
        System.out.println("> create_patient <username> <password>");  //TODO: implement create_patient (Part 1)
        System.out.println("> create_caregiver <username> <password>");
        System.out.println("> login_patient <username> <password>");  // TODO: implement login_patient (Part 1)
        System.out.println("> login_caregiver <username> <password>");
        System.out.println("> search_caregiver_schedule <date>");  // TODO: implement search_caregiver_schedule (Part 2)
        System.out.println("> reserve <date> <vaccine>");  // TODO: implement reserve (Part 2)
        System.out.println("> upload_availability <date>");
        System.out.println("> cancel <appointment_id>");  // TODO: implement cancel (extra credit)
        System.out.println("> add_doses <vaccine> <number>");
        System.out.println("> show_appointments");  // TODO: implement show_appointments (Part 2)
        System.out.println("> logout");  // TODO: implement logout (Part 2)
        System.out.println("> quit");
        System.out.println();

        // read input from user
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            String response = "";
            try {
                response = r.readLine();
            } catch (IOException e) {
                System.out.println("Please try again!");
            }
            // split the user input by spaces
            String[] tokens = response.split(" ");
            // check if input exists
            if (tokens.length == 0) {
                System.out.println("Please try again!");
                continue;
            }
            // determine which operation to perform
            String operation = tokens[0];
            if (operation.equals("create_patient")) {
                createPatient(tokens);
            } else if (operation.equals("create_caregiver")) {
                createCaregiver(tokens);
            } else if (operation.equals("login_patient")) {
                loginPatient(tokens);
            } else if (operation.equals("login_caregiver")) {
                loginCaregiver(tokens);
            } else if (operation.equals("search_caregiver_schedule")) {
                searchCaregiverSchedule(tokens);
            } else if (operation.equals("reserve")) {
                reserve(tokens);
            } else if (operation.equals("upload_availability")) {
                uploadAvailability(tokens);
            } else if (operation.equals("cancel")) {
                cancel(tokens);
            } else if (operation.equals("add_doses")) {
                addDoses(tokens);
            } else if (operation.equals("show_appointments")) {
                showAppointments(tokens);
            } else if (operation.equals("logout")) {
                logout(tokens);
            } else if (operation.equals("quit")) {
                System.out.println("Bye!");
                return;
            } else {
                System.out.println("Invalid operation name!");
            }
        }
    }

    private static boolean isStrongPassword(String password) {
        if (password.length() < 8) {
            return false;
        }

        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if ("!@#?".indexOf(c) >= 0) hasSpecial = true;
        }
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    private static void createPatient(String[] tokens) {
        // TODO: Part 1
        // Check if the command has the correct number of arguments
        if (tokens.length != 3) {
            System.out.println("Failed to create patient. Please provide a username and password.");
            return;
        }

        String username = tokens[1];
        String password = tokens[2];

        // Check for strong password
        if (!isStrongPassword(password)) {
            System.out.println("Password does not meet the strength requirements.");
            return;
        }

        // Check if the username already exists
        if (usernameExistsPatient(username)) {
            System.out.println("Username already taken, please try a different one.");
            return;
        }

        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);

        try {
            // Assuming Patient class has a similar builder pattern as Caregiver
            Patient patient = new Patient.PatientBuilder(username, salt, hash).build();
            patient.saveToDB();
            System.out.println("Patient account created successfully: " + username);
        } catch (SQLException e) {
            System.out.println("Failed to create patient due to a database error.");
            e.printStackTrace();
        }
    }

    private static boolean usernameExistsPatient(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Patients WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns true if the cursor is not before the first record or if there are rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking patient username");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        return false;
    }

    private static void createCaregiver(String[] tokens) {
        // create_caregiver <username> <password>
        // check 1: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Failed to create user.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        // Check for strong password
        if (!isStrongPassword(password)) {
            System.out.println("Password does not meet the strength requirements.");
            return;
        }

        // check 2: check if the username has been taken already
        if (usernameExistsCaregiver(username)) {
            System.out.println("Username taken, try again!");
            return;
        }
        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        // create the caregiver
        try {
            Caregiver caregiver = new Caregiver.CaregiverBuilder(username, salt, hash).build();
            // save to caregiver information to our database
            caregiver.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e) {
            System.out.println("Failed to create user.");
            e.printStackTrace();
        }
    }

    private static boolean usernameExistsCaregiver(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Caregivers WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        return true;
    }

    private static void loginPatient(String[] tokens) {
        // TODO: Part 1
        // Check if the command has the correct number of arguments
        if (tokens.length != 3) {
            System.out.println("Login failed. Please provide a username and password.");
            return;
        }

        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("Another user is already logged in. Please logout first.");
            return;
        }

        String username = tokens[1];
        String password = tokens[2];

        Patient patient = null;
        try {
            patient = new Patient.PatientGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Login failed.");
            e.printStackTrace();
        }
        // check if the login was successful
        if (patient == null) {
            System.out.println("Login failed.");
        } else {
            System.out.println("Logged in as: " + username);
            currentPatient = patient;
        }
    }

    private static void loginCaregiver(String[] tokens) {
        // login_caregiver <username> <password>
        // check 1: if someone's already logged-in, they need to log out first
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("User already logged in.");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Login failed.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Caregiver caregiver = null;
        try {
            caregiver = new Caregiver.CaregiverGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Login failed.");
            e.printStackTrace();
        }
        // check if the login was successful
        if (caregiver == null) {
            System.out.println("Login failed.");
        } else {
            System.out.println("Logged in as: " + username);
            currentCaregiver = caregiver;
        }
    }

    private static void searchCaregiverSchedule(String[] tokens) {
        //TODO: part 2
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first!");
            return;
        }

        if (tokens.length != 2) {
            System.out.println("Please enter a valid date!");
            return;
        }

        String date = tokens[1];
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String query = "SELECT c.Username, " +
                "(CASE WHEN ap.PatientUsername IS NULL THEN 'Available' ELSE 'Not Available' END) as Availability, " +
                "v.Name as Vaccine, ISNULL(v.Doses, 0) as Doses " +
                "FROM Caregivers c " +
                "LEFT JOIN Appointments ap ON c.Username = ap.CaregiverUsername AND ap.Time = ? " +
                "CROSS JOIN Vaccines v " +
                "GROUP BY c.Username, ap.PatientUsername, v.Name, v.Doses " +
                "ORDER BY c.Username, v.Name";

        try (PreparedStatement statement = con.prepareStatement(query)) {
            Date queryDate = Date.valueOf(tokens[1]);
            statement.setDate(1, queryDate);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String caregiverUsername = resultSet.getString(1);
                String availability = resultSet.getString(2);
                String vaccineName = resultSet.getString(3);
                int doses = resultSet.getInt(4);

                System.out.println(caregiverUsername + " - " + availability + " - Vaccine: " + (vaccineName != null ? vaccineName : "Not Available") + " - Doses: " + doses);
            }
        } catch (SQLException e) {
            System.out.println("Error occurred when searching caregiver schedule");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
    }

    private static void reserve(String[] tokens) {
        // TODO: Part 2
        if (currentPatient == null) {
            System.out.println(currentCaregiver != null ? "Please login as a patient!" : "Please login first!");
            return;
        }

        if (tokens.length != 3) {
            System.out.println("Please enter a valid date and vaccine name!");
            return;
        }

        String date = tokens[1];
        String vaccineName = tokens[2];
        String caregiverUsername = null;
        int remainingDoses = 0;

        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        try {
            // Find an available caregiver
            String findCaregiverQuery = "SELECT TOP 1 c.Username " +
                    "FROM Caregivers c " +
                    "LEFT JOIN Appointments ap ON c.Username = ap.CaregiverUsername AND ap.Time = ? " +
                    "WHERE ap.PatientUsername IS NULL " + // Ensure that the caregiver is actually available
                    "ORDER BY c.Username"; // Ensure alphabetical order

            PreparedStatement findCaregiverStmt = con.prepareStatement(findCaregiverQuery);
            findCaregiverStmt.setDate(1, Date.valueOf(date));
            ResultSet caregiverResult = findCaregiverStmt.executeQuery();

            if (!caregiverResult.next()) {
                System.out.println("No Caregiver is available!");
                return;
            }
            caregiverUsername = caregiverResult.getString(1);

            // Second I need to check vaccine availability
            String checkVaccineQuery = "SELECT Doses FROM Vaccines WHERE Name = ?"; // Corrected table name
            PreparedStatement checkVaccineStmt = con.prepareStatement(checkVaccineQuery);
            checkVaccineStmt.setString(1, vaccineName);
            ResultSet vaccineResult = checkVaccineStmt.executeQuery();

            if (!vaccineResult.next() || vaccineResult.getInt(1) <= 0) {
                System.out.println("Not enough available doses!");
                return;
            }

            remainingDoses = vaccineResult.getInt(1) - 1;

            // Third step is to create an appointment
            String createAppointmentQuery = "INSERT INTO Appointments (Time, PatientUsername, CaregiverUsername, VaccineName) VALUES (?, ?, ?, ?)";
            PreparedStatement createAppointmentStmt = con.prepareStatement(createAppointmentQuery, PreparedStatement.RETURN_GENERATED_KEYS);
            createAppointmentStmt.setDate(1, Date.valueOf(date));
            createAppointmentStmt.setString(2, currentPatient.getUsername());
            createAppointmentStmt.setString(3, caregiverUsername);
            createAppointmentStmt.setString(4, vaccineName);
            createAppointmentStmt.executeUpdate();

            ResultSet generatedKeys = createAppointmentStmt.getGeneratedKeys();
            if (!generatedKeys. next()) {
                System.out.println("Failed to create appointment.");
                return;
            }
            int appointmentId = generatedKeys.getInt(1);

            // Fourth step is to update vaccine doses
            String updateDosesQuery = "UPDATE Vaccines SET Doses = ? WHERE Name = ?";
            PreparedStatement updateDosesStmt = con.prepareStatement(updateDosesQuery);
            updateDosesStmt.setInt(1, remainingDoses);
            updateDosesStmt.setString(2, vaccineName);
            updateDosesStmt.executeUpdate();

            System.out.println("Appointment ID: " + appointmentId + ", Caregiver username: " + caregiverUsername);

        } catch (SQLException e) {
            System.out.println("Error occurred when reserving appointment");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
    }

    private static void uploadAvailability(String[] tokens) {
        // upload_availability <date>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 2 to include all information (with the operation name)
        if (tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }
        String date = tokens[1];
        try {
            Date d = Date.valueOf(date);
            currentCaregiver.uploadAvailability(d);
            System.out.println("Availability uploaded!");
        } catch (IllegalArgumentException e) {
            System.out.println("Please enter a valid date!");
        } catch (SQLException e) {
            System.out.println("Error occurred when uploading availability" + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void cancel(String[] tokens) {
        // TODO: Extra credit
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first!");
            return;
        }

        if (tokens.length != 2) {
            System.out.println("Please provide the appointment ID!");
            return;
        }

        int appointmentId = Integer.parseInt(tokens[1]);
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        try {
            // Check if the appointment exists and belongs to the current user
            String checkQuery = "SELECT * FROM Appointments WHERE AppointmentID = ? AND (PatientUsername = ? OR CaregiverUsername = ?)";
            PreparedStatement checkStmt = con.prepareStatement(checkQuery);
            checkStmt.setInt(1, appointmentId);
            checkStmt.setString(2, currentPatient != null ? currentPatient.getUsername() : currentCaregiver.getUsername());
            checkStmt.setString(3, currentCaregiver != null ? currentCaregiver.getUsername() : currentPatient.getUsername());

            ResultSet resultSet = checkStmt.executeQuery();
            if (!resultSet.next()) {
                System.out.println("Appointment not found or does not belong to the current user.");
                return;
            }

            // Delete the appointment
            String deleteQuery = "DELETE FROM Appointments WHERE AppointmentID = ?";
            PreparedStatement deleteStmt = con.prepareStatement(deleteQuery);
            deleteStmt.setInt(1, appointmentId);
            deleteStmt.executeUpdate();

            System.out.println("Appointment cancelled successfully.");

        } catch (SQLException e) {
            System.out.println("Error occurred when cancelling appointment");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
    }

    private static void addDoses(String[] tokens) {
        // add_doses <vaccine> <number>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String vaccineName = tokens[1];
        int doses = Integer.parseInt(tokens[2]);
        Vaccine vaccine = null;
        try {
            vaccine = new Vaccine.VaccineGetter(vaccineName).get();
        } catch (SQLException e) {
            System.out.println("Error occurred when adding doses");
            e.printStackTrace();
        }
        // check 3: if getter returns null, it means that we need to create the vaccine and insert it into the Vaccines
        //          table
        if (vaccine == null) {
            try {
                vaccine = new Vaccine.VaccineBuilder(vaccineName, doses).build();
                vaccine.saveToDB();
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                e.printStackTrace();
            }
        } else {
            // if the vaccine is not null, meaning that the vaccine already exists in our table
            try {
                vaccine.increaseAvailableDoses(doses);
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                e.printStackTrace();
            }
        }
        System.out.println("Doses updated!");
    }

    private static void showAppointments(String[] tokens) {
        // TODO: Part 2
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first!");
            return;
        }

        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        try {
            String query;
            PreparedStatement statement;

            if (currentCaregiver != null) {
                // Fetch appointments for the caregiver
                query = "SELECT AppointmentID, VaccineName, Time, p.Username " +
                        "FROM Appointments a " +
                        "JOIN Patients p ON a.PatientUsername = p.Username " +
                        "WHERE CaregiverUsername = ? " +
                        "ORDER BY AppointmentID";
                statement = con.prepareStatement(query);
                statement.setString(1, currentCaregiver.getUsername());
            } else {
                // Fetch appointments for the patient
                query = "SELECT AppointmentID, VaccineName, Time, c.Username " +
                        "FROM Appointments a " +
                        "JOIN Caregivers c ON a.CaregiverUsername = c.Username " +
                        "WHERE PatientUsername = ? " +
                        "ORDER BY AppointmentID";
                statement = con.prepareStatement(query);
                statement.setString(1, currentPatient.getUsername());
            }

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int appointmentId = resultSet.getInt("AppointmentID");
                String vaccineName = resultSet.getString("VaccineName");
                Date appointmentDate = resultSet.getDate("Time");
                String otherPartyUsername = resultSet.getString(4);

                System.out.println("Appointment ID: " + appointmentId + ", Vaccine: " + vaccineName + ", Date: " + appointmentDate + ", With: " + otherPartyUsername);
            }
        } catch (SQLException e) {
            System.out.println("Error occurred when showing appointments" + e.getMessage());
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
    }

    private static void logout(String[] tokens) {
        // TODO: Part 2
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("No user is currently logged in.");
            return;
        }

        if (currentCaregiver != null) {
            System.out.println("Logged out caregiver: " + currentCaregiver.getUsername());
            currentCaregiver = null;
        }

        if (currentPatient != null) {
            System.out.println("Logged out patient: " + currentPatient.getUsername());
            currentPatient = null;
        }
    }
}
