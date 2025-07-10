


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

                // 結果を表示
                System.out.println("APIから取得した日時情報: " + response.toString());
            } else {
                System.out.println("APIリクエスト失敗: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
