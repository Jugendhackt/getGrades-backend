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
            Connection connection = DriverManager.getConnection("jdbc:mysql://" + info[0] + ":3306/notenverwaltung", info[1], info[2]);
            Statement statement = connection.createStatement();
            ResultSet set = statement.executeQuery("SELECT `text` FROM `test`");
            printResult(set);
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
