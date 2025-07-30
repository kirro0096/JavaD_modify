package catapi;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class cat {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final String API_KEY = "live_3gH3cPQu0RKIs78EtsIidJ9NH4uzkRJCh6wcDIZsxjIxxkoN0Td4bhbeynOEesQe";

    // TheCatApiから猫の品種リストを取得
    private static JSONArray getCatBreeds() {
        String apiUrl = "https://api.thecatapi.com/v1/breeds";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("x-api-key", API_KEY)
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.err.println("APIリクエストエラー (Breeds): ステータスコード " + response.statusCode());
                return null;
            }
            return new JSONArray(response.body());
        } catch (Exception e) {
            System.err.println("品種リストの取得中にエラーが発生しました: " + e.getMessage());
            return null;
        }
    }

    // TheCatApiから指定された品種の画像情報を取得
    private static JSONArray getCatImageJson(String breedId) {
        String imageApiUrl = "https://api.thecatapi.com/v1/images/search?breed_ids=" + breedId;
        try {
            HttpRequest imgRequest = HttpRequest.newBuilder()
                    .uri(URI.create(imageApiUrl))
                    .header("x-api-key", API_KEY)
                    .build();
            HttpResponse<String> imgResponse = client.send(imgRequest, HttpResponse.BodyHandlers.ofString());
            if (imgResponse.statusCode() != 200) {
                System.err.println("APIリクエストエラー (Image): ステータスコード " + imgResponse.statusCode());
                return null;
            }
            return new JSONArray(imgResponse.body());
        } catch (Exception e) {
            System.err.println("画像情報の取得中にエラーが発生しました: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        Map<String, String> nameToId = new HashMap<>();
        // 英語名→日本語名の対応表（一部例。必要に応じて追加してください）
        Map<String, String> catJp = Map.ofEntries(
                Map.entry("Abyssinian", "アビシニアン"),
                Map.entry("American Bobtail", "アメリカン・ボブテイル"),
                Map.entry("American Curl", "アメリカン・カール"),
                Map.entry("American Shorthair", "アメリカン・ショートヘア"),
                Map.entry("Bengal", "ベンガル"),
                Map.entry("Birman", "ビルマ"),
                Map.entry("Bombay", "ボンベイ"),
                Map.entry("British Shorthair", "ブリティッシュ・ショートヘア"),
                Map.entry("Burmese", "バーミーズ"),
                Map.entry("Chartreux", "シャルトリュー"),
                Map.entry("Devon Rex", "デボンレックス"),
                Map.entry("Egyptian Mau", "エジプシャン・マウ"),
                Map.entry("Exotic Shorthair", "エキゾチック・ショートヘア"),
                Map.entry("Japanese Bobtail", "ジャパニーズ・ボブテイル"),
                Map.entry("Maine Coon", "メインクーン"),
                Map.entry("Manx", "マンクス"),
                Map.entry("Norwegian Forest Cat", "ノルウェージャンフォレストキャット"),
                Map.entry("Oriental", "オリエンタル"),
                Map.entry("Persian", "ペルシャ"),
                Map.entry("Ragdoll", "ラグドール"),
                Map.entry("Russian Blue", "ロシアンブルー"),
                Map.entry("Scottish Fold", "スコティッシュフォールド"),
                Map.entry("Siamese", "シャム"),
                Map.entry("Siberian", "シベリアン"),
                Map.entry("Singapura", "シンガプーラ"),
                Map.entry("Somali", "ソマリ"),
                Map.entry("Sphynx", "スフィンクス"),
                Map.entry("Turkish Angora", "ターキッシュアンゴラ"),
                Map.entry("Turkish Van", "ターキッシュバン"));
        JSONArray arr = getCatBreeds();
        if (arr == null) {
            System.out.println("品種リストの取得に失敗しました。プログラムを終了します。");
            return;
        }
        System.out.println("猫種リスト:");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            if (obj.has("name") && obj.has("id")) {
                String name = obj.getString("name");
                String id = obj.getString("id");
                nameToId.put(name, id);
                // 日本語名がある場合のみ表示
                if (catJp.containsKey(name)) {
                    String jp = catJp.get(name);
                    System.out.println("- " + jp);
                }
            }
        }
        System.out.print("画像を見たい猫種名（日本語または英語）を入力してください: ");
        Scanner scanner = new Scanner(System.in,"shift-jis");
        String input = scanner.nextLine().trim();
        // 入力が日本語の場合、英語名に変換
        String breedId = nameToId.get(input);
        String searchInput = input; // JFrameタイトル用にfinal変数へ
        if (breedId == null) {
            // 日本語→英語変換
            String enName = catJp.entrySet().stream()
                    .filter(e -> e.getValue().equals(input))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);
            if (enName != null) {
                breedId = nameToId.get(enName);
                searchInput = enName;
            }
        }
        scanner.close();
        if (breedId == null) {
            System.out.println("その猫種は見つかりませんでした。");
            return;
        }
        JSONArray imgArr = getCatImageJson(breedId);
        if (imgArr == null) {
            System.out.println("画像情報の取得に失敗しました。");
            return;
        }
        if (imgArr.length() > 0 && imgArr.getJSONObject(0).has("url")) {
            String imageUrl = imgArr.getJSONObject(0).getString("url");
            System.out.println("画像URL: " + imageUrl);
            // Swingで画像を表示
            try {
                java.net.URL url = java.net.URI.create(imageUrl).toURL();
                javax.swing.ImageIcon icon = new javax.swing.ImageIcon(url);
                javax.swing.JLabel label = new javax.swing.JLabel(icon);
                javax.swing.JFrame frame = new javax.swing.JFrame(searchInput + " の画像");
                frame.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
                frame.getContentPane().add(label);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            } catch (Exception ex) {
                System.out.println("画像をGUIで表示できませんでした: " + ex.getMessage());
            }
        } else {
            System.out.println("画像が見つかりませんでした。");
        }
    }
}
