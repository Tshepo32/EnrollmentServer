
package za.ac.cput.Domain;

import java.io.Serializable;

/**
 *
 * @author Acer
 */
public class Login implements Serializable{
    private String userName;
    private String password;
    private static final long serialVersionUID = 7038872149364983861L;

    public Login(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getUsername() {
        return userName;
    }

    public void setUsername(String username) {
        this.userName = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    @Override
    public String toString() {
        return "Admin{" + "userName=" + userName + ", password=" + password + '}';
    }
}

