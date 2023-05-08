package nablarch.core.validation.ee;

import nablarch.core.util.StringUtil;
import nablarch.core.util.annotation.Published;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.math.BigDecimal;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 許容する値を列挙型で指定するためのアノテーション。
 * <p/>
 * 入力値が{@code null}または空文字列の場合は、validと判定する。<br/>
 * 上記以外の場合は、入力値と{@link #value()}で指定された列挙型定数を比較し、一致するものが存在すれば検証成功と判定される。
 * 比較ロジックは以下の通りとなる。
 * <ol>
 *     <li>
 *         列挙型が{@link WithValue}を実装していない場合、入力値と列挙型定数の名前（{@link Enum#name()}で取得した値）を比較する。
 *         入力値は{@code String}に制限される（それ以外の場合、実行時エラーが発生する）。
 *         デフォルトでは、比較時に入力値及び列挙型定数の大文字小文字は区別しない。
 *         区別する場合は{@link #caseInsensitive()}を{@code false}に設定する（デフォルト:{@code true}）。
 *     </li>
 *     <li>
 *         列挙型が{@link WithValue}を実装している場合、入力値と{@link WithValue#getValue()}が返却する値を比較する。
 *         入力値は{@code String}もしくは{@code Number}に制限される（それ以外の場合、実行時エラーが発生する）。
 *         この場合、{@link #caseInsensitive()}を指定しても効果は無い（{@link WithValue#getValue()}で自由に制御できるため）。
 *     </li>
 * </ol>
 * <p>
 * <p/>
 * {@link WithValue#getValue()}を実装した列挙型を使用する場合の例を以下に示す。
 * <pre>
 * public class SampleBean {
 *     {@code @IncludedIn(SampleEnum.class})
 *     String sampleString;
 * }
 *
 * public enum SampleEnum implements EnValue{@code <String>} {
 *     ON("1"), OFF("0");
 *
 *     private final String value;
 *
 *     SampleEnum(String value) {
 *         this.value = value;
 *     }
 *
 *     {@code @Override}
 *     public String getValue() {
 *         return value;
 *     }
 * }
 * </pre>
 * エラー時のメッセージは、以下のルールにより決定される。
 * <ol>
 *     <li>{@link #message()}が指定されている場合は、その値を使用する。</li>
 *     <li>{@link #message()}が未指定で入力値に一致する列挙型定数が存在しない場合は、<b>{nablarch.core.validation.ee.IncludedIn.noElement.message}</b></li>
 *     <li>{@link #message()}が未指定で入力値の型と列挙型定数フィールドの型が一致しない場合は、<b>{nablarch.core.validation.ee.IncludedIn.typeMismatch.message}</b></li>
 * </ol>
 *
 * @author Takayuki UCHIDA
 */

@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {IncludedIn.IncludedInValidator.class})
@Published
public @interface IncludedIn {

    /**
     * グループ
     */
    Class<?>[] groups() default {};

    /**
     * メッセージ
     */
    String message() default "";

    /**
     * payload
     */
    Class<? extends Payload>[] payload() default {};

    Class<? extends Enum<?>> value();

    /**
     * 大文字小文字を区別するか否か（{@code true}: 区別しない）
     */
    boolean caseInsensitive() default true;

    /**
     * 複数指定用のアノテーション
     */
    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        IncludedIn[] value();
    }

    interface WithValue<T> {
        T getValue();
    }

    class IncludedInValidator implements ConstraintValidator<IncludedIn, Object> {

        private static final String NO_ELEMENT_MESSAGE = "{nablarch.core.validation.ee.IncludedIn.noElement.message}";
        private static final String TYPE_MISMATCH_MESSAGE = "{nablarch.core.validation.ee.IncludedIn.typeMismatch.message}";
        private Enum<?>[] enums;
        private boolean caseInsensitive;
        private String message;

        /**
         * {@inheritDoc}
         */
        @Override
        public void initialize(IncludedIn constraintAnnotation) {
            this.enums = constraintAnnotation.value().getEnumConstants();
            this.caseInsensitive = constraintAnnotation.caseInsensitive();
            this.message = constraintAnnotation.message();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isValid(Object value, ConstraintValidatorContext context) {

            if (value == null || value instanceof String && StringUtil.isNullOrEmpty((String) value)) {
                return true;
            }

            for (Enum<?> e : this.enums) {
                if (e instanceof WithValue) {
                    Object enValue = ((WithValue<?>) e).getValue();
                    // 列挙型定数のフィールド値と入力値を比較するとき、型はString/Numberのみ許可する。
                    if (enValue instanceof String && value instanceof String ||
                        enValue instanceof BigDecimal && value instanceof BigDecimal) {
                        if (value.equals(enValue)) {
                            return true;
                        }
                    } else if (enValue instanceof Number && value instanceof Number) {
                        BigDecimal v1 = new BigDecimal(enValue.toString());
                        BigDecimal v2 = new BigDecimal(value.toString());
                        if (v2.equals(v1)) {
                            return true;
                        }
                    } else {
                        // 型が一致しないときは、検証エラーとする。
                        buildMessage(context, TYPE_MISMATCH_MESSAGE);
                        return false;
                    }
                } else {
                    if (value instanceof String) {
                        // Enum要素名と比較する場合は、検証対象フィールドの型はStringのみ許容する。
                        if (caseInsensitive && value.toString().equalsIgnoreCase(e.name()) || value.toString().equals(e.name())) {
                            return true;
                        }
                    } else {
                        // 型が一致しないときは、検証エラーとする。
                        buildMessage(context, TYPE_MISMATCH_MESSAGE);
                        return false;
                    }
                }
            }

            buildMessage(context, NO_ELEMENT_MESSAGE);
            return false;
        }

        /**
         * エラー時のメッセージを構築する
         *
         * @param context        制約バリデーションのコンテキスト（{@link ConstraintValidatorContext}）
         * @param defaultMessage デフォルトメッセージ
         */
        private void buildMessage(final ConstraintValidatorContext context, final String defaultMessage) {
            if (StringUtil.isNullOrEmpty(message)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(defaultMessage)
                    .addConstraintViolation();
            }
        }
    }
}
