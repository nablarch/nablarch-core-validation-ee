package nablarch.core.validation.ee;

import nablarch.core.util.annotation.Published;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

/**
 * 指定されたドメイン定義に合致することを表わすアノテーション。
 * <p>
 *   <p>
 *     <b>ドメイン定義</b>
 *   </p>
 *   ドメインはBeanで定義する。
 *   Beanの各プロパティがそれぞれドメイン定義となり、
 *   プロパティ名がドメイン名となる。
 *   <pre>
 *     {@code public class SampleDomain} {
 *         {@code @Length(max = 10)}
 *         {@code @SystemChar(charsetDef="全角文字")
 *         String name;
 *     }}
 *   </pre>
 *   バリデーション条件のアノテーションの詳細は{@link nablarch.core.validation.ee}パッケージのjavadocを参照。
 *
 *   <p>
 *     <b>ドメインマネージャの設定</b>
 *   </p>
 *   上で定義したドメインを使用するためにドメインマネージャを設定する必要がある。
 *   次のように、{@link DomainManager}を実装したクラスを用意し、
 *   "domainManager" という名前でコンポーネント定義する。
 *   <pre>
 *     {@code import nablarch.core.validation.ee.DomainManager;
 *
 *     public class SampleDomainManager implements DomainManager<SampleDomain>} {
 *         {@code @Override
 *         public Class<SampleDomain> getDomainBean() {
 *             return SampleDomain.class;
 *         }
 *     }}
 *   </pre>
 *   <pre>
 *     {@code <component name="domainManager" class="com.example.SampleDomainManager"/>}
 *   </pre>
 *
 *   <p>
 *     <b>ドメイン指定</b>
 *   </p>
 *   定義したドメインをバリデーション対象のBean(Formなど)のプロパティに次のように設定する。
 *   継承時にバリデーションの設定を引き継ぐためにプロパティのgetterに本アノテーションを設定することを推奨する。
 *   <pre>
 *     {@code public class SampleBean} {
 *
 *         {@code private String name;}
 *
 *         {@code @Domain("name")
 *         public String getName() {
 *             return name;
 *         }
 *     }}
 *   </pre>
 * </p>
 *
 * @author kawasima
 * @author T.Kawasaki
 */
@Target({METHOD, FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {DomainValidator.class})
@Published
public @interface Domain {

    /** メッセージ */
    String message() default "{nablarch.core.validation.ee.Domain.message}";

    /** グループ */
    Class<?>[] groups() default {};

    /** payload */
    Class<? extends Payload>[] payload() default {};

    /** 複数指定用のアノテーション */
    @Target({METHOD, FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        Domain[] value();
    }

    /**
     *  ドメイン名
     *  <p>
     *    ドメイン定義したBeanのプロパティ名を指定する
     *  </p>
     */
    String value();
}
