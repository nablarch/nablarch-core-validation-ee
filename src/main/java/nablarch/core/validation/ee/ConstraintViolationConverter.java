package nablarch.core.validation.ee;

import nablarch.core.message.Message;
import nablarch.core.message.StringResource;
import nablarch.core.util.annotation.Published;
import nablarch.core.validation.ValidationResultMessage;

import javax.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Bean Validationのバリデーション結果を{@link Message}に変換するクラス。
 *
 * @author T.Kawasaki
 */
@Published
public class ConstraintViolationConverter {

    /** バリデーション対象オブジェクトのプロパティ名のプレフィクス */
    private String prefix = "";

    /**
     * ConstraintViolationConverterオブジェクトを生成する。
     */
    public ConstraintViolationConverter() {
    }

    /**
     * 指定された{@code prefix}を保持したConstraintViolationConverterオブジェクトを生成する。
     *
     * @param prefix バリデーション対象オブジェクトのプロパティ名に付与するプレフィクス
     */
    public ConstraintViolationConverter(String prefix) {
        this.prefix = prefix + ".";
    }

    /**
     * Bean Validationのバリデーション結果を{@link Message}に変換する。
     *
     * @param <BEAN> バリデーション対象Beanの型
     * @param violations BeanValidationのバリデーション結果
     * @return バリデーション結果のリスト
     *          (BeanValidationのバリデーション結果が空の場合は空のリストを返す)
     */
    public <BEAN> List<Message> convert(Set<ConstraintViolation<BEAN>> violations) {
        List<Message> result = new ArrayList<Message>(violations.size());
        for (ConstraintViolation<BEAN> violation : violations) {
            result.add(convert(violation));
        }
        return result;
    }

    /**
     * Bean Validationのバリデーション結果を{@link Message}に変換する。
     *
     * @param violation BeanValidationのバリデーション結果
     * @return バリデーション結果のメッセージ
     */
    public Message convert(ConstraintViolation<?> violation) {
        String propertyName = prefix + violation.getPropertyPath().toString();
        StringResource stringResource = new ViolationBasedStringResource(violation);
        return new ValidationResultMessage(propertyName, stringResource, null);
    }

    /**
     * {@link ConstraintViolation}をもとにした{@link StringResource}の実装クラス。
     */
    private static class ViolationBasedStringResource implements StringResource {

        /** 発生した{@link ConstraintViolation} */
        private final ConstraintViolation<?> violation;

        /**
         * コンストラクタ
         * @param violation 発生した{@link ConstraintViolation}
         */
        ViolationBasedStringResource(ConstraintViolation<?> violation) {
            this.violation = violation;
        }

        /** {@inheritDoc} */
        @Override
        public String getId() {
            return violation.getConstraintDescriptor()
                            .getAnnotation()
                            .annotationType()
                            .getName();
        }

        /** {@inheritDoc} */
        @Override
        public String getValue(Locale notUsed) {
            return violation.getMessage();
        }
    }

}
