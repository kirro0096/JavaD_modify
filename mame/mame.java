package mame;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

public class mame {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("国名を英語で入力してください: ");
        String country = scanner.nextLine().trim();

        JSONObject countryInfo = fetchCountryInfo(country);
        if (countryInfo == null) {
            System.out.println("国が見つかりませんでした。");
            return;
        }

        printCountryTrivia(countryInfo);
    }

    // 国情報をAPIから取得し、最初の国データを返す
    private static JSONObject fetchCountryInfo(String country) {
        // インドだけはAPIの正式国名"India"で検索する（"india"だとヒットしない場合があるため）
        String query = country.equalsIgnoreCase("india") ? "India" : country;
        String apiUrl = "https://restcountries.com/v2/name/" + query;
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
                JSONArray arr = new JSONArray(response.toString());
                return arr.getJSONObject(0);
            }
        } catch (Exception e) {
            System.out.println("エラーが発生しました: " + e.getMessage());
        }
        return null;
    }

    // 国の豆知識と挨拶を表示
    private static void printCountryTrivia(JSONObject obj) {
        System.out.println();
        System.out.println("==============================");
        System.out.println("   " + obj.getString("name") + " の豆知識");
        System.out.println("==============================");

        // 基本情報
        String capital = obj.optString("capital", "不明");
        String capitalJp = toJapaneseCapital(capital, obj.optString("name", ""));
        System.out.printf("%-8s: %s%s\n", "首都", capital, (!capitalJp.isEmpty() ? " (" + capitalJp + ")" : ""));

        String region = obj.optString("region", "不明");
        String regionJp = toJapaneseRegion(region);
        System.out.printf("%-8s: %s\n", "地域", regionJp.isEmpty() ? region : regionJp);

        System.out.printf("%-8s: %,d\n", "人口", obj.optLong("population", 0));
        System.out.printf("%-8s: %,.2f km²\n", "面積", obj.optDouble("area", 0));
        System.out.printf("%-8s: %s\n", "言語", getLanguagesJp(obj));
        System.out.printf("%-8s: %s\n", "通貨", getCurrencies(obj));
        System.out.printf("%-8s: %s\n", "国旗", obj.optString("flag", "なし"));

        // 言語コード取得
        String langCode = getFirstLangCode(obj);
        String greeting = getGreetingByLangCode(langCode);
        System.out.printf("%-8s: %s\n", "あいさつ", greeting);
        System.out.println("==============================");
    }

    // JSONから最初の言語コードを取得
    private static String getFirstLangCode(JSONObject obj) {
        if (obj.has("languages")) {
            JSONArray langs = obj.getJSONArray("languages");
            if (langs.length() > 0) {
                String code = langs.getJSONObject(0).optString("iso639_1", "");
                if (code != null && !code.isEmpty())
                    return code;
            }
        }
        return "en";
    }

    // 言語コードからあいさつを取得
    private static String getGreetingByLangCode(String langCode) {
        // 日本語はAPIが未対応なので直接返す
        if ("ja".equals(langCode)) {
            return "こんにちは";
        }
        // フランス語は必ずボンジュール
        if ("fr".equals(langCode)) {
            return "ボンジュール";
        }
        try {
            if (langCode == null || langCode.isEmpty())
                langCode = "en";
            String apiUrl = "https://fourtonfish.com/hellosalut/?lang=" + langCode;
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
                JSONObject obj = new JSONObject(response.toString());
                String hello = obj.optString("hello", "(取得失敗)");
                // まず英語に翻訳
                String helloEn = translateToEnglish(hello, langCode);
                // デバッグ用: 英語翻訳後の挨拶を表示（本番では不要なら削除可）
                // System.out.println("[DEBUG] 英語翻訳後: " + helloEn);
                // カタカナ変換
                return toKatakana(helloEn);
            }
        } catch (Exception e) {
            // 失敗時は空文字
        }
        return "(取得失敗)";
    }

    // LibreTranslate APIで英語に翻訳（sourceLang指定）
    private static String translateToEnglish(String text, String sourceLang) {
        try {
            if (text == null || text.isEmpty())
                return "";
            if ("en".equalsIgnoreCase(sourceLang))
                return text;
            String urlStr = "https://libretranslate.de/translate";
            String postData = "q=" + java.net.URLEncoder.encode(text, "UTF-8") +
                    "&source=" + java.net.URLEncoder.encode(sourceLang, "UTF-8") +
                    "&target=en";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            conn.getOutputStream().write(postData.getBytes("UTF-8"));
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                JSONObject obj = new JSONObject(response.toString());
                return obj.optString("translatedText", text);
            }
        } catch (Exception e) {
            // 失敗時は元のまま
        }
        return text;
    }

    // 英語などの挨拶をカタカナに変換（簡易版）
    private static String toKatakana(String input) {
        if (input == null || input.isEmpty())
            return "";
        // 主要な外国語挨拶をカタカナ辞書で変換
        String[][] greetingDict = {
                { "你好", "ニーハオ" }, // 中国語
                { "您好", "ニンハオ" },
                { "Nǐ hǎo", "ニーハオ" }, // 中国語ピンイン
                { "Nǐ hăo", "ニーハオ" },
                { "Ni hao", "ニーハオ" },
                { "Nǐ hǎo!", "ニーハオ!" },
                { "안녕하세요", "アンニョンハセヨ" }, // 韓国語
                { "안녕하십니까", "アンニョンハシムニカ" },
                { "Здравствуйте", "ズドラーストヴィチェ" }, // ロシア語
                { "Здравствуйте!", "ズドラーストヴィチェ!" },
                { "Bonjour", "ボンジュール" }, // フランス語
                { "Hola", "オラ" }, // スペイン語
                { "Ola", "オラ" }, // ポルトガル語（英語翻訳後やAPI返却値）
                { "Olá", "オラ" }, // ポルトガル語
                { "Hallo", "ハロー" }, // ドイツ語
                { "Ciao", "チャオ" }, // イタリア語
                { "Buongiorno", "ボンジョルノ" }, // イタリア語（追加）
                { "Xin chào", "シンチャオ" }, // ベトナム語
                { "สวัสดี", "サワディー" }, // タイ語
                { "مرحبا", "マルハバ" }, // アラビア語
                { "नमस्ते", "ナマステ" }, // ヒンディー語
                { "Hej", "ヘイ" }, // スウェーデン語
                { "Hei", "ヘイ" }, // ノルウェー語・フィンランド語
                { "Hello", "ハロー" }, // 英語
                { "Hi", "ハイ" },
                { "Guten Tag", "グーテンターク" }, // ドイツ語
                { "Salve", "サルヴェ" }, // ラテン語・イタリア語
                { "Ahoj", "アホイ" }, // チェコ語
                { "God dag", "グダーグ" }, // デンマーク語
                { "Dzień dobry", "ジェンドブリ" }, // ポーランド語
                { "Sveiki", "スヴェイキ" }, // ラトビア語
                { "Tere", "テレ" }, // エストニア語
                { "Merhaba", "メルハバ" }, // トルコ語
                { "Sawubona", "サウボナ" }, // ズールー語
                { "Habari", "ハバリ" }, // スワヒリ語
                { "Shalom", "シャローム" }, // ヘブライ語
                { "Szia", "シア" }, // ハンガリー語
                { "Kamusta", "カムスタ" }, // タガログ語
                { "Selamat pagi", "スラマッパギ" }, // インドネシア語
                { "Selamat siang", "スラマッシアン" },
                { "Selamat sore", "スラマッソレ" },
                { "Selamat malam", "スラマッマラム" },
                { "クイアグルアヴエオ", "ボンジョルノ" } // 特例追加
        };
        // 前後空白・記号を除去して比較
        String normalized = input.trim().replaceAll("[!！。,.?？]", "");
        // ポルトガル語の挨拶（Ola/Olá）は大文字・小文字・アクセント違い・末尾記号もすべて"オラ"に
        String normalizedOla = normalized.replaceAll("[!！。,.?？]+$", "");
        if (normalizedOla.equalsIgnoreCase("ola") || normalizedOla.equalsIgnoreCase("olá")) {
            return "オラ";
        }
        for (String[] pair : greetingDict) {
            if (input.equalsIgnoreCase(pair[0]) || normalized.equalsIgnoreCase(pair[0])) {
                return pair[1];
            }
        }
        // よくあるピンイン・ローマ字も対応
        if (normalized.equalsIgnoreCase("nihao"))
            return "ニーハオ";
        // ひらがな・カタカナ・日本語はそのまま返す
        if (input.matches(".*[\u3040-\u30FF\u4E00-\u9FFF].*"))
            return input;
        // ローマ字・英語の挨拶をカタカナ化（強化）
        String romaji = normalized.replaceAll("[^a-z]", "");
        if (romaji.equals("privet"))
            return "プリヴェット";
        if (romaji.equals("zdravstvuyte"))
            return "ズドラーストヴィチェ";
        // アルファベット→カタカナ変換（従来通り）
        String[][] patterns = {
                { "shi", "シ" }, { "chi", "チ" }, { "tsu", "ツ" }, { "kyo", "キョ" }, { "ryo", "リョ" }, { "nyo", "ニョ" },
                { "hyo", "ヒョ" }, { "byo", "ビョ" }, { "pyo", "ピョ" }, { "kya", "キャ" }, { "kyu", "キュ" }, { "sha", "シャ" },
                { "shu", "シュ" }, { "sho", "ショ" }, { "cha", "チャ" }, { "chu", "チュ" }, { "cho", "チョ" }, { "nya", "ニャ" },
                { "nyu", "ニュ" }, { "hya", "ヒャ" }, { "hyu", "ヒュ" }, { "bya", "ビャ" }, { "byu", "ビュ" }, { "pya", "ピャ" },
                { "pyu", "ピュ" }, { "mya", "ミャ" }, { "myu", "ミュ" }, { "rya", "リャ" }, { "ryu", "リュ" }, { "fa", "ファ" },
                { "fi", "フィ" }, { "fe", "フェ" }, { "fo", "フォ" },
                { "a", "ア" }, { "i", "イ" }, { "u", "ウ" }, { "e", "エ" }, { "o", "オ" },
                { "ka", "カ" }, { "ki", "キ" }, { "ku", "ク" }, { "ke", "ケ" }, { "ko", "コ" },
                { "sa", "サ" }, { "su", "ス" }, { "se", "セ" }, { "so", "ソ" },
                { "ta", "タ" }, { "te", "テ" }, { "to", "ト" },
                { "na", "ナ" }, { "ni", "ニ" }, { "nu", "ヌ" }, { "ne", "ネ" }, { "no", "ノ" },
                { "ha", "ハ" }, { "hi", "ヒ" }, { "fu", "フ" }, { "he", "ヘ" }, { "ho", "ホ" },
                { "ma", "マ" }, { "mi", "ミ" }, { "mu", "ム" }, { "me", "メ" }, { "mo", "モ" },
                { "ya", "ヤ" }, { "yu", "ユ" }, { "yo", "ヨ" },
                { "ra", "ラ" }, { "ri", "リ" }, { "ru", "ル" }, { "re", "レ" }, { "ro", "ロ" },
                { "wa", "ワ" }, { "wo", "ヲ" }, { "n", "ン" },
                { "b", "ブ" }, { "c", "ク" }, { "d", "ド" }, { "f", "フ" }, { "g", "グ" }, { "h", "ハ" }, { "j", "ジ" },
                { "k", "ク" }, { "l", "ル" }, { "m", "ム" }, { "p", "プ" }, { "q", "ク" }, { "r", "ル" }, { "s", "ス" },
                { "t", "ト" }, { "v", "ヴ" }, { "w", "ウ" }, { "x", "クス" }, { "y", "イ" }, { "z", "ズ" }
        };
        String result = input.toLowerCase();
        // 長いパターンから順に置換
        String[] order = { "shi", "chi", "tsu", "kyo", "ryo", "nyo", "hyo", "byo", "pyo", "kya", "kyu", "sha", "shu",
                "sho", "cha", "chu", "cho", "nya", "nyu", "hya", "hyu", "bya", "byu", "pya", "pyu", "mya", "myu", "rya",
                "ryu", "fa", "fi", "fe", "fo" };
        for (String pat : order) {
            for (String[] pair : patterns) {
                if (pair[0].equals(pat)) {
                    result = result.replace(pat, pair[1]);
                }
            }
        }
        for (String[] pair : patterns) {
            result = result.replace(pair[0], pair[1]);
        }
        // アルファベット以外はそのまま
        result = result.replaceAll("[^\u30A0-\u30FF]", "");
        // 何も変換できなければ元のまま返す
        if (result.isEmpty())
            return input;
        return result;
    }

    // 国名から言語コードを取得（主要国のみ対応、なければen）
    private static String getLangCodeFromCountry(String country) {
        country = country.trim().toLowerCase();
        switch (country) {
            case "japan":
                return "ja";
            case "france":
                return "fr";
            case "germany":
                return "de";
            case "china":
                return "zh";
            case "korea":
            case "south korea":
                return "ko";
            case "spain":
                return "es";
            case "italy":
                return "it";
            case "russia":
                return "ru";
            case "brazil":
                return "pt";
            case "vietnam":
                return "vi";
            case "thailand":
                return "th";
            case "indonesia":
                return "id";
            case "netherlands":
                return "nl";
            case "sweden":
                return "sv";
            case "norway":
                return "no";
            case "finland":
                return "fi";
            case "egypt":
                return "ar";
            case "india":
                return "hi";
            case "usa":
            case "united states":
            case "united states of america":
            case "canada":
            case "australia":
            case "new zealand":
            case "united kingdom":
            case "uk":
            case "ireland":
                return "en";
            default:
                return "";
        }
    }

    // 地域名を日本語に変換
    private static String toJapaneseRegion(String region) {
        switch (region) {
            case "Asia":
                return "アジア";
            case "Europe":
                return "ヨーロッパ";
            case "Africa":
                return "アフリカ";
            case "Oceania":
                return "オセアニア";
            case "Americas":
                return "アメリカ大陸";
            case "Antarctic":
                return "南極";
            case "Polar":
                return "極地";
            case "":
                return "";
            default:
                return "";
        }
    }

    // 言語名を日本語に変換して表示
    private static String getLanguagesJp(JSONObject obj) {
        if (!obj.has("languages"))
            return "不明";
        JSONArray arr = obj.getJSONArray("languages");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject lang = arr.getJSONObject(i);
            String name = lang.optString("name", "");
            String jp = toJapaneseLanguage(name);
            sb.append(name);
            if (!jp.isEmpty()) {
                sb.append(" (").append(jp).append(")");
            }
            if (i < arr.length() - 1)
                sb.append(", ");
        }
        return sb.toString();
    }

    // 英語の言語名を日本語に変換（代表的なもののみ）
    private static String toJapaneseLanguage(String name) {
        if (name.equalsIgnoreCase("Japanese"))
            return "日本語";
        if (name.equalsIgnoreCase("English"))
            return "英語";
        if (name.equalsIgnoreCase("Chinese"))
            return "中国語";
        if (name.equalsIgnoreCase("Korean"))
            return "韓国語";
        if (name.equalsIgnoreCase("French"))
            return "フランス語";
        if (name.equalsIgnoreCase("German"))
            return "ドイツ語";
        if (name.equalsIgnoreCase("Russian"))
            return "ロシア語";
        if (name.equalsIgnoreCase("Spanish"))
            return "スペイン語";
        if (name.equalsIgnoreCase("Portuguese"))
            return "ポルトガル語";
        if (name.equalsIgnoreCase("Italian"))
            return "イタリア語";
        if (name.equalsIgnoreCase("Vietnamese"))
            return "ベトナム語";
        if (name.equalsIgnoreCase("Thai"))
            return "タイ語";
        if (name.equalsIgnoreCase("Arabic"))
            return "アラビア語";
        if (name.equalsIgnoreCase("Hindi"))
            return "ヒンディー語";
        if (name.equalsIgnoreCase("Indonesian"))
            return "インドネシア語";
        if (name.equalsIgnoreCase("Malay"))
            return "マレー語";
        if (name.equalsIgnoreCase("Dutch"))
            return "オランダ語";
        if (name.equalsIgnoreCase("Swedish"))
            return "スウェーデン語";
        if (name.equalsIgnoreCase("Norwegian"))
            return "ノルウェー語";
        if (name.equalsIgnoreCase("Finnish"))
            return "フィンランド語";
        // 必要に応じて追加
        return "";
    }

    private static String getCurrencies(JSONObject obj) {
        if (!obj.has("currencies"))
            return "不明";
        JSONArray arr = obj.getJSONArray("currencies");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject cur = arr.getJSONObject(i);
            String name = cur.optString("name", "");
            String code = cur.optString("code", "");
            String jp = toJapaneseCurrency(name, code);
            sb.append(name);
            if (!jp.isEmpty()) {
                sb.append(" (").append(jp).append(")");
            }
            if (i < arr.length() - 1)
                sb.append(", ");
        }
        return sb.toString();
    }

    // 首都名から日本語名を返す（代表的なもののみ）
    private static String toJapaneseCapital(String capital, String country) {
        if (capital.equalsIgnoreCase("Tokyo"))
            return "東京";
        if (capital.equalsIgnoreCase("Washington, D.C.") || capital.equalsIgnoreCase("Washington D.C."))
            return "ワシントンD.C.";
        if (capital.equalsIgnoreCase("Beijing"))
            return "北京";
        if (capital.equalsIgnoreCase("Seoul"))
            return "ソウル";
        if (capital.equalsIgnoreCase("London"))
            return "ロンドン";
        if (capital.equalsIgnoreCase("Paris"))
            return "パリ";
        if (capital.equalsIgnoreCase("Berlin"))
            return "ベルリン";
        if (capital.equalsIgnoreCase("Moscow"))
            return "モスクワ";
        if (capital.equalsIgnoreCase("Canberra"))
            return "キャンベラ";
        if (capital.equalsIgnoreCase("Ottawa"))
            return "オタワ";
        if (capital.equalsIgnoreCase("New Delhi"))
            return "ニューデリー";
        if (capital.equalsIgnoreCase("Brasília"))
            return "ブラジリア";
        if (capital.equalsIgnoreCase("Rome"))
            return "ローマ";
        if (capital.equalsIgnoreCase("Madrid"))
            return "マドリード";
        if (capital.equalsIgnoreCase("Bangkok"))
            return "バンコク";
        if (capital.equalsIgnoreCase("Cairo"))
            return "カイロ";
        if (capital.equalsIgnoreCase("Hanoi"))
            return "ハノイ";
        if (capital.equalsIgnoreCase("Jakarta"))
            return "ジャカルタ";
        if (capital.equalsIgnoreCase("Singapore"))
            return "シンガポール";
        // 必要に応じて追加
        return "";
    }

    // 通貨名・コードから日本語名を返す（代表的なもののみ）
    private static String toJapaneseCurrency(String name, String code) {
        if (code.equals("JPY") || name.equalsIgnoreCase("Yen"))
            return "日本円";
        if (code.equals("USD") || name.equalsIgnoreCase("Dollar"))
            return "アメリカドル";
        if (code.equals("EUR") || name.equalsIgnoreCase("Euro"))
            return "ユーロ";
        if (code.equals("CNY") || name.equalsIgnoreCase("Yuan"))
            return "人民元";
        if (code.equals("KRW") || name.equalsIgnoreCase("Won"))
            return "韓国ウォン";
        if (code.equals("GBP") || name.equalsIgnoreCase("Pound Sterling"))
            return "イギリスポンド";
        if (code.equals("AUD") || name.equalsIgnoreCase("Australian Dollar"))
            return "オーストラリアドル";
        if (code.equals("CAD") || name.equalsIgnoreCase("Canadian Dollar"))
            return "カナダドル";
        if (code.equals("INR") || name.equalsIgnoreCase("Indian Rupee"))
            return "インドルピー";
        if (code.equals("RUB") || name.equalsIgnoreCase("Russian Ruble"))
            return "ロシアルーブル";
        // 必要に応じて追加
        return "";
    }
}