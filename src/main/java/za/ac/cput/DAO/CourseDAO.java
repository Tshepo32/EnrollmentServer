
package za.ac.cput.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import za.ac.cput.Connection.DBConnection;
import za.ac.cput.Domain.Course;

/**
 *
 * @author Acer
 */
public class CourseDAO {
    private Connection con;
    private PreparedStatement pstmt;
    private ResultSet rs;

    public CourseDAO() {
        this.con = DBConnection.getConnection();
    }

    public boolean addCourse(Course course) {
    Connection conn = null;
    PreparedStatement pstmt = null;

    try {
        conn = DBConnection.getConnection();
        String insertQuery = "INSERT INTO course (COURSE_CODE, COURSE_NAME) VALUES (?, ?)";
        pstmt = conn.prepareStatement(insertQuery);
        pstmt.setString(1, course.getCourseCode());
        pstmt.setString(2, course.getCourseName());

        int rowsAffected = pstmt.executeUpdate();
        
        return rowsAffected > 0;
    } catch (SQLException e) {
        e.printStackTrace();
    } 
    return false;
}

    public Course getCourseById(int courseId) {
        // Implement the logic to retrieve a course by its ID
        return null;
        // Implement the logic to retrieve a course by its ID
    }

    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String query = "SELECT * FROM course";
        try {
            pstmt = con.prepareStatement(query);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Course course = new Course();
                course.setCourseCode(rs.getString("COURSE_CODE"));
                course.setCourseName(rs.getString("COURSE_NAME"));
                courses.add(course);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return courses;
    }

    public boolean updateCourse(Course course) {
        // Implement the logic to update a course's information in the database
        return false;
        // Implement the logic to update a course's information in the database
    }

    public boolean deleteCourse(String courseCode) {
        try {
            // Define the SQL statement to delete the course by course code
            String sql = "DELETE FROM course WHERE COURSE_CODE = ?";

            // Create a PreparedStatement
            try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
                // Set the course code in the PreparedStatement
                preparedStatement.setString(1, courseCode);

                int rowsAffected = preparedStatement.executeUpdate();

                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; 
        }
    }
}
