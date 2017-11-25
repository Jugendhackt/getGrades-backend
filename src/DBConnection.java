import java.sql.*;

public class DBConnection {

    public static void main(String[] args) {
        new DBConnection();
    }

    private DBConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://10.1.1.56:3306/notenverwaltung", "notenadmin", "jugendhackt");
            Statement statement = connection.createStatement();

            printResult(statement.executeQuery("SELECT * FROM `test`"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void printResult(ResultSet set) throws SQLException {
        while (set.next()) {
            String text = set.getString("text");
            System.out.println(text);
        }
    }
}
