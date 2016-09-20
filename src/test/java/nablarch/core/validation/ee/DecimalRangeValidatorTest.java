package nablarch.core.validation.ee;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hamcrest.collection.IsCollectionWithSize;

import org.junit.Test;

/**
 * {@link nablarch.core.validation.ee.DecimalRange.DecimalRangeValidator}のテストクラス。
 *
 * @author Ukawa Shohei
 */
public class DecimalRangeValidatorTest extends BeanValidationTestCase {

    private static class DecimalRangeBean {
        @DecimalRange(max = "0.1", min = "-0.4")
        Double minAndMax;

        @DecimalRange(min = "0.9999999999")
        Integer minOnly;

        @DecimalRange(max = "999999999.9")
        Long maxOnly;

        @DecimalRange(max = "15", min = "-3")
        String str;

        @DecimalRange(max = "0.1", min = "-0.4")
        BigDecimal decimal;

        @DecimalRange
        Double nonValidate;

        @DecimalRange(min = "1", max = "2")
        Boolean bool;

        @DecimalRange(min = "2", message = "値を正しく入力してください")
        Integer intValue;
    }

    DecimalRangeBean bean = new DecimalRangeBean();

    @Test
    public void testNoValue() {
        bean.minAndMax = null;
        bean.minOnly = null;
        bean.maxOnly = null;
        bean.str = null;
        bean.decimal = null;
        bean.nonValidate = null;
        Set<ConstraintViolation<DecimalRangeBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(0));

        bean.minAndMax = null;
        bean.minOnly = null;
        bean.maxOnly = null;
        bean.str = "";
        bean.decimal = null;
        bean.nonValidate = null;
        violations = validator.validate(bean);
        assertThat(violations.size(), is(0));
    }

    @Test
    public void testValid() {
        bean.minAndMax = 0.1D;
        bean.minOnly = 1;
        bean.maxOnly = 999999999L;
        bean.str = "3";
        bean.decimal = new BigDecimal("0.1");
        bean.nonValidate = Double.MAX_VALUE;
        Set<ConstraintViolation<DecimalRangeBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(0));
    }

    @Test
    public void testValid2() {
        bean.minAndMax = 0.0000000009D;
        bean.minOnly = 2;
        bean.maxOnly = 0L;
        bean.str = "-3";
        bean.decimal = new BigDecimal("0.0000000000000009");
        bean.nonValidate = Double.MIN_VALUE;
        Set<ConstraintViolation<DecimalRangeBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(0));
    }

    @Test
    public void testInValid() {
        bean.minAndMax = 0.1000000001D;
        bean.minOnly = 0;
        bean.maxOnly = 1000000000L;
        bean.str = "aaa";
        bean.decimal = new BigDecimal("0.1000000001");
        Set<ConstraintViolation<DecimalRangeBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(5));
    }

    @Test
    public void testMinAndMax() {
        bean.minAndMax = -0.4000000001D;
        Set<ConstraintViolation<DecimalRangeBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));

        ConstraintViolation<DecimalRangeBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("minAndMax"));
        assertThat(v.getMessage(), is("-0.4以上0.1以内で入力してください。"));
    }

    @Test
    public void testMin() {
        bean.minOnly = 0;
        Set<ConstraintViolation<DecimalRangeBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));

        ConstraintViolation<DecimalRangeBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("minOnly"));
        assertThat(v.getMessage(), is("0.9999999999以上で入力してください。"));
    }

    @Test
    public void testMax() {
        bean.maxOnly = 10000000000L;
        Set<ConstraintViolation<DecimalRangeBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));

        ConstraintViolation<DecimalRangeBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("maxOnly"));
        assertThat(v.getMessage(), is("999999999.9以内で入力してください。"));
    }

    @Test
    public void testDecimal() {
        bean.decimal = new BigDecimal("-0.4000000001");
        Set<ConstraintViolation<DecimalRangeBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));

        ConstraintViolation<DecimalRangeBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("decimal"));
        assertThat(v.getMessage(), is("-0.4以上0.1以内で入力してください。"));
    }

    /**
     * 全角数値の場合にバリデーションエラーが発生すること。
     */
    @Test
    public void testEmNumberString() {
        bean.str = "3.５";
        Set<ConstraintViolation<DecimalRangeBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));

        ConstraintViolation<DecimalRangeBean> v = violations.iterator().next();
        assertThat(v.getMessage(), is("-3以上15以内で入力してください。"));
    }

    /**
     * 明示的に+を指定した正数の場合にバリデーションエラーが発生しないこと。
     */
    @Test
    public void testPlusString() {
        bean.str = "+15";
        Set<ConstraintViolation<DecimalRangeBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(0));
    }

    /**
     * 数値型、文字列型でないフィールドにアノテーションを設定した場合、バリデーションエラーが発生すること。
     */
    @Test
    public void testBoolean() {
        bean.bool = true;

        Set<ConstraintViolation<DecimalRangeBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));
        ConstraintViolation<DecimalRangeBean> v = violations.iterator().next();
        assertThat(v.getMessage(), is("1以上2以内で入力してください。"));
    }

    /**
     * message属性を指定した場合、その値がメッセージに設定されること。
     */
    @Test
    public void testSpecifyMessage() throws Exception {
        bean.intValue = 1;

        final Set<ConstraintViolation<DecimalRangeBean>> violations = validator.validate(bean);
        assertThat(violations, IsCollectionWithSize.hasSize(1));

        final ConstraintViolation<DecimalRangeBean> v = violations.iterator().next();
        assertThat(v.getMessage(), is("値を正しく入力してください"));
    }

    private interface NormalUser {}
    private interface PremiumUser {}

    private class ListAndGroupsBean {
        @DecimalRange.List({
                @DecimalRange(max = "3.1", groups = NormalUser.class),
                @DecimalRange(max = "10.1", groups = PremiumUser.class)
        })
        private String test;
    }

    /**
     * Listとgroupsが正しく定義されていること。
     */
    @Test
    public void testListAndGroups() {

        ListAndGroupsBean bean = new ListAndGroupsBean();
        bean.test = "4";

        Set<ConstraintViolation<ListAndGroupsBean>> violations = validator.validate(bean, NormalUser.class);
        assertThat(violations.size(), is(1));
        assertThat(violations.iterator().next().getMessage(), is("3.1以内で入力してください。"));

        assertThat(validator.validate(bean, PremiumUser.class).size(), is(0));
    }
}
