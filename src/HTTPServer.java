import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class HTTPServer {

    private final static String infoFile = "res/password.txt";

    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(1337), 0);
            server.createContext("/", new Handler());
            server.createContext("/login", new LoginHandler());
            server.createContext("/newuser", new NewUserHandler());
            server.createContext("/getgrades", new GetGradesHandler());
            server.createContext("/getsubjects", new GetSubjectsHandler());
            server.createContext("/getuserdata", new GetUserDataHandler());
            System.out.println("Server wird gestartet...");
            server.setExecutor(null);
            server.start();
            System.out.println("Server ist betriebsbereit");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Handler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            write(query, 200, exchange);

        }
    }

    private static class LoginHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            HashMap<String, String> userdata = queryToMap(query);
            String username = userdata.get("username");
            String password = userdata.get("password");

            JSONObject obj = new JSONObject();

            try {
                final String infoFile = "res/password.txt";
                String[] info = new String(Files.readAllBytes(Paths.get(infoFile))).split(";");
                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = DriverManager.getConnection("jdbc:mysql://10.23.41.229:3306/notenverwaltung", "notenadmin", info[2]);

                PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE users.name = ? AND users.pwd = ?");
                statement.setString(1, username);
                statement.setString(2, password);
                ResultSet resultSet = statement.executeQuery();

                obj.put("username", password);
                obj.put("password", username);
                obj.put("success", resultSet.next());

                write(obj.toJSONString(), 200, exchange);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            }

    }

    private static class NewUserHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            HashMap<String, String> map = queryToMap(query);
            try {
                String[] info = new String(Files.readAllBytes(Paths.get(infoFile))).split(";");
                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = DriverManager.getConnection("jdbc:mysql://" + info[0] + ":3306/notenverwaltung", info[1], info[2]);
                PreparedStatement statement = connection.prepareStatement("SELECT email FROM users WHERE email = ?");
                statement.setString(1, map.get("email"));
                ResultSet set = statement.executeQuery();

                String name = map.get("name");
                String password = map.get("password");
                String group = map.get("groupId");
                String email = map.get("email");
                if (exists(set)) {
                    write("{\"error\": \"Dieser Benutzer ist schon vorhanden\"}", 401, exchange);
                } else if (nullOrEmpty(name) || nullOrEmpty(password) || nullOrEmpty(group) || nullOrEmpty(email)) {
                    write("{\"error\": \"Dieser Benutzer ist schon vorhanden\"}", 400, exchange);
                } else {
                    PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO users VALUES (DEFAULT, ?, ?, ?, ?)");
                    insertStatement.setString(1, name);
                    insertStatement.setString(2, password);
                    insertStatement.setString(3, group);
                    insertStatement.setString(4, email);
                    insertStatement.executeUpdate();
                    write("{\"result\": \"Der Benutzer mit der E-Mail-Adresse " + map.get("email") + " wird erstellt\"}", 201, exchange);
                }
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private static class GetGradesHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

        }
    }

    private static class GetSubjectsHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

        }
    }

    private static class GetUserDataHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

        }
    }

    private static void write(String text, int responseCode, HttpExchange e) throws IOException {
        e.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        e.sendResponseHeaders(responseCode, 0);
        OutputStream os = e.getResponseBody();
        os.write(text.getBytes("UTF-8"));
        os.close();
    }

    private static HashMap<String, String> queryToMap(String s){
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        String[] e = s.split("&");
        for(String el : e){
            map.put(el.substring(0, el.indexOf("=")), el.substring(el.indexOf("=")+1));
        }
        return map;
    }

    private static boolean exists(ResultSet set) {
        try {
            return set.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean nullOrEmpty(String string) {
        return string == null || string.equals("");
    }
}
