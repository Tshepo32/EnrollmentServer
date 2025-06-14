package za.ac.cput.DAO;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import za.ac.cput.Connection.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.ac.cput.Domain.Course;
import za.ac.cput.Domain.Enrollment;
import za.ac.cput.Domain.Student;

public class EnrollmentDAO {
    private static final Logger logger = LoggerFactory.getLogger(EnrollmentDAO.class);
    private Connection con;

    public EnrollmentDAO() {
        con = DBConnection.getConnection();
    }

    public boolean enrollStudentInCourse(String studentID, String courseName) {
        if (studentID == null || courseName == null || studentID.isEmpty() || courseName.isEmpty()) {
            logger.error("Invalid student ID or course name provided.");
            return false;
        }

        String query = "INSERT INTO enrollment (student_id, course_code, enrollment_date) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setString(1, studentID);
            preparedStatement.setString(2, courseName);
            preparedStatement.setDate(3, Date.valueOf(LocalDate.now())); // Set current date as enrollment date
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Error enrolling student {} in course {}: {}", studentID, courseName, e.getMessage());
            return false;
        }
    }

    public boolean enrollStudentInCourse(Enrollment enrollment) {
        if (enrollment == null || enrollment.getStudent() == null || enrollment.getCourse() == null) {
            logger.error("Invalid enrollment object provided.");
            return false;
        }

        String studentId = enrollment.getStudent().getStudentID();
        String courseCode = enrollment.getCourse().getCourseCode();
        LocalDate localDate = LocalDate.now();
        Date enrollmentDate = Date.valueOf(localDate);

        if (studentId == null || courseCode == null || studentId.isEmpty() || courseCode.isEmpty()) {
            logger.error("Invalid student ID or course name provided.");
            return false;
        }

        String query = "INSERT INTO enrollment (student_id, course_code, enrollment_date) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setString(1, studentId);
            preparedStatement.setString(2, courseCode);
            preparedStatement.setDate(3, enrollmentDate); // Set current date as enrollment date
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Error enrolling student {} in course {}: {}", studentId, courseCode, e.getMessage());
            return false;
        }
    }

    // These ensure data integrity before attempting a delete.
    public boolean isStudentExists(String studentId) {
        String query = "SELECT COUNT(*) FROM student WHERE student_id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            // Replace with your actual logger
            System.err.println("Error checking if student " + studentId + " exists: " + e.getMessage());
            return false;
        }
        return false;
    }

    public boolean isStudentEnrolledInCourse(String studentId, String courseCode) {
        String query = "SELECT COUNT(*) FROM enrollment WHERE student_id = ? AND course_code = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, studentId);
            ps.setString(2, courseCode);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking enrollment for student " + studentId + " in course " + courseCode + ": " + e.getMessage());
            return false;
        }
        return false;
    }

    // --- Modified unenrollStudentFromCourse Method ---
// This method now accepts an Enrollment object and returns a String response.
    public String unenrollStudentFromCourse(Enrollment enrollment) { // Accepts Enrollment object
        if (enrollment == null || enrollment.getStudent() == null || enrollment.getCourse() == null) {
            System.err.println("Invalid enrollment object provided for unenrollment (null enrollment, student, or course).");
            return "INVALID_INPUT";
        }

        String studentId = enrollment.getStudent().getStudentID();
        String courseCode = enrollment.getCourse().getCourseCode();

        if (studentId == null || courseCode == null || studentId.isEmpty() || courseCode.isEmpty()) {
            System.err.println("Invalid student ID or course code extracted from enrollment object for unenrollment.");
            return "INVALID_INPUT";
        }

        // Perform checks before attempting to delete
        if (!isStudentExists(studentId)) {
            System.out.println("Unenrollment failed for student " + studentId + ": Student does not exist in the system.");
            return "STUDENT_NOT_FOUND";
        }

        if (!isCourseExists(courseCode)) {
            System.out.println("Unenrollment failed for course " + courseCode + ": Course does not exist in the system.");
            return "COURSE_NOT_FOUND";
        }

        if (!isStudentEnrolledInCourse(studentId, courseCode)) {
            System.out.println("Unenrollment failed: Student " + studentId + " is not currently enrolled in course " + courseCode + ".");
            return "ENROLLMENT_NOT_FOUND";
        }

        // If all checks pass, proceed with deletion from 'enrollment' table (singular)
        String sql = "DELETE FROM enrollment WHERE student_id = ? AND course_code = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, courseCode);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Student " + studentId + " successfully unenrolled from course " + courseCode + ".");
                return "UNENROLLMENT_SUCCESSFUL";
            } else {
                // This case should ideally not be reached if isStudentEnrolledInCourse returned true
                System.out.println("Unenrollment for student " + studentId + " from course " + courseCode + " had no rows affected. Enrollment might have disappeared unexpectedly.");
                return "UNENROLLMENT_FAILED"; // A general failure if nothing was deleted
            }
        } catch (SQLException e) {
            System.err.println("Database error during unenrollment for student " + studentId + " from course " + courseCode + ": " + e.getMessage());
            return "DATABASE_ERROR";
        }
    }

    private boolean isCourseExists(String courseCode) {
        String query = "SELECT COUNT(*) FROM course WHERE course_code = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, courseCode);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking if course " + courseCode + " exists: " + e.getMessage());
            return false;
        }
        return false;
    }

    // Get EnrollmentsByStudentId method
    public List<Enrollment> getEnrollmentsByStudentId(String studentId) {
        List<Enrollment> enrollments = new ArrayList<>();
        String query = "SELECT " +
                "e.enrollment_id, e.enrollment_date, " +
                "s.student_id, s.first_name, s.last_name, " +
                "c.course_code, c.course_name " + // Removed 'c.credits'
                "FROM enrollment e " +
                "JOIN student s ON e.student_id = s.student_id " + // Using singular 'student' table
                "JOIN course c ON e.course_code = c.course_code " + // Using singular 'course' table
                "WHERE e.student_id = ?";

        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sId = rs.getString("student_id");
                    String fName = rs.getString("first_name");
                    String lName = rs.getString("last_name");
                    Student student = new Student(sId, fName, lName);

                    String cCode = rs.getString("course_code");
                    String cName = rs.getString("course_name");
                    Course course = new Course(cCode, cName); // Constructor without credits

                    int enrollmentId = rs.getInt("enrollment_id");
                    String enrollmentDateStr = rs.getString("enrollment_date");

                    Enrollment enrollment = new Enrollment(enrollmentId, student, course, enrollmentDateStr);
                    enrollments.add(enrollment);
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving enrollments for student {}: {}", studentId, e.getMessage(), e);
        }
        return enrollments;
    }

    public List<Course> getEnrolledCoursesOnlyByStudentId(String studentId) {
        List<Course> enrolledCourses = new ArrayList<>();
        String selectQuery = "SELECT c.course_code, c.course_name " + // Removed 'c.credits'
                "FROM enrollment e " +
                "JOIN course c ON e.course_code = c.course_code " + // Using singular 'course' table
                "WHERE e.student_id = ?";

        try (PreparedStatement preparedStatement = con.prepareStatement(selectQuery)) {
            preparedStatement.setString(1, studentId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String courseCode = resultSet.getString("course_code");
                String courseName = resultSet.getString("course_name");
                Course course = new Course(courseCode, courseName); // Constructor without credits
                enrolledCourses.add(course);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving enrolled courses for student {}: {}", studentId, e.getMessage(), e);
        }
        return enrolledCourses;
    }

    public List<Enrollment> getAllEnrollments() {
        List<Enrollment> enrollments = new ArrayList<>();
        String query = "SELECT " +
                "e.enrollment_id, e.enrollment_date, " +
                "s.student_id, s.first_name, s.last_name, " +
                "c.course_code, c.course_name " + // Removed 'c.credits'
                "FROM enrollment e " +
                "JOIN student s ON e.student_id = s.student_id " +
                "JOIN course c ON e.course_code = c.course_code";

        try (PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String studentId = rs.getString("student_id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                Student student = new Student(studentId, firstName, lastName);

                String courseCode = rs.getString("course_code");
                String courseName = rs.getString("course_name");
                Course course = new Course(courseCode, courseName); // Constructor without credits

                int enrollmentId = rs.getInt("enrollment_id");
                String enrollmentDateStr = rs.getString("enrollment_date");

                Enrollment enrollment = new Enrollment(enrollmentId, student, course, enrollmentDateStr);
                enrollments.add(enrollment);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving all enrollments: {}", e.getMessage(), e);
        }
        return enrollments;
    }

    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String query = "SELECT course_code, course_name FROM course";
        try (PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String courseCode = rs.getString("course_code");
                String courseName = rs.getString("course_name");
                courses.add(new Course(courseCode, courseName));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving all courses: {}", e.getMessage(), e);
        }
        return courses;
    }

    public List<Student> getEnrolledStudentsOnly() {
        List<Student> enrolledStudents = new ArrayList<>();
        // SQL to select distinct students who appear in the enrollment table
        String query = "SELECT DISTINCT s.student_id, s.first_name, s.last_name " +
                "FROM student s " +
                "JOIN enrollment e ON s.student_id = e.student_id";
        try (PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String studentId = rs.getString("student_id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                enrolledStudents.add(new Student(studentId, firstName, lastName));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving only enrolled students: {}", e.getMessage(), e);
        }
        return enrolledStudents;
    }

}
