package sample.model;

import com.mysql.jdbc.Connection;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBase {

    private final StringProperty host;
    private final StringProperty port;
    private final StringProperty user;
    private final StringProperty password;
    private final StringProperty dbname;

    /**
     * Default constructor.
     */
    public DataBase() {
        this(null, null, null, null, null);
    }

    /**
     * Constructor with some initial data.
     *
     */
    public DataBase(String host, String port, String user, String password, String dbname) {
        this.host = new SimpleStringProperty(host);
        this.port = new SimpleStringProperty(port);
        this.user = new SimpleStringProperty(user);
        this.password = new SimpleStringProperty(password);
        this.dbname = new SimpleStringProperty(dbname);
    }

    public static void getConnection(){
        Connection conn = null;
        try {
            conn = (Connection) DriverManager.getConnection("jdbc:mysql://localhost/BioImageApp?" +
                            "user=oleh&password=oleh123");
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }

    public String getHost(){return host.get();}
    public void setHost(String host){this.host.set(host);}
    public StringProperty HostProperty() {
        return host;
    }

    public String getPort(){return port.get();}
    public void setPort(String port){this.port.set(port);}
    public StringProperty PortProperty() {
        return port;
    }

    public String getUser(){return user.get();}
    public void setUser(String user){this.user.set(user);}
    public StringProperty UserProperty() {
        return user;
    }

    public String getPassword(){return password.get();}
    public void setPassword(String password){this.password.set(password);}
    public StringProperty PasswordProperty() {
        return password;
    }

    public String getdbname(){return dbname.get();}
    public void setdbname(String dbname){this.dbname.set(dbname);}
    public StringProperty dbnameProperty() {
        return dbname;
    }

}