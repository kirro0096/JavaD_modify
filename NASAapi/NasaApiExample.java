import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NasaApiExample {
    public static void main(String[] args) {
        String apiKey = "OqUGsfRmjpXxNGwjHwpKnCQjkagy1yk9qs2A6gXP";
        String endpoint = "https://api.nasa.gov/planetary/apod?api_key=" + apiKey;
        try {
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                String json = response.toString();
                // タイトル、説明、画像URLを抽出
                String title = SimpleJsonParser.getValue(json, "title");
                String explanation = SimpleJsonParser.getValue(json, "explanation");
                String urlImg = SimpleJsonParser.getValue(json, "url");
                System.out.println("\n--- NASA APOD ---");
                System.out.println("タイトル: " + title);
                System.out.println("説明: " + explanation);
                System.out.println("画像URL: " + urlImg);
            } else {
                System.out.println("GET request failed. Response Code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
