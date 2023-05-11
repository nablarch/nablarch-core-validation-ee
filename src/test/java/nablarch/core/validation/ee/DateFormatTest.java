package nablarch.core.validation.ee;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.validation.ConstraintViolation;
import java.util.Set;

public class DateFormatTest extends BeanValidationTestCase {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        ValidatorUtil.clearCachedValidatorFactory();
    }

    @Test
    public void 入力値がnullの場合_検証に成功する() {
        TestBean bean = new TestBean();

        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);

        Assert.assertTrue(violations.isEmpty());

    }

    @Test
    public void 入力値が空文字列の場合_検証に成功する() {
        TestBean bean = new TestBean();
        bean.defaultFormatInput = "";

        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);

        Assert.assertTrue(violations.isEmpty());

    }

//    @Test
//    public void システムリポジトリに設定しているデフォルト書式で検証できる() {
//        prepareSystemRepository("dateFormatValidator.xml");
//
//        TestBean bean = new TestBean();
//        bean.defaultFormatInput = "2023/05/11";
//
//        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);
//
//        Assert.assertTrue(violations.isEmpty());
//
//    }
//
    @Test
    public void システムリポジトリに設定していない場合のデフォルト書式で検証できる() {
        TestBean bean = new TestBean();
        bean.defaultFormatInput = "20230511";

        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);

        Assert.assertTrue(violations.isEmpty());

    }

    @Test
    public void 入力値が指定した書式に一致する場合_検証に成功する() {
        TestBean bean = new TestBean();
        bean.withFormatInput = "2023-05-11";

        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);

        Assert.assertTrue(violations.isEmpty());

    }

    @Test
    public void 入力値が指定した書式に一致しない場合_検証に失敗する() {
        TestBean bean = new TestBean();
        bean.withFormatInput = "2023-05-11 20:58:49";

        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);

        Assert.assertEquals(1, violations.size());

        ConstraintViolation<TestBean> v = violations.iterator().next();
        Assert.assertEquals("日付書式[yyyy-MM-dd]に一致しません。", v.getMessage());

    }

    @Test
    public void 入力値が日付形式ではない場合_検証に失敗する() {
        TestBean bean = new TestBean();
        bean.withFormatInput = "hogehoge";

        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);

        Assert.assertEquals(1, violations.size());

        ConstraintViolation<TestBean> v = violations.iterator().next();
        Assert.assertEquals("日付書式[yyyy-MM-dd]に一致しません。", v.getMessage());

    }

    @Test
    public void 不正なフォーマットが与えられた場合_検証に失敗する() {
        TestFailBean bean = new TestFailBean();
        bean.invalidFormatInput = "2023/05/11 20:58:49";

        expectedException.expectCause(Matchers.allOf(
            Matchers.instanceOf(IllegalStateException.class),
            Matchers.hasProperty("message", Matchers.is("Invalid date format string. [ABCD]"))
        ));

        validator.validate(bean);

    }



    private static class TestBean {

        @DateFormat
        String defaultFormatInput;

        @DateFormat("yyyy-MM-dd")
        String withFormatInput;

    }

    private static class TestFailBean {

        @DateFormat("ABCD")
        String invalidFormatInput;

    }
}
