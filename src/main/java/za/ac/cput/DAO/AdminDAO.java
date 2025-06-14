
package za.ac.cput.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import za.ac.cput.Connection.DBConnection;
import za.ac.cput.Domain.Admin;
import za.ac.cput.Domain.Course;
import za.ac.cput.Domain.Student;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Acer
 */
public class AdminDAO {
    private static final Logger logger = LoggerFactory.getLogger(AdminDAO.class);
     private Connection con;
    private PreparedStatement pstmt;
    

    public AdminDAO() throws SQLException {
        con = DBConnection.getConnection();
    }

    public boolean validateAdmin(Admin admin) {
    String query = "SELECT * FROM User WHERE username = ? AND password = ?";
    boolean isValid = false;

    try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
        preparedStatement.setString(1, admin.getUserName());
        preparedStatement.setString(2, admin.getPassword());

        try (ResultSet result = preparedStatement.executeQuery()) {
            if (result.next()) {
                // Admin credentials are valid
                System.out.println("Admin login successful");
                isValid = true;
            } else {
                // Admin credentials are invalid
                System.out.println("Admin login failed");
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
        System.out.println("Database error");
    }

    return isValid;

    }

    public boolean addStudent(Student student) {
        String insertQuery = "INSERT INTO STUDENT (student_id, first_name, last_name) VALUES (?, ?, ?)";

        try ( PreparedStatement preparedStatement = con.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, student.getStudentID());
            preparedStatement.setString(2, student.getFirstName());
            preparedStatement.setString(3, student.getLastName());

            int rowsAffected = preparedStatement.executeUpdate();


            if (rowsAffected > 0) {
                System.out.println(rowsAffected);
                return true; // Insertion was successful
            }
        } catch (SQLException e) {
            // Handle the SQL exception appropriately (e.g., log the error or throw an exception)
            e.printStackTrace();
        }

        return false; // Insertion failed
    }

    public boolean deleteStudent(String studentNumber) {
        // Define the SQL DELETE statement
        String deleteQuery = "DELETE FROM STUDENT WHERE student_id = ?";

        try ( PreparedStatement preparedStatement = con.prepareStatement(deleteQuery)) {
            // Set the student number in the prepared statement
            preparedStatement.setString(1, studentNumber);

            // Execute the DELETE statement
            int rowsAffected = preparedStatement.executeUpdate();

            // Check if the deletion was successful (rowsAffected > 0)
            return rowsAffected > 0;
        } catch (SQLException e) {
            // Handle any SQL exceptions (e.g., connection error, SQL syntax error)
            e.printStackTrace();
            return false; // Deletion failed
        }
    }

    public Student getStudentById(String studentId) {
        String selectQuery = "SELECT s.student_id, s.first_name, s.last_name FROM student s WHERE s.student_id = ?";
        Student student = null;

        try (PreparedStatement preparedStatement = con.prepareStatement(selectQuery)) {
            preparedStatement.setString(1, studentId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String retrievedStudentId = resultSet.getString("student_id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                student = new Student(retrievedStudentId, firstName, lastName);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving student with ID {}: {}", studentId, e.getMessage(), e);
        }
        return student;
    }

    // NEW METHOD: To retrieve all courses a student is enrolled in
    public List<Course> getEnrolledCoursesByStudentId(String studentId) {
        List<Course> enrolledCourses = new ArrayList<>();
        // SQL query to join enrollment and course tables filter by student_id
        String selectQuery = "SELECT c.course_code, c.course_name " +
                "FROM enrollment e " +
                "JOIN course c ON e.course_code = c.course_code " + // Join on courseCode from course table
                "WHERE e.student_id = ?";

        try (PreparedStatement preparedStatement = con.prepareStatement(selectQuery)) {
            preparedStatement.setString(1, studentId);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Iterate through the results, as a student can be in multiple courses
            while (resultSet.next()) {
                String courseCode = resultSet.getString("course_code");
                String courseName = resultSet.getString("course_name"); // Ensure your 'course' table has 'courseName'

                // Create a new Course object and add it to the list
                Course course = new Course(courseCode, courseName);
                enrolledCourses.add(course);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving enrolled courses for student {}: {}", studentId, e.getMessage(), e);
        }

        return enrolledCourses;
    }


    public List<Student> getAllStudents() {
        List<Student> studentList = new ArrayList<>();

        // Define the SQL SELECT query
        String selectQuery = "SELECT * FROM STUDENT";

        try (PreparedStatement preparedStatement = con.prepareStatement(selectQuery)) {
            // Execute the SELECT query
            ResultSet resultSet = preparedStatement.executeQuery();

            // Iterate over the result set
            while (resultSet.next()) {
                // Retrieve student data from the result set
                String studentId = resultSet.getString("student_id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");

                // Create a Student object with the retrieved data
                Student student = new Student(studentId, firstName, lastName);

                // Add the Student object to the list
                studentList.add(student);
            }
        } catch (SQLException e) {
            // Handle any SQL exceptions (e.g., connection error, SQL syntax error)
            e.printStackTrace();
        }

        return studentList;
    }
//
//    public boolean updateStudent(Student student) {
//        // Implement the logic to update a student's information in the database
//    }
//
//    public boolean deleteStudent(int studentId) {
//        // Implement the logic to delete a student from the database
//    }
}
