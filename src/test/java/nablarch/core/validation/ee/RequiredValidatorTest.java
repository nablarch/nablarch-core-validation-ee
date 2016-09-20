package nablarch.core.validation.ee;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.groups.Default;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * {@link RequiredValidatorTest}のテストクラス。
 *
 * @author T.Kawasaki
 */
public class RequiredValidatorTest extends BeanValidationTestCase {

    private static class RequiredStringBean {
        @Required
        String string;

    }
    private static class RequiredObjectBean {
        @Required
        Object object;
    }

    RequiredStringBean bean = new RequiredStringBean();


    @Test
    public void testNull() {
        bean.string = null;
        Set<ConstraintViolation<RequiredStringBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));

        ConstraintViolation<RequiredStringBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("string"));
        assertThat(v.getMessage(), is("必須項目です。"));
    }

    @Test
    public void testEmptyString() {
        bean.string = "";
        Set<ConstraintViolation<RequiredStringBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));

        ConstraintViolation<RequiredStringBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("string"));
        assertThat(v.getMessage(), is("必須項目です。"));
    }

    @Test
    public void testNotNullObject() {
        RequiredObjectBean bean = new RequiredObjectBean();
        bean.object = new Object();
        Set<ConstraintViolation<RequiredObjectBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(0));

    }


    @Test
    public void testStringNotEmpty() {
        bean.string = "1";
        Set<ConstraintViolation<RequiredStringBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(0));
    }

    private interface NormalUser {}
    private interface PremiumUser {}

    private class ListAndGroupsBean {
        @Required.List({
                @Required(groups = NormalUser.class),
                @Required(groups = PremiumUser.class)
        })
        private String test;
    }

    /**
     * Listとgroupsが正しく定義されていること。
     */
    @Test
    public void testListAndGroups() {

        ListAndGroupsBean bean = new ListAndGroupsBean();
        bean.test = null;

        Set<ConstraintViolation<ListAndGroupsBean>> violations = validator.validate(bean, NormalUser.class);
        assertThat(violations.size(), is(1));
        assertThat(violations.iterator().next().getMessage(), is("必須項目です。"));

        violations = validator.validate(bean, PremiumUser.class);
        assertThat(violations.size(), is(1));
        assertThat(violations.iterator().next().getMessage(), is("必須項目です。"));

        assertThat(validator.validate(bean, Default.class).size(), is(0));
    }
}