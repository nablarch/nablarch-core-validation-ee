package nablarch.core.validation.ee;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import nablarch.core.repository.SystemRepository;
import nablarch.core.util.annotation.Published;
import nablarch.core.validation.validator.unicode.CharsetDefValidationUtil;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

/**
 * システム許容文字で構成された文字列であることを表わすアノテーション。
 * <p>
 *   許容文字集合 "全角文字" を次のように定義する。
 *   許容文字集合の定義方法は、範囲指定やリテラル指定などいくつかあるので、詳細は{@link nablarch.core.validation.validator.unicode}パッケージのjavadocを参照。
 *   <pre>
 *      {@code <component name="全角文字" class="nablarch.core.validation.validator.unicode.RangedCharsetDef">
 *         <!-- 省略 -->
 *     </component>}
 *   </pre>
 *   上で定義した許容文字集合 "全角文字" のバリデーションを行うドメインを次のように定義する。
 *   <pre>
 *      {@code public class SampleDomain }{
 *          {@code @Length(max = 10)}
 *          {@code @SystemChar(charsetDef="全角文字")
 *         String name;
 *     }}
 *   </pre>
 *   このドメイン定義を使用して、バリデーションを行う設定については{@link Domain}のjavadocを参照。
 * </p>
 *
 * <p>
 *   このバリデーションでは、デフォルトではサロゲートペアを許容しない。
 *   （例え{@link nablarch.core.validation.validator.unicode.LiteralCharsetDef LiteralCharsetDef}で明示的にサロゲートペアの文字を定義していても許容しない）
 * </p>
 * <p>
 *   サロゲートペアを許容する場合は次のようにコンポーネント設定ファイルに{@link SystemCharConfig}を設定する必要がある。
 * </p>
 * <pre>
 *   {@code <component name="ee.SystemCharConfig" class="nablarch.core.validation.ee.SystemCharConfig">
 *     <property name="allowSurrogatePair" value="true"/>
 *   </component>}
 * </pre>
 * 
 * @author T.Kawasaki
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = { SystemChar.SystemCharValidator.class})
@Published
public @interface SystemChar {

    /** グループ */
    Class<?>[] groups() default {};

    /** メッセージ */
    String message() default "{nablarch.core.validation.ee.SystemChar.message}";

    /** payload */
    Class<? extends Payload>[] payload() default {};

    /** 複数指定用のアノテーション */
    @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        /** バリデーション用アノテーション */
        SystemChar[] value();
    }

    /** 許容文字集合定義の名称
     * <p>
     *   コンポーネントに定義された{@link nablarch.core.validation.validator.unicode.CharsetDef}の名前を指定する。
     * </p>
     */
    String charsetDef() default "";

    /**
     * 改行コードを許容するかどうか。
     * デフォルトは{@code false}（許容しない）。
     */
    boolean allowLineSeparator() default false;

    /**
     * システム許容文字のバリデーションを行う{@link ConstraintValidator}実装クラス。
     * <p>
     *   {@link SystemChar}アノテーションで設定された許容文字であるかをバリデーションする。
     * </p>
     */
    class SystemCharValidator implements ConstraintValidator<SystemChar, String> {

        /** {@link SystemRepository}に定義されている設定名 */
        private static final String CONFIG_NAME = "ee.SystemCharConfig";

        /** デフォルトの設定 */
        private static final SystemCharConfig DEFAULT_CONFIG = new SystemCharConfig();

        /** アノテーション */
        private SystemChar annotation;

        /** {@inheritDoc} */
        @Override
        public void initialize(SystemChar constraintAnnotation) {
            annotation = constraintAnnotation;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null) {
                return true;
            }
            SystemCharConfig config = SystemRepository.get(CONFIG_NAME);
            if (config == null) {
                config = DEFAULT_CONFIG;
            }
            return CharsetDefValidationUtil.isValid(
                    annotation.charsetDef(),           // 許容される文字集合の定義
                    value,                             // バリデーション対象文字列
                    annotation.allowLineSeparator(),    // 改行コードを許容するか
                    config.isAllowSurrogatePair()
            );
        }
    }
}
