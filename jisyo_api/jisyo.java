package jisyo_api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class jisyo {
    public static void main(String[] args) {
        String word = null;
        if (args.length == 0) {
            System.out.print("検索したい英単語を入力してください: ");
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                word = reader.readLine();
                if (word == null || word.trim().isEmpty()) {
                    System.out.println("単語が入力されませんでした。");
                    return;
                }
            } catch (Exception e) {
                System.out.println("入力エラー: " + e.getMessage());
                return;
            }
        } else {
            word = args[0];
        }
        String apiUrl = "https://api.dictionaryapi.dev/api/v2/entries/en/" + word;
        try {
            URL url = new URL(apiUrl);
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
                printDictionaryResult(response.toString());
            } else {
                System.out.println("単語が見つかりませんでした。");
            }
        } catch (Exception e) {
            System.out.println("エラー: " + e.getMessage());
        }
    }

    private static void printDictionaryResult(String json) {
        try {
            JSONArray arr = new JSONArray(json);
            JSONObject obj = arr.getJSONObject(0);
            String word = obj.getString("word");
            System.out.println("\n【単語】" + word);
            JSONArray meanings = obj.getJSONArray("meanings");
            for (int i = 0; i < meanings.length(); i++) {
                JSONObject meaning = meanings.getJSONObject(i);
                String partOfSpeech = meaning.getString("partOfSpeech");
                JSONArray definitions = meaning.getJSONArray("definitions");
                // 品詞を日本語で表示
                System.out.println("\n【品詞】(" + partOfSpeech + ") " + getJapanesePartOfSpeech(partOfSpeech));
                for (int j = 0; j < definitions.length(); j++) {
                    JSONObject def = definitions.getJSONObject(j);
                    String definition = def.getString("definition");
                    String jaDefinition = translateToJapanese(definition);
                    System.out.println("【意味】" + jaDefinition);
                    if (def.has("example")) {
                        System.out.println("【例文】" + def.getString("example"));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("辞書データの解析に失敗しました: " + e.getMessage());
        }
    }

    // Google翻訳Web APIで英語→日本語に変換
    private static String translateToJapanese(String text) {
        try {
            String urlStr = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=ja&dt=t&q="
                    + java.net.URLEncoder.encode(text, "UTF-8");
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                // 結果は [[[['日本語訳','原文',...]],...],...]
                JSONArray arr = new JSONArray(response.toString());
                return arr.getJSONArray(0).getJSONArray(0).getString(0);
            }
        } catch (Exception e) {
            // 失敗時は英語のまま返す
        }
        return text;
    }

    // 英語の品詞を日本語に変換
    private static String getJapanesePartOfSpeech(String pos) {
        switch (pos) {
            case "noun":
                return "名詞";
            case "verb":
                return "動詞";
            case "adjective":
                return "形容詞";
            case "adverb":
                return "副詞";
            case "pronoun":
                return "代名詞";
            case "preposition":
                return "前置詞";
            case "conjunction":
                return "接続詞";
            case "interjection":
                return "間投詞";
            case "determiner":
                return "限定詞";
            case "exclamation":
                return "感嘆詞";
            default:
                return "";
        }
    }
}
