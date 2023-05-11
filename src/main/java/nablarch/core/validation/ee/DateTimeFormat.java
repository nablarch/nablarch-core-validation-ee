package nablarch.core.validation.ee;

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
import java.util.Locale;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 *
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {DateTimeFormat.DateTimeFormatValidator.class})
@Published
public @interface DateTimeFormat {
    /**
     * グループ
     */
    Class<?>[] groups() default {};

    /**
     * メッセージ
     */
    String message() default "{nablarch.core.validation.ee.DateTimeFormat.message}";

    /**
     * payload
     */
    Class<? extends Payload>[] payload() default {};

    Type type();

    String formatString() default "";



    enum Type {
        DATE("yyyy/MM/dd"),
        TIME("HH:mm:ss"),
        DATETIME("yyyy/MM/dd HH:mm:ss"),
        CUSTOM(null);

        final String defaultFormat;

        Type(String defaultFormat) {
            this.defaultFormat = defaultFormat;
        }

        public String getDefaultFormat() {
            return defaultFormat;
        }
    }

    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        DateTimeFormat[] value();
    }

    class DateTimeFormatValidator implements ConstraintValidator<DateTimeFormat, String> {

        private String formatString;

        @Override
        public void initialize(DateTimeFormat constraintAnnotation) {
            Type type = constraintAnnotation.type();
            String fs = constraintAnnotation.formatString();

            switch (type) {
            case DATE:
            case TIME:
            case DATETIME:
                if (!StringUtil.isNullOrEmpty(fs)) {
                    throw new IllegalArgumentException("efgh");
                }
                this.formatString = type.getDefaultFormat();
                break;
            case CUSTOM:
                if (StringUtil.isNullOrEmpty(fs)) {
                    throw new IllegalArgumentException("abcd");
                }
                this.formatString = fs;
                break;
            }
        }

        @Override
        public boolean isValid(String date, ConstraintValidatorContext context) {

            if (StringUtil.isNullOrEmpty(date)) {
                return true;
            }

            try {
                // DateUtil.getParsedDate()では、入力文字列が厳密にフォーマットに一致することを確認している。
                // そのため、単に非nullチェックを実施すればよい。
                return DateUtil.getParsedDate(date, formatString) != null;

            } catch (IllegalArgumentException ignore) {
                // 不正な引数が渡ってきた場合は、単に検証失敗とする。
                return false;

            }
        }
    }
}
