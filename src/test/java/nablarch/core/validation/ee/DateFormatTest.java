package nablarch.core.validation.ee;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.validation.ConstraintViolation;
import java.util.Set;

public class DateFormatTest extends BeanValidationTestCase {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        ValidatorUtil.clearCachedValidatorFactory();
        ValidatorUtil.getValidatorFactory().close();
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

    @Test
    public void システムリポジトリに設定しているデフォルト書式で検証できる() {
        prepareSystemRepository("dateFormatValidator.xml");

        TestBean bean = new TestBean();
        bean.defaultFormatInput = "2023/05/11";

        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);

        Assert.assertTrue(violations.isEmpty());

    }

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
        Assert.assertEquals("日付書式に一致しません。", v.getMessage());

    }

    @Test
    public void 入力値が日付形式ではない場合_検証に失敗する() {
        TestBean bean = new TestBean();
        bean.withFormatInput = "hogehoge";

        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);

        Assert.assertEquals(1, violations.size());

        ConstraintViolation<TestBean> v = violations.iterator().next();
        Assert.assertEquals("日付書式に一致しません。", v.getMessage());

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

    @Test
    public void リストとグループが正しく動作する() {
        ListAndGroupsBean bean = new ListAndGroupsBean();
        bean.listGroupsInput = "2023_05_12";

        Set<ConstraintViolation<ListAndGroupsBean>> violations1 = validator.validate(bean, Test2.class);
        Assert.assertTrue(violations1.isEmpty());

        Set<ConstraintViolation<ListAndGroupsBean>> violations2 = validator.validate(bean, Test1.class);
        Assert.assertEquals(1, violations2.size());
        ConstraintViolation<ListAndGroupsBean> v = violations2.iterator().next();
        Assert.assertEquals("listGroupsInput", v.getPropertyPath().toString());
        Assert.assertEquals("日付書式に一致しません。", v.getMessage());
    }


    private interface Test1 {
    }

    private interface Test2 {
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

    private static class ListAndGroupsBean {
        @DateFormat.List({
            @DateFormat(groups = {Test1.class}),
            @DateFormat(value = "yyyy_MM_dd", groups = {Test2.class})
        })
        private String listGroupsInput;
    }
}
