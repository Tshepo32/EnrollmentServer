
package za.ac.cput.DAO;

import za.ac.cput.Connection.DBConnection;

import java.sql.*;

import za.ac.cput.Domain.Student;



/**
 *
 * @author Acer
 */
public class StudentDAO {
    private Connection con;
   private Statement stmnt;
   private PreparedStatement pstmnt;
   private String jdbcUrl;
    private String username;
    private String password;
   
   public StudentDAO() {
       con = DBConnection.getConnection();
    }


}
