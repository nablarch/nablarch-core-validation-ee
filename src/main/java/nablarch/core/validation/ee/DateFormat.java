package nablarch.core.validation.ee;

import nablarch.core.repository.SystemRepository;
import nablarch.core.util.DateUtil;
import nablarch.core.util.StringUtil;
import nablarch.core.util.annotation.Published;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
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
 *     <li>{@link java.text.SimpleDateFormat}の厳密な日時解析の結果、入力値が正当な日付と判定されること。</li>
 *     <li>入力値が、{@link java.text.SimpleDateFormat}に準拠した日付書式に一致していること。</li>
 * </ol>
 * 日付書式の設定方法は、優先度の高い順に以下の3つとなる（優先度の高い設定方法での値は、優先度の低い設定方法での値を上書きする）。
 * <ol>
 *     <li>
 *         アノテーションの{@link #value()}属性に、日付書式文字列を設定する。
 *         アノテーションを付与する対象毎に設定する日付書式を変更したい場合は、この方法を使用する。
 *     </li>
 *     <li>
 *         {@code nablarch.dateFormatValidator.defaultFormat}をキーとしたプロパティに、日付書式文字列を設定する。
 *         このプロパティは、システムリポジトリ構築時に読み込まれるいずれかのプロパティファイルに記載する必要がある。
 *         この方法で日付書式を設定する場合は、アノテーションの{@link #value()}属性は設定しない。
 *         アプリケーション全体でデフォルトの日付書式を設定したい場合は、この方法を使用する。
 *     </li>
 *     <li>
 *         モジュール内に、デフォルトの日付書式{@code yyyyMMdd}がハードコードされている。
 *         これは、上記2つの方法いずれも実施しなかった場合に選択される日付書式である。
 *         後方互換のために設定であり、通常使用することは想定されていない。
 *         そのため、この日付書式がプロジェクト要件を満たすものであっても、上記2つの方法のいずれかで日付書式文字列を設定すること。
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

    String value() default "";

    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        DateFormat[] value();
    }

    class DateFormatValidator implements ConstraintValidator<DateFormat, String> {

        public static final String DEFAULT_FORMAT = "yyyyMMdd";
        public static final String PROPS_DEFAULT_FORMAT = "nablarch.dateFormatValidator.defaultFormat";
        private String formatString;

        @Override
        public void initialize(DateFormat constraintAnnotation) {
            String value = constraintAnnotation.value();

            this.formatString = StringUtil.isNullOrEmpty(value) ? getDefaultFormat() : value;

            validateFormat(formatString);
        }

        /**
         * @param formatString
         */
        private void validateFormat(String formatString) {
            try {
                DateUtil.formatDate(new Date(), formatString);

            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Invalid date format string. [" + formatString + "]");
            }
        }

        @Override
        public boolean isValid(String date, ConstraintValidatorContext context) {
            if (StringUtil.isNullOrEmpty(date)) {
                return true;
            }
            return DateUtil.getParsedDate(date, formatString) != null;

        }

        /**
         * デフォルトの日付書式を取得する。
         *
         * @return 日付書式
         */
        private String getDefaultFormat() {
            String format = SystemRepository.get(PROPS_DEFAULT_FORMAT);
            if (format != null) {
                return format;
            }
            return DEFAULT_FORMAT;
        }


    }
}
