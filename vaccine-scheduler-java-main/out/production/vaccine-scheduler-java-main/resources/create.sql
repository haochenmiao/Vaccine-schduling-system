-- Creating the Caregivers table
CREATE TABLE Caregivers (
                            Username varchar(255) NOT NULL,
                            Salt BINARY(16) NOT NULL,
                            Hash BINARY(16) NOT NULL,
                            PRIMARY KEY (Username)
);

-- Creating the Patients table
CREATE TABLE Patients (
                          Username varchar(255) NOT NULL,
                          Salt BINARY(16) NOT NULL,
                          Hash BINARY(16) NOT NULL,
                          PRIMARY KEY (Username)
);

-- Creating the Vaccines table
CREATE TABLE Vaccines (
                          Name varchar(255) NOT NULL,
                          Doses int NOT NULL,
                          PRIMARY KEY (Name)
);

-- Creating the Appointments table
CREATE TABLE Appointments (
                              AppointmentID int IDENTITY(1,1) PRIMARY KEY,
                              Time date NOT NULL,
                              PatientUsername varchar(255),
                              CaregiverUsername varchar(255),
                              VaccineName varchar(255),
                              FOREIGN KEY (PatientUsername) REFERENCES Patients(Username),
                              FOREIGN KEY (CaregiverUsername) REFERENCES Caregivers(Username),
                              FOREIGN KEY (VaccineName) REFERENCES Vaccines(Name)
);

-- Creating the Availabilities table
CREATE TABLE Availabilities (
                                Time date NOT NULL,
                                Username varchar(255) NOT NULL,
                                PRIMARY KEY (Time, Username),
                                FOREIGN KEY (Username) REFERENCES Caregivers(Username)
);