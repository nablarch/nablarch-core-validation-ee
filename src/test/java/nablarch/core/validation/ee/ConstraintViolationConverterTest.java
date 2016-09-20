package nablarch.core.validation.ee;

import nablarch.core.message.Message;
import nablarch.core.validation.ValidationResultMessage;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@link ConstraintViolationConverterTest}のテストクラス。
 *
 * @author T.Kawasaki
 */
public class ConstraintViolationConverterTest {

    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();


    /**
     * コンストラクタにprefix設定すると、エラーメッセージの
     * プロパティ名にprefixが付加されることを確認する.
     */
    @Test
    public void testAddPrefixToPropertyName() {
        ConstraintViolationConverter sut = new ConstraintViolationConverter("form");
        TestBean bean = new TestBean();
        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));
        assertThat(violations.iterator().next().getPropertyPath().toString(), is("name"));

        List<Message> convert = sut.convert(violations);
        ValidationResultMessage vrm = (ValidationResultMessage) convert.get(0);
        assertThat(vrm.getPropertyName(), is("form.name"));
        assertThat(vrm.getMessageId(), is("javax.validation.constraints.NotNull"));
        assertThat(vrm.formatMessage(), is("may not be null"));
    }

    static class TestBean {

        private String name = null;

        @NotNull
        public String getName() {
            return name;
        }
    }
}