
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

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

                JSONObject json = new JSONObject(response.toString());
                int year = getYear(json);
                String date = getDate(json);
                String time = getTime(json);
                String dayOfWeek = getDayOfWeek(json);

                System.out.println("現在の日時情報（東京）");
                System.out.println("年: " + year);
                System.out.println("日付: " + date);
                System.out.println("時間: " + time);
                System.out.println("曜日: " + dayOfWeek);
            } else {
                System.out.println("APIリクエスト失敗: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 年を取得
    private static int getYear(JSONObject json) {
        return json.getInt("year");
    }

    // 日付を取得（例: 07月10日）
    private static String getDate(JSONObject json) {
        int month = json.getInt("month");
        int day = json.getInt("day");
        return String.format("%02d月%02d日", month, day);
    }

    // 時間を取得（例: 14時23分45秒）
    private static String getTime(JSONObject json) {
        int hour = json.getInt("hour");
        int minute = json.getInt("minute");
        int seconds = json.getInt("seconds");
        return String.format("%02d時%02d分%02d秒", hour, minute, seconds);
    }

    // 曜日を日本語で取得
    private static String getDayOfWeek(JSONObject json) {
        String dayOfWeek = json.getString("dayOfWeek");
        switch (dayOfWeek) {
            case "Monday":
                return "月曜日";
            case "Tuesday":
                return "火曜日";
            case "Wednesday":
                return "水曜日";
            case "Thursday":
                return "木曜日";
            case "Friday":
                return "金曜日";
            case "Saturday":
                return "土曜日";
            case "Sunday":
                return "日曜日";
            default:
                return dayOfWeek;
        }
    }
}
