# Chronos – Job Scheduling & Notification System

Chronos is a Spring Boot–based job scheduler that allows you to define, manage, and monitor recurring tasks with automated notifications via email.  
It integrates with MySQL for persistence, JWT for authentication, and Spring Mail for failure alerts.

---

## Features

- JWT-based authentication and authorization
- Job scheduling with cron-style triggers
- Job persistence with MySQL
- Email notifications on job failures
- RESTful APIs for job management
- Spring Boot powered with embedded Tomcat

---

## Tech Stack

- **Backend:** Spring Boot 3, Spring Security, Spring Data JPA
- **Database:** MySQL 8
- **Auth:** JWT (JSON Web Tokens)
- **Notifications:** Spring Mail (SMTP)
- **Build Tool:** Maven
- **Language:** Java 17+

---

## Project Structure (simplified)

chronos/
┣ src/main/java/com/chronos
┃ ┣ config/ # Security & JWT configuration
┃ ┣ controller/ # REST controllers
┃ ┣ model/ # Entities (Job, User, etc.)
┃ ┣ repository/ # JPA repositories
┃ ┣ service/ # Services (JobService, AuthService, NotificationService)
┃ ┗ ChronosApp.java # Main Spring Boot application
┣ src/main/resources/
┃ ┣ application.properties # App configuration
┣ pom.xml
┗ README.md



---

## Configuration

Update your `application.properties` with the following:

```properties
# ===============================
# Server
# ===============================
server.port=8080

# ===============================
# Database (MySQL)
# ===============================
spring.datasource.url=jdbc:mysql://localhost:3307/chronosdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=chronos_user
spring.datasource.password=chronos_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# ===============================
# Mail (SMTP)
# ===============================
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# ===============================
# JWT
# ===============================
jwt.secret=your-32-char-secret-key
jwt.expiration-ms=3600000


Running the Application

1. Clone the repository:
  git clone https://github.com/your-username/chronos.git
cd chronos

2. Create a MySQL database and user:
 CREATE DATABASE chronosdb;
CREATE USER 'chronos_user'@'%' IDENTIFIED BY 'chronos_password';
GRANT ALL PRIVILEGES ON chronosdb.* TO 'chronos_user'@'%';


3. Run with Maven:
 mvn spring-boot:run


Example: NotificationService
@Service
@Slf4j
public class NotificationService {
    private final JavaMailSender mailSender;
    private final String from;

    public NotificationService(JavaMailSender mailSender, 
        @Value("${spring.mail.username}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void sendFailureNotification(String to, String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage(), e);
        }
    }
}


Author

Avnish Kumar
Email: avnish01kumar@gmail.com