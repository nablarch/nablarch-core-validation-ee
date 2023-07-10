package nablarch.core.validation.ee;

import nablarch.core.message.ApplicationException;
import nablarch.core.message.Message;
import nablarch.core.repository.SystemRepository;
import nablarch.core.util.StringUtil;
import nablarch.core.util.annotation.Published;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link Validator}に関するユーティリティクラス。
 *
 * @author T.Kawasaki
 */
public final class ValidatorUtil {

    /** {@link SystemRepository}から取得する際のキー */
    private static final String VALIDATOR_FACTORY_BUILDER = "validatorFactoryBuilder";

    /** コンストラクタ */
    private ValidatorUtil() {
    }

    /**
     * {@link Validator}インスタンスを取得する。
     * <p/>
     * {@link Validator}インスタンスは以下の手順で取得される。<br/>
     * <ol>
     *     <li>
     *         {@link SystemRepository}から"validatorFactoryBuilder"という名前で
     *         {@link ValidatorFactoryBuilder}を取得する。
     *         {@link SystemRepository}から{@link ValidatorFactoryBuilder}が取得できなかった場合は、
     *         このクラスの内部クラスとして用意されている{@link ValidatorFactoryBuilder}のデフォルトの実装が使用される。
     *     </li>
     *     <li>
     *         {@link ValidatorFactoryBuilder}を使用して、{@link ValidatorFactory}を生成する。
     *     </li>
     *     <li>
     *         {@link ValidatorFactory}から{@link Validator}インスタンスを生成して返却する。
     *     </li>
     * </ol>
     *
     * @return  {@link Validator}インスタンス
     */
    @Published
    public static Validator getValidator() {
        return getValidatorFactory().getValidator();
    }

    /**
     * キャッシュをクリアする。
     * テスト用。通常は使用しない。
     */
    public static void clearCachedValidatorFactory() {
        getValidatorFactoryBuilder().clear();
    }

    /**
     * {@link ValidatorFactory}インスタンスを取得する。
     * @return {@link ValidatorFactory}
     */
    public static ValidatorFactory getValidatorFactory() {
        return getValidatorFactoryBuilder().buildValidatorFactory();
    }

    /**
     * {@link ValidatorFactoryBuilder}インスタンスを取得する。
     * @return {@link ValidatorFactoryBuilder}
     */
    private static ValidatorFactoryBuilder getValidatorFactoryBuilder() {
        ValidatorFactoryBuilder builder = SystemRepository.get(VALIDATOR_FACTORY_BUILDER);
        if (builder == null) {
            return DefaultValidatorFactory.INSTANCE;
        }
        return builder;
    }

    /**
     * 指定されたBeanオブジェクトに対してBean Validationを行う。
     * <p/>
     * バリデーションエラーが発生した場合には、発生した全てのメッセージを持つ{@link ApplicationException}を送出する。
     *
     * @param bean Bean Validation対象のオブジェクト
     * @throws ApplicationException バリデーションエラーが発生した場合
     */
    @Published
    public static void validate(Object bean) {
        final Validator validator = getValidator();
        final Set<ConstraintViolation<Object>> constraintViolations = validator.validate(bean);
        if (!constraintViolations.isEmpty()) {
            final List<Message> messages = new ConstraintViolationConverterFactory().create().convert(constraintViolations);
            throw new ApplicationException(messages);
        }
    }

    /**
     * 指定されたBeanオブジェクトのプロパティに対してBean Validationを行う。
     * <p/>
     * {@code propertyNames}が{@code null}または空の場合は何もしない。
     * プロパティ名が重複している場合でも、バリデーションエラーの際に生成されるエラーメッセージは一つになる。
     * バリデーションエラーが発生した場合は、発生した全てのメッセージを持つ{@link ApplicationException}を送出する。
     *
     * @param bean Bean Validation対象のオブジェクト
     * @param propertyNames Bean Validation対象のプロパティ名
     * @throws ApplicationException バリデーションエラーが発生した場合
     */
    @Published
    public static void validate(Object bean, String... propertyNames) {
        if (StringUtil.hasValue(propertyNames)) {
            final Validator validator = getValidator();
            final Set<ConstraintViolation<Object>> constraintViolations = new HashSet<ConstraintViolation<Object>>();
            for (String propertyName : propertyNames) {
                constraintViolations.addAll(validator.validateProperty(bean, propertyName));
            }
            if (!constraintViolations.isEmpty()) {
                final List<Message> messages = new ConstraintViolationConverterFactory().create().convert(constraintViolations);
                throw new ApplicationException(messages);
            }
        }
    }

    /**
     * 指定されたBeanオブジェクトに対してBean Validationを行う。
     * {@code groups}に1個以上の値が存在する場合、{@code groups}をBean Validationにおけるグループとして使用する。
     * <p/>
     * バリデーションエラーが発生した場合には、発生した全てのメッセージを持つ{@link ApplicationException}を送出する。
     *
     * @see <a href="https://beanvalidation.org/1.1/spec/#constraintdeclarationvalidationprocess-groupsequence">4.4. Group and group sequence</a>
     *
     * @param bean Bean Validation対象のオブジェクト
     * @param groups Bean Validationのグループ
     * @throws ApplicationException バリデーションエラーが発生した場合
     */
    @Published
    public static void validate(Object bean, Class<?>... groups) {
        final Validator validator = getValidator();
        final Set<ConstraintViolation<Object>> constraintViolations = validator.validate(bean, groups);
        if (!constraintViolations.isEmpty()) {
            final List<Message> messages = new ConstraintViolationConverterFactory().create().convert(constraintViolations);
            throw new ApplicationException(messages);
        }
    }


    /**
     * 指定されたBeanオブジェクトのプロパティに対してBean Validationを行う。
     * {@code groups}に1個以上の値が存在する場合、{@code groups}をBean Validationにおけるグループとして使用する。
     * <p/>
     * バリデーションエラーが発生した場合には、発生した全てのメッセージを持つ{@link ApplicationException}を送出する。
     *
     * @param bean Bean Validation対象のオブジェクト
     * @param propertyName Bean Validation対象のプロパティ名
     * @param groups Bean Validationのグループ
     * @throws ApplicationException バリデーションエラーが発生した場合
     */
    @Published
    public static void validateProperty(Object bean, String propertyName, Class<?>... groups) {
        final Validator validator = getValidator();
        final Set<ConstraintViolation<Object>> constraintViolations = validator.validateProperty(bean, propertyName, groups);
        if (!constraintViolations.isEmpty()) {
            final List<Message> messages = new ConstraintViolationConverterFactory().create().convert(constraintViolations);
            throw new ApplicationException(messages);
        }

    }

    /** デフォルトの{@link ValidatorFactoryBuilder}実装。 */
    private static class DefaultValidatorFactory extends ValidatorFactoryBuilder {

        /** インスタンス */
        private static final ValidatorFactoryBuilder INSTANCE = new DefaultValidatorFactory();

        /** ファクトリクラスのインスタンス */
        private static final ValidatorFactory VALIDATOR_FACTORY = createDefaultValidatorFactory();

        /**
         * デフォルトの{@link ValidatorFactoryBuilder}インスタンスを生成する。
         * @return インスタンス
         */
        private static ValidatorFactory createDefaultValidatorFactory() {
            return Validation.byDefaultProvider()
                             .configure()
                             .messageInterpolator(new MultiLanguageMessageInterpolator())
                             .buildValidatorFactory();
        }

        /** {@inheritDoc} */
        @Override
        protected ValidatorFactory build() {
            return VALIDATOR_FACTORY;
        }
    }
}
