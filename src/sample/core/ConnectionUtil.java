package sample.core;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;

/**
 * Created by oleh on 05.04.2016.
 */
public class ConnectionUtil {

    private DataSource dataSource;

    private static ConnectionUtil instance = new ConnectionUtil();

    private ConnectionUtil() {
        try {
            Context initContext = new InitialContext();
            dataSource = (DataSource) initContext.lookup("JNDI_LOOKUP_NAME");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public static ConnectionUtil getInstance() {
        return instance;
    }

    public Connection getConnection() throws SQLException {
        Connection connection = dataSource.getConnection();
        return connection;
    }

    public void close(Connection connection) throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        connection = null;
    }

}
