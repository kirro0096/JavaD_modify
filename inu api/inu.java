import java.util.*;

public class inu {
    public static void main(String[] args) {
        // 2024年日本人気犬種ランキング（1位～30位、日本語名のみ）
        List<String> rankingJp = Arrays.asList(
            "トイ・プードル",
            "チワワ",
            "ダックスフンド",
            "柴犬",
            "ポメラニアン",
            "ミニチュア・シュナウザー",
            "ヨークシャー・テリア",
            "シーズー",
            "パピヨン",
            "フレンチ・ブルドッグ",
            "マルチーズ",
            "コーギー",
            "ゴールデン・レトリバー",
            "ラブラドール・レトリバー",
            "ビーグル",
            "ペキニーズ",
            "ミニチュア・ピンシャー",
            "ボーダー・コリー",
            "パグ",
            "ボストン・テリア",
            "キャバリア・キング・チャールズ・スパニエル",
            "シェットランド・シープドッグ",
            "イタリアン・グレーハウンド",
            "ジャック・ラッセル・テリア",
            "ブルドッグ",
            "シベリアンハスキー",
            "秋田犬",
            "サモエド",
            "ウェルシュ・コーギー・ペンブローク",
            "バーニーズ・マウンテン・ドッグ"
        );
        // ランダムに犬種を選ぶ
        Random rand = new Random();
        int idx = rand.nextInt(rankingJp.size());
        String breedJpName = rankingJp.get(idx);
        System.out.println("犬種: " + breedJpName);
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
