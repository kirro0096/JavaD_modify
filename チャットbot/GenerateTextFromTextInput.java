import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class GenerateTextFromTextInput {
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=AIzaSyDVbUqp2N_iuPzlJnuc_CFtb0cWubUComk";

    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("あなた: ");
            String userMessage = reader.readLine();
            if (userMessage == null || userMessage.trim().equals("exit") || userMessage.trim().equals("quit")) {
                System.out.println("終了します。");
                break;
            }
            String response = chatWithBot(userMessage);
            System.out.println("Bot: " + response);
        }
    }

    public static String chatWithBot(String userMessage) throws Exception {
        String prompt = userMessage + "\n必ず日本語で答えてください。";
        String jsonInput = "{\"contents\":[{\"role\":\"user\",\"parts\":[{\"text\":\"" + prompt.replace("\"", "\\\"")
                + "\"}]}]}";
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInput.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        int code = conn.getResponseCode();
        if (code != 200) {
            return "APIエラー: " + code;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
        StringBuilder response = new StringBuilder();
        String responseLine;
        while ((responseLine = br.readLine()) != null) {
            response.append(responseLine.trim());
        }
        String res = response.toString();
        int idx = res.indexOf("\"text\":");
        if (idx != -1) {
            int start = res.indexOf('"', idx + 7) + 1;
            int end = res.indexOf('"', start);
            return res.substring(start, end);
        }
        return "返答がありませんでした。";
    }
}
