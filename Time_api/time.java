import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class time {
    public static void main(String[] args) {
        try {
            String apiUrl = "https://www.timeapi.io/api/Time/current/zone?timeZone=Asia/Tokyo";
            URL url = new URL(apiUrl);
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

                // 結果を見やすく整形して表示
                String json = response.toString();
                // 簡易的に主要項目だけ抽出
                String dateTime = extractValue(json, "dateTime");
                String date = extractValue(json, "date");
                String t = extractValue(json, "time");
                String timeZone = extractValue(json, "timeZone");
                String dayOfWeek = extractValue(json, "dayOfWeek");
                System.out.println("\n--- 現在の日時情報 ---");
                System.out.println("タイムゾーン: " + timeZone);
                System.out.println("日付: " + date);
                System.out.println("曜日: " + dayOfWeek);
                System.out.println("時刻: " + t);
                System.out.println("-------------------\n");
            } else {
                System.out.println("APIリクエスト失敗: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // JSONからキーの値を抜き出す簡易メソッド（厳密なパースではありません）
    public static String extractValue(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);
        if (start == -1) return "(取得失敗)";
        start += pattern.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return "(取得失敗)";
        return json.substring(start, end);
    }
}
