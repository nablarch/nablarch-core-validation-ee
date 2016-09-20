package nablarch.core.validation.ee;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.junit.Before;
import org.junit.Test;

/**
 * {@link SystemCharValidatorTest}のテストクラス。
 *
 * @author T.Kawasaki
 */
public class SystemCharValidatorTest extends BeanValidationTestCase {

    @Before
    public void setUp() {
        prepareSystemRepository();
    }

    /** テスト用のBean */
    private static class TestBean {
        @SystemChar(charsetDef = "英大文字")
        String name;

        @SystemChar.List({
                @SystemChar(charsetDef = "英大文字"),
                @SystemChar(charsetDef = "数字")
        })
        String multi;
    }

    /** バリデーション対象Bean */
    TestBean bean = new TestBean();

    /** バリデーション処理に所要する{@link Validator}のインスタンス。 */
    Validator validator = ValidatorUtil.getValidator();

    /** 許容された文字種である場合、バリデーションエラーが0件であること。*/
    @Test
    public void testValidateSuccess() {
        String[] validValues = {
                "VALID",  // 英大文字で構成された文字列
                "",       // 空文字
                null      // null
        };

        for (String e : validValues) {
            bean.name = e;
            Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);
            assertThat(e, violations.isEmpty(), is(true));
        }

    }

    /** 許容文字以外が含まれるとき、バリデーションエラーが取得できること */
    @Test
    public void testValidateFail() {
        bean.name = "invalid";
        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));
        ConstraintViolation<TestBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("name"));
        assertThat(v.getMessage(), is("英大文字でないですよ。"));
    }

    /** 複数のアノテーションが付与されている場合、それぞれのバリデーションが行われること。*/
    @Test
    public void testMultipleAnnotation() {
        bean.multi = "あああ";
        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(2));

        List<ConstraintViolation<TestBean>> actual = sort(violations);

        Iterator<ConstraintViolation<TestBean>> iterator = actual.iterator();
        {
            ConstraintViolation<TestBean> v = iterator.next();
            assertThat(v.getPropertyPath().toString(), is("multi"));
            assertThat(v.getMessage(), is("数字でないですよ。"));
        }
        {
            ConstraintViolation<TestBean> v = iterator.next();
            assertThat(v.getPropertyPath().toString(), is("multi"));
            assertThat(v.getMessage(), is("英大文字でないですよ。"));
        }

    }

    private interface NormalUser {}
    private interface PremiumUser {}

    private class ListAndGroupsBean {
        @SystemChar.List({
                @SystemChar(charsetDef = "英大文字", groups = NormalUser.class),
                @SystemChar(charsetDef = "数字", groups = PremiumUser.class)
        })
        private String test;
    }

    /**
     * Listとgroupsが正しく定義されていること。
     */
    @Test
    public void testListAndGroups() {

        ListAndGroupsBean bean = new ListAndGroupsBean();
        bean.test = "12345";

        Set<ConstraintViolation<ListAndGroupsBean>> violations = validator.validate(bean, NormalUser.class);
        assertThat(violations.size(), is(1));
        assertThat(violations.iterator().next().getMessage(), is("英大文字でないですよ。"));

        assertThat(validator.validate(bean, PremiumUser.class).size(), is(0));
    }
}