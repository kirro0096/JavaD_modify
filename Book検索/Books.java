import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

class Book {
    private String title;
    private String author;
    private String isbn;

    public Book(String title, String author, String isbn) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    @Override
    public String toString() {
        return "タイトル: " + title + ", 著者: " + author + ", ISBN: " + isbn;
    }
}

public class Books {
    private static final String API_KEY = "AIzaSyCEY0FwO4wweQl0gSCILyxL8f6klDGFHPQ";
    private static List<Book> bookList = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in, "UTF-8");
        System.out.println("==============================");
        System.out.println("    書籍検索アプリへようこそ！");
        System.out.println("==============================");
        while (true) {
            System.out.println("\n検索方法を選んでください：");
            System.out.println("  1: タイトルで検索");
            System.out.println("  2: 著者で検索");
            System.out.println("  3: キーワードで検索");
            System.out.print("番号を入力（終了するには exit と入力）：");
            String mode = scanner.nextLine().trim();
            if (mode.equalsIgnoreCase("exit")) {
                System.out.println("アプリを終了します。");
                break;
            }
            if (mode.equals("1")) {
                System.out.print("検索したい書籍のタイトルを入力してください（戻るには enter）：");
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("exit")) {
                    System.out.println("アプリを終了します。");
                    break;
                }
                if (input.isEmpty())
                    continue;
                searchBooksFromApi(input, "intitle");
            } else if (mode.equals("2")) {
                System.out.print("検索したい著者名を入力してください（戻るには enter）：");
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("exit")) {
                    System.out.println("アプリを終了します。");
                    break;
                }
                if (input.isEmpty())
                    continue;
                searchBooksFromApi(input, "inauthor");
            } else if (mode.equals("3")) {
                System.out.print("検索したいキーワードを入力してください（戻るには enter）：");
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("exit")) {
                    System.out.println("アプリを終了します。");
                    break;
                }
                if (input.isEmpty())
                    continue;
                searchBooksFromApi(input, "");
            } else {
                System.out.println("無効な入力です。1～3のいずれかを入力してください。");
            }
        }
        scanner.close();
    }

    /**
     * Google Books APIで書籍検索（タイトル・著者・キーワード）
     */
    private static void searchBooksFromApi(String keyword, String type) {
        try {
            String urlStr;
            if (type == null || type.isEmpty()) {
                urlStr = "https://www.googleapis.com/books/v1/volumes?q=" +
                        java.net.URLEncoder.encode(keyword, "UTF-8") + "&key=" + API_KEY + "&maxResults=10";
            } else {
                urlStr = "https://www.googleapis.com/books/v1/volumes?q=" +
                        type + ":" + java.net.URLEncoder.encode(keyword, "UTF-8") + "&key=" + API_KEY
                        + "&maxResults=10";
            }
            URL url = new URL(urlStr);
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
                JSONObject json = new JSONObject(response.toString());
                JSONArray items = json.optJSONArray("items");
                if (items == null || items.length() == 0) {
                    System.out.println("\n------------------------------");
                    System.out.println("該当する書籍が見つかりませんでした。");
                    System.out.println("------------------------------");
                    return;
                }
                System.out.println("\n------------------------------");
                System.out.println("検索結果（最大10件）:");
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    JSONObject volumeInfo = item.getJSONObject("volumeInfo");
                    printBookInfo(i + 1, volumeInfo);
                }
            } else {
                System.out.println("APIリクエストに失敗しました。ステータスコード: " + responseCode);
            }
        } catch (Exception e) {
            System.out.println("エラーが発生しました: " + e.getMessage());
        }
    }

    /**
     * 1冊分の書籍情報を見やすく表示
     */
    private static void printBookInfo(int index, JSONObject volumeInfo) {
        String bookTitle = volumeInfo.optString("title", "タイトル不明");
        String authors = volumeInfo.has("authors")
                ? String.join(", ", volumeInfo.getJSONArray("authors").toList().toArray(new String[0]))
                : "著者不明";
        String isbn = "不明";
        if (volumeInfo.has("industryIdentifiers")) {
            JSONArray ids = volumeInfo.getJSONArray("industryIdentifiers");
            for (int j = 0; j < ids.length(); j++) {
                JSONObject idObj = ids.getJSONObject(j);
                if (idObj.getString("type").contains("ISBN")) {
                    isbn = idObj.getString("identifier");
                    break;
                }
            }
        }
        System.out.println(index + ". タイトル: " + bookTitle);
        System.out.println("   著者: " + authors);
        System.out.println("   ISBN（国際標準図書番号）: " + isbn);
        System.out.println("------------------------------");
    }
}
