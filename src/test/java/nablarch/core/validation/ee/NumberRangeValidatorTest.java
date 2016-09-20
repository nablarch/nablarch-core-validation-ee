package nablarch.core.validation.ee;

import java.util.Set;
import javax.validation.ConstraintViolation;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.hamcrest.collection.IsCollectionWithSize;

/**
 * {@link NumberRangeValidatorTest}のテストクラス。
 *
 * @author T.Kawasaki
 */
public class NumberRangeValidatorTest extends BeanValidationTestCase {

    private static class NumberRangeBean {
        @NumberRange(max = 1, min = -2)
        Double minAndMax;

        @NumberRange(min = 4)
        Integer minOnly;

        @NumberRange(max = 10)
        Long maxOnly;

        @NumberRange(max = 15, min = -3)
        String str;

        @NumberRange
        Long nonValidate;

        @NumberRange(min = 1, max = 2)
        Boolean bool;

        @NumberRange(min = 1, max = 1, message = "正しく入力してください")
        Integer intValue;
    }

    NumberRangeBean bean = new NumberRangeBean();

    @Test
    public void testNoValue() {
        bean.minAndMax = null;
        bean.minOnly = null;
        bean.maxOnly = null;
        bean.str = null;
        bean.nonValidate = null;
        Set<ConstraintViolation<NumberRangeBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(0));

        bean.minAndMax = null;
        bean.minOnly = null;
        bean.maxOnly = null;
        bean.str = "";
        bean.nonValidate = null;
        violations = validator.validate(bean);
        assertThat(violations.size(), is(0));
    }

    @Test
    public void testValid() {
        bean.minAndMax = 1D;
        bean.minOnly = 4;
        bean.maxOnly = 9L;
        bean.str = "3";
        bean.nonValidate = Long.MAX_VALUE;
        Set<ConstraintViolation<NumberRangeBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(0));
    }

    @Test
    public void testValid2() {
        bean.minAndMax = 0.0000000009D;
        bean.minOnly = 4;
        bean.maxOnly = 9L;
        bean.str = "-3";
        bean.nonValidate = Long.MIN_VALUE;
        Set<ConstraintViolation<NumberRangeBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(0));
    }

    @Test
    public void testInValid() {
        bean.minAndMax = -2.0000000001D;
        bean.minOnly = 3;
        bean.maxOnly = 11L;
        bean.str = "aaa";
        Set<ConstraintViolation<NumberRangeBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(4));
    }

    @Test
    public void testMinAndMax() {
        bean.minAndMax = 1.00000000001D;
        Set<ConstraintViolation<NumberRangeBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));

        ConstraintViolation<NumberRangeBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("minAndMax"));
        assertThat(v.getMessage(), is("-2以上1以内で入力してください。"));
    }

    @Test
    public void testMin() {
        bean.minOnly = 3;
        Set<ConstraintViolation<NumberRangeBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));

        ConstraintViolation<NumberRangeBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("minOnly"));
        assertThat(v.getMessage(), is("4以上で入力してください。"));
    }

    @Test
    public void testMax() {
        bean.maxOnly = 11L;
        Set<ConstraintViolation<NumberRangeBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));

        ConstraintViolation<NumberRangeBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("maxOnly"));
        assertThat(v.getMessage(), is("10以内で入力してください。"));
    }

    /**
     * 全角数値の場合にバリデーションエラーが発生すること。
     */
    @Test
    public void testEmNumberString() {
        bean.str = "1３";
        Set<ConstraintViolation<NumberRangeBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));

        ConstraintViolation<NumberRangeBean> v = violations.iterator().next();
        assertThat(v.getMessage(), is("-3以上15以内で入力してください。"));
    }

    /**
     * 明示的に+を指定した正数の場合にバリデーションエラーが発生しないこと。
     */
    @Test
    public void testPlusString() {
        bean.str = "+15";
        Set<ConstraintViolation<NumberRangeBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(0));
    }

    /**
     * 数値型、文字列型でないフィールドにアノテーションを設定した場合、バリデーションエラーが発生すること。
     */
    @Test
    public void testBoolean() {
        bean.bool = true;

        Set<ConstraintViolation<NumberRangeBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));
        ConstraintViolation<NumberRangeBean> v = violations.iterator().next();
        assertThat(v.getMessage(), is("1以上2以内で入力してください。"));
    }

    /**
     * message属性を指定した場合、そのメッセージが設定されること。
     */
    @Test
    public void testSpecifyMessage() throws Exception {
        bean.intValue = 2;

        Set<ConstraintViolation<NumberRangeBean>> violations = validator.validate(bean);
        assertThat(violations, IsCollectionWithSize.hasSize(1));
        ConstraintViolation<NumberRangeBean> v = violations.iterator().next();
        assertThat(v.getMessage(), is("正しく入力してください"));
    }

    private interface NormalUser {}
    private interface PremiumUser {}

    private class ListAndGroupsBean {
        @NumberRange.List({
                @NumberRange(max = 3, groups = NormalUser.class),
                @NumberRange(max = 10, groups = PremiumUser.class)
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
        assertThat(violations.iterator().next().getMessage(), is("3以内で入力してください。"));

        assertThat(validator.validate(bean, PremiumUser.class).size(), is(0));
    }
}