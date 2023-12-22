package nablarch.core.validation.ee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsCollectionContaining;

import nablarch.core.message.ApplicationException;
import nablarch.core.message.Message;
import nablarch.core.repository.ObjectLoader;
import nablarch.core.repository.SystemRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * {@link NablarchMessageInterpolator}のテスト。
 */
public class NablarchMessageInterpolatorTest {

    // test bean
    private static class TestBean {

        @Required
        @Length(max = 5)
        private String name;

        @Length(max = 5, message = "カスタムメッセージ:{max}以下で入力してください。")
        private String customMessage;

        @NumberRange(min = 10, max = 15)
        private Integer number;

        @NumberRange(min = 1, max = 2, message = "カスタムメッセージ:{min}と{max}の間で入力してください。")
        private Integer number2;


    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        ValidatorUtil.clearCachedValidatorFactory();
        SystemRepository.clear();

        SystemRepository.load(new ObjectLoader() {
            @Override
            public Map<String, Object> load() {
                final HashMap<String, Object> objects = new HashMap<String, Object>();
                objects.put("messageInterpolator", new NablarchMessageInterpolator());

                // デフォルトの実装がValidatorFactoryをstaticフィールドで保持していて差し替え不可だから
                // デフォルトと同じ実装を明示的に設定して他のテストの影響を受けないようにする。
                objects.put("validatorFactoryBuilder", new CustomValidatorFactory());
                return objects;
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        SystemRepository.clear();
    }

    /**
     * Nablarchのメッセージ管理のメッセージが参照出来ること
     */
    @Test
    public void messageInNablarchMessage() throws Exception {
        final TestBean bean = new TestBean();
        bean.name = null;
        bean.number = 16;

        expectedException.expect(ApplicationException.class);
        expectedException.expect(IsMessageContaining.hasItems(
                "必須項目です。", "10以上15以内で入力してください。"));

        ValidatorUtil.validate(bean);
    }

    /**
     * カッコ無しのメッセージを指定した場合、その値をメッセージIDとしてメッセージが取得できること。
     */
    @Test
    public void withoutBrackets() throws Exception {
        final TestBean bean = new TestBean();
        bean.name = "1";
        bean.customMessage = "123456";
        bean.number2 = 3;

        expectedException.expect(ApplicationException.class);
        expectedException.expect(IsMessageContaining.hasItems(
                "カスタムメッセージ:5以下で入力してください。", "カスタムメッセージ:1と2の間で入力してください。"));

        ValidatorUtil.validate(bean);
    }

    /**
     * messageInterpolatorに直接{@link NablarchMessageInterpolator}を指定した場合のケース
     */
    @Test
    public void specifyNablarchMessageInterpolator() throws Exception {
        SystemRepository.load(new ObjectLoader() {
            @Override
            public Map<String, Object> load() {
                final HashMap<String, Object> objects = new HashMap<String, Object>();
                objects.put("validatorFactoryBuilder", new CustomValidatorFactory2());
                return objects;
            }
        });

        final TestBean bean = new TestBean();
        bean.name = null;
        bean.customMessage = "123456";
        bean.number2 = 3;

        expectedException.expect(ApplicationException.class);
        expectedException.expect(IsMessageContaining.hasItems(
                "必須項目です。", "カスタムメッセージ:5以下で入力してください。", "カスタムメッセージ:1と2の間で入力してください。"));
        ValidatorUtil.validate(bean);
    }

    private static class IsMessageContaining extends TypeSafeMatcher<ApplicationException> {

        private final Matcher<Iterable<String>> matcher;

        public IsMessageContaining(final String[] messages) {
            matcher = IsCollectionContaining.hasItems(messages);
        }

        private static IsMessageContaining hasItems(String... messages) {
            return new IsMessageContaining(messages);
        }

        @Override

        protected boolean matchesSafely(final ApplicationException item) {
            return matcher.matches(toStringMessage(item.getMessages()));
        }

        private static List<String> toStringMessage(final List<Message> messages1) {
            final List<String> messages = new ArrayList<String>();
            for (final Message message : messages1) {
                messages.add(message.formatMessage());
            }
            return messages;
        }

        @Override
        protected void describeMismatchSafely(final ApplicationException item, final Description mismatchDescription) {
            mismatchDescription.appendText("was ")
                               .appendValue(toStringMessage(item.getMessages()));
        }

        @Override
        public void describeTo(final Description description) {
            description.appendDescriptionOf(matcher);
        }
    }
    
    private static class CustomValidatorFactory extends ValidatorFactoryBuilder {
        @Override
        protected ValidatorFactory build() {
            return Validation
                    .byDefaultProvider()
                    .configure()
                    .messageInterpolator(new MultiLanguageMessageInterpolator())
                    .buildValidatorFactory();
        }
    }
    
    private static class CustomValidatorFactory2 extends ValidatorFactoryBuilder {
        @Override
        protected ValidatorFactory build() {
            return Validation
                    .byDefaultProvider()
                    .configure()
                    .messageInterpolator(new NablarchMessageInterpolator())
                    .buildValidatorFactory();
        }
    }
}