package nablarch.core.validation.ee;

import nablarch.core.util.StringUtil;
import nablarch.core.util.annotation.Published;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import static java.lang.annotation.ElementType.*;


/**
 * 指定された範囲内の文字列長であることを表すアノテーション。
 * <br/>
 * 入力値がnull又は空文字の場合は、validと判定する。
 *
 * エラー時のメッセージは、以下のルールにより決定される。
 * <ol>
 *     <li>{@link #message()}が指定されている場合は、その値を使用する。</li>
 *     <li>{@link #message()}が未指定で{@link #min()}のみ指定の場合は、<b>{nablarch.core.validation.ee.Length.min.message}</b></li>
 *     <li>{@link #message()}が未指定で{@link #max()}のみ指定の場合は、<b>{nablarch.core.validation.ee.Length.max.message}</b></li>
 *     <li>{@link #message()}が未指定で{@link #max()}と{@link #min()}に指定した値が同じ場合は、<b>{nablarch.core.validation.ee.Length.fixed.message}</b></li>
 *     <li>{@link #message()}が未指定で{@link #min()}と{@link #max()}に指定した値が異なる場合は、<b>{nablarch.core.validation.ee.Length.min.max.message}</b></li>
 * </ol>
 * 
 * 文字列長の計算はサロゲートペアを考慮して行われる。
 * 
 * @author T.Kawasaki
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {Length.LengthValidator.class})
@Published
public @interface Length {

    /** グループ */
    Class<?>[] groups() default {};

    /** メッセージ */
    String message() default "";

    /** payload */
    Class<? extends Payload>[] payload() default {};

    /** 複数指定用のアノテーション */
    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        Length[] value();
    }

    /** 文字列の最小長。*/
    int min() default 0;

    /** 文字列の最大長。*/
    int max() default 0;

    /**
     * 文字列長のバリデーションを行う{@link ConstraintValidator}実装クラス。<br/>
     */
    class LengthValidator implements ConstraintValidator<Length, CharSequence> {

        /** 最大値 */
        private int max;

        /** 最小値 */
        private int min;

        /** メッセージ */
        private String message;

        /** デフォルトメッセージ(最小値のみ) */
        private static final String MIN_MESSAGE = "{nablarch.core.validation.ee.Length.min.message}";

        /** デフォルトメッセージ(最大値のみ) */
        private static final String MAX_MESSAGE = "{nablarch.core.validation.ee.Length.max.message}";

        /** デフォルトメッセージ(固定長) */
        private static final String FIXED_MESSAGE = "{nablarch.core.validation.ee.Length.fixed.message}";
        
        /** デフォルトメッセージ(可変長) */
        private static final String MIN_MAX_MESSAGE = "{nablarch.core.validation.ee.Length.min.max.message}";

        /** {@inheritDoc} */
        @Override
        public void initialize(Length constraintAnnotation) {
            min = constraintAnnotation.min();
            max = constraintAnnotation.max();
            message = constraintAnnotation.message();
        }

        /** {@inheritDoc} */
        @Override
        public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
            if (value == null || value.length() == 0) {
                return true;
            }
            final int length = Character.codePointCount(value, 0, value.length());

            if (isValid(length)) {
                return true;
            }

            if (StringUtil.isNullOrEmpty(message)) {
                // メッセージが指定されていない場合は、デフォルトのメッセージを構築する。
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(getDefaultMessage())
                       .addConstraintViolation();
            }
            return false;
        }

        /**
         * デフォルトのメッセージ定義を返す。
         *
         * @return デフォルトのメッセージ定義
         */
        private String getDefaultMessage() {
            final String defaultMessage;
            if (min == 0) {
                defaultMessage = MAX_MESSAGE;
            } else if (max == 0) {
                defaultMessage = MIN_MESSAGE;
            } else if (min == max) {
                defaultMessage = FIXED_MESSAGE;
            } else {
                defaultMessage = MIN_MAX_MESSAGE;
            }
            return defaultMessage;
        }

        /**
         * 値の長さが有効な桁数かどうか。
         *
         * @param length 値の桁数。
         * @return 有効な桁数の場合は{@code true}
         */
        private boolean isValid(final int length) {
            return min <= length && length <= max();
        }

        /**
         * 許容する最大の文字数を返す。
         * <p>
         * {@link #max}が0の場合は{@link Integer#MAX_VALUE}を返す。
         * {@link #max}が0以外の場合は、その値を返す。
         *
         * @return 最大の文字数。
         */
        private int max() {
            return max == 0 ? Integer.MAX_VALUE : max;
        }
    }
}
