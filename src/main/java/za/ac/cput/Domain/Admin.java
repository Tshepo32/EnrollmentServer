
package za.ac.cput.Domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.io.Serializable;

/**
 *
 * @author Acer
 */
@Entity
public class Admin implements Serializable{
    @Id
    private String userName;
    private String password;
    private static final long serialVersionUID = 7038872149364983861L;

    public Admin(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public Admin() {

    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Admin{" + "Email = " + userName + ", password = " + password + '}';
    }
}

