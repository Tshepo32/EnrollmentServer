
package za.ac.cput.Connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Acer
 */
public class DBConnection {


    public static Connection getConnection() {
        try {
            // Establish a database connection
            Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/studentenrolmentdb", "root", "#Mama2222");
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

}
