import java.sql.*;

public class DatabaseManager {
  static final String JDBC_URL = "jdbc:mysql://localhost:3306/GamePoints";
  static final String USERNAME = "root";
  static final String PASSWORD = "1BG22CS103@BNMIT";

  public static Connection getConnection() {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");

      Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
      return connection;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public static void close(Connection connection, Statement statement, ResultSet resultSet) {
    try {
      if (resultSet != null)
        resultSet.close();
      if (statement != null)
        statement.close();
      if (connection != null)
        connection.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
