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
 *         この場合、{@link #caseInsensitive()}を指定しても無視される。
 *     </li>
 * </ol>
 * <p>
 * <p/>
 * {@link WithValue#getValue()}を実装した列挙型を使用する場合の例を以下に示す。
 * <pre>
 * public class SampleBean {
 *     {@code @EnumElement(SampleEnum.class})
 *     String sampleString;
 * }
 *
 * public enum SampleEnum implements EnumElement.WithValue{@code <String>} {
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
 * @author Takayuki UCHIDA
 */

@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {EnumElement.EnumElementValidator.class})
@Published
public @interface EnumElement {

    /**
     * グループ
     */
    Class<?>[] groups() default {};

    /**
     * メッセージ
     */
    String message() default "{nablarch.core.validation.ee.EnumElement.message}";

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
        EnumElement[] value();
    }

    interface WithValue<T> {
        T getValue();
    }

    interface Validator {
        boolean isValid(Object value);
    }

    class WithValueValidator implements Validator {

        private final Enum<?>[] enums;

        public WithValueValidator(Enum<?>[] enums) {
            this.enums = enums;
        }

        @Override
        public boolean isValid(Object value) {
            for (Enum<?> e : enums) {
                Object enValue = ((WithValue<?>) e).getValue();
                // 列挙型定数のフィールド値と入力値を比較するとき、型はString/Numberのみ許可する。
                if (enValue instanceof String && value instanceof String) {
                    if (value.equals(enValue)) {
                        return true;
                    }
                } else if (enValue instanceof Number && value instanceof Number) {
                    BigDecimal v1 = new BigDecimal(enValue.toString());
                    BigDecimal v2 = new BigDecimal(value.toString());
                    if (v2.equals(v1)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    class ConstantValidator implements Validator {

        private final Enum<?>[] enums;
        private final boolean caseInsensitive;

        ConstantValidator(Enum<?>[] enums, boolean caseInsensitive) {
            this.enums = enums;
            this.caseInsensitive = caseInsensitive;
        }

        @Override
        public boolean isValid(Object value) {
            for (Enum<?> e : enums) {
                if (value instanceof String) {
                    // Enum要素名と比較する場合は、検証対象フィールドの型はStringのみ許容する。
                    if ((caseInsensitive && ((String) value).equalsIgnoreCase(e.name())) || value.equals(e.name())) {
                        return true;
                    }
                }
            }
            return false;
        }
    }


    class EnumElementValidator implements ConstraintValidator<EnumElement, Object> {

        private Validator validator;

        /**
         * {@inheritDoc}
         */
        @Override
        public void initialize(EnumElement constraintAnnotation) {
            if (WithValue.class.isAssignableFrom(constraintAnnotation.value())) {
                validator = new WithValueValidator(constraintAnnotation.value().getEnumConstants());
            } else {
                validator = new ConstantValidator(constraintAnnotation.value().getEnumConstants(), constraintAnnotation.caseInsensitive());
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isValid(Object value, ConstraintValidatorContext context) {

            if (value == null || (value instanceof String && StringUtil.isNullOrEmpty((String) value))) {
                return true;
            }

            return validator.isValid(value);
        }

    }
}
