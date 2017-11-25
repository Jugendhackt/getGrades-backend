import org.json.simple.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.HashMap;

public class DBConnection {

    private final String infoFile = "res/password.txt";

    public static void main(String[] args) {
        new DBConnection();
    }

    private DBConnection() {

        try {
            final String infoFile = "res/password.txt";
            String[] info = new String(Files.readAllBytes(Paths.get(infoFile))).split(";");
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://10.23.41.229:3306/notenverwaltung", "notenadmin", info[2]);

            String schuelerId = "1";

            PreparedStatement statement = connection.prepareStatement("SELECT subjects.name FROM users\n" +
                    "LEFT JOIN relationshipsStudents ON users.id= relationshipsStudents.studentID\n" +
                    "LEFT JOIN relationshipsTeacher ON relationshipsStudents.classID = relationshipsTeacher.klassenId\n" +
                    "LEFT JOIN subjects ON relationshipsTeacher.fachId = subjects.id\n" +
                    "WHERE relationshipsStudents.studentID = ?");
            statement.setString(1, schuelerId);
            ResultSet resultSet = statement.executeQuery();



            printResult(resultSet);
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    private void printResult(ResultSet set) throws SQLException {
        while (set.next()) {
            String text = set.getString("name");
            System.out.println(text);
        }
    }
}
