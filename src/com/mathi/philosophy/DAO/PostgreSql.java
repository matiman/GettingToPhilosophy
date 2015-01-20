package com.mathi.philosophy.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/*
 * This is a data layer to separate interaction with db from the service or presentation layer
 */
public class PostgreSql {

	public static Connection CONNECTION;

	public static Connection getConnection() throws ClassNotFoundException,
			SQLException {

		if (CONNECTION == null) {
			Class.forName("org.postgresql.Driver");
			CONNECTION = DriverManager.getConnection(
					"jdbc:postgresql://localhost:5432/WikiContent", "postgres",
					"");
		}

		return CONNECTION;
	}

	public static void insertPath(String title, String path)
			throws ClassNotFoundException, SQLException {

		PreparedStatement preparedStatement = getConnection().prepareStatement(
				"INSERT INTO philosophy (title, path) VALUES( ?, ?)");
		preparedStatement.setObject(1, title);
		preparedStatement.setObject(2, path.toString());

		preparedStatement.executeUpdate();

	}

	public static ResultSet fetchPath(String title)
			throws ClassNotFoundException, SQLException {

		PreparedStatement stmt = getConnection().prepareStatement(
				"select path from public.philosophy where title=?");
		stmt.setString(1, title);

		ResultSet rset = stmt.executeQuery();
		return rset;

	}

}
