# Healthcare Patient Management System (Legacy)

A deliberately **legacy** Java web application, used as the input corpus for the
Developer-RAG modernization POC. It models a hospital system from 2009–2016:
Servlets + JSP + raw JDBC + MySQL, configured through `web.xml`, and deployed as a WAR to Tomcat.

> ⚠️ **Do not deploy this anywhere real.** It intentionally contains security flaws
> (SQL injection, XSS, weak hashing, PHI leakage) so that the modernization agent has
> real problems to find. All patient data in `database/healthcare.sql` is fictitious.

## Modules

| Module | Servlet (URL) | DAO | JSP views |
|---|---|---|---|
| Login / Logout | `LoginServlet` (`/login`), `LogoutServlet` (`/logout`) | `UserDAO` | `login.jsp` |
| Dashboard | — (JSP calls DAOs directly) | `PatientDAO`, `DoctorDAO`, `AppointmentDAO` | `home.jsp` |
| Patient Registration & Search | `PatientServlet` (`/patient`) | `PatientService` → `PatientDAO` | `patient-list.jsp`, `patient.jsp`, `patient-form.jsp` |
| Doctor Management | `DoctorServlet` (`/doctor`) | `DoctorDAO` | `doctor-list.jsp`, `doctor-form.jsp` |
| Appointment Booking | `AppointmentServlet` (`/appointment`) | `AppointmentDAO`, `BillingDAO` | `appointment.jsp`, `appointment-list.jsp` |
| Medical Records | `MedicalRecordServlet` (`/medicalRecord`) | `MedicalRecordDAO` | `medical-record.jsp` |
| Billing | `BillingServlet` (`/billing`) | `BillingDAO` | `billing.jsp`, `bill-list.jsp`, `bill-view.jsp` |
| Reports | `ReportServlet` (`/report`) | *(raw JDBC + stored procedure)* | `reports.jsp` |

Cross-cutting: `AuthenticationFilter` (all URLs), `DBConnection`, `DateUtil`, `PasswordUtil`,
`WEB-INF/includes/header.jspf` / `footer.jspf` (static includes), `error.jsp`.

## Architecture

```
Browser
   │
   ▼
AuthenticationFilter  (/*  – session check only, no role checks)
   │
   ▼
Servlet layer  (action=... dispatching, validation, some business rules)
   │                         │
   ▼                         ▼
PatientService         (other modules call DAOs directly)
   │                         │
   ▼                         ▼
DAO layer  (JDBC, Statement / PreparedStatement mix)
   │
   ▼
DBConnection  (DriverManager, new connection per call, no pool)
   │
   ▼
MySQL 5.7  (tables + sp_patient_visit_summary)
```

### Example flow: Patient search / view

```
GET /patient?action=search&name=wil
  → AuthenticationFilter          (session "user" present?)
  → PatientServlet.doGet          (action == "search")
  → PatientService.searchPatients (all blank → getAllPatients)
  → PatientDAO.searchPatients     (SQL built by string concatenation)
  → DBConnection.getConnection    → MySQL
  → List<Patient> in request attribute "patients"
  → forward /patient-list.jsp     (scriptlets render table, SSN shown unmasked)

GET /patient?action=view&patientId=1
  → PatientService.getPatient("1")   (Integer.parseInt, NumberFormatException → error.jsp)
  → PatientService.calculateAge      (deprecated Date API)
  → AppointmentDAO / MedicalRecordDAO / BillingDAO (called directly from servlet)
  → forward /patient.jsp
```

### Example flow: Appointment completion → billing (hidden coupling)

```
GET /appointment?action=complete&id=4
  → AppointmentDAO.updateStatus(4, "COMPLETED")
  → BillingDAO.getBillByAppointment(4) == null
  → creates Bill with consultation fee + 18% tax  (duplicated rule, no senior/insurance discount)
```

## Business rules (as implemented)

| Rule | Where |
|---|---|
| MRN = `MRN` + `yyyyMMdd` + 4 random digits (collisions possible) | `PatientService.generateMrn` |
| Required: first/last name, DOB (not in future), phone ≥ 10 chars; SSN format `999-99-9999` | `PatientService.validatePatient` (+ duplicated JS in `patient-form.jsp`) |
| Duplicate patient = same SSN (loads *all* patients to check) | `PatientService.registerPatient` |
| Patients are soft-deleted (`active = 0`), ADMIN only | `PatientServlet`, `PatientDAO.deletePatient` |
| Doctors are **hard**-deleted, any role | `DoctorServlet`, `DoctorDAO.deleteDoctor` |
| Appointments: not in past, not Sunday, 09:00–16:30 in 30-min slots, one per doctor per slot | `AppointmentServlet.doPost`, `AppointmentDAO.isSlotAvailable`, `appointment.jsp` |
| Doctors see only their own appointments by default | `AppointmentServlet` (list) |
| Only DOCTOR/ADMIN can add medical records; doctors record against themselves | `MedicalRecordServlet.doPost` |
| Billing: senior (60+) 10% off subtotal; insured → 30% of lab charges off; 18% tax on (subtotal − discount) | `BillingServlet.doPost` |
| Auto consultation bill when an appointment is completed (fee + 18%, **no discounts**) | `AppointmentServlet` (complete) |
| Billing screens: ADMIN/BILLING; invoice view: anyone | `BillingServlet` |

## Known legacy characteristics (ground truth for the modernization agent)

These are intentional. Use them to evaluate whether the analysis agent finds them.

### Security
| # | Issue | Location |
|---|---|---|
| S1 | SQL injection in login (string-concatenated query) | `UserDAO.authenticate` |
| S2 | SQL injection in patient search (name/phone/MRN) | `PatientDAO.searchPatients` |
| S3 | SQL injection in revenue report (`month` param) | `ReportServlet` (type=revenue) |
| S4 | Other concatenated SQL (specialization, status, ids) | `DoctorDAO.getDoctorsBySpecialization`, `BillingDAO.getAllBills`, `AppointmentDAO` |
| S5 | Unsalted MD5 password hashes + plain-text fallback | `PasswordUtil`, `UserDAO`, `users` seed data |
| S6 | Password written to logs on login | `LoginServlet.doPost` (log.debug) |
| S7 | Reflected/stored XSS – no output encoding in scriptlets or EL | `patient-list.jsp` (`searchName`), all JSPs |
| S8 | No CSRF protection; state changes via GET (delete, cancel, complete, pay) | `PatientServlet`, `DoctorServlet`, `AppointmentServlet`, `BillingServlet` |
| S9 | Session fixation – session id not rotated after login | `LoginServlet` |
| S10 | Login lockout stored in session (bypass by dropping cookie) | `LoginServlet` |
| S11 | Broken access control: Reports hidden in nav but `/report` has no role check | `ReportServlet`, `header.jspf` |
| S12 | Doctor management has no role check | `DoctorServlet` |
| S13 | Full PHI export incl. SSN to CSV for any logged-in user | `ReportServlet.exportPatientsCsv` |
| S14 | PHI in logs (SSN, DOB, insurance no.) and in `Patient.toString()` | `PatientService.registerPatient`, `Patient` |
| S15 | SSN displayed unmasked in lists and detail pages | `patient-list.jsp`, `patient.jsp` |
| S16 | Stack traces rendered to users | `error.jsp` |
| S17 | Hard-coded DB credentials in source and committed properties | `DBConnection`, `db.properties` |
| S18 | Outdated dependencies: log4j 1.2.17 (EOL, CVEs), MySQL Connector 5.1 | `pom.xml` |
| S19 | Filter allow-list based on `endsWith`/`indexOf` URI matching | `AuthenticationFilter` |

### Architecture / code quality
| # | Issue | Location |
|---|---|---|
| A1 | Inconsistent layering – only patients have a service; other servlets hold business logic | `service/`, `BillingServlet`, `AppointmentServlet` |
| A2 | Business rule duplicated and divergent (billing tax/discounts) | `BillingServlet` vs `AppointmentServlet` |
| A3 | Data access from JSP scriptlets | `home.jsp` |
| A4 | Raw SQL in a servlet, bypassing DAOs; stored procedure dependency | `ReportServlet`, `sp_patient_visit_summary` |
| A5 | No connection pool; new `DriverManager` connection per call | `DBConnection` |
| A6 | Manual resource cleanup in `finally`, exceptions swallowed / `printStackTrace` | all DAOs |
| A7 | `double` used for money | `Bill`, `Doctor`, `bills` table |
| A8 | Thread-unsafe shared `SimpleDateFormat` | `DateUtil` |
| A9 | Deprecated `java.util.Date` APIs for age calculation | `PatientService.calculateAge` |
| A10 | Magic strings for roles/statuses (constants exist but unused) | `Appointment`, servlets, JSPs |
| A11 | God servlets dispatching on `action` parameter | all servlets |
| A12 | Mixed view tech: scriptlets and JSTL/EL | JSPs |
| A13 | Config duplicated: tax rate in `web.xml` (display) and hard-coded `0.18` (calculation) | `web.xml`, `BillingServlet`, `AppointmentServlet` |
| A14 | N+1 / full-table loads (dropdowns, duplicate SSN check, no pagination) | `PatientService`, `AppointmentServlet.loadFormData` |
| A15 | Race condition in slot booking (check-then-insert, no unique constraint) | `AppointmentServlet`, `AppointmentDAO` |
| A16 | MRN generation can collide (random suffix) | `PatientService.generateMrn` |
| A17 | Missing FKs (doctors, bills, medical_records); `latin1` charset | `healthcare.sql` |
| A18 | No automated tests | — |

## Build & run

Requirements: JDK 8+ (the build targets Java 8), Maven 3, Docker.

```bash
mvn clean package
```

```bash
docker compose up -d
```

Open http://localhost:8080/healthcare/. MySQL is exposed on host port `3307`.

Demo logins:

| User | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `drsmith` | `doctor123` | DOCTOR (Dr. John Smith) |
| `drraman` | `doctor123` | DOCTOR (Dr. Priya Raman) |
| `reception` | `welcome1` | RECEPTIONIST |
| `billing` | `billing123` | BILLING (plain-text password row) |

To run on your own Tomcat 7/8.5/9 instead, load `database/healthcare.sql` into MySQL 5.7
and either edit `src/main/resources/db.properties` or pass `-Ddb.url=... -Ddb.username=... -Ddb.password=...`
in `CATALINA_OPTS`.

## Project layout

```
healthcare-legacy/
├── pom.xml
├── docker-compose.yml
├── database/healthcare.sql
└── src/main/
    ├── java/com/healthcare/
    │   ├── servlet/   Login, Logout, Patient, Doctor, Appointment, MedicalRecord, Billing, Report
    │   ├── service/   PatientService
    │   ├── dao/       User, Patient, Doctor, Appointment, MedicalRecord, Billing
    │   ├── model/     User, Patient, Doctor, Appointment, MedicalRecord, Bill
    │   ├── filter/    AuthenticationFilter
    │   └── util/      DBConnection, DateUtil, PasswordUtil
    ├── resources/     db.properties, log4j.properties
    └── webapp/
        ├── *.jsp      login, home, patient*, doctor*, appointment*, medical-record, billing, bill-*, reports, error
        ├── css/style.css
        └── WEB-INF/   web.xml, includes/header.jspf, includes/footer.jspf
```
