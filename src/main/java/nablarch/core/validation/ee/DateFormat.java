package nablarch.core.validation.ee;

import nablarch.core.repository.SystemRepository;
import nablarch.core.util.DateUtil;
import nablarch.core.util.StringUtil;
import nablarch.core.util.annotation.Published;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Date;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 入力値が日付書式に一致することを検証するアノテーション。
 * <p/>
 * 入力値が{@code null}または空文字列の場合は、validと判定する。<br/>
 * 上記以外の場合、入力値が以下2点を両方共満たしていれば、validと判定する。
 * <ol>
 *     <li>実在する日付であること</li>
 *     <li>設定された日付書式に一致すること</li>
 * </ol>
 * 日付書式は、以下のいずれかの方法で設定する。
 * <ol>
 *     <li>
 *         アノテーションの{@link #value()}属性に、日付書式を指定する。
 *     </li>
 *     <li>
 *         {@link #value()}が指定されていない場合、デフォルトの日付書式である yyyyMMdd が設定される。
 *         デフォルトの日付書式を変更する場合は、プロパティファイルにプロパティ名{@code nablarch.dateFormatValidator.defaultFormat}で日付書式を定義する。
 *     </li>
 * </ol>
 * <p>
 * <p/>
 * 実装例を以下に示す。
 * <pre>
 *  private static class SampleBean {
 *
 *      {@code @DateFormat}
 *      String defaultFormatDate;
 *
 *      {@code @DateFormat("yyyy-MM-dd")}
 *      String sampleFormatDate;
 *  }
 * </pre>
 *
 * @author Takayuki UCHIDA
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {DateFormat.DateFormatValidator.class})
@Published
public @interface DateFormat {
    /**
     * グループ
     */
    Class<?>[] groups() default {};

    /**
     * メッセージ
     */
    String message() default "{nablarch.core.validation.ee.DateFormat.message}";

    /**
     * payload
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * 日付書式
     */
    String value() default "";

    /**
     * 複数指定用のアノテーション
     */
    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        DateFormat[] value();
    }

    /**
     * 日付書式をバリデーションする{@link ConstraintValidator}クラス。
     */
    class DateFormatValidator implements ConstraintValidator<DateFormat, String> {

        /** デフォルト日付書式 **/
        public static final String DEFAULT_FORMAT = "yyyyMMdd";

        /** デフォルト日付書式を設定するプロパティ名 **/
        public static final String DEFAULT_FORMAT_KEY = "nablarch.dateFormatValidator.defaultFormat";

        /** 日付書式 */
        private String formatString;

        @Override
        public void initialize(DateFormat constraintAnnotation) {
            String value = constraintAnnotation.value();
            formatString = StringUtil.isNullOrEmpty(value) ? getDefaultFormat() : value;

            validateFormat(formatString);
        }

        @Override
        public boolean isValid(String date, ConstraintValidatorContext context) {
            if (StringUtil.isNullOrEmpty(date)) {
                return true;
            }
            return DateUtil.getParsedDate(date, formatString) != null;

        }

        /**
         * 指定された日付書式が有効であるか検証します。
         *
         * @param formatString 日付書式
         * @throws IllegalStateException 日付書式が不正である場合
         */
        private void validateFormat(String formatString) throws IllegalStateException {
            try {
                DateUtil.formatDate(new Date(), formatString);

            } catch (Exception e) {
                throw new IllegalStateException("Invalid date format. [" + formatString + "]");
            }
        }

        /**
         * デフォルトの日付書式を取得する。
         *
         * @return 日付書式
         */
        private String getDefaultFormat() {
            String format = SystemRepository.get(DEFAULT_FORMAT_KEY);
            if (format != null) {
                return format;
            }
            return DEFAULT_FORMAT;
        }
    }
}
