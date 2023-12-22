package nablarch.core.validation.ee;

import nablarch.core.util.annotation.Published;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 必須項目を表すアノテーション。
 * <p>
 *   必須項目であるプロパティに本アノテーションを次のように設定する。
 *   <pre>
 *     {@code @Required}
 *     {@code @Domain(value = "name")
 *     private String name;}
 *   </pre>
 * </p>
 * 
 * @author T.Kawasaki
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {Required.RequiredValidator.class})
@Published
public @interface Required {

    /** グループ */
    Class<?>[] groups() default {};

    /** メッセージ */
    String message() default "{nablarch.core.validation.ee.Required.message}";

    /** payload */
    Class<? extends Payload>[] payload() default {};

    /** 複数指定用のアノテーション */
    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        Required[] value();
    }

    /**
     * 必須項目が設定されていることを検証するバリデータ。
     * <p>
     *   {@link Required}アノテーションで指定されたプロパティに値が入力されているかをチェックする。
     * </p>
     *
     * @author T.Kawasaki
     */
    class RequiredValidator  implements ConstraintValidator<Required, Object> {

        /** {@inheritDoc} */
        @Override
        public void initialize(Required constraintAnnotation) {
        }

        /** {@inheritDoc} */
        @Override
        public boolean isValid(Object value, ConstraintValidatorContext context) {
            if (value == null) {
                return false;
            }
            if (value instanceof CharSequence) {
                CharSequence c = (CharSequence) value;
                return c.length() > 0;
            }
            return true;
        }
    }
}
