import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
        map.put("Collin", "Alpert");
        System.out.println(map.get("Hallo") == null);
    }
}