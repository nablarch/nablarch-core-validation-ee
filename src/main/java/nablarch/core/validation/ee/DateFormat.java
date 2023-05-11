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
 *
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
         *
         * @param formatString
         */
        private void validateFormat(String formatString) {
            try {
                DateUtil.formatDate(new Date(), formatString);

            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Invalid date format string. ["+ formatString +"]");
            }
        }

        @Override
        public boolean isValid(String date, ConstraintValidatorContext context) {

            if (StringUtil.isNullOrEmpty(date)) {
                return true;
            }

            // DateUtil.getParsedDate()では、入力文字列が厳密にフォーマットに一致することを確認している。
            // そのため、単に非nullチェックを実施すればよい。
            return DateUtil.getParsedDate(date, formatString) != null;

        }

        /**
         * @return
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
