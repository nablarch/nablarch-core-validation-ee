package nablarch.core.validation.ee;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import nablarch.core.message.Message;
import nablarch.core.validation.ValidationResultMessage;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@link ConstraintViolationConverterTest}のテストクラス。
 *
 * @author T.Kawasaki
 */
public class ConstraintViolationConverterTest {

    Validator validator;

    @Before
    public void setUp() throws Exception {
        validator = Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new MessageInterpolator() {
                    private final MessageInterpolator delegate = Validation.buildDefaultValidatorFactory().getMessageInterpolator();

                    @Override
                    public String interpolate(String messageTemplate, Context context) {
                        return interpolate(messageTemplate, context, Locale.ENGLISH);
                    }

                    @Override
                    public String interpolate(String messageTemplate, Context context, Locale locale) {
                        return delegate.interpolate(messageTemplate, context, locale);
                    }
                })
                .buildValidatorFactory()
                .getValidator();
    }

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
        assertThat(vrm.getMessageId(), is("jakarta.validation.constraints.NotNull"));
        assertThat(vrm.formatMessage(), is("must not be null"));
    }

    /**
     * コンストラクタにprefixとして空文字を設定すると、エラーメッセージの
     * プロパティ名にprefixが付加されないことを確認する.
     */
    @Test
    public void testNotAddEmptyPrefixToPropertyName() {
        ConstraintViolationConverter sut = new ConstraintViolationConverter("");
        TestBean bean = new TestBean();
        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));
        assertThat(violations.iterator().next().getPropertyPath().toString(), is("name"));

        List<Message> convert = sut.convert(violations);
        ValidationResultMessage vrm = (ValidationResultMessage) convert.get(0);
        assertThat(vrm.getPropertyName(), is("name"));
        assertThat(vrm.getMessageId(), is("jakarta.validation.constraints.NotNull"));
        assertThat(vrm.formatMessage(), is("must not be null"));
    }

    static class TestBean {

        private String name = null;

        @NotNull
        public String getName() {
            return name;
        }
    }
}
