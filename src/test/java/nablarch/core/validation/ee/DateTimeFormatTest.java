package nablarch.core.validation.ee;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import java.util.Set;

public class DateTimeFormatTest extends BeanValidationTestCase {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void 入力値がnullの場合_検証に成功する() {
        TestBean bean = new TestBean();

        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);

        Assert.assertTrue(violations.isEmpty());

    }

    @Test
    public void 入力値が空文字列の場合_検証に成功する() {
        TestBean bean = new TestBean();
        bean.emptyInput = "";

        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);

        Assert.assertTrue(violations.isEmpty());

    }

    @Test
    public void フォーマット指定がDATEの時_入力値がフォーマットに一致する場合_検証に成功する() {
        TestBean bean = new TestBean();
        bean.dateInput = "2023/05/11";

        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);

        Assert.assertTrue(violations.isEmpty());

    }

    @Test
    public void フォーマット指定がDATEの時_入力値がフォーマットに一致しない場合_検証に失敗する() {
        TestBean bean = new TestBean();
        bean.dateInput = "2023/05/11 20:58:49";

        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);

        Assert.assertEquals(1, violations.size());

        ConstraintViolation<TestBean> v = violations.iterator().next();
        Assert.assertEquals("フォーマットに一致しません。", v.getMessage());

    }

    @Test
    public void フォーマット指定がTIMEの時_入力値がフォーマットに一致する場合_検証に成功する() {
        TestBean bean = new TestBean();
        bean.timeInput = "20:58:49";

        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);

        Assert.assertTrue(violations.isEmpty());

    }

    @Test
    public void フォーマット指定がTIMEの時_入力値がフォーマットに一致しない場合_検証に失敗する() {
        TestBean bean = new TestBean();
        bean.timeInput = "2023/05/11 20:58:49";

        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);

        Assert.assertEquals(1, violations.size());

        ConstraintViolation<TestBean> v = violations.iterator().next();
        Assert.assertEquals("フォーマットに一致しません。", v.getMessage());

    }

    @Test
    public void フォーマット指定がDATETIMEの時_入力値がフォーマットに一致する場合_検証に成功する() {
        TestBean bean = new TestBean();
        bean.dateTimeInput = "2023/05/11 20:58:49";

        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);

        Assert.assertTrue(violations.isEmpty());

    }

    @Test
    public void フォーマット指定がDATETIMEの時_入力値がフォーマットに一致しない場合_検証に失敗する() {
        TestBean bean = new TestBean();
        bean.dateTimeInput = "2023/05/11";

        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);

        Assert.assertEquals(1, violations.size());

        ConstraintViolation<TestBean> v = violations.iterator().next();
        Assert.assertEquals("フォーマットに一致しません。", v.getMessage());

    }

    @Test
    public void フォーマット指定がCUSTOMの時_入力値がフォーマットに一致する場合_検証に成功する() {
        TestBean bean = new TestBean();
        bean.customInput = "2023-05-11 20_58_49";

        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);

        Assert.assertTrue(violations.isEmpty());


    }

    @Test
    public void フォーマット指定がCUSTOMの時_入力値がフォーマットに一致しない場合_検証に失敗する() {
        TestBean bean = new TestBean();
        bean.customInput = "2023/05/11 20:58:49";

        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);

        Assert.assertEquals(1, violations.size());

        ConstraintViolation<TestBean> v = violations.iterator().next();
        Assert.assertEquals("フォーマットに一致しません。", v.getMessage());
    }

    @Test
    public void フォーマット指定がCUSTOMの時_不正なフォーマットが与えられた場合_検証に失敗する() {
        TestBean bean = new TestBean();
        bean.invalidFormatInput = "2023/05/11 20:58:49";

        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);

        Assert.assertEquals(1, violations.size());

        ConstraintViolation<TestBean> v = violations.iterator().next();
        Assert.assertEquals("フォーマットに一致しません。", v.getMessage());

    }

    @Test
    public void フォーマット指定がCUSTOMの時_かつロケールを設定した時_入力値がフォーマットに一致する場合_検証に成功する() {

    }

    @Test
    public void フォーマット指定がCUSTOMの時_かつロケールを設定した時_入力値がフォーマットに一致しない場合_検証に失敗する() {

    }

    @Test
    public void フォーマット指定がCUSTOMの時_フォーマットが指定されていない場合_例外を送出する() {
        IllegalInputCUSTOMBean bean = new IllegalInputCUSTOMBean();
        bean.nullFormatInput = "2023/05/11 20:58:49";

        expectedException.expect(ValidationException.class);
        expectedException.expectCause(Matchers.allOf(
            Matchers.instanceOf(IllegalArgumentException.class),
            Matchers.hasProperty("message", Matchers.is("abcd"))
        ));

        validator.validate(bean);

    }

    @Test
    public void フォーマット指定がDATEの時_フォーマットが指定されている場合_例外を送出する() {
        IllegalInputDATEBean bean = new IllegalInputDATEBean();
        bean.illegalCombinationDateInput = "2023-05-11";

        expectedException.expect(ValidationException.class);
        expectedException.expectCause(Matchers.allOf(
            Matchers.instanceOf(IllegalArgumentException.class),
            Matchers.hasProperty("message", Matchers.is("efgh"))
        ));

        validator.validate(bean);
    }

    @Test
    public void フォーマット指定がTIMEの時_フォーマットが指定されている場合_例外を送出する() {
        IllegalInputTIMEBean bean = new IllegalInputTIMEBean();
        bean.illegalCombinationTimeInput = "20_58_49";

        expectedException.expect(ValidationException.class);
        expectedException.expectCause(Matchers.allOf(
            Matchers.instanceOf(IllegalArgumentException.class),
            Matchers.hasProperty("message", Matchers.is("efgh"))
        ));

        validator.validate(bean);
    }

    @Test
    public void フォーマット指定がDATETIMEの時_フォーマットが指定されている場合_例外を送出する() {
        IllegalInputDATETIMEBean bean = new IllegalInputDATETIMEBean();
        bean.illegalCombinationDateTimeInput = "2023-05-11 20_58_49";

        expectedException.expect(ValidationException.class);
        expectedException.expectCause(Matchers.allOf(
            Matchers.instanceOf(IllegalArgumentException.class),
            Matchers.hasProperty("message", Matchers.is("efgh"))
        ));

        validator.validate(bean);
    }

    @Test
    public void フォーマット指定がCUSTOM以外の時_ロケールがnullでない場合_例外を送出する() {

    }


    private static class TestBean {

        @DateTimeFormat(type = DateTimeFormat.Type.DATE)
        String emptyInput;

        @DateTimeFormat(type = DateTimeFormat.Type.DATE)
        String dateInput;

        @DateTimeFormat(type = DateTimeFormat.Type.TIME)
        String timeInput;

        @DateTimeFormat(type = DateTimeFormat.Type.DATETIME)
        String dateTimeInput;

        @DateTimeFormat(type = DateTimeFormat.Type.CUSTOM, formatString = "yyyy-MM-dd HH_mm_ss")
        String customInput;

        @DateTimeFormat(type = DateTimeFormat.Type.CUSTOM, formatString = "ABCD")
        String invalidFormatInput;

    }

    private static class IllegalInputCUSTOMBean {
        @DateTimeFormat(type = DateTimeFormat.Type.CUSTOM)
        String nullFormatInput;
    }

    private static class IllegalInputDATEBean {
        @DateTimeFormat(type = DateTimeFormat.Type.DATE, formatString = "yyyy-MM-dd")
        String illegalCombinationDateInput;

    }

    private static class IllegalInputTIMEBean {
        @DateTimeFormat(type = DateTimeFormat.Type.DATE, formatString = "HH_mm_ss")
        String illegalCombinationTimeInput;

    }

    private static class IllegalInputDATETIMEBean {
        @DateTimeFormat(type = DateTimeFormat.Type.DATE, formatString = "yyyy-MM-dd HH_mm_ss")
        String illegalCombinationDateTimeInput;

    }

}
