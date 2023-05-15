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
 *         区別する場合は{@link #caseSensitive()}を{@code true}に設定する（デフォルト:{@code false}）。
 *     </li>
 *     <li>
 *         列挙型が{@link WithValue}を実装している場合、入力値と{@link WithValue#getValue()}が返却する値を比較する。
 *         入力値は{@code String}もしくは{@code Number}に制限される（それ以外の場合、実行時エラーが発生する）。
 *         この場合、{@link #caseSensitive()}を指定しても無視される。
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
 *
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

    /**
     * 許容する値を含む列挙型
     */
    Class<? extends Enum<?>> value();

    /**
     * 大文字小文字を区別するか否か（{@code true}: 区別しない）
     */
    boolean caseSensitive() default false;

    /**
     * 複数指定用のアノテーション
     */
    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        EnumElement[] value();
    }

    /**
     * {@link EnumElement}で許容する列挙型の値を実装するためのインタフェース。
     *
     * @param <T> 値の型
     */
    @Published
    interface WithValue<T> {

        /**
         * 許容する値を取得する。
         *
         * @return 値
         */
        T getValue();
    }

    /**
     * 許容値を列挙型でバリデーションする{@link ConstraintValidator}クラス。
     */
    class EnumElementValidator implements ConstraintValidator<EnumElement, Object> {

        /** {@link EnumElement} の設定に応じたバリデータ */
        private Validator validator;

        /**
         * {@inheritDoc}
         */
        @Override
        public void initialize(EnumElement constraintAnnotation) {

            Class<? extends Enum<?>> enumClass = constraintAnnotation.value();

            if (WithValue.class.isAssignableFrom(enumClass)) {
                validator = new WithValueValidator((WithValue<?>[]) enumClass.getEnumConstants());
            } else {
                validator = new ConstantValidator(enumClass.getEnumConstants(), constraintAnnotation.caseSensitive());
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

        /**
         * 列挙型定数と入力値を比較するバリデータのインタフェース。
         */
        private interface Validator {

            /**
             * 入力値を検証する。
             *
             * @param value 入力値
             * @return 許容する入力値であれば {@code true}
             */
            boolean isValid(Object value);
        }

        /**
         * 列挙型要素のフィールドと入力値を比較する場合のバリデータ実装。
         */
        private static class WithValueValidator implements Validator {

            /** 列挙型要素 */
            private final WithValue<?>[] enums;

            public WithValueValidator(WithValue<?>[] enums) {

                // enumの要素数が0のときはNullPointerExceptionが発生するが、利用方法を考慮すると要素0個のenumは実装誤りなので問題ない。
                Object value = enums[0].getValue();
                if (!(value instanceof String || value instanceof Number)) {
                    throw new IllegalArgumentException("The return type of EnumElement.WithValue#getValue() must be String or Number.");
                }
                this.enums = enums;
            }

            @Override
            public boolean isValid(Object value) {
                for (WithValue<?> e : enums) {
                    if (e.getValue().equals(value)) {
                        return true;
                    }
                }
                return false;
            }
        }

        /**
         * 列挙型要素の名前と入力値を比較する場合のバリデータ実装。
         */
        private static class ConstantValidator implements Validator {

            /** 列挙型要素 */
            private final Enum<?>[] enums;

            /** 大文字小文字を区別するか否か（{@code true}: 区別しない） */
            private final boolean caseSensitive;

            ConstantValidator(Enum<?>[] enums, boolean caseSensitive) {
                this.enums = enums;
                this.caseSensitive = caseSensitive;
            }

            @Override
            public boolean isValid(Object value) {
                for (Enum<?> e : enums) {
                    // Enum要素名と比較する場合は、検証対象フィールドの型はStringのみ許容する。
                    if ((!caseSensitive && e.name().equalsIgnoreCase(value.toString())) || e.name().equals(value)) {
                        return true;
                    }
                }
                return false;
            }
        }
    }
}
