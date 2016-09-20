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
import java.math.BigDecimal;

import javax.validation.Constraint;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import nablarch.core.util.StringUtil;
import nablarch.core.util.annotation.Published;

/**
 * 入力値が指定の範囲内であるかチェックする。
 * 入力値が実数の場合は、{@link DecimalRange}を用いること。
 * <pre>
 * 入力値が1以上10以下の範囲内であるかチェックする
 * {@code public class Sample}{
 *     {@code @NumberRange(min = 1, max = 10)
 *     String sales;
 * }}
 *
 * 入力値が0以上であるかチェックする
 * {@code public class Sample}{
 *     {@code @NumberRange(min = 0)
 *     String sales;
 * }}
 * </pre>
 * エラー時のメッセージは、以下のルールにより決定される。
 * <ol>
 *     <li>{@link #message()}が指定されている場合は、その値を使用する。</li>
 *     <li>{@link #message()}が未指定で{@link #min()}のみ指定の場合は、<b>{nablarch.core.validation.ee.NumberRange.min.message}</b></li>
 *     <li>{@link #message()}が未指定で{@link #max()}のみ指定の場合は、<b>{nablarch.core.validation.ee.NumberRange.max.message}</b></li>
 *     <li>{@link #message()}が未指定で{@link #min()}と{@link #max()}を指定の場合は、<b>{nablarch.core.validation.ee.NumberRange.min.max.message}</b></li>
 * </ol>
 *
 * @author T.Kawasaki
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {NumberRange.NumberRangeValidator.class})
@Published
public @interface NumberRange {

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
        NumberRange[] value();
    }


    /** 数値の最小値。*/
    long min() default Long.MIN_VALUE;

    /** 数値の最大値。*/
    long max() default Long.MAX_VALUE;

    /**
     * 指定された整数の範囲の数値であることを検証するバリデータ。
     */
    class NumberRangeValidator extends RangeValidatorSupport<NumberRange> {

        /** 最大値のみ指定のメッセージ */
        private static final String MAX_MESSAGE = "{nablarch.core.validation.ee.NumberRange.max.message}";

        /** 最小値のみ指定のメッセージ */
        private static final String MIN_MESSAGE = "{nablarch.core.validation.ee.NumberRange.min.message}";

        /** 最小値、最大値ともに指定のメッセージ */
        private static final String MIN_MAX_MESSAGE = "{nablarch.core.validation.ee.NumberRange.min.max.message}";

        /** {@link nablarch.core.validation.ee.NumberRange} */
        private NumberRange numberRange;

        @Override
        public void initialize(final NumberRange constraintAnnotation) {
            super.initialize(constraintAnnotation);
            numberRange = constraintAnnotation;
        }

        @Override
        protected Range getRange(NumberRange constraintAnnotation) {
            return new Range(BigDecimal.valueOf(constraintAnnotation.max()),
                    BigDecimal.valueOf(constraintAnnotation.min()));
        }

        @Override
        protected void buildMessage(final ConstraintValidatorContext context) {
            if (StringUtil.isNullOrEmpty(numberRange.message())) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(getDefaultMessage())
                       .addConstraintViolation();
            }
        }

        /**
         * デフォルトメッセージを返す。
         *
         * @return デフォルトのメッセージ
         */
        private String getDefaultMessage() {
            final String defaultMessage;
            if (numberRange.min() == Long.MIN_VALUE) {
                defaultMessage = MAX_MESSAGE;
            } else if (numberRange.max() == Long.MAX_VALUE) {
                defaultMessage = MIN_MESSAGE;
            } else {
                defaultMessage = MIN_MAX_MESSAGE;
            }
            return defaultMessage;
        }
    }
}
