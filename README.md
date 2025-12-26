# Hospital Patient Management System

A comprehensive hospital patient management system with electronic health record (EPA/EHR) integration, built with Jakarta EE, PostgreSQL, and modern web technologies.

## Features

- **Patient Management**: Complete CRUD operations for patient records
- **EPA Integration**: FHIR R4 compliant electronic patient record integration
- **Search & Filter**: Advanced patient search capabilities
- **Responsive UI**: Modern, mobile-friendly interface
- **RESTful API**: Well-documented REST endpoints
- **Data Security**: Secure patient data handling with consent management
- **Audit Trail**: Complete logging of EPA synchronization activities

## Technology Stack

### Backend
- **Jakarta EE 10**: Enterprise Java framework
- **JPA/Hibernate**: Object-relational mapping
- **JAX-RS**: RESTful web services
- **EJB**: Business logic layer
- **PostgreSQL 17**: Relational database
- **WildFly 38**: Application server

### Frontend
- **HTML5**: Modern markup
- **CSS3**: Responsive design with gradients and animations
- **Vanilla JavaScript**: No framework dependencies
- **Fetch API**: Asynchronous data operations

### Standards
- **FHIR R4**: Healthcare interoperability standard
- **RESTful**: API design principles
- **JSON**: Data interchange format

## Prerequisites

- **Java 17** or higher
- **Maven 3.8+** for building
- **PostgreSQL 17** (or compatible version)
- **WildFly 38** (or compatible Jakarta EE 10 server)

## Quick Start

### 1. Database Setup

```bash
# Create PostgreSQL database and user
sudo -u postgres psql

CREATE USER hospital_admin WITH PASSWORD 'dbpassword';
CREATE DATABASE hospital_db OWNER hospital_admin;
GRANT ALL PRIVILEGES ON DATABASE hospital_db TO hospital_admin;
\q

# Import database schema
psql -U hospital_admin -d hospital_db -f hospital_db.sql

# Import EPA extensions
psql -U hospital_admin -d hospital_db -f hospital_db_ema_migration.sql
```

### 2. WildFly Configuration

#### Install PostgreSQL JDBC Driver

```bash
# Create module directory
mkdir -p $JBOSS_HOME/modules/system/layers/base/org/postgresql/main

# Download JDBC driver
wget https://jdbc.postgresql.org/download/postgresql-42.7.4.jar \
  -O $JBOSS_HOME/modules/system/layers/base/org/postgresql/main/postgresql.jar

# Create module.xml
cat > $JBOSS_HOME/modules/system/layers/base/org/postgresql/main/module.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<module xmlns="urn:jboss:module:1.9" name="org.postgresql">
    <resources>
        <resource-root path="postgresql.jar"/>
    </resources>
    <dependencies>
        <module name="javax.api"/>
        <module name="javax.transaction.api"/>
    </dependencies>
</module>
EOF
```

#### Configure DataSource

```bash
# Start WildFly
$JBOSS_HOME/bin/standalone.sh &

# Add PostgreSQL driver
$JBOSS_HOME/bin/jboss-cli.sh --connect \
  --command="/subsystem=datasources/jdbc-driver=postgresql:add(driver-name=postgresql,driver-module-name=org.postgresql,driver-class-name=org.postgresql.Driver)"

# Add datasource
$JBOSS_HOME/bin/jboss-cli.sh --connect \
  --command="data-source add \
    --name=PostgresDS \
    --jndi-name=java:/PostgresDS \
    --driver-name=postgresql \
    --connection-url=jdbc:postgresql://localhost:5432/hospital_db \
    --user-name=hospital_admin \
    --password=dbpassword \
    --use-ccm=true \
    --max-pool-size=25 \
    --blocking-timeout-wait-millis=5000 \
    --enabled=true \
    --driver-class=org.postgresql.Driver \
    --jta=true \
    --use-java-context=true"

# Test connection
$JBOSS_HOME/bin/jboss-cli.sh --connect \
  --command="/subsystem=datasources/data-source=PostgresDS:test-connection-in-pool"
```

### 3. Build and Deploy

```bash
# Clone the repository
git clone https://github.com/hjstephan/hospital-system.git
cd hospital-system

# Build with Maven
mvn clean package

# Deploy to WildFly
cp target/hospital-management.war $JBOSS_HOME/standalone/deployments/
```

### 4. Access the Application

- **Main Application**: http://localhost:8080/hospital-management
- **WildFly Admin Console**: http://localhost:9990

## Project Structure

```
hospital-system/
├── src/
│   └── main/
│       ├── java/com/hospital/
│       │   ├── entity/          # JPA entities
│       │   │   ├── Patient.java
│       │   │   ├── Diagnosis.java
│       │   │   └── Medication.java
│       │   ├── rest/            # REST endpoints
│       │   │   ├── PatientResource.java
│       │   │   └── EPAResource.java
│       │   └── epa/             # EPA integration
│       │       ├── EPAIntegrationService.java
│       │       └── FHIRConverter.java
│       ├── resources/
│       │   └── META-INF/
│       │       └── persistence.xml
│       └── webapp/
│           └── index.html       # Frontend application
├── hospital_db.sql              # Database schema
├── hospital_db_ema_migration.sql # EPA extensions
└── pom.xml                      # Maven configuration
```

## API Endpoints

### Patient Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/patients` | Get all patients |
| GET | `/api/patients?status=active` | Get active patients |
| GET | `/api/patients/{id}` | Get patient by ID |
| GET | `/api/patients/search?q={query}` | Search patients |
| POST | `/api/patients` | Create new patient |
| PUT | `/api/patients/{id}` | Update patient |
| DELETE | `/api/patients/{id}` | Delete patient |

### EPA Integration

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/epa/sync/{patientId}` | Sync patient to EPA |
| POST | `/api/epa/sync-all` | Sync all patients |
| PUT | `/api/epa/consent/{patientId}?enabled=true` | Set EPA consent |
| GET | `/api/epa/status/{patientId}` | Get EPA sync status |
| GET | `/api/epa/fetch/{epaId}` | Fetch from EPA |
| GET | `/api/epa/test-connection` | Test EPA connection |
| GET | `/api/epa/statistics` | Get sync statistics |

## Database Schema

### Main Tables

- **patients**: Core patient information with EPA fields
- **diagnoses**: Patient diagnoses and medical conditions
- **medications**: Prescribed medications and treatments
- **epa_sync_log**: Audit trail for EPA synchronization
- **epa_configuration**: EPA system configuration

### Key Fields

**Patients Table:**
- Personal data (name, DOB, gender, contact)
- Medical data (blood type, allergies)
- Emergency contacts
- EPA integration fields (epa_id, sync_status, consent)

## EPA Integration

The system implements FHIR R4 standard for healthcare interoperability:

### Configuration

Set environment variables:

```bash
export EPA_BASE_URL="https://your-epa-system.com/api"
export EPA_API_KEY="your-api-key"
```

### Patient Consent

Patients must provide explicit consent for EPA synchronization:

```java
// Enable EPA for a patient
PUT /api/epa/consent/{patientId}?enabled=true
```

### FHIR Mapping

The system converts patient data to FHIR R4 format:

- Patient resource with identifiers
- Demographics (name, gender, birthDate)
- Contact information (telecom, address)
- Extensions for blood type and allergies
- Emergency contacts

## 🧪 Testing

### Manual Testing

```bash
# Get all patients
curl http://localhost:8080/hospital-management/api/patients

# Create a patient
curl -X POST http://localhost:8080/hospital-management/api/patients \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "dateOfBirth": "1990-01-01",
    "gender": "Männlich",
    "insuranceNumber": "INS-2024-999",
    "epaEnabled": true
  }'

# Sync to EPA
curl -X POST http://localhost:8080/hospital-management/api/epa/sync/1
```

### Run Unit Tests

```bash
mvn test
```

## Features Detail

### Patient Management
- Complete patient records with medical history
- Advanced search and filtering
- Status management (active/discharged)
- Real-time updates

### EPA Synchronization
- FHIR R4 compliant data exchange
- Automatic sync on data changes
- Error handling and retry logic
- Detailed sync status tracking

### Security
- Patient consent management
- Secure password storage
- Data validation
- Audit logging

## Configuration

### persistence.xml

Located in `src/main/resources/META-INF/persistence.xml`:

```xml
<persistence-unit name="hospitalPU" transaction-type="JTA">
    <jta-data-source>java:/PostgresDS</jta-data-source>
    <!-- Entity classes -->
    <properties>
        <property name="hibernate.dialect" value="org.hibernate.dialect.PostgreSQLDialect"/>
        <property name="hibernate.hbm2ddl.auto" value="update"/>
    </properties>
</persistence-unit>
```

### Database Connection

Modify in WildFly configuration or use environment variables:

```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=hospital_db
DB_USER=hospital_admin
DB_PASSWORD=dbpassword
```

## Troubleshooting

### Database Connection Issues

```bash
# Check PostgreSQL is running
sudo systemctl status postgresql

# Verify database exists
psql -U hospital_admin -d hospital_db -c "\dt"

# Test datasource from WildFly
$JBOSS_HOME/bin/jboss-cli.sh --connect \
  --command="/subsystem=datasources/data-source=PostgresDS:test-connection-in-pool"
```

### Deployment Issues

```bash
# Check WildFly logs
tail -f $JBOSS_HOME/standalone/log/server.log

# Verify deployment
ls -l $JBOSS_HOME/standalone/deployments/

# Redeploy
rm $JBOSS_HOME/standalone/deployments/hospital-management.war
cp target/hospital-management.war $JBOSS_HOME/standalone/deployments/
```

### EPA Integration Issues

```bash
# Test EPA connection
curl http://localhost:8080/hospital-management/api/epa/test-connection

# Check sync status
curl http://localhost:8080/hospital-management/api/epa/statistics
```

## Development

### Building from Source

```bash
# Clone repository
git clone https://github.com/hjstephan/hospital-system.git
cd hospital-system

# Build
mvn clean package

# Run tests
mvn test

# Skip tests
mvn clean package -DskipTests
```

### IDE Setup

**IntelliJ IDEA:**
1. Import as Maven project
2. Configure WildFly server
3. Set up database connection

**Eclipse:**
1. Import Maven project
2. Add WildFly runtime
3. Configure server runtime environment

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Acknowledgments

- Jakarta EE community
- FHIR community
- PostgreSQL team
- WildFly project

## Support

For issues and questions:
- Create an issue on GitHub
- Check existing documentation
- Review WildFly and PostgreSQL documentation

## Future Enhancements

- Advanced reporting and analytics
- Multi-language support
- Role-based access control
- Document management
- Appointment scheduling
- Integration with medical devices
- Mobile applications
- Real-time notifications