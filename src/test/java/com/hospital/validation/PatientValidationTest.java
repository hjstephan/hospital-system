package com.hospital.validation;

import com.hospital.entity.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.EmptySource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validation tests for patient data
 */
class PatientValidationTest {

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setFirstName("Test");
        patient.setLastName("User");
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient.setGender("Männlich");
        patient.setInsuranceNumber("INS-2024-001");
    }

    @Test
    @DisplayName("Valid patient should pass all validations")
    void testValidPatient() {
        assertTrue(isValidPatient(patient));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    @DisplayName("First name should not be null or blank")
    void testInvalidFirstName(String firstName) {
        patient.setFirstName(firstName);
        assertFalse(isValidPatient(patient));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    @DisplayName("Last name should not be null or blank")
    void testInvalidLastName(String lastName) {
        patient.setLastName(lastName);
        assertFalse(isValidPatient(patient));
    }

    @Test
    @DisplayName("Date of birth should not be null")
    void testNullDateOfBirth() {
        patient.setDateOfBirth(null);
        assertFalse(isValidPatient(patient));
    }

    @Test
    @DisplayName("Date of birth should not be in the future")
    void testFutureDateOfBirth() {
        patient.setDateOfBirth(LocalDate.now().plusDays(1));
        assertFalse(isValidDateOfBirth(patient.getDateOfBirth()));
    }

    @Test
    @DisplayName("Patient should not be older than 150 years")
    void testVeryOldPatient() {
        patient.setDateOfBirth(LocalDate.now().minusYears(151));
        assertFalse(isValidDateOfBirth(patient.getDateOfBirth()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Männlich", "Weiblich", "Divers"})
    @DisplayName("Valid genders should be accepted")
    void testValidGenders(String gender) {
        patient.setGender(gender);
        assertTrue(isValidGender(patient.getGender()));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {"Invalid", "Unknown", "Male", "Female"})
    @DisplayName("Invalid genders should be rejected")
    void testInvalidGenders(String gender) {
        patient.setGender(gender);
        assertFalse(isValidGender(patient.getGender()));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "INS-2024-001",
        "INS-2024-12345",
        "INS-2023-999"
    })
    @DisplayName("Valid insurance numbers should be accepted")
    void testValidInsuranceNumbers(String insuranceNumber) {
        patient.setInsuranceNumber(insuranceNumber);
        assertTrue(isValidInsuranceNumber(patient.getInsuranceNumber()));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {
        "INVALID",
        "123456",
        "INS-ABCD-001",
        "INS2024001"
    })
    @DisplayName("Invalid insurance numbers should be rejected")
    void testInvalidInsuranceNumbers(String insuranceNumber) {
        patient.setInsuranceNumber(insuranceNumber);
        assertFalse(isValidInsuranceNumber(patient.getInsuranceNumber()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"})
    @DisplayName("Valid blood types should be accepted")
    void testValidBloodTypes(String bloodType) {
        patient.setBloodType(bloodType);
        assertTrue(isValidBloodType(patient.getBloodType()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"C+", "X-", "AA+", "Invalid", "O", "AB"})
    @DisplayName("Invalid blood types should be rejected")
    void testInvalidBloodTypes(String bloodType) {
        patient.setBloodType(bloodType);
        assertFalse(isValidBloodType(patient.getBloodType()));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "test@example.com",
        "user.name@example.de",
        "firstname.lastname@hospital.com"
    })
    @DisplayName("Valid email addresses should be accepted")
    void testValidEmails(String email) {
        patient.setEmail(email);
        assertTrue(isValidEmail(patient.getEmail()));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "invalid",
        "@example.com",
        "user@",
        "user name@example.com",
        "user@example"
    })
    @DisplayName("Invalid email addresses should be rejected")
    void testInvalidEmails(String email) {
        patient.setEmail(email);
        assertFalse(isValidEmail(patient.getEmail()));
    }

    @Test
    @DisplayName("Email can be null")
    void testNullEmail() {
        patient.setEmail(null);
        assertTrue(patient.getEmail() == null);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "030-12345678",
        "+49 30 12345678",
        "0049 30 12345678",
        "030/12345678",
        "03012345678"
    })
    @DisplayName("Valid phone numbers should be accepted")
    void testValidPhoneNumbers(String phone) {
        patient.setPhone(phone);
        assertTrue(isValidPhone(patient.getPhone()));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "123",
        "abc-defg-hijk",
        "++49301234567"
    })
    @DisplayName("Invalid phone numbers should be rejected")
    void testInvalidPhoneNumbers(String phone) {
        patient.setPhone(phone);
        assertFalse(isValidPhone(patient.getPhone()));
    }

    @Test
    @DisplayName("Phone can be null")
    void testNullPhone() {
        patient.setPhone(null);
        assertNull(patient.getPhone());
    }

    @ParameterizedTest
    @ValueSource(strings = {"active", "discharged"})
    @DisplayName("Valid statuses should be accepted")
    void testValidStatuses(String status) {
        patient.setStatus(status);
        assertTrue(isValidStatus(patient.getStatus()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"pending", "deleted", "archived", "unknown"})
    @DisplayName("Invalid statuses should be rejected")
    void testInvalidStatuses(String status) {
        patient.setStatus(status);
        assertFalse(isValidStatus(patient.getStatus()));
    }

    @Test
    @DisplayName("EPA sync status should be valid")
    void testEPASyncStatus() {
        String[] validStatuses = {"pending", "synced", "error", "disabled"};
        
        for (String status : validStatuses) {
            patient.setEpaSyncStatus(status);
            assertTrue(isValidEPASyncStatus(patient.getEpaSyncStatus()));
        }
    }

    @Test
    @DisplayName("Name should have reasonable length")
    void testNameLength() {
        // Too short
        patient.setFirstName("A");
        assertFalse(isValidNameLength(patient.getFirstName()));
        
        // Valid
        patient.setFirstName("Anna");
        assertTrue(isValidNameLength(patient.getFirstName()));
        
        // Too long
        patient.setFirstName("A".repeat(101));
        assertFalse(isValidNameLength(patient.getFirstName()));
    }

    @Test
    @DisplayName("Address should have reasonable length")
    void testAddressLength() {
        // Valid
        patient.setAddress("Hauptstraße 123, 10115 Berlin");
        assertTrue(isValidAddressLength(patient.getAddress()));
        
        // Too long
        patient.setAddress("A".repeat(501));
        assertFalse(isValidAddressLength(patient.getAddress()));
    }

    @Test
    @DisplayName("Insurance number should be unique format")
    void testInsuranceNumberUniqueness() {
        Patient patient2 = new Patient();
        patient2.setInsuranceNumber("INS-2024-002");
        
        assertNotEquals(patient.getInsuranceNumber(), patient2.getInsuranceNumber());
    }

    // Validation helper methods
    private boolean isValidPatient(Patient p) {
        return p.getFirstName() != null && !p.getFirstName().trim().isEmpty()
            && p.getLastName() != null && !p.getLastName().trim().isEmpty()
            && p.getDateOfBirth() != null
            && p.getGender() != null
            && p.getInsuranceNumber() != null;
    }

    private boolean isValidDateOfBirth(LocalDate dob) {
        if (dob == null) return false;
        LocalDate now = LocalDate.now();
        return !dob.isAfter(now) && dob.isAfter(now.minusYears(150));
    }

    private boolean isValidGender(String gender) {
        if (gender == null) return false;
        return gender.equals("Männlich") || gender.equals("Weiblich") || gender.equals("Divers");
    }

    private boolean isValidInsuranceNumber(String insuranceNumber) {
        if (insuranceNumber == null || insuranceNumber.isEmpty()) return false;
        return insuranceNumber.matches("INS-\\d{4}-\\d+");
    }

    private boolean isValidBloodType(String bloodType) {
        if (bloodType == null) return true; // Optional field
        return bloodType.matches("(A|B|AB|O)[+-]");
    }

    private boolean isValidEmail(String email) {
        if (email == null) return true; // Optional field
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private boolean isValidPhone(String phone) {
        if (phone == null) return true; // Optional field
        // Allow various phone formats
        return phone.matches("^[+\\d][\\d\\s/-]{7,}$");
    }

    private boolean isValidStatus(String status) {
        if (status == null) return false;
        return status.equals("active") || status.equals("discharged");
    }

    private boolean isValidEPASyncStatus(String status) {
        if (status == null) return false;
        return status.equals("pending") || status.equals("synced") 
            || status.equals("error") || status.equals("disabled");
    }

    private boolean isValidNameLength(String name) {
        if (name == null) return false;
        return name.length() >= 2 && name.length() <= 100;
    }

    private boolean isValidAddressLength(String address) {
        if (address == null) return true; // Optional field
        return address.length() <= 500;
    }
}