import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Main {

    private Connection connection = null;
    private final boolean testAsJAR = true;

    public static void main(String[] args) {
        new Main();
    }

    private Main() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String[] info;
            if (testAsJAR) {
                InputStream in = getClass().getResourceAsStream("/password.txt");
                info = new BufferedReader(new InputStreamReader(in)).lines().toArray(String[]::new);
            } else
                info = new String(Files.readAllBytes(Paths.get("res/password.txt"))).split("\n");
            connection = DriverManager.getConnection("jdbc:mysql://" + info[0] + ":3306/notenverwaltung", info[1], info[2]);
            HttpServer server = HttpServer.create(new InetSocketAddress(1337), 0);
            server.createContext("/", new Handler());
            server.createContext("/login", new LoginHandler());
            server.createContext("/newuser", new NewUserHandler());
            server.createContext("/getgrades", new GetGradesHandler());
            server.createContext("/getsubjects", new GetSubjectsHandler());
            server.createContext("/getuserdata", new GetUserDataHandler());
            server.createContext("/getclassdata", new GetClassDataHandler());
            server.createContext("/getclasssubjects", new GetClassSubjectsHandler());
            server.createContext("/updategrades", new UpdateGradesHandler());
            System.out.println("Server wird gestartet...");
            server.setExecutor(null);
            server.start();
            System.out.println("Server ist betriebsbereit");
        } catch (IOException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private class Handler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            write("{\"response\": \"Wohoo!\"}", 200, exchange);
        }
    }



    private class LoginHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            HashMap<String, String> userdata = queryToMap(query);
            String username = userdata.get("username");
            String password = userdata.get("password");

            try {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE email=? AND pwd=?");
                statement.setString(1, username);
                statement.setString(2, hash(password));
                ResultSet resultSet = statement.executeQuery();
                JSONObject responseObject = new JSONObject();
                responseObject.put("name", null);
                responseObject.put("groupId", null);
                responseObject.put("response", false);
                if (nullOrEmpty(username) || nullOrEmpty(password)) {
                    write(responseObject.toJSONString(), 401, exchange);
                } else {
                    while (resultSet.next()){
                        System.out.println("entered");
                        responseObject.put("name", resultSet.getString("name"));
                        responseObject.put("groupId", resultSet.getString("groupId"));
                        responseObject.put("response", true);
                    }
                    write(responseObject.toJSONString(), 200, exchange);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    private class NewUserHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            HashMap<String, String> map = queryToMap(query);
            try {
                PreparedStatement statement = connection.prepareStatement("SELECT email FROM users WHERE email = ?");
                statement.setString(1, map.get("email"));
                ResultSet set = statement.executeQuery();

                String name = map.get("name");
                name = name.replace('+', ' ');
                String password = map.get("password");
                String group = map.get("groupId");
                String email = map.get("email");
                if (exists(set)) {
                    write("{\"error\": \"Dieser Benutzer ist schon vorhanden\"}", 401, exchange);
                } else if (nullOrEmpty(name) || nullOrEmpty(password) || nullOrEmpty(group) || nullOrEmpty(email)) {
                    write("{\"error\": \"Es wurden nicht alle Felder ausgefüllt\"}", 400, exchange);
                } else {
                    PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO users VALUES (DEFAULT, ?, ?, ?, ?)");
                    insertStatement.setString(1, name);
                    insertStatement.setString(2, password);
                    insertStatement.setString(3, group);
                    insertStatement.setString(4, email);
                    insertStatement.executeUpdate();
                    write("{\"result\": \"Der Benutzer mit der E-Mail-Adresse " + map.get("email") + " wird erstellt\"}", 201, exchange);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private class GetGradesHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String studentId = queryToMap(exchange.getRequestURI().getQuery()).get("studentId");
            try {
                //SQL Query
                PreparedStatement statement = connection.prepareStatement("SELECT val, testId FROM grades WHERE studentId=?");
                statement.setString(1, studentId);
                ResultSet resultSet = statement.executeQuery();

                //Result to JSONObject
                JSONObject grades = new JSONObject();
                while (resultSet.next()) {
                    grades.put(resultSet.getString("testId"), resultSet.getInt("val"));
                }

                if (!grades.isEmpty()) {
                    //Structure: TestId, Note vom Test
                    write(grades.toJSONString(), 200, exchange);
                } else {
                    write("{\"response:Nothing to see here\"]", 404, exchange);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }

    private class GetClassSubjectsHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String classId = queryToMap(exchange.getRequestURI().getQuery()).get("classId");
            try {
                //httpquery: ...?classId=class
                //Result to JSONObject
                PreparedStatement statement = connection.prepareStatement("SELECT subjects.name FROM relationshipsTeacher LEFT JOIN subjects ON relationshipsTeacher.fachId = subjects.id WHERE klassenId = ?");
                statement.setString(1, classId);
                ResultSet resultSet = statement.executeQuery();

                //Result to JSONObject
                JSONArray subjects = new JSONArray();
                while (resultSet.next()) {
                    subjects.add(resultSet.getString("name"));
                }

                if (!subjects.isEmpty()) {
                    //Structure: Array mit Fächern der Klasse
                    write(subjects.toJSONString(), 200, exchange);
                } else {
                    write("[\"Nothing to see here\"]", 404, exchange);
                }
            } catch (SQLException sql) {
                sql.printStackTrace();
            }
        }
    }

    private class GetSubjectsHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                PreparedStatement statement = connection.prepareStatement("SELECT name FROM subjects");
                ResultSet set = statement.executeQuery();
                JSONObject obj = new JSONObject();
                JSONArray array = new JSONArray();
                while (set.next()) {
                    array.add(set.getString("name"));
                }
                obj.put("subjects", array);
                write(obj.toJSONString(), 200, exchange);
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }

    private class GetUserDataHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder columnValue = new StringBuilder();

            try {
                Statement statement = connection.createStatement();
                ResultSet set = statement.executeQuery("SELECT name FROM users");

                while (set.next()) {
                    columnValue.append(set.getString(1) + " ");
                }
                if(set.next()){
                    write(columnValue.toString(), 200, exchange);
                } else {
                    write(columnValue.toString(), 401, exchange);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class UpdateGradesHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            HashMap<String, String> data = queryToMap(query);
            String gradeID = data.get("gradeId");
            String teacherID = data.get("teacherId");
            String value = data.get("value");
            JSONObject obj = new JSONObject();
            try {
                PreparedStatement statement = connection.prepareStatement("UPDATE grades " +
                        "LEFT JOIN tests ON grades.testId=tests.Id " +
                        "LEFT JOIN users ON tests.lehrerId = users.id " +
                        "SET grades.val = ? " +
                        "WHERE users.id = ? AND grades.id = ?");
                statement.setString(1, value);
                statement.setString(2, teacherID);
                statement.setString(3, gradeID);
                int result = statement.executeUpdate();

                obj.put("Success", result>0);

                write(obj.toJSONString(), 200, exchange);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }


    }

    private class GetClassDataHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder columnValue = new StringBuilder();

            try {
                Statement statement = connection.createStatement();
                ResultSet set = statement.executeQuery("SELECT name FROM classes");

                while (set.next()) {
                    columnValue.append(set.getString(1) + " ");
                }
                if(set.next()){
                    write(columnValue.toString(), 200, exchange);
                } else {
                    write(columnValue.toString(), 401, exchange);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void write(String text, int responseCode, HttpExchange e) throws IOException {
        e.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        e.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        e.sendResponseHeaders(responseCode, 0);
        OutputStream os = e.getResponseBody();
        os.write(text.getBytes("UTF-8"));
        os.close();
    }

    private HashMap<String, String> queryToMap(String s){
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        String[] e = s.split("&");
        for(String el : e){
            map.put(el.substring(0, el.indexOf("=")), el.substring(el.indexOf("=")+1));
        }
        return map;
    }

    private boolean exists(ResultSet set) {
        try {
            return set.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean nullOrEmpty(String string) {
        return string == null || string.equals("");
    }

    private String hash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] array = digest.digest(text.getBytes("UTF-8"));

            StringBuilder buffer = new StringBuilder();
            for (byte anArray : array)
                buffer.append(Integer.toString((anArray & 0xff) + 0x100, 16).substring(1));
            return buffer.toString();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }
}
