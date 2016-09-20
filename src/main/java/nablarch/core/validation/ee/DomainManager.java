package nablarch.core.validation.ee;

/**
 * ドメインバリデーションに使用するドメイン定義を管理するクラス。
 * <p>
 *   本インタフェースの実装クラスは、
 *   PJで作成したドメイン定義BeanのClassを返却するように実装し、
 *   コンポーネント定義に{@literal domainManager}というキーで登録する。
 *   ドメインの定義方法は{@link Domain}を参照。
 * </p>
 *
 * @author kawasima
 * @author T.Kawasaki
 */
public interface DomainManager<T> {

    /**
     * ドメインバリデーションに使用するドメイン定義BeanのClassを取得する。
     *
     * @return ドメイン定義BeanのClass
     */
    Class<T> getDomainBean();
}
