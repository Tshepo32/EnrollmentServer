package za.ac.cput.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import za.ac.cput.Connection.DBConnection;
import za.ac.cput.Domain.Admin;
import za.ac.cput.Domain.Login;
import za.ac.cput.Domain.User;

public class UserDAO {
    private Connection con;
    private PreparedStatement pstmt;

    public UserDAO() {
        this.con = DBConnection.getConnection();
    }

    public boolean registerUser(User user) {
        String insertUserSQL = "INSERT INTO User (username, password, role) VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatement = con.prepareStatement(insertUserSQL)) {
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getRole());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {

                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQL Error: " + e.getMessage());
            return false;
        }
    }

    public boolean authenticateUser(Login login) {
        String selectUserSQL = "SELECT * FROM User WHERE username = ? AND password = ?";
        boolean isValid = false;
        
        try (PreparedStatement preparedStatement = con.prepareStatement(selectUserSQL)) {
            preparedStatement.setString(1, login.getUsername());
            preparedStatement.setString(2, login.getPassword());
            
            try (ResultSet result = preparedStatement.executeQuery()) {
            if (result.next()) {
                // !Admin credentials are valid
                System.out.println("Not admin login successful");
                isValid = true;
            } else {
                // Admin credentials are invalid
                System.out.println("Not admin login failed");
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
        System.out.println("Database error");
    }

    return isValid;

    }
}
