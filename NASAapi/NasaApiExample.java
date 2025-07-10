import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.swing.*;
import java.awt.*;

/**
 * NASAのAPOD（天文写真）APIから画像・タイトル・説明を取得し、
 * Swingウィンドウで表示するサンプルプログラム。
 *
 * - APIからJSONを取得
 * - タイトル・説明・画像URLを抽出
 * - Swingで画像と説明を表示
 */
public class NasaApiExample {
    public static void main(String[] args) {
        String apiKey = "OqUGsfRmjpXxNGwjHwpKnCQjkagy1yk9qs2A6gXP"; // NASA APIキー
        String endpoint = "https://api.nasa.gov/planetary/apod?api_key=" + apiKey;
        try {
            // --- APIへリクエスト ---
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // --- レスポンス(JSON)を読み込む ---
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                String json = response.toString();

                // --- JSONから必要な情報を抽出 ---
                String title = SimpleJsonParser.getValue(json, "title"); // タイトル
                String explanation = SimpleJsonParser.getValue(json, "explanation"); // 説明
                String urlImg = SimpleJsonParser.getValue(json, "url"); // 画像URL

                // --- デバッグ用にコンソール出力 ---
                System.out.println("\n--- NASA APOD ---");
                System.out.println("タイトル: " + title);
                System.out.println("説明: " + explanation);
                System.out.println("画像URL: " + urlImg);

                // --- Swingで画像と情報を表示 ---
                SwingUtilities.invokeLater(() -> {
                    JFrame frame = new JFrame("NASA APOD");
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setSize(800, 600);
                    frame.setLayout(new BorderLayout());

                    // タイトル・説明パネル
                    JPanel textPanel = new JPanel();
                    textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
                    JLabel titleLabel = new JLabel("タイトル: " + title);
                    JTextArea explanationArea = new JTextArea("説明: " + explanation);
                    explanationArea.setLineWrap(true);
                    explanationArea.setWrapStyleWord(true);
                    explanationArea.setEditable(false);
                    JScrollPane scrollPane = new JScrollPane(explanationArea);
                    textPanel.add(titleLabel);
                    textPanel.add(scrollPane);

                    // 画像の取得と表示
                    try {
                        URL imgUrl = new URL(urlImg); // 画像URL
                        ImageIcon imageIcon = new ImageIcon(imgUrl);
                        Image image = imageIcon.getImage();
                        Image scaledImage = image.getScaledInstance(600, 400, Image.SCALE_SMOOTH);
                        JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
                        frame.add(imageLabel, BorderLayout.CENTER);
                    } catch (Exception ex) {
                        JLabel errorLabel = new JLabel("画像の取得に失敗しました。");
                        frame.add(errorLabel, BorderLayout.CENTER);
                    }

                    frame.add(textPanel, BorderLayout.SOUTH);
                    frame.setVisible(true);
                });
            } else {
                // エラー時の処理
                System.out.println("GET request failed. Response Code: " + responseCode);
            }
        } catch (Exception e) {
            // 例外発生時の処理
            e.printStackTrace();
        }
    }
}
