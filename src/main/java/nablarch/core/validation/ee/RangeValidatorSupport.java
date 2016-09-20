package nablarch.core.validation.ee;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;

/**
 * 数値が範囲内であるかのバリデーションを行う抽象クラス。
 *
 * @author Kiyohito Itoh
 * @param <T> アノテーションの型
 */
public abstract class RangeValidatorSupport<T extends Annotation> extends NumberValidatorSupport<T> {

    /** バリデーション範囲 */
    private Range range;

    @Override
    public void initialize(T constraintAnnotation) {
        range = getRange(constraintAnnotation);
    }

    /**
     * バリデーション範囲({@link Range})を取得する。
     *
     * @param constraintAnnotation バリデーション用のアノテーション
     * @return バリデーション範囲
     */
    protected abstract Range getRange(T constraintAnnotation);

    @Override
    protected boolean isValid(BigDecimal value) {
        return range.isValid(value);
    }

    /**
     * {@link BigDecimal}型に変換した値を取得する。
     * @param value 検証対象オブジェクト
     * @return {@link BigDecimal}型に変換した値
     */
    protected BigDecimal getDecimalValue(CharSequence value) {
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException ignore) {
            return null;
        }
    }

    /**
     * バリデーション範囲を指定し、バリデーションを行うクラス。
     */
    protected static class Range {
        /** 最大値 */
        private BigDecimal max;

        /** 最小値 */
        private BigDecimal min;

        /**
         * バリデーション範囲のコンストラクタ。
         * 最大値・最小値ともにバリデーション範囲に含まれる。
         *
         * @param max 最大値
         * @param min 最小値
         */
        protected Range(BigDecimal max, BigDecimal min) {
            this.max = max;
            this.min = min;
        }

        /**
         * バリデーションを行う。
         *
         * @param value バリデーション対象
         * @return バリデーション結果
         */
        private boolean isValid(BigDecimal value) {
            boolean minValid = lessThanOrEqual(min, value);
            boolean maxValid = lessThanOrEqual(value, max);
            return minValid && maxValid;
        }

        /**
         * {@link BigDecimal}の大小を比較する。
         * low, highのどちらかがnullならtrueを返す。
         *
         * @param low 小さい値
         * @param high 大きい値
         * @return low <= high
         */
        private boolean lessThanOrEqual(BigDecimal low, BigDecimal high) {
            if (low == null || high == null) {
                return true;
            }
            return low.compareTo(high) <= 0;
        }
    }
}
