import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;

public class Yotube {
    // 利用API https://www.googleapis.com/youtube/v3/search
    private static final String API_KEY = "AIzaSyDygHjcMIilSCixVEHEmuwHRaO-iPkLLEQ";
    private static final String SEARCH_URL = "https://www.googleapis.com/youtube/v3/search";

    public static void main(String[] args) {
        String query = inputKeyword();
        List<JSONObject> videoSnippets = new ArrayList<>();
        List<String> videoIds = searchYoutube(query, videoSnippets);
        if (videoIds.isEmpty()) {
            System.out.println("該当する動画がありませんでした。");
            return;
        }
        java.util.Map<String, Long> viewCountMap = getViewCounts(videoIds);
        printSortedVideos(videoSnippets, viewCountMap);
    }

    // キーワード入力
    private static String inputKeyword() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("検索キーワードを入力してください: ");
        String query = scanner.nextLine();
        scanner.close();
        return query;
    }

    // YouTube検索API呼び出し
    private static List<String> searchYoutube(String query, List<JSONObject> videoSnippets) {
        List<String> videoIds = new ArrayList<>();
        try {
            String charset = "UTF-8";
            String q = URLEncoder.encode(query, charset);
            String urlStr = SEARCH_URL + "?part=snippet&type=video&maxResults=5&q=" + q + "&type=video&key=" + API_KEY;
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

            JSONObject json = new JSONObject(response.toString());
            JSONArray items = json.getJSONArray("items");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                if (!item.has("id"))
                    continue;
                JSONObject id = item.getJSONObject("id");
                if (!id.has("videoId"))
                    continue;
                String videoId = id.getString("videoId");
                videoIds.add(videoId);
                JSONObject snippet = item.getJSONObject("snippet");
                JSONObject snippetWithId = new JSONObject(snippet.toString());
                snippetWithId.put("videoId", videoId);
                videoSnippets.add(snippetWithId);
            }
        } catch (Exception e) {
            System.out.println("検索APIエラー: " + e.getMessage());
            e.printStackTrace();
        }
        return videoIds;
    }

    // 再生回数取得
    private static java.util.Map<String, Long> getViewCounts(List<String> videoIds) {
        java.util.Map<String, Long> viewCountMap = new java.util.HashMap<>();
        try {
            String charset = "UTF-8";
            String ids = String.join(",", videoIds);
            String videoUrlStr = "https://www.googleapis.com/youtube/v3/videos?part=statistics&id=" + ids + "&key="
                    + API_KEY;
            URL videoUrl = new URL(videoUrlStr);
            HttpURLConnection videoConn = (HttpURLConnection) videoUrl.openConnection();
            videoConn.setRequestMethod("GET");
            videoConn.setRequestProperty("Accept-Charset", charset);
            BufferedReader videoIn = new BufferedReader(new InputStreamReader(videoConn.getInputStream()));
            String inputLine;
            StringBuilder videoResponse = new StringBuilder();
            while ((inputLine = videoIn.readLine()) != null) {
                videoResponse.append(inputLine);
            }
            videoIn.close();
            JSONObject videoJson = new JSONObject(videoResponse.toString());
            JSONArray videoItems = videoJson.getJSONArray("items");
            for (int i = 0; i < videoItems.length(); i++) {
                JSONObject vitem = videoItems.getJSONObject(i);
                String vid = vitem.getString("id");
                long viewCount = vitem.getJSONObject("statistics").getLong("viewCount");
                viewCountMap.put(vid, viewCount);
            }
        } catch (Exception e) {
            System.out.println("再生回数取得エラー: " + e.getMessage());
            e.printStackTrace();
        }
        return viewCountMap;
    }

    // 再生回数順に表示
    private static void printSortedVideos(List<JSONObject> videoSnippets, java.util.Map<String, Long> viewCountMap) {
        List<JSONObject> sorted = videoSnippets.stream()
                .sorted(Comparator.comparingLong(s -> -viewCountMap.getOrDefault(s.getString("videoId"), 0L)))
                .collect(java.util.stream.Collectors.toList());
        for (JSONObject snippet : sorted) {
            String videoId = snippet.getString("videoId");
            String title = snippet.getString("title");
            long viewCount = viewCountMap.getOrDefault(videoId, 0L);
            System.out.println("タイトル: " + title);
            System.out.println("URL: https://www.youtube.com/watch?v=" + videoId);
            System.out.println("再生回数: " + viewCount);
            System.out.println("----------------------");
        }
    }
}
