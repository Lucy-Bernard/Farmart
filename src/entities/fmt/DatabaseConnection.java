package entities.fmt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This class provides a utility method for connecting to a database It contains
 * the connection parameters, username, password, and url necessary for
 * connecting to the database
 * 
 * @author lucyb
 *
 */
public class DatabaseConnection {

	public static final String parameters = "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
	public static final String username = "lbernard";
	public static final String password = "Giblesaurr1228!";
	public static final String url = "jdbc:mysql://localhost/" + username + parameters;

	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(url, username, password);
	}
}