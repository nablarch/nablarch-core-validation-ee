package nablarch.core.validation.ee;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.junit.Test;

import nablarch.core.repository.SystemRepository;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author kawasima
 */
public class DomainValidatorTest extends BeanValidationTestCase {

    @Test
    public void testValidate() throws Exception {
        prepareSystemRepository();
        TestBean bean = new TestBean();
        bean.name = "ABCDEF";
        bean.beans = new ArrayList<TestBean>();
        TestBean inner1 = new TestBean();
        inner1.name = "hoge";
        inner1.balance = 1000;
        bean.beans.add(inner1);

        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);

        List<ConstraintViolation<TestBean>> actual = sort(violations);
        Iterator<ConstraintViolation<TestBean>> itr = actual.iterator();
        {
            ConstraintViolation<TestBean> v = itr.next();
            assertThat(v.getPropertyPath().toString(), is("beans[0].balance"));

            assertThat(v.getMessage(), is("整数部は3桁以内で入力してください。"));
        }

        {
            ConstraintViolation<TestBean> v = itr.next();
            assertThat(v.getPropertyPath().toString(), is("beans[0].name"));

            assertThat(v.getMessage(), is("英大文字でないですよ。"));
        }
        {
            ConstraintViolation<TestBean> v = itr.next();
            assertThat(v.getPropertyPath().toString(), is("name"));

            assertThat(v.getMessage(), is("5文字以内で入力してください。"));
        }

        assertThat(violations.size(), is(3));
    }


    /** DomainManagerがコンポーネント定義されていない場合、例外が発生すること。 */
    @Test(expected = IllegalStateException.class)
    public void testDomainManagerNotFound() {
        SystemRepository.clear();
        new DomainValidator().getDomainManager();
    }


    /** テスト用のBean */
    public static class TestBean {

        @NotNull
        @Domain("name")
        String name;

        @Domain("money")
        int balance;

        @Valid
        List<TestBean> beans;

    }
    private interface NormalUser {}
    private interface PremiumUser {}

    private class ListAndGroupsBean {
        @Domain.List({
                @Domain(value = "money", groups = { NormalUser.class }),
                @Domain(value = "bigMoney", groups = { PremiumUser.class })
        })
        private BigDecimal test;
    }

    /**
     * Listとgroupsが正しく定義されていること。
     */
    @Test
    public void testListAndGroups() {
        prepareSystemRepository();

        ListAndGroupsBean bean = new ListAndGroupsBean();
        bean.test = new BigDecimal("12345");

        Set<ConstraintViolation<ListAndGroupsBean>> violations = validator.validate(bean, NormalUser.class);
        assertThat(violations.size(), is(1));
        assertThat(violations.iterator().next().getMessage(),
                is("整数部は3桁以内で入力してください。"));

        assertThat(validator.validate(bean, PremiumUser.class).size(), is(0));
    }
}
