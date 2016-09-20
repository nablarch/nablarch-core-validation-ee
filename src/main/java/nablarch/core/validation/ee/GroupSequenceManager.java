package nablarch.core.validation.ee;

import nablarch.core.util.annotation.Published;

/**
 * BeanValidationのバリデーション順序を定義するインタフェース。
 * <p/>
 * バリデーション順序をカスタマイズしたい場合、
 * 本インタフェースを実装したクラスをコンポーネント定義に{@literal groupSequenceManager}というキーで登録する。
 * 
 * @author Ryo Asato
 */
@Published
public interface GroupSequenceManager {

    /**
     * {@link javax.validation.GroupSequence}を定義したインタフェースの{@link Class}を取得する。
     * 
     * @return グループシーケンスを既定したインタフェース
     */
    Class<?> getGroupSequence();

}