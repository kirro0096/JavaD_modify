import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class inu {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        // --- ここからAPIで犬種リスト取得 ---
        List<String> ranking = new ArrayList<>();
        try {
            String breedsApiUrl = "https://api.ehrIIKvl4UasDx8GzQLZ3nXAaYMSr0DJ1WWzahs8BDaFyMSCGmiLOtHxqu9zFnc/breeds";
            HttpRequest breedsRequest = HttpRequest.newBuilder()
                    .uri(URI.create(breedsApiUrl))
                    .build();
            HttpResponse<String> breedsResponse = client.send(breedsRequest, HttpResponse.BodyHandlers.ofString());
            JSONArray breedsArr = new JSONArray(breedsResponse.body());
            for (int i = 0; i < breedsArr.length(); i++) {
                JSONObject breedObj = breedsArr.getJSONObject(i);
                if (breedObj.has("name")) {
                    ranking.add(breedObj.getString("name"));
                }
            }
        } catch (Exception e) {
            System.out.println("犬種リスト取得エラー: " + e.getMessage());
            // 取得できなかった場合は従来のリストを使う
            ranking = Arrays.asList(
                    "Toy Poodle", "Chihuahua", "Dachshund", "Shiba Inu", "Pomeranian", "Miniature Schnauzer",
                    "Yorkshire Terrier", "Shih Tzu", "Papillon", "French Bulldog",
                    "Maltese", "Corgi", "Golden Retriever", "Labrador Retriever", "Beagle", "Pekingese",
                    "Miniature Pinscher", "Border Collie", "Pug", "Boston Terrier",
                    "Cavalier King Charles Spaniel", "Shetland Sheepdog", "Italian Greyhound", "Jack Russell Terrier",
                    "Bulldog", "Siberian Husky", "Akita", "Samoyed", "Welsh Corgi Pembroke");
        }
        // --- ここまでAPIで犬種リスト取得 ---

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
        // ランダムに犬種を選ぶ
        Random rand = new Random();
        int idx = rand.nextInt(ranking.size());
        String breedEn = ranking.get(idx);
        String breedJpName = breedJp.get(breedEn);
        System.out.println("犬種: " + breedJpName);

        // --- ここからAPIで画像取得 ---
        // （画像取得処理は不要なので削除）
        // --- ここまでAPIで画像取得 ---

        System.out.print("この犬種は人気ランキングで何位でしょうか？（数字で答えてください）: ");
        Scanner scanner = new Scanner(System.in);
        String answer = scanner.nextLine().trim();
        int correct = idx + 1;
        try {
            int ans = Integer.parseInt(answer);
            if (ans == correct) {
                System.out.println("正解！");
            } else {
                System.out.println("不正解！正解は " + correct + " 位です。");
            }
        } catch (NumberFormatException e) {
            System.out.println("数字で答えてください。正解は " + correct + " 位です。");
        }
        scanner.close();
    }
}
