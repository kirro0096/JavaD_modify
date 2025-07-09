package shokuzaiapi;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.JSONArray;
import org.json.JSONObject;

public class shokuhinn {
    public static void main(String[] args) throws Exception {
        String apiKey = "FIdgDMV4WO56JFzMaz7smx7Jk2UkBpElC3La9gv0";
        String apiUrlBase = "https://api.nal.usda.gov/fdc/v1/foods/search";

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
        System.out.print("成分を調べたい食べ物名を入力してください: ");
        String foodNameJp = reader.readLine().trim();
        reader.close();

        // MyMemory Translation APIで日本語→英語に変換
        // 入力値がローマ字の場合はそのまま、ひらがな・カタカナ・漢字の場合もUTF-8でAPIに渡す
        String encodedQuery = java.net.URLEncoder.encode(foodNameJp, "UTF-8");
        String langpair = java.net.URLEncoder.encode("ja|en", "UTF-8");
        String translateApiUrl = "https://api.mymemory.translated.net/get?q=" + encodedQuery + "&langpair=" + langpair;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest translateRequest = HttpRequest.newBuilder()
                .uri(URI.create(translateApiUrl))
                .build();
        String foodName;
        try {
            HttpResponse<String> translateResponse = client.send(translateRequest,
                    HttpResponse.BodyHandlers.ofString());
            JSONObject transRoot = new JSONObject(translateResponse.body());
            foodName = transRoot.getJSONObject("responseData").getString("translatedText");
            // もし翻訳結果がローマ字（例: "ringo"）なら、再度日本語で翻訳を試みる
            if (foodName.matches("[a-zA-Z\\s]+") && !foodName.equalsIgnoreCase(foodNameJp)) {
                // 日本語→英語の再翻訳
                String jpEncoded = java.net.URLEncoder.encode(foodNameJp, "UTF-8");
                String jpTranslateApiUrl = "https://api.mymemory.translated.net/get?q=" + jpEncoded + "&langpair=ja|en";
                HttpRequest jpTranslateRequest = HttpRequest.newBuilder()
                        .uri(URI.create(jpTranslateApiUrl))
                        .build();
                HttpResponse<String> jpTranslateResponse = client.send(jpTranslateRequest,
                        HttpResponse.BodyHandlers.ofString());
                JSONObject jpTransRoot = new JSONObject(jpTranslateResponse.body());
                String jpFoodName = jpTransRoot.getJSONObject("responseData").getString("translatedText");
                if (!jpFoodName.matches("[a-zA-Z\\s]+")) {
                    foodName = jpFoodName;
                }
            }
            System.out.println("翻訳結果: " + foodNameJp + " → " + foodName);
        } catch (Exception e) {
            System.out.println("翻訳APIエラー: " + e.getMessage());
            foodName = foodNameJp; // 翻訳失敗時は日本語のまま
        }

        String apiUrl = apiUrlBase + "?query=" + java.net.URLEncoder.encode(foodName, "UTF-8") + "&api_key=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject root = new JSONObject(response.body());
            JSONArray foods = root.optJSONArray("foods");
            if (foods == null || foods.length() == 0) {
                System.out.println("該当する食べ物が見つかりませんでした。");
                return;
            }
            JSONObject food = foods.getJSONObject(0);
            System.out.println("【" + foodName + "】の主な成分:");
            if (food.has("foodNutrients")) {
                JSONArray nutrients = food.getJSONArray("foodNutrients");
                // 英語→日本語の栄養素名対応表
                java.util.Map<String, String> nutrientJp = java.util.Map.ofEntries(
                        java.util.Map.entry("Calcium, Ca", "カルシウム"),
                        java.util.Map.entry("Iron, Fe", "鉄"),
                        java.util.Map.entry("Sodium, Na", "ナトリウム"),
                        java.util.Map.entry("Vitamin A, IU", "ビタミンA"),
                        java.util.Map.entry("Vitamin C, total ascorbic acid", "ビタミンC"),
                        java.util.Map.entry("Cholesterol", "コレステロール"),
                        java.util.Map.entry("Fatty acids, total saturated", "飽和脂肪酸"),
                        java.util.Map.entry("Protein", "たんぱく質"),
                        java.util.Map.entry("Carbohydrate, by difference", "炭水化物"),
                        java.util.Map.entry("Energy", "エネルギー"),
                        java.util.Map.entry("Total Sugars", "糖質"),
                        java.util.Map.entry("Fiber, total dietary", "食物繊維"),
                        java.util.Map.entry("Potassium, K", "カリウム"),
                        java.util.Map.entry("Fatty acids, total trans", "トランス脂肪酸"),
                        java.util.Map.entry("Total lipid (fat)", "脂質"));
                // 10件まで表示
                int displayCount = 0;
                for (int i = 0; i < nutrients.length(); i++) {
                    if (displayCount >= 10)
                        break;
                    JSONObject nut = nutrients.getJSONObject(i);
                    String nameEn = nut.optString("nutrientName", "不明");
                    String name = nutrientJp.getOrDefault(nameEn, nameEn); // 日本語があれば日本語、なければ英語
                    String amount = nut.opt("value") != null ? nut.get("value").toString() : "不明";
                    String unit = nut.optString("unitName", "");
                    System.out.println("- " + name + ": " + amount + " " + unit);
                    displayCount++;
                }
            } else {
                System.out.println("成分情報が見つかりませんでした。");
            }
        } catch (Exception e) {
            System.out.println("API取得エラー: " + e.getMessage());
        }
    }
}
