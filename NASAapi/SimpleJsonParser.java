// 簡易的なJSONパーサークラス（外部ライブラリ不要）
public class SimpleJsonParser {
    public static String getValue(String json, String key) {
        String pattern = "\"" + key + "\"\s*:\s*\"(.*?)\"";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
}
