import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

public class DBConnection {

    private final String infoFile = "res/password.txt";

    public static void main(String[] args) {
        new DBConnection();
    }

    private DBConnection() {
        try {
            String[] info = new String(Files.readAllBytes(Paths.get(infoFile))).split(";");
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://10.23.41.229:3306/notenverwaltung", "notenadmin", info[2]);

            String username = "hans";
            String pwd = "123456789";


            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE users.name = ? AND users.pwd = ?");
            statement.setString(1, username);
            statement.setString(2, pwd);

            ResultSet resultSet = statement.executeQuery();
            Boolean ret = resultSet.next();

            System.out.println(ret);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void printResult(ResultSet set) throws SQLException {
        while (set.next()) {
            String text = set.getString("pwd");
            System.out.println(text);
        }
    }
}
