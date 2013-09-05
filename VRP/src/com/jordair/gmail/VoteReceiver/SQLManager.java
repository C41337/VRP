package com.jordair.gmail.VoteReceiver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class SQLManager {

	private Connection sql;
	public final Logger logger;
	public final String prefix, hostname, database, username, password;
	public final int port;
	private boolean connected;

	/**
	 * A simple MySQL tool for ease of access.
	 * 
	 * @param logger
	 *            The plugin logger
	 * @param prefix
	 *            Prefix for SQL output
	 * @param hostname
	 *            Host
	 * @param port
	 *            Port
	 * @param database
	 *            Database
	 * @param username
	 *            Username
	 * @param password
	 *            Password
	 */
	public SQLManager(Logger logger, String prefix, String hostname, int port, String database, String username, String password) {
		this.logger = logger;
		this.prefix = prefix;
		this.hostname = hostname;
		this.port = port;
		this.database = database;
		this.username = username;
		this.password = password;
		logger.info("Connecting...");
		connect();
		setup();
	}

	public void executeQuery(String username, String password, String databaseHost, String databaseName, int port, String query) {
		Connection conn;
		String url = "jdbc:mysql://" + databaseHost + ":" + port + "/" + databaseName;

		// Attempt to connect
		try {
			// Connection succeeded
			conn = DriverManager.getConnection(url, username, password);
			PreparedStatement statement = conn.prepareStatement(query);
			statement.executeQuery();
		} catch (Exception e) {
			// Couldn't connect to the database
		}
	}

	/**
	 * Establish connection with MySQL.
	 */
	public void connect() {
		String url = "jdbc:mysql://" + hostname + ":" + port + "/" + database;

		// Attempt to connect
		try {
			// Connection succeeded
			sql = DriverManager.getConnection(url, username, password);
			connected = true;
		} catch (Exception e) {
			// Couldn't connect to the database
		}
	}

	/**
	 * Disconnect from MySQL.
	 */
	public void disconnect() {
		if (sql != null && connected) {
			logger.info("Disconnected.");
			try {
				sql.close();
			} catch (SQLException e) {
				logger.warning("Unable to close SQL connection: " + e.getMessage());
			}
		}
	}

	/**
	 * @return true if connected to MySQL.
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Create tables.
	 */
	public void setup() {
		if (!isConnected())
			return;
		try {
			logger.info("Setting up MySQL.");
			query("CREATE TABLE IF NOT EXISTS votedata (username VARCHAR(25) PRIMARY KEY, votes INT, last_vote VARCHAR(35))");
		} catch (Exception e) {
		}
	}

	/**
	 * Empty the given table.
	 * 
	 * @param table
	 *            The table name
	 * @return non null if deletion was successful
	 */
	public ResultSet emptyTable(String table) {
		return query("TRUNCATE TABLE " + table);
	}

	/**
	 * Add a player to the table if they're not currently in.
	 * 
	 * @param user
	 *            The username to add
	 */
	public void add(String user) {
		if (!isIn("votedata", user))
			query("INSERT INTO votedata (username) VALUES ('" + user + "')");
	}

	/**
	 * Query a MySQL command
	 * 
	 * @param cmd
	 *            The command
	 * @return If the command executed properly
	 */
	private ResultSet query(String cmd) {
		if (!isConnected())
			return null;
		try {
			PreparedStatement statement = sql.prepareStatement(cmd);
			return statement.executeQuery();
		} catch (Exception exc) {
			return null;
		}
	}

	/**
	 * Determine whether or not a given username is in the table.
	 * 
	 * @param table
	 *            The table name
	 * @param name
	 *            The username to check
	 * @return true if the user is in the table.
	 */
	private boolean isIn(String table, String name) {
		if (!isConnected())
			return false;
		try {
			return query("SELECT * FROM " + table + " WHERE username = '" + name + "' LIMIT 1").next();
		} catch (Exception e) {
			try {
				connect();
			} catch (Exception ex) {

			}
		}
		return false;
	}

	/**
	 * Get a list of all primary keys in the table.
	 * 
	 * @param table
	 *            The table name
	 * @return A list of all the primary keys, an empty list if none found or
	 *         null if not connected.
	 */
	public List<String> getKeys(String table) {
		if (!isConnected())
			return null;
		List<String> list = new ArrayList<String>();
		try {
			ResultSet rs = query("SELECT * FROM " + table + " WHERE username != 'null'");
			if (rs != null)
				while (rs.next())
					if (rs.getString("username") != null)
						list.add(rs.getString("username"));
		} catch (Exception e) {
			try {
				connect();
			} catch (Exception ex) {

			}
		}
		return list;
	}

	/**
	 * Run a query to set data in MySQL.
	 * 
	 * @param table
	 *            The table
	 * @param name
	 *            The primary key
	 * @param field
	 *            The field
	 * @param value
	 *            The value
	 */
	public void set(String table, String name, String field, Object value) {
		query("UPDATE " + table + " SET " + field + " = " + value + " WHERE username = '" + name + "' LIMIT 1");
	}

	/**
	 * Get a piece of integer data out of the MySQL database.
	 * 
	 * @param table
	 *            The table
	 * @param name
	 *            The primary key
	 * @param field
	 *            The field
	 * @return The int received or 0 if nothing found
	 */
	public int getInt(String table, String name, String field) {
		try {
			ResultSet rs = query("SELECT * FROM " + table + " WHERE username = '" + name + "' LIMIT 1");
			if (rs.next())
				return rs.getInt(field);
		} catch (Exception e) {
			try {
				connect();
			} catch (Exception ex) {

			}
		}
		return 0;
	}

	/**
	 * Get a piece of string data out of the MySQL database.
	 * 
	 * @param table
	 *            The table
	 * @param name
	 *            The primary key
	 * @param field
	 *            The field
	 * @return The string received or an empty string if nothing found
	 */
	public String getString(String table, String name, String field) {
		try {
			ResultSet rs = query("SELECT * FROM " + table + " WHERE username = '" + name + "' LIMIT 1");
			if (rs.next())
				return rs.getObject(field) + "";
		} catch (Exception e) {
			try {
				connect();
			} catch (Exception ex) {

			}
		}
		return "";
	}
}
