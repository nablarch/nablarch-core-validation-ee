package nablarch.core.validation.ee;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import nablarch.core.util.StringUtil;
import nablarch.core.util.annotation.Published;

/**
 * 入力値が指定された値の範囲内であるかチェックする。
 * 入力値が整数の場合は、{@link NumberRange}を用いること。
 * <pre>
 * salesが-1.5～1.5の範囲内であるかチェックする
 * {@code public class Sample}{
 *  {@code @DecimalRange(min = -1.5, max = 1.5)
 *  String sales;
 * }}
 * </pre>
 * <ol>
 *     <li>{@link #message()}が指定されている場合は、その値を使用する。</li>
 *     <li>{@link #message()}が未指定で{@link #min()}のみ指定の場合は、<b>{nablarch.core.validation.ee.DecimalRange.min.message}</b></li>
 *     <li>{@link #message()}が未指定で{@link #max()}のみ指定の場合は、<b>{nablarch.core.validation.ee.DecimalRange.max.message}</b></li>
 *     <li>{@link #message()}が未指定で{@link #min()}と{@link #max()}を指定の場合は、<b>{nablarch.core.validation.ee.DecimalRange.min.max.message}</b></li>
 * </ol>
 *
 * @author Ukawa Shohei
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {DecimalRange.DecimalRangeValidator.class})
@Published
public @interface DecimalRange {

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
        DecimalRange[] value();
    }

    /** 数値の最小値。*/
    String min() default "";

    /** 数値の最大値。*/
    String max() default "";

    /**
     * 指定された実数の範囲の数値であることを検証するバリデータ。
     */
    class DecimalRangeValidator extends RangeValidatorSupport<DecimalRange> {

        /** 最小値のみ指定のメッセージ */
        private static final String MIN_MESSAGE = "{nablarch.core.validation.ee.DecimalRange.min.message}";

        /** 最大値のみ指定のメッセージ */
        private static final String MAX_MESSAGE = "{nablarch.core.validation.ee.DecimalRange.max.message}";

        /** 最小値、最大値のみ指定のメッセージ */
        private static final String MIN_MAX_MESSAGE = "{nablarch.core.validation.ee.DecimalRange.min.max.message}";

        /** {@link nablarch.core.validation.ee.DecimalRange} */
        private DecimalRange decimalRange;

        @Override
        public void initialize(final DecimalRange constraintAnnotation) {
            super.initialize(constraintAnnotation);
            decimalRange = constraintAnnotation;
        }

        @Override
        protected Range getRange(DecimalRange constraintAnnotation) {
            return new Range(
                    getDecimalValue(constraintAnnotation.max()),
                    getDecimalValue(constraintAnnotation.min()));
        }

        @Override
        protected void buildMessage(final ConstraintValidatorContext context) {
            if (StringUtil.isNullOrEmpty(decimalRange.message())) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(getDefaultMessage())
                       .addConstraintViolation();
            }
        }

        /**
         * デフォルトのメッセージを返す。
         *
         * @return デフォルトのメッセージ
         */
        public String getDefaultMessage() {
            final String defaultMessage;
            if (StringUtil.isNullOrEmpty(decimalRange.max())) {
                defaultMessage = MIN_MESSAGE;
            } else if (StringUtil.isNullOrEmpty(decimalRange.min())) {
                defaultMessage = MAX_MESSAGE;
            } else {
                defaultMessage = MIN_MAX_MESSAGE;
            }
            return defaultMessage;
        }
    }
}
