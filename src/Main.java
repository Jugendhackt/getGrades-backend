import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        Map<String, Integer> map = new HashMap<>();
        map.put("Collin", 18);
        map.put("Timo", 17);

        System.out.println(map.get("Timo"));
    }
}
