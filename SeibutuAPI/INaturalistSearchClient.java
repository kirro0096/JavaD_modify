import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
// ...existing code...
// EncodingConverterはデフォルトパッケージなのでimport不要

/**
 * iNaturalist APIを使った生物検索CLIサンプル
 * https://api.inaturalist.org/v1/docs/ 参照
 */
public class INaturalistSearchClient {
    private static final String API_URL = "https://api.inaturalist.org/v1/observations";
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    /**
     * JsonObjectから指定キーの値を安全に取得（nullならdefault値）
     */
    private String getStringOrDefault(JsonObject obj, String key, String defaultValue) {
        return (obj.has(key) && !obj.get(key).isJsonNull()) ? obj.get(key).getAsString() : defaultValue;
    }

    /**
     * iNaturalist APIで生物名（和名・英名）から観察データを検索
     * @param query 検索キーワード
     */
    public void searchObservations(String query) throws IOException, InterruptedException {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String requestUrl = API_URL + "?q=" + encodedQuery + "&per_page=5";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            JsonObject obj = gson.fromJson(response.body(), JsonObject.class);
            JsonArray results = obj.getAsJsonArray("results");
            if (results == null || results.size() == 0) {
                System.out.println("該当する観察データが見つかりませんでした。");
                return;
            }
            for (int i = 0; i < results.size(); i++) {
                JsonObject obs = results.get(i).getAsJsonObject();
                String speciesGuess = getStringOrDefault(obs, "species_guess", "-");
                String place = getStringOrDefault(obs, "place_guess", "-");
                String observedOn = getStringOrDefault(obs, "observed_on", "-");
                // 写真URL取得
                String imageUrl = "(画像なし)";
                if (obs.has("photos") && obs.get("photos").isJsonArray()) {
                    JsonArray photos = obs.getAsJsonArray("photos");
                    if (photos.size() > 0) {
                        JsonObject photoObj = photos.get(0).getAsJsonObject();
                        if (photoObj.has("url") && !photoObj.get("url").isJsonNull()) {
                            imageUrl = photoObj.get("url").getAsString();
                        }
                    }
                }
                System.out.printf("\n%d. 種名: %s\n   場所: %s\n   観察日: %s\n   画像: %s\n", 
                (i+1), speciesGuess, place, observedOn, imageUrl);
            }
        } else {
            System.err.println("APIエラー: " + response.statusCode() + ", Body: " + response.body());
        }
    }

    public static void main(String[] args) {
        // Shift_JISで入力を受け取り、UTF-8で検索
        System.out.print("検索したい生物名（Shift_JIS）を入力してください: ");
        Optional<String> sjisInput = EncodingConverter.readLineFromTerminal("MS932");
        if (sjisInput.isEmpty() || sjisInput.get().isBlank()) {
            System.out.println("生物名が入力されていません。");
            return;
        }
        String query = sjisInput.get(); // Java内部表現はUTF-16なのでそのまま検索OK
        INaturalistSearchClient client = new INaturalistSearchClient();
        try {
            client.searchObservations(query);
        } catch (Exception e) {
            System.err.println("検索中にエラーが発生しました。");
            e.printStackTrace();
        }
    }
}
