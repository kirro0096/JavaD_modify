import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class inu {
    private static final HttpClient client = HttpClient.newHttpClient();
    // 注: このURLはAPIキーがドメイン名に含まれているように見え、正しくない可能性があります。
    // TheDogAPIを利用する場合、通常は "https://api.thedogapi.com/v1/breeds" のようなURLを使用します。
    private static final String BREEDS_API_URL = "https://api.thedogapi.com/v1/breeds";

    /**
     * APIから犬種のリストを取得します。
     * @return 犬種名のリスト。取得に失敗した場合はnullを返します。
     */
    private static List<String> getDogBreedsFromApi() {
        try {
            HttpRequest breedsRequest = HttpRequest.newBuilder()
                    .uri(URI.create(BREEDS_API_URL))
                    .header("x-api-key", "live_eehrIIKvl4UasDx8GzQLZ3nXAaYMSr0DJ1WWzahs8BDaFyMSCGmiLOtHxqu9zFnc")
                    .build();
            HttpResponse<String> breedsResponse = client.send(breedsRequest, HttpResponse.BodyHandlers.ofString());

            if (breedsResponse.statusCode() != 200) {
                System.err.println("APIリクエストエラー: ステータスコード " + breedsResponse.statusCode());
                return null;
            }

            JSONArray breedsArr = new JSONArray(breedsResponse.body());
            List<String> breeds = new ArrayList<>();
            for (int i = 0; i < breedsArr.length(); i++) {
                JSONObject breedObj = breedsArr.getJSONObject(i);
                if (breedObj.has("name")) {
                    breeds.add(breedObj.getString("name"));
                }
            }
            return breeds;
        } catch (Exception e) {
            System.err.println("犬種リストの取得中にエラーが発生しました: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        List<String> ranking = getDogBreedsFromApi();

        // APIからの取得に失敗した場合、フォールバック用の固定リストを使用
        if (ranking == null || ranking.isEmpty()) {
            System.out.println("APIからの犬種リスト取得に失敗したため、固定のリストを使用します。");
            ranking = Arrays.asList(
                    "Toy Poodle", "Chihuahua", "Dachshund", "Shiba Inu", "Pomeranian", "Miniature Schnauzer",
                    "Yorkshire Terrier", "Shih Tzu", "Papillon", "French Bulldog",
                    "Maltese", "Corgi", "Golden Retriever", "Labrador Retriever", "Beagle", "Pekingese",
                    "Miniature Pinscher", "Border Collie", "Pug", "Boston Terrier",
                    "Cavalier King Charles Spaniel", "Shetland Sheepdog", "Italian Greyhound", "Jack Russell Terrier",
                    "Bulldog", "Siberian Husky", "Akita", "Samoyed", "Welsh Corgi Pembroke");
        }
        // 犬種リストが空でないことを確認
        if (ranking.isEmpty()) {
            System.out.println("犬種リストが空です。");
            return;
        }
        // ランダムに犬種を選ぶ
        Random rand = new Random();
        int idx = rand.nextInt(ranking.size());

        Map<String, String> breedJp = Map.ofEntries(
                Map.entry("Toy Poodle", "トイ・プードル"),
                Map.entry("Chihuahua", "チワワ"),
                Map.entry("Dachshund", "ダックスフンド"),
                Map.entry("Shiba Inu", "柴犬"),
                Map.entry("Pomeranian", "ポメラニアン"),
                Map.entry("Miniature Schnauzer", "ミニチュア・シュナウザー"),
                Map.entry("Yorkshire Terrier", "ヨークシャー・テリア"),
                Map.entry("Shih Tzu", "シーズー"),
                Map.entry("Papillon", "パピヨン"),
                Map.entry("French Bulldog", "フレンチ・ブルドッグ"),
                Map.entry("Maltese", "マルチーズ"),
                Map.entry("Corgi", "コーギー"),
                Map.entry("Golden Retriever", "ゴールデン・レトリバー"),
                Map.entry("Labrador Retriever", "ラブラドール・レトリバー"),
                Map.entry("Beagle", "ビーグル"),
                Map.entry("Pekingese", "ペキニーズ"),
                Map.entry("Miniature Pinscher", "ミニチュア・ピンシャー"),
                Map.entry("Border Collie", "ボーダー・コリー"),
                Map.entry("Pug", "パグ"),
                Map.entry("Boston Terrier", "ボストン・テリア"),
                Map.entry("Cavalier King Charles Spaniel", "キャバリア・キング・チャールズ・スパニエル"),
                Map.entry("Shetland Sheepdog", "シェットランド・シープドッグ"),
                Map.entry("Italian Greyhound", "イタリアン・グレーハウンド"),
                Map.entry("Jack Russell Terrier", "ジャック・ラッセル・テリア"),
                Map.entry("Bulldog", "ブルドッグ"),
                Map.entry("Siberian Husky", "シベリアンハスキー"),
                Map.entry("Akita", "秋田犬"),
                Map.entry("Samoyed", "サモエド"),
                Map.entry("Welsh Corgi Pembroke", "ウェルシュ・コーギー・ペンブローク"));
        // 英語名を取得
        String breedEn = ranking.get(idx);
        // 日本語名を取得、存在しない場合は英語名を使用
        String breedJpName = breedJp.getOrDefault(breedEn, breedEn + "（日本語名なし）");
        System.out.println("犬種: " + breedJpName + " (英語名: " + breedEn + ")");

         System.out.print("この犬種は人気ランキングで何位でしょうか？（数字で答えてください）: ");
         Scanner scanner = new Scanner(System.in);
         String answer = scanner.nextLine().trim();
         int correct = idx + 1; // 正しい順位はリストのインデックス+1
         try {
             int ans = Integer.parseInt(answer);
             System.out.println(ans == correct ? "正解！" : "不正解！正解は " + correct + " 位です。");
         } catch (NumberFormatException e) {
             System.out.println("数字で答えてください。正解は " + correct + " 位です。");
         } finally {
             scanner.close();
         }
    }
}
