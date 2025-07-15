package kokkiapi;

// GUIのためのSwingおよびAWTインポート
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.imageio.ImageIO;

// URLおよびネットワーク関連のインポート
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

// ユーティリティ関連のインポート
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

// JSON解析
import org.json.JSONArray;
import org.json.JSONObject;

public class kokki extends JFrame {

    // --- GUIコンポーネント ---
    private final JLabel flagLabel;
    private final JTextField answerField;
    private final JButton submitButton;
    private final JLabel resultLabel;
    private final JLabel scoreLabel;

    // --- ゲームの状態 ---
    private List<Country> countries;
    private Country currentCountry;
    private int score = 0;
    private int rounds = 0;
    private final Random random = new Random();

    // APIクライアントとURLを定数として定義
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final String API_URL = "https://restcountries.com/v3.1/all?fields=name,translations,flags";

    // 国のデータを保持するためのシンプルな内部クラス
    private static class Country {
        String nameJpn;
        String nameCommon;
        String flagUrl;

        Country(String nameJpn, String nameCommon, String flagUrl) {
            this.nameJpn = nameJpn;
            this.nameCommon = nameCommon;
            this.flagUrl = flagUrl;
        }
    }

    public kokki() {
        super("国旗当てゲーム"); // ウィンドウのタイトル

        // --- UIの初期化 ---
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setSize(500, 480);
        setLocationRelativeTo(null); // ウィンドウを中央に表示

        // 1. 国旗表示エリア (中央)
        flagLabel = new JLabel("国データを読み込み中...", SwingConstants.CENTER);
        flagLabel.setPreferredSize(new Dimension(400, 250));
        flagLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        add(flagLabel, BorderLayout.CENTER);

        // 2. 入力およびコントロールパネル (下部)
        JPanel southPanel = new JPanel(new BorderLayout(10, 10));
        southPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 入力コンポーネント
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        answerField = new JTextField();
        answerField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        submitButton = new JButton("回答");
        inputPanel.add(new JLabel("国名: "), BorderLayout.WEST);
        inputPanel.add(answerField, BorderLayout.CENTER);
        inputPanel.add(submitButton, BorderLayout.EAST);
        southPanel.add(inputPanel, BorderLayout.NORTH);

        // 結果とスコアの表示
        resultLabel = new JLabel("国名を入力して回答ボタンを押してください。");
        resultLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        scoreLabel = new JLabel("スコア: 0/0");
        scoreLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        southPanel.add(resultLabel, BorderLayout.CENTER);
        southPanel.add(scoreLabel, BorderLayout.SOUTH);

        add(southPanel, BorderLayout.SOUTH);

        // --- イベントリスナー ---
        ActionListener answerListener = e -> checkAnswer();
        submitButton.addActionListener(answerListener);
        answerField.addActionListener(answerListener); // Enterキーでも回答できるようにする

        // --- ゲーム開始 ---
        startGame();
    }

    /**
     * 国データをバックグラウンドスレッドで取得してゲームを開始します。
     */
    private void startGame() {
        // 読み込み中は入力を無効化
        setInteractionEnabled(false);
        flagLabel.setText("世界の国データを読み込んでいます...");

        new SwingWorker<List<Country>, Void>() {
            @Override
            protected List<Country> doInBackground() throws Exception {
                return fetchCountryData();
            }

            @Override
            protected void done() {
                try {
                    countries = get();
                    if (countries == null || countries.isEmpty()) {
                        flagLabel.setText("データ取得に失敗しました。");
                        JOptionPane.showMessageDialog(kokki.this, "国データの取得に失敗しました。\nネットワーク接続を確認して再起動してください。", "エラー",
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        nextQuestion();
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(kokki.this, "エラーが発生しました: " + e.getMessage(), "エラー",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /**
     * 次の問題を表示します。
     */
    private void nextQuestion() {
        if (countries.isEmpty())
            return;

        rounds++;
        currentCountry = countries.get(random.nextInt(countries.size()));

        resultLabel.setText("この国はどこでしょう？");
        resultLabel.setForeground(Color.BLACK);
        scoreLabel.setText("スコア: " + score + "/" + (rounds - 1));
        answerField.setText("");
        flagLabel.setIcon(null);
        flagLabel.setText("国旗を読み込み中...");
        setInteractionEnabled(false);

        // 国旗画像をバックグラウンドスレッドで読み込みます
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                URL url = new URL(currentCountry.flagUrl);
                Image image = ImageIO.read(url);
                // ラベルに合わせて画像をスケーリング
                Image scaledImage = image.getScaledInstance(380, 220, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    flagLabel.setText("");
                    flagLabel.setIcon(icon);
                    setInteractionEnabled(true);
                    answerField.requestFocusInWindow();
                } catch (Exception e) {
                    e.printStackTrace();
                    flagLabel.setText("画像読込エラー");
                }
            }
        }.execute();
    }

    /**
     * ユーザーの回答をチェックします。
     */
    private void checkAnswer() {
        String userAnswer = answerField.getText().trim();
        if (userAnswer.isEmpty())
            return;

        setInteractionEnabled(false);

        if (userAnswer.equalsIgnoreCase(currentCountry.nameJpn)
                || userAnswer.equalsIgnoreCase(currentCountry.nameCommon)) {
            score++;
            resultLabel.setText("🎉 正解！素晴らしい！");
            resultLabel.setForeground(new Color(0, 128, 0)); // 深緑色
        } else {
            resultLabel.setText("残念... 正解は " + currentCountry.nameJpn + " (" + currentCountry.nameCommon + ")");
            resultLabel.setForeground(Color.RED);
        }
        scoreLabel.setText("スコア: " + score + "/" + rounds);

        // 2秒待ってから次の問題を表示します
        Timer timer = new Timer(2000, e -> nextQuestion());
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * ユーザーが操作するコンポーネント（入力欄やボタン）を有効または無効にします。
     *
     * @param enabled trueで有効化、falseで無効化
     */
    private void setInteractionEnabled(boolean enabled) {
        answerField.setEnabled(enabled);
        submitButton.setEnabled(enabled);
    }

    /**
     * REST Countries APIから国データを取得します。
     *
     * @return Countryオブジェクトのリスト
     * @throws Exception データの取得または解析に失敗した場合
     */
    private static List<Country> fetchCountryData() throws Exception {
        List<Country> countryList = new ArrayList<>();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_URL)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONArray countriesJson = new JSONArray(response.body());
            for (int i = 0; i < countriesJson.length(); i++) {
                JSONObject countryJson = countriesJson.getJSONObject(i);
                JSONObject jpnTranslation = countryJson.getJSONObject("translations").optJSONObject("jpn");
                if (jpnTranslation != null) {
                    String nameJpn = jpnTranslation.getString("common");
                    String nameCommon = countryJson.getJSONObject("name").getString("common");
                    String flagUrl = countryJson.getJSONObject("flags").getString("png");
                    countryList.add(new Country(nameJpn, nameCommon, flagUrl));
                }
            }
        } else {
            // API呼び出しが失敗した場合、例外をスローします
            throw new java.io.IOException("APIからのデータ取得に失敗しました。ステータスコード: " + response.statusCode());
        }
        return countryList;
    }

    /**
     * アプリケーションを起動するためのmainメソッドです。
     */
    public static void main(String[] args) {
        // GUIの作成をイベントディスパッチスレッドで確実に行います
        SwingUtilities.invokeLater(() -> new kokki().setVisible(true));
    }
}
