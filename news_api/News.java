package news_api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class News {
    // NewsAPIのAPIキー
    private static final String API_KEY = "09ceb912c9264c15947a0c518360e3d0";

    public static void main(String[] args) {
        System.out.println("今日の主要ニュース:");
        String json = fetchTopHeadlines();
        if (json == null || !hasArticles(json)) {
            // top-headlinesで記事がなければeverythingで日本語ニュースを取得
            json = fetchLatestJapaneseNews();
            if (json == null || !hasArticles(json)) {
                // NewsAPIでも主要ニュースがなければYahoo!ニュースRSSを使う
                System.out.println("NewsAPIで主要ニュースが取得できませんでした。Yahoo!ニュースから取得します。");
                // Yahoo!ニュースRSS取得・表示処理をここに直接記述
                try {
                    String rssUrl = "https://news.yahoo.co.jp/rss/topics/top-picks.xml";
                    URL url = new URL(rssUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                        StringBuilder xml = new StringBuilder();
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            xml.append(inputLine);
                        }
                        in.close();
                        // XMLから主要ニュース5件を表示
                        try {
                            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory
                                    .newInstance();
                            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
                            org.w3c.dom.Document doc = builder
                                    .parse(new java.io.ByteArrayInputStream(xml.toString().getBytes("UTF-8")));
                            org.w3c.dom.NodeList items = doc.getElementsByTagName("item");
                            int count = Math.min(5, items.getLength());
                            for (int i = 0; i < count; i++) {
                                org.w3c.dom.Element item = (org.w3c.dom.Element) items.item(i);
                                String title = item.getElementsByTagName("title").item(0).getTextContent();
                                String link = item.getElementsByTagName("link").item(0).getTextContent();
                                System.out.println("\n【Yahoo! " + (i + 1) + "】" + title);
                                System.out.println("URL: " + link);
                            }
                        } catch (Exception e) {
                            System.out.println("Yahoo!ニュースRSSの解析に失敗しました: " + e.getMessage());
                        }
                        return;
                    }
                } catch (Exception e) {
                    System.out.println("Yahoo!ニュースRSSの取得に失敗しました: " + e.getMessage());
                }
                System.out.println("主要ニュースがありません。");
                return;
            }
        }
        printNewsSummary(json);

    }

    private static String fetchLatestJapaneseNews() {
        try {
            String urlStr = "https://newsapi.org/v2/everything?language=ja&sortBy=publishedAt&pageSize=5&apiKey="
                    + API_KEY;
            // Java 20以降のURL(String)非推奨対応
            URL url = new URL(urlStr); // Java 21以降でも警告のみで動作可。警告を許容する場合はこちらが簡単です。
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            }
        } catch (Exception e) {
            System.out.println("エラー: " + e.getMessage());
        }
        return null;
    }

    // 記事が1件以上あるか判定
    private static boolean hasArticles(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            JSONArray articles = obj.optJSONArray("articles");
            return articles != null && articles.length() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ニュースAPIからトップヘッドラインを取得
    private static String fetchTopHeadlines() {
        try {
            // NewsAPI無料枠（Developerプラン）は24時間遅延のニュースのみ取得可能
            // そのため、"top-headlines"エンドポイントで日本の最新ニュース（24時間以内）を取得
            String urlStr = "https://newsapi.org/v2/top-headlines?country=jp&language=ja&pageSize=5&apiKey=" + API_KEY;
            // Java 20以降のURL(String)非推奨対応
            URL url = java.net.URI.create(urlStr).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            }
        } catch (Exception e) {
            System.out.println("エラー: " + e.getMessage());
        }
        return null;
    }

    // ニュースの要約を表示
    private static void printNewsSummary(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            JSONArray articles = obj.getJSONArray("articles");
            for (int i = 0; i < Math.min(5, articles.length()); i++) {
                JSONObject article = articles.getJSONObject(i);
                String title = article.optString("title", "(タイトルなし)");
                String desc = article.optString("description", "(説明なし)");
                String url = article.optString("url", "");
                System.out.println("\n【" + (i + 1) + "】" + title);
                System.out.println(desc);
                if (!url.isEmpty()) {
                    System.out.println("URL: " + url);
                }
            }
        } catch (Exception e) {
            System.out.println("ニュースの解析に失敗しました: " + e.getMessage());
        }
    }
}
