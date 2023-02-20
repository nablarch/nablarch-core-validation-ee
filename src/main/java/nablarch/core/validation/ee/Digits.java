package nablarch.core.validation.ee;

import nablarch.core.util.StringUtil;
import nablarch.core.util.annotation.Published;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

/**
 * 数値の整数部、小数部が指定された桁数以内であることを表すアノテーション。
 * <p/>
 * 入力値が{@code null}または空文字列の場合は、validと判定する。<br/>
 * {@link #fraction()}が未指定の場合は、{@link #fraction()}を{@code 0}として判定を行う。
 * つまり、入力値が整数であるかを判定することになる。
 * <p/>
 * 整数部3桁、小数部4桁の例を以下に示す。
 * <pre>
 * public class SampleBean {
 *  {@code @Digits(integer} = 3, fraction = 4)
 *  String sampleString;
 * </pre>
 * エラー時のメッセージは、以下のルールにより決定される。
 * <ol>
 *     <li>{@link #message()}が指定されている場合は、その値を使用する。</li>
 *     <li>{@link #message()}が未指定で{@link #integer()}のみ指定の場合は、<b>{nablarch.core.validation.ee.Digits.integer.message}</b></li>
 *     <li>{@link #message()}が未指定で{@link #integer()}と{@link #fraction()}を指定の場合は、<b>{nablarch.core.validation.ee.Digits.message}</b></li>
 * </ol>
 *
 * @author Sumida
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {Digits.DigitsValidator.class})
@Published
public @interface Digits {

    /** グループ */
    Class<?>[] groups() default {};

    /** メッセージ */
    String message() default "";

    /** payload */
    Class<? extends Payload>[] payload() default {};

    /** 複数指定用のアノテーション */
    @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        Digits[] value();
    }

    /** 整数部の桁数。 */
    int integer();

    /** 小数部の桁数。 */
    int fraction() default 0;

    /**
     * 桁数をバリデーションする{@link ConstraintValidator}クラス。
     */
    class DigitsValidator extends NumberValidatorSupport<Digits> {

        /** 整数部のみのメッセージ */
        private static final String INTEGER_MESSAGE = "{nablarch.core.validation.ee.Digits.integer.message}";

        /** 整数、小数ともに指定時のメッセージ */
        private static final String MESSAGE = "{nablarch.core.validation.ee.Digits.message}";

        /** 整数部の桁数 */
        private int integer;

        /** 小数部の桁数 */
        private int fraction;

        /** メッセージ */
        private String message;

        @Override
        public void initialize(Digits constraintAnnotation) {
            this.integer = constraintAnnotation.integer();
            this.fraction = constraintAnnotation.fraction();
            message = constraintAnnotation.message();
        }

        @Override
        protected boolean isValid(BigDecimal value) {
            final int integerLength = value.precision() - value.scale();
            final int fractionLength = value.scale() < 0 ? 0 : value.scale();
            return integer >= integerLength && fraction >= fractionLength;
        }

        @Override
        protected void buildMessage(final ConstraintValidatorContext context) {
            if (StringUtil.isNullOrEmpty(message)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(getDefaultMessage())
                       .addConstraintViolation();
            }
        }

        /**
         * デフォルトのメッセージを返す。
         *
         * @return メッセージ
         */
        private String getDefaultMessage() {
            return fraction == 0 ? INTEGER_MESSAGE : MESSAGE;
        }
    }
}