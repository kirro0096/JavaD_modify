import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class PokeQ extends JFrame {
    private JLabel pokemonImageLabel;
    private JTextField answerField;
    private JButton submitButton;
    private JLabel messageLabel;
    private final PokeApiClient apiClient = new PokeApiClient();
    private String correctName;
    private int hintLevel;

    public PokeQ() {
        setTitle("このポケモンの名前は？");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(540, 480); // ウィンドウをさらに横長に
        setLocationRelativeTo(null);
        pokemonImageLabel = new JLabel();
        pokemonImageLabel.setPreferredSize(new Dimension(240, 240));
        pokemonImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        answerField = new JTextField();
        answerField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 22));
        answerField.setPreferredSize(new Dimension(400, 40));
        answerField.setMinimumSize(new Dimension(400, 40));
        answerField.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        submitButton = new JButton("こたえを送信");
        submitButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        messageLabel = new JLabel("このポケモンの名前は？（カタカナで）");
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));

        Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPane.add(pokemonImageLabel, gbc);
        contentPane.add(messageLabel, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPane.add(answerField, gbc);
        gbc.fill = GridBagConstraints.NONE;
        contentPane.add(submitButton, gbc);

        submitButton.addActionListener(e -> checkAnswerGUI());
        answerField.addActionListener(e -> checkAnswerGUI());
    }

    private void showPokemonImage(String imageUrl) throws IOException {
        ImageIcon originalIcon = new ImageIcon(new URL(imageUrl));
        Image originalImage = originalIcon.getImage();
        int newSize = 240; // 画像サイズを大きく
        Image scaledImage = originalImage.getScaledInstance(newSize, newSize, Image.SCALE_SMOOTH);
        pokemonImageLabel.setIcon(new ImageIcon(scaledImage));
    }

    private static Optional<String> findJapaneseName(PokeApiClient.PokemonData.PokemonSpeciesResponse species) {
        return species.names().stream()
                .filter(n -> "ja-Hrkt".equals(n.language().name()))
                .map(PokeApiClient.PokemonData.NameEntry::name)
                .findFirst()
                .or(() -> species.names().stream()
                        .filter(n -> "ja".equals(n.language().name()))
                        .map(PokeApiClient.PokemonData.NameEntry::name)
                        .findFirst());
    }

    private void loadNextQuestion() {
        new Thread(() -> {
            try {
                hintLevel = 0;
                SwingUtilities.invokeLater(() -> {
                    messageLabel.setText("このポケモンの名前は？（カタカナで）");
                    answerField.setText("");
                    answerField.setEnabled(false);
                    submitButton.setEnabled(false);
                });
                PokeApiClient.PokemonData.PokemonResponse pokemon = apiClient.fetchRandomPokemon()
                        .orElseThrow(() -> new IOException("ポケモンの取得に失敗しました。"));
                PokeApiClient.PokemonData.PokemonSpeciesResponse species = apiClient.fetchPokemonSpecies(pokemon.species().url())
                        .orElseThrow(() -> new IOException("ポケモンの日本語名の取得に失敗しました。"));
                correctName = findJapaneseName(species)
                        .orElseThrow(() -> new IOException("ポケモンの日本語名が見つかりませんでした。"));
                SwingUtilities.invokeLater(() -> {
                    try {
                        showPokemonImage(pokemon.sprites().frontDefault());
                        answerField.setEnabled(true);
                        submitButton.setEnabled(true);
                        answerField.requestFocusInWindow();
                    } catch (IOException e) {
                        messageLabel.setText("画像の表示に失敗: " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> messageLabel.setText("エラー: " + e.getMessage()));
            }
        }).start();
    }

    // GUI用の判定・ヒント表示
    private void checkAnswerGUI() {
        String userAnswer = answerField.getText().trim();
        if (userAnswer.isEmpty()) return;
        String normalizedUser = toKatakana(java.text.Normalizer.normalize(userAnswer, java.text.Normalizer.Form.NFKC))
            .replaceAll("[^\u30A0-\u30FFー]", "");
        String normalizedCorrect = java.text.Normalizer.normalize(correctName, java.text.Normalizer.Form.NFC)
            .replaceAll("[^\u30A0-\u30FFー]", "");
        if (normalizedUser.equals(normalizedCorrect)) {
            messageLabel.setText("正解！すごい！");
            answerField.setEnabled(false);
            submitButton.setEnabled(false);
            // 2秒待って次の問題
            new Thread(() -> {
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                loadNextQuestion();
            }).start();
        } else {
            hintLevel++;
            if (correctName.length() > 1) {
                int hintLength = Math.min(hintLevel, correctName.length() - 1);
                String hint = correctName.substring(0, hintLength);
                messageLabel.setText("ちがうよ！ヒントは『" + hint + "』");
            } else {
                messageLabel.setText("ちがうよ！もう一度考えてみて！");
            }
            answerField.setText("");
            answerField.requestFocusInWindow();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PokeQ app = new PokeQ();
            app.setVisible(true);
            app.loadNextQuestion();
        });
    }

    // ひらがな→カタカナ変換
    private static String toKatakana(String input) {
        StringBuilder sb = new StringBuilder(input.length());
        for (char c : input.toCharArray()) {
            if (c >= '\u3041' && c <= '\u3096') {
                sb.append((char)(c + 0x60));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
