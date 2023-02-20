package nablarch.core.validation.ee;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.validation.ValidatorFactory;

/**
 * {@link ValidatorFactory}を生成するクラス。
 *
 * @author T.Kawasaki
 */
public abstract class ValidatorFactoryBuilder {

    /**
     * 自身のインスタンスが生成した{@link ValidatorFactory}をキャッシュする。
     */
    private final Map<ValidatorFactoryBuilder, ValidatorFactory> cache = new ConcurrentHashMap<ValidatorFactoryBuilder, ValidatorFactory>();

    /**
     * {@link ValidatorFactory}を組み立てる。
     * キャッシュに存在する場合は、キャッシュのインスタンスを使用する。
     *
     * @return {@link ValidatorFactory}インスタンス
     */
    public ValidatorFactory buildValidatorFactory() {
        ValidatorFactory validatorFactory = cache.get(this);
        if (validatorFactory != null) {
            return validatorFactory;
        }
        synchronized (cache) {
            validatorFactory = cache.get(this);
            if (validatorFactory == null) {
                validatorFactory = build();
                cache.put(this, validatorFactory);
            }
            return validatorFactory;
        }
    }

    /**
     * キャッシュをクリアする。
     * テスト用。通常は使用しない。
     */
    void clear() {
        cache.clear();
    }

    /**
     * {@link ValidatorFactory}を組み立てる。
     *
     * @return {@link ValidatorFactory}インスタンス
     */
    protected abstract ValidatorFactory build();
}
