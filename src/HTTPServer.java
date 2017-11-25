import com.sun.net.httpserver.*;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

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
            String query = exchange.getRequestURI().getQuery();
            write(query, exchange);

        }
    }

    private static class LoginHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            write("Hallo Welt! Sie sind eingeloggt", exchange);
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
				String[] info = new String(Files.readAllBytes(Paths.get("res/password.txt"))).split(";");
	
				String studentId = queryToMap(exchange.getRequestURI().getQuery()).get("studentId");
				System.out.println(studentId);
				try {
					Connection connection = DriverManager.getConnection("jdbc:mysql://" + info[0] + ":3306/notenverwaltung", info[1], info[2]);
					
					//SQL Query
					PreparedStatement statement = connection.prepareStatement("SELECT val, testId FROM grades WHERE studentId=?");
					statement.setString(1, studentId);
					ResultSet resultSet = statement.executeQuery();
					
					//Reult to JSONObject
					JSONObject grades = new JSONObject();
					while (resultSet.next()) {
						grades.put(resultSet.getString("testId"), resultSet.getInt("val"));
					}
					
					//Result to Map
					HashMap<String, Integer> vals = new HashMap<>();
					while (resultSet.next()) {
						vals.put(resultSet.getString("testId"), resultSet.getInt("val"));
					}
					
					//Structure: TestId, Note vom Test
					write(grades.toJSONString(), exchange);
				} catch (SQLException e) {
					e.printStackTrace();
				}
	
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
