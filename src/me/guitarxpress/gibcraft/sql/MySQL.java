package me.guitarxpress.gibcraft.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import me.guitarxpress.gibcraft.GibCraft;

public class MySQL {

	private String host;
	private String port;
	private String database;
	private String username;
	private String password;
	private boolean useSSL;

	private Connection connection;

	public MySQL(GibCraft plugin) {

	}

	public boolean isConnected() {
		return connection != null;
	}

	public void connect() throws ClassNotFoundException, SQLException {
		if (!isConnected())
			connection = DriverManager.getConnection(
					"jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + useSSL, username, password);

	}

	public void disconnect() {
		if (isConnected()) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public Connection getConnection() {
		return connection;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isUseSSL() {
		return useSSL;
	}

	public void setUseSSL(boolean useSSL) {
		this.useSSL = useSSL;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

}
