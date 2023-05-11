package nablarch.core.validation.ee;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.validation.ConstraintViolation;
import java.util.Set;

public class DateFormatSystemRepositoryTest extends BeanValidationTestCase {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        ValidatorUtil.clearCachedValidatorFactory();
    }

    @Test
    public void システムリポジトリに設定しているデフォルト書式で検証できる() {
        prepareSystemRepository("dateFormatValidator.xml");

        TestBean bean = new TestBean();
        bean.defaultFormatInput = "2023/05/11";

        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);

        Assert.assertTrue(violations.isEmpty());

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
