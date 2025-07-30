import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class INaturalistSwingSlideshow extends JFrame {
    private static final String API_URL = "https://api.inaturalist.org/v1/observations";
    private static final int IMAGE_WIDTH = 400;
    private static final int IMAGE_HEIGHT = 300;
    private static final int SLIDESHOW_DELAY_MS = 2500;
    private static final int RESULTS_PER_PAGE = 20; // 取得件数を増やす

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    // 観察データを保持するレコード
    private record ObservationData(String imageUrl, String infoText) {}
    private List<ObservationData> observationDataList = new ArrayList<>();
    private JLabel imageLabel = new JLabel();
    private JLabel infoLabel = new JLabel();
    private Timer timer;

    public INaturalistSwingSlideshow() {
        setTitle("iNaturalist 画像スライドショー");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        JTextField searchField = new JTextField(20);
        JButton searchBtn = new JButton("検索");
        topPanel.add(new JLabel("生物名: "));
        topPanel.add(searchField);
        topPanel.add(searchBtn);
        add(topPanel, BorderLayout.NORTH);

        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setVerticalAlignment(JLabel.CENTER);
        add(imageLabel, BorderLayout.CENTER);
        infoLabel.setHorizontalAlignment(JLabel.CENTER);
        add(infoLabel, BorderLayout.SOUTH);

        searchBtn.addActionListener(e -> {
            String query = searchField.getText().trim();
            if (query.isEmpty()) {
                JOptionPane.showMessageDialog(this, "生物名を入力してください。", "エラー", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // 検索処理をバックグラウンドで実行
            searchAndShowImages(query);
        });
    }

    private void searchAndShowImages(String query) {
        infoLabel.setText("「" + query + "」を検索中...");
        imageLabel.setIcon(null);
        if (timer != null) {
            timer.stop();
        }

        // SwingWorkerを使ってネットワーク処理をバックグラウンドで行う
        SwingWorker<List<ObservationData>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ObservationData> doInBackground() throws Exception {
                String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
                String requestUrl = API_URL + "?q=" + encodedQuery + "&per_page=" + RESULTS_PER_PAGE + "&order=desc&order_by=created_at";
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(requestUrl))
                        .GET()
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new IOException("APIエラー: " + response.statusCode());
                }

                List<ObservationData> newObservations = new ArrayList<>();
                JsonObject obj = gson.fromJson(response.body(), JsonObject.class);
                JsonArray results = obj.getAsJsonArray("results");
                if (results == null) {
                    return Collections.emptyList();
                }

                for (int i = 0; i < results.size(); i++) {
                    JsonObject obs = results.get(i).getAsJsonObject();
                    String speciesGuess = getJsonString(obs, "species_guess", "-");
                    String place = getJsonString(obs, "place_guess", "-");
                    String observedOn = getJsonString(obs, "observed_on", "-");
                    String info = String.format("種名: %s / 場所: %s / 観察日: %s", speciesGuess, place, observedOn);

                    if (obs.has("photos") && obs.get("photos").isJsonArray()) {
                        JsonArray photos = obs.getAsJsonArray("photos");
                        for (int j = 0; j < photos.size(); j++) {
                            JsonObject photoObj = photos.get(j).getAsJsonObject();
                            if (photoObj.has("url") && !photoObj.get("url").isJsonNull()) {
                                String url = photoObj.get("url").getAsString().replace("square", "large");
                                newObservations.add(new ObservationData(url, info));
                            }
                        }
                    }
                }
                return newObservations;
            }

            @Override
            protected void done() {
                try {
                    observationDataList = get();
                    if (observationDataList.isEmpty()) {
                        infoLabel.setText("該当する画像付きの観察データが見つかりませんでした。");
                        return;
                    }
                    // スライドショー開始
                    startSlideshow();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // 割り込み状態を復元
                    JOptionPane.showMessageDialog(null, "検索が中断されました。", "エラー", JOptionPane.ERROR_MESSAGE);
                    infoLabel.setText("検索が中断されました。");
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    JOptionPane.showMessageDialog(null, "検索中にエラーが発生しました: " + cause.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
                    infoLabel.setText("エラーが発生しました。");
                }
            }
        };
        worker.execute();
    }

    private void startSlideshow() {
        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(SLIDESHOW_DELAY_MS, new ActionListener() {
            private int currentIndex = -1; // 最初に0になるように-1で初期化

            @Override
            public void actionPerformed(ActionEvent e) {
                currentIndex = (currentIndex + 1) % observationDataList.size();
                showImage(currentIndex);
            }
        });
        timer.setInitialDelay(0); // タイマーをすぐに開始
        timer.start();
    }

    private void showImage(int index) {
        try {
            ObservationData data = observationDataList.get(index);
            ImageIcon icon = new ImageIcon(new java.net.URL(data.imageUrl()));
            Image img = icon.getImage().getScaledInstance(IMAGE_WIDTH, IMAGE_HEIGHT, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(img));
            infoLabel.setText(data.infoText());
        } catch (Exception e) {
            imageLabel.setIcon(null);
            infoLabel.setText("画像の取得に失敗しました。");
        }
    }

    /**
     * JsonObjectから安全に文字列を取得するヘルパーメソッド
     */
    private String getJsonString(JsonObject obj, String key, String defaultValue) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return defaultValue;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            INaturalistSwingSlideshow frame = new INaturalistSwingSlideshow();
            frame.setVisible(true);
        });
    }
}
