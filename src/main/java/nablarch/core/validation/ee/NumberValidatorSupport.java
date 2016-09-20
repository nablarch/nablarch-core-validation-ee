package nablarch.core.validation.ee;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * 数値関連のバリデーションを行う抽象クラス。
 *
 * @author Naoki Yamamoto
 */
public abstract class NumberValidatorSupport<T extends Annotation> implements ConstraintValidator<T, Object> {

    /** 有効な数値を表すパターン */
    private static final Pattern PATTERN = Pattern.compile("^[+-]?\\d*\\.?\\d+$");

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        BigDecimal number = null;
        if (value instanceof BigDecimal) {
            number = (BigDecimal) value;
        } else if (value instanceof CharSequence) {
            CharSequence casted = (CharSequence) value;
            if (casted.length() == 0) {
                return true;
            }
            if (!PATTERN.matcher(casted).matches()) {
                buildMessage(context);
                return false;
            }
            number = getDecimalValue(casted);
        } else if (value instanceof Number) {
            Number casted = (Number) value;
            number = getDecimalValue(casted.toString());
        } else {
            buildMessage(context);
            return false;
        }
        if (isValid(number)) {
            return true;
        }
        buildMessage(context);
        return false;
    }

    /**
     * {@link BigDecimal}型に変換した値を取得する。
     * @param value 検証対象オブジェクト
     * @return {@link BigDecimal}型に変換した値
     */
    private BigDecimal getDecimalValue(CharSequence value) {
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException ignore) {
            return null;
        }
    }

    /**
     * 検証対象オブジェクトのバリデーションを行う。
     *
     * @param value 検証対象オブジェクト
     * @return バリデーション成否
     */
    protected abstract boolean isValid(final BigDecimal value);

    /**
     * 検証エラー時のメッセージを構築する。
     *
     * @param context コンテキスト
     */
    protected abstract void buildMessage(final ConstraintValidatorContext context);
}
