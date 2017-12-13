package nablarch.core.validation.ee;

/**
 * システム許容文字のバリデーションに関する設定を保持するクラス。
 * 
 * @author Taichi Uragami
 */
public class SystemCharConfig {

    /** サロゲートペアを許容するかどうかを表すフラグ */
    private boolean allowSurrogatePair;

    /**
     * サロゲートペアを許容するかどうかを表すフラグを取得する。
     * @return サロゲートペアを許容する場合は{@code true}（デフォルトは{@code false}）
     */
    public boolean isAllowSurrogatePair() {
        return allowSurrogatePair;
    }

    /**
     * サロゲートペアを許容するかどうかを表すフラグを設定する
     * @param allowSurrogatePair サロゲートペアを許容する場合は{@code true}（デフォルトは{@code false}）
     */
    public void setAllowSurrogatePair(boolean allowSurrogatePair) {
        this.allowSurrogatePair = allowSurrogatePair;
    }
}
