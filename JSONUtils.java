import java.io.BufferedReader;
import java.io.FileReader;

import org.json.JSONObject;
import java.io.FileWriter;

public class JSONUtils {
    // INPUTS: file_path - a string representing the path to the file
    // OUTPUTS: a JSONObject representing the contents of the file
    // DESCRIPTION: reads the contents of the file and returns a JSONObject
    static JSONObject readJSONFile(String jsonFilePath) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(jsonFilePath));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            return new JSONObject(sb.toString());
        } catch (Exception e) {
            System.out.println("Error: " + e);
            return null;
        }
    }

    static void createJSONFile(String jsonFilePath, JSONObject json) {
        try {
            FileWriter file = new FileWriter(jsonFilePath);
            file.write(json.toString());
            file.flush();
            file.close();
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

    static Boolean jsonFileExists(String jsonFilePath) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(jsonFilePath));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    static void updateJSONFile(String jsonFilePath, JSONObject json) {
        try {
            JSONObject oldJSON = readJSONFile(jsonFilePath);
            for (String key : json.keySet()) {
                oldJSON.put(key, json.get(key));
            }
            createJSONFile(jsonFilePath, oldJSON);
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }
}