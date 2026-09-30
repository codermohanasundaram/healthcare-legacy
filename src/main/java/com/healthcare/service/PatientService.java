package com.healthcare.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import com.healthcare.dao.PatientDAO;
import com.healthcare.model.Patient;

/**
 * Business logic for patients.
 *
 * (The other modules - appointments, billing, records - were written later
 * and call their DAOs straight from the servlets.)
 */
public class PatientService {

    private static Logger log = Logger.getLogger(PatientService.class);

    private PatientDAO patientDAO = new PatientDAO();

    public Patient getPatient(String patientId) {
        if (patientId == null || patientId.trim().length() == 0) {
            return null;
        }
        int id = Integer.parseInt(patientId.trim());
        return patientDAO.getPatientById(id);
    }

    public List<Patient> getAllPatients() {
        return patientDAO.getAllPatients();
    }

    public List<Patient> searchPatients(String name, String phone, String mrn) {
        if ((name == null || name.trim().equals(""))
                && (phone == null || phone.trim().equals(""))
                && (mrn == null || mrn.trim().equals(""))) {
            return patientDAO.getAllPatients();
        }
        return patientDAO.searchPatients(name, phone, mrn);
    }

    /**
     * Returns an error message, or null if the patient is valid.
     */
    public String validatePatient(Patient p) {
        if (p.getFirstName() == null || p.getFirstName().trim().equals("")) {
            return "First name is required";
        }
        if (p.getLastName() == null || p.getLastName().trim().equals("")) {
            return "Last name is required";
        }
        if (p.getDateOfBirth() == null) {
            return "Date of birth is required (yyyy-MM-dd)";
        }
        if (p.getDateOfBirth().after(new Date())) {
            return "Date of birth cannot be in the future";
        }
        if (p.getPhone() == null || p.getPhone().trim().length() < 10) {
            return "Valid phone number is required";
        }
        if (p.getSsn() != null && !p.getSsn().equals("")
                && !p.getSsn().matches("\\d{3}-\\d{2}-\\d{4}")) {
            return "SSN must be in format 999-99-9999";
        }
        return null;
    }

    public int registerPatient(Patient p, String createdBy) {
        String error = validatePatient(p);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        // duplicate check on SSN
        if (p.getSsn() != null && !p.getSsn().equals("")) {
            List<Patient> all = patientDAO.getAllPatients();
            for (int i = 0; i < all.size(); i++) {
                if (p.getSsn().equals(all.get(i).getSsn())) {
                    throw new IllegalArgumentException("A patient with this SSN already exists (MRN "
                            + all.get(i).getMrn() + ")");
                }
            }
        }

        p.setMrn(generateMrn());
        p.setCreatedBy(createdBy);

        log.info("Registering patient " + p.getFirstName() + " " + p.getLastName()
                + ", DOB=" + p.getDateOfBirth() + ", SSN=" + p.getSsn()
                + ", insurance=" + p.getInsuranceNumber() + ", by " + createdBy);

        int id = patientDAO.addPatient(p);
        if (id <= 0) {
            throw new RuntimeException("Patient could not be saved");
        }
        return id;
    }

    public boolean updatePatient(Patient p) {
        String error = validatePatient(p);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
        log.info("Updating patient " + p);
        return patientDAO.updatePatient(p);
    }

    /**
     * Age in whole years.
     */
    @SuppressWarnings("deprecation")
    public static int calculateAge(Date dob) {
        if (dob == null) return 0;
        Date today = new Date();
        int age = today.getYear() - dob.getYear();
        if (today.getMonth() < dob.getMonth()
                || (today.getMonth() == dob.getMonth() && today.getDate() < dob.getDate())) {
            age--;
        }
        return age;
    }

    private String generateMrn() {
        String datePart = new SimpleDateFormat("yyyyMMdd").format(new Date());
        int random = 1000 + new Random().nextInt(9000);
        return "MRN" + datePart + random;
    }
}
