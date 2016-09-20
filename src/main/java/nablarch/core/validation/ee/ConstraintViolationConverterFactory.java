package nablarch.core.validation.ee;

import nablarch.core.repository.SystemRepository;
import nablarch.core.util.annotation.Published;

/**
 * {@link ConstraintViolationConverter}を生成するファクトリクラス。
 * <p>
 * {@link SystemRepository}に{@code ConstraintViolationConverterFactory}が設定されている場合は、そのファクトリクラスに処理を委譲する。
 *
 * @author Hisaaki Shioiri
 */
@Published(tag = "architect")
public class ConstraintViolationConverterFactory {

    /**
     * {@link ConstraintViolationConverter}を生成する。
     *
     * @return {@code ConstraintViolationConverter}
     */
    public ConstraintViolationConverter create() {
        final ConstraintViolationConverterFactory factory = getFactoryClass();
        if (factory != null) {
            return factory.create();
        } else {
            return new ConstraintViolationConverter();
        }
    }

    /**
     * プレフィックス付きの{@link ConstraintViolationConverter}を生成する。
     *
     * @param prefix プレフィックス
     * @return {@code ConstraintViolationConverter}
     */
    public ConstraintViolationConverter create(final String prefix) {
        final ConstraintViolationConverterFactory factory = getFactoryClass();
        if (factory != null) {
            return factory.create(prefix);
        } else {
            return new ConstraintViolationConverter(prefix);
        }
    }

    /**
     * {@link SystemRepository}から委譲先の{@code ConstraintViolationConverterFactory}の実装を取得する。
     *
     * @return 委譲先の{@code ConstraintViolationConverterFactory}の実装クラス。存在しない場合は{@code null}。
     */
    private ConstraintViolationConverterFactory getFactoryClass() {
        return SystemRepository.get("constraintViolationConverterFactory");
    }
}
