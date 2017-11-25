import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.simple.JSONObject;
import com.sun.net.httpserver.*;
import org.json.simple.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class HTTPServer {
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
            Headers headers = exchange.getRequestHeaders();
            write("Hallo Welt!", exchange);
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
          StringBuilder columnValue = new StringBuilder();

            try {
                String[] info = new String(Files.readAllBytes(Paths.get("res/passwort.txt"))).split(";");
                Connection connection = DriverManager
                    .getConnection("jdbc:mysql://" + info[0] + ":3306/notenverwaltung", info[1], info[2]);
                Statement statement = connection.createStatement();
                ResultSet set = statement.executeQuery("SELECT name FROM users");
                System.out.println(set);

              while (set.next()) {
                columnValue.append(set.getString(1) + " ");
              }
              if(set.next()){
                write(columnValue, 200, exchange);
              } else {
                write(columnValue, 401, exchange);
              }
              System.out.println(columnValue);
            } catch (Exception e) {
              e.printStackTrace();
            }
        }
    }

    private static void write(String text, HttpExchange e) throws IOException {
        e.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        e.sendResponseHeaders(200, 0);
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

}
