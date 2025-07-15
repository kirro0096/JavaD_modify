package kuizu;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Scanner;

public class kuizu {
    private static final HttpClient client = HttpClient.newHttpClient();

    // MyMemory APIで英語→日本語翻訳
    public static String translateToJapanese(String text) throws Exception {
        String encodedQuery = java.net.URLEncoder.encode(text, "UTF-8");
        String langpair = java.net.URLEncoder.encode("en|ja", "UTF-8");
        String apiUrl = "https://api.mymemory.translated.net/get?q=" + encodedQuery + "&langpair=" + langpair;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject root = new JSONObject(response.body());
        return root.getJSONObject("responseData").getString("translatedText");
    }

    // Open Trivia Database APIからクイズを1問取得
    private static JSONObject getQuizFromApi(String difficulty) throws Exception {
        String apiUrl = "https://opentdb.com/api.php?amount=1&type=multiple&difficulty=" + difficulty;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return new JSONObject(response.body());
    }

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in, "UTF-8");
        System.out.println("難易度を選んでください:");
        System.out.println("1: easy");
        System.out.println("2: medium");
        System.out.println("3: hard");
        System.out.print("番号で選択してください: ");
        String diffInput = scanner.nextLine().trim();
        String difficulty = "easy";
        switch (diffInput) {
            case "1":
                difficulty = "easy";
                break;
            case "2":
                difficulty = "medium";
                break;
            case "3":
                difficulty = "hard";
                break;
            default:
                System.out.println("不正な選択です。1, 2, 3 から選んでください。");
                scanner.close();
                return;
        }
        // Open Trivia Database APIからクイズを1問取得（難易度指定）
        JSONObject root = getQuizFromApi(difficulty);
        if (!root.has("results")) {
            System.out.println("APIレスポンスに'results'がありません: " + root.toString());
            scanner.close();
            return;
        }
        JSONArray results = root.getJSONArray("results");
        if (results.length() == 0) {
            System.out.println("クイズが取得できませんでした。");
            scanner.close();
            return;
        }
        JSONObject quiz = results.getJSONObject(0);
        String questionEn = quiz.getString("question").replaceAll("&quot;", "\"").replaceAll("&#039;", "'");
        String correctEn = quiz.getString("correct_answer").replaceAll("&quot;", "\"").replaceAll("&#039;", "'");
        JSONArray incorrect = quiz.getJSONArray("incorrect_answers");
        String[] choicesEn = new String[incorrect.length() + 1];
        int correctIndex = (int) (Math.random() * (incorrect.length() + 1));
        int idx = 0;
        for (int i = 0; i < choicesEn.length; i++) {
            if (i == correctIndex) {
                choicesEn[i] = correctEn;
            } else {
                choicesEn[i] = incorrect.getString(idx++).replaceAll("&quot;", "\"").replaceAll("&#039;", "'");
            }
        }
        // 英語→日本語に翻訳
        String questionJp = translateToJapanese(questionEn);
        String[] choicesJp = new String[choicesEn.length];
        for (int i = 0; i < choicesEn.length; i++) {
            choicesJp[i] = translateToJapanese(choicesEn[i]);
        }
        System.out.println("【クイズ】");
        System.out.println(questionJp);
        for (int i = 0; i < choicesJp.length; i++) {
            System.out.println((i + 1) + ": " + choicesJp[i]);
        }
        System.out.print("番号で答えてください: ");
        String input = scanner.nextLine().trim();
        int ans = -1;
        try {
            ans = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("数字で入力してください。");
            System.exit(1);
        }
        scanner.close();
        if (ans - 1 == correctIndex) {
            System.out.println("正解！");
        } else {
            System.out.println("不正解。正解は: " + choicesJp[correctIndex]);
        }
    }
}
