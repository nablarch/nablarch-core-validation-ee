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
import java.util.Collection;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

/**
 * 要素数が指定した値の範囲内であるかチェックするアノテーション。
 * <pre>
 * arrayの要素数が1～3の範囲内であるかチェック
 * {@code public class Sample}{
 *     {@code @Size(min = 1, max = 3)
 *     String[] array;
 * }}
 *
 * arrayの要素数が2以上であるかチェック
 * {@code public class Sample}{
 *     {@code @Size(min = 2)
 *     String[] array;
 * }}
 * </pre>
 * 
 * エラー時のメッセージは、以下のルールにより決定される。
 * <ol>
 *     <li>{@link #message()}が指定されている場合は、その値を使用する。</li>
 *     <li>{@link #message()}が未指定で{@link #min()}のみ指定の場合は、<b>{nablarch.core.validation.ee.Size.min.message}</b></li>
 *     <li>{@link #message()}が未指定で{@link #max()}のみ指定の場合は、<b>{nablarch.core.validation.ee.Size.max.message}</b></li>
 *     <li>{@link #message()}が未指定で{@link #min()}と{@link #max()}を指定の場合は、<b>{nablarch.core.validation.ee.Size.min.max.message}</b></li>
 * </ol>
 *
 * @author T.Kawasaki
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {
        Size.ArraySizeValidator.class,
        Size.CollectionSizeValidator.class
})
@Published
public @interface Size {

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
        Size[] value();
    }

    /** 最小値 */
    int min() default 0;

    /** 最大値 */
    int max() default 0;

    /**
     * 要素数をバリデーションする{@link ConstraintValidator}実装クラス（抽象クラス）。
     */
    abstract class AbstractSizeValidator<T> implements ConstraintValidator<Size, T> {

        /** 最大要素数 */
        private int max;

        /** 最小要素数 */
        private int min;

        /** メッセージ */
        private String message;

        /** デフォルトメッセージ(最小値のみ) */
        private static final String MIN_MESSAGE = "{nablarch.core.validation.ee.Size.min.message}";

        /** デフォルトメッセージ(最大値のみ) */
        private static final String MAX_MESSAGE = "{nablarch.core.validation.ee.Size.max.message}";

        /** デフォルトメッセージ(最小、最大ともに指定) */
        private static final String MIN_MAX_MESSAGE = "{nablarch.core.validation.ee.Size.min.max.message}";

        /** {@inheritDoc} */
        @Override
        public void initialize(Size constraintAnnotation) {
            max = constraintAnnotation.max();
            min = constraintAnnotation.min();
            message = constraintAnnotation.message();
        }

        /** {@inheritDoc} */
        @Override
        public boolean isValid(T value, ConstraintValidatorContext context) {
            if (value == null) {
                return true;
            }
            final int size = getActualSizeOf(value);

            if (isValid(size)) {
                return true;
            }

            if (StringUtil.isNullOrEmpty(message)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(getDefaultMessage())
                       .addConstraintViolation();
            }
            return false;
        }

        private String getDefaultMessage() {
            final String defaultMessage;
            if (min == 0) {
                defaultMessage = MAX_MESSAGE;
            } else if (max == 0) {
                defaultMessage = MIN_MESSAGE;
            } else {
                defaultMessage = MIN_MAX_MESSAGE;
            }
            return defaultMessage;
        }

        private boolean isValid(final int size) {
            return min <= size && size <= max();
        }

        private int max() {
            return max == 0 ? Integer.MAX_VALUE : max;
        }

        /**
         * 与えられたオブジェクトのサイズを取得する。
         *
         * @param value サイズ取得元オブジェクト(nullでない)
         * @return サイズ
         * @throws IllegalStateException 引数が配列、コレクションでない場合
         */
        abstract protected int getActualSizeOf(T value);
    }

    /**
     * 配列の要素数をバリデーションする{@link ConstraintValidator}実装クラス。
     */
    class ArraySizeValidator extends Size.AbstractSizeValidator<Object[]> {

        /** {@inheritDoc} */
        @Override
        protected int getActualSizeOf(Object[] value) {
            return value.length;
        }
    }

    /**
     * コレクションの要素数をバリデーションする{@link ConstraintValidator}実装クラス。
     */
    class CollectionSizeValidator extends Size.AbstractSizeValidator<Collection<?>> {

        /** {@inheritDoc} */
        @Override
        protected int getActualSizeOf(Collection<?> value) {
            return value.size();
        }
    }

}
