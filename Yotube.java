import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

public class Yotube {
    // 利用API https://www.googleapis.com/youtube/v3/search
    private static final String API_KEY = "AIzaSyDygHjcMIilSCixVEHEmuwHRaO-iPkLLEQ";
    private static final String SEARCH_URL = "https://www.googleapis.com/youtube/v3/search";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("検索キーワードを入力してください: ");
        String query = scanner.nextLine();
        scanner.close();
        searchYoutube(query);
    }

    private static void searchYoutube(String query) {
        try {
            String charset = "UTF-8";
            String q = URLEncoder.encode(query, charset);
            String urlStr = SEARCH_URL + "?part=snippet&type=video&maxResults=5&q=" + q + "&key=" + API_KEY;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept-Charset", charset);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // JSONをパースして動画情報を表示
            JSONObject json = new JSONObject(response.toString());
            JSONArray items = json.getJSONArray("items");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                if (!item.has("id"))
                    continue;
                JSONObject id = item.getJSONObject("id");
                if (!id.has("videoId"))
                    continue; // videoIdがなければスキップ
                String videoId = id.getString("videoId");
                JSONObject snippet = item.getJSONObject("snippet");
                String title = snippet.getString("title");
                System.out.println("タイトル: " + title);
                System.out.println("URL: https://www.youtube.com/watch?v=" + videoId);
                System.out.println("----------------------");
            }
        } catch (Exception e) {
            System.out.println("エラー: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
