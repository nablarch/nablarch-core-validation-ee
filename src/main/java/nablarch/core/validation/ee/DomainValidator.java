package nablarch.core.validation.ee;

import java.util.Set;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import nablarch.core.repository.SystemRepository;

/**
 * ドメインバリデーションを行う{@link ConstraintValidator}実装クラス。
 * <p>
 *   {@link Domain}アノテーションが設定されたプロパティに対してバリデーションを行う。
 *   設定方法などの使い方は{@link Domain}のjavadocを参照。
 * </p>
 * @author kawasima
 * @author T.Kawasaki
 */
public class DomainValidator implements ConstraintValidator<Domain, Object> {

    /** ドメイン定義Bean */
    private Class<Object> domainBean;

    /** {@link Validator}インスタンス */
    private Validator validator;

    /** ドメイン名(ドメイン定義Beanのプロパティ名) */
    private String domainName;

    /** {@inheritDoc} */
    @Override
    public void initialize(Domain constraintAnnotation) {
        validator = ValidatorUtil.getValidator();
        domainBean = getDomainBeanClass();
        domainName = constraintAnnotation.value();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {

        // デフォルトの制約をオフにする。
        context.disableDefaultConstraintViolation();

        // ドメイン定義Beanのプロパティに付与されたバリデーション用のアノテーションで、入力値をバリデーションする。
        Set<ConstraintViolation<Object>> violations
                = validator.validateValue(domainBean,  // ドメイン定義Bean
                                          domainName,  // ドメイン名（ドメイン定義Beanのプロパティ名）
                                          value        // バリデーション対象となる値
        );

        // 全バリデーション結果をConstraintValidatorContextに通知する。
        for (ConstraintViolation<Object> cv : violations) {
            context.buildConstraintViolationWithTemplate(cv.getMessage())
                   .addConstraintViolation();
        }

        return violations.isEmpty();
    }

    /** {@link SystemRepository}から{@link DomainManager}インスタンスを取得する際のキー名 */
    static final String DOMAIN_MANAGER_KEY = "domainManager";

    /**
     * {@link DomainManager}インスタンスを取得する。
     *
     * @param <T> ドメイン定義Beanの型
     * @return インスタンス。
     */
    <T> DomainManager<T> getDomainManager() {
        DomainManager<T> manager = SystemRepository.get(DOMAIN_MANAGER_KEY);
        if (manager == null) {
            throw new IllegalStateException(
                    "DomainManager must be registered in SystemRepository. key=[" + DOMAIN_MANAGER_KEY + "]");
        }
        return manager;
    }

    /**
     * ドメイン定義Beanの{@link Class}を取得する。
     *
     * @param <T> ドメイン定義Beanの型
     * @return ドメイン定義BeanのClass
     */
    <T> Class<T> getDomainBeanClass() {
        return this.<T>getDomainManager().getDomainBean();
    }
}
