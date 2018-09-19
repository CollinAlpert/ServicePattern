package com.github.collinalpert.java2db.database;

import com.mysql.cj.exceptions.CJCommunicationsException;
import com.mysql.cj.jdbc.exceptions.CommunicationsException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Collin Alpert
 * @see <a href="https://github.com/CollinAlpert/APIs/blob/master/de/collin/DBConnection.java">GitHub</a>
 */
public class DBConnection implements AutoCloseable {
	private Connection connection = null;
	private boolean isConnectionValid;

	public static String HOST;
	public static String DATABASE;
	public static String USERNAME;
	public static String PASSWORD;
	public static DatabaseTypes DATABASE_TYPE;
	public static int PORT;
	public static boolean LOG_QUERIES = true;

	public DBConnection() {
		try {
			String driver;
			String connectionString;
			if (PORT == 0) {
				switch (DATABASE_TYPE) {
					case MICROSOFT:
						PORT = 1433;
						break;
					case MYSQL:
					default:
						PORT = 3306;
						break;
				}
			}
			switch (DATABASE_TYPE) {
				case MICROSOFT:
					driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
					connectionString = "jdbc:sqlserver://" + HOST + ":" + PORT + ";databaseName=" + DATABASE;
					break;
				case MYSQL:
				default:
					driver = "com.mysql.cj.jdbc.Driver";
					connectionString = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE + "?serverTimezone=UTC";
					break;
			}
			Class.forName(driver);
			DriverManager.setLoginTimeout(5);
			connection = DriverManager.getConnection(connectionString, USERNAME, PASSWORD);
			isConnectionValid = true;
		} catch (CJCommunicationsException | CommunicationsException e) {
			System.err.println("The connection to the database failed. Please check if the MySQL server is reachable and if you have an internet connection.");
			isConnectionValid = false;
			System.exit(1);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			isConnectionValid = false;
		}
	}

	/**
	 * Checks if the connection is valid/successful.
	 *
	 * @return True if connection was successful, false if not.
	 */
	public boolean isValid() {
		return this.isConnectionValid;
	}


	/**
	 * Executes an SELECT SQL statement on the database without Java parameters.
	 *
	 * @param query The query to be executed.
	 * @return The {@link ResultSet} containing the result from the SELECT query.
	 */
	public ResultSet execute(String query) {
		try {
			Statement statement = connection.createStatement();
			var set = statement.executeQuery(query);
			statement.closeOnCompletion();
			return set;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Executes an SELECT SQL statement on the database with Java parameters.
	 *
	 * @param query  The query to be executed.
	 * @param params The Java parameters to be inserted into the query.
	 * @return The {@link ResultSet} containing the result from the SELECT query.
	 */
	public ResultSet execute(String query, Object... params) {
		try {
			var statement = connection.prepareStatement(query);
			for (int i = 0; i < params.length; i++) {
				statement.setObject(i + 1, params[i]);
			}
			var set = statement.executeQuery();
			statement.closeOnCompletion();
			return set;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * This command is used for any queries that are supposed to update the database, such as UPDATE, DELETE, TRUNCATE etc.
	 *
	 * @param query The query to be executed.
	 * @return {@code True} if the update was successful, {@code false} if not.
	 */
	public boolean update(String query) {
		try {
			var statement = connection.createStatement();
			statement.executeUpdate(query);
			statement.closeOnCompletion();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * This command is used for any queries that are supposed to update the database, such as UPDATE, DELETE, TRUNCATE etc.
	 *
	 * @param query  The query to be executed.
	 * @param params The Java parameters to be inserted into the query.
	 * @return {@code True} if the update was successful, {@code false} if not.
	 */
	public boolean update(String query, Object... params) {
		try {
			var statement = connection.prepareStatement(query);
			for (int i = 0; i < params.length; i++) {
				statement.setObject(i + 1, params[i]);
			}
			statement.executeUpdate();
			statement.closeOnCompletion();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Determines if a connection to the database still exists or not.
	 *
	 * @return {@code True} if a connection exists, {@code false} if not.
	 * This method will return {@code false} if an exception occurs.
	 */
	public boolean isOpen() {
		try {
			return !connection.isClosed();
		} catch (SQLException e) {
			System.err.println("Could not determine connection status");
			isConnectionValid = false;
			return false;
		}
	}

	/**
	 * Closes the connection to the database.
	 */
	@Override
	public void close() {
		try {
			connection.close();
		} catch (SQLException e) {
			System.err.println("Could not close database connection");
		} finally {
			isConnectionValid = false;
		}
	}
}
