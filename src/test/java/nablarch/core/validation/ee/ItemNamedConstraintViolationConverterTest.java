package nablarch.core.validation.ee;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Payload;
import javax.validation.Valid;
import javax.validation.Validator;

import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matchers;

import nablarch.core.message.Message;
import nablarch.core.repository.ObjectLoader;
import nablarch.core.repository.SystemRepository;
import nablarch.core.validation.ValidationResultMessage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * {@link ItemNamedConstraintViolationConverterFactory.ItemNamedConstraintViolationConverter}のテスト。
 */
public class ItemNamedConstraintViolationConverterTest {

    @Before
    public void setUp() throws Exception {
        SystemRepository.clear();
        SystemRepository.load(new ObjectLoader() {
            @Override
            public Map<String, Object> load() {
                final HashMap<String, Object> result = new HashMap<String, Object>();
                result.put("domainManager", new DomainManagerImpl());
                return result;
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        SystemRepository.clear();
    }

    /**
     * 項目名がメッセージの先頭に付加されること
     */
    @Test
    public void convertMessage() throws Exception {

        // -------------------------------------------------- setup
        final Parent parent = new Parent();
        parent.number = "1";
        parent.children = Collections.singletonList(new Child("name"));

        // -------------------------------------------------- execute
        final Validator validator = ValidatorUtil.getValidator();
        final Set<ConstraintViolation<Parent>> constraintViolations = validator.validate(parent);
        final ConstraintViolationConverter converter = new ItemNamedConstraintViolationConverterFactory().create();
        final List<Message> messages = converter.convert(constraintViolations);

        // -------------------------------------------------- assert
        assertThat(messages, Matchers.contains(
                MessageMatcher.is("name", "[名前]必須項目です。")
        ));
    }

    /**
     * ネストしたBeanの場合でも項目名が先頭に付加されること
     *
     * @throws Exception
     */
    @Test
    public void convertNestedBeanMessage() throws Exception {
        // -------------------------------------------------- setup
        final Parent parent = new Parent();
        parent.name = "test";
        parent.children = Collections.singletonList(new Child(""));

        // -------------------------------------------------- execute
        final Validator validator = ValidatorUtil.getValidator();
        final Set<ConstraintViolation<Parent>> constraintViolations = validator.validate(parent);
        final ConstraintViolationConverter converter = new ItemNamedConstraintViolationConverterFactory().create();
        final List<Message> messages = converter.convert(constraintViolations);

        // -------------------------------------------------- assert
        assertThat(messages, contains(MessageMatcher.is("children[0].name", "[子供の名前]必須項目です。")));
    }

    /**
     * プレフィックス付きの場合でもメッセージが導出できること。
     *
     * @throws Exception
     */
    @Test
    public void convertMessageWithPrefix() throws Exception {
        // -------------------------------------------------- setup
        final Parent parent = new Parent();
        parent.number = "1";
        parent.children = Collections.singletonList(new Child("name"));

        // -------------------------------------------------- execute
        final Validator validator = ValidatorUtil.getValidator();
        final Set<ConstraintViolation<Parent>> constraintViolations = validator.validate(parent);
        final ConstraintViolationConverter converter = new ItemNamedConstraintViolationConverterFactory().create(
                "form");
        final List<Message> messages = converter.convert(constraintViolations);

        // -------------------------------------------------- assert
        assertThat(messages, Matchers.contains(
                MessageMatcher.is("form.name", "[名前]必須項目です。")
        ));
    }

    /**
     * 項目名が存在しない場合、メッセージには項目名が付加されないこと
     */
    @Test
    public void convertMessageNotFoundItemName() throws Exception {
        // -------------------------------------------------- setup
        final Parent parent = new Parent();
        parent.name = "test";
        parent.number = "123";

        // -------------------------------------------------- execute
        final Validator validator = ValidatorUtil.getValidator();
        final Set<ConstraintViolation<Parent>> constraintViolations = validator.validate(parent);
        final ConstraintViolationConverter converter = new ItemNamedConstraintViolationConverterFactory().create();
        final List<Message> messages = converter.convert(constraintViolations);

        // -------------------------------------------------- assert
        assertThat(messages, contains(MessageMatcher.is("number", "整数部は2桁以内で入力してください。")));
    }

    /**
     * ドメインバリデーションを使用した場合でも項目名が設定されること。
     */
    @Test
    public void convertMessageWithDomainValidation() throws Exception {

        // -------------------------------------------------- setup
        final Parent parent = new Parent();
        parent.name = "123456";

        // -------------------------------------------------- execute
        final Validator validator = ValidatorUtil.getValidator();
        final Set<ConstraintViolation<Parent>> constraintViolations = validator.validate(parent);
        final ConstraintViolationConverter converter = new ItemNamedConstraintViolationConverterFactory()
                .create("parent");
        final List<Message> messages = converter.convert(constraintViolations);

        // -------------------------------------------------- assert
        assertThat(messages, contains(MessageMatcher.is("parent.name", "[名前]5文字以内で入力してください。")));

    }

    /**
     * プロパティ名を指定したバリデーションでも、メッセージに項目名が付加されること。
     */
    @Test
    public void convertMessageSpecifyProperty() throws Exception {

        // -------------------------------------------------- setup
        final Parent parent = new Parent();
        parent.name = "123456";

        // -------------------------------------------------- execute
        final Validator validator = ValidatorUtil.getValidator();
        final Set<ConstraintViolation<Parent>> constraintViolations = validator.validateProperty(parent, "name");
        final ConstraintViolationConverter converter = new ItemNamedConstraintViolationConverterFactory()
                .create("parent");
        final List<Message> messages = converter.convert(constraintViolations);

        // -------------------------------------------------- assert
        assertThat(messages, contains(MessageMatcher.is("parent.name", "[名前]5文字以内で入力してください。")));

    }

    /**
     * 値バリデーションの場合はバリデーション対象のBeanオブジェクトが存在しないため項目名が設定されないこと。
     */
    @Test
    public void convertMessageOfValidateValue() throws Exception {

        // -------------------------------------------------- execute
        final Validator validator = ValidatorUtil.getValidator();
        final Set<ConstraintViolation<Parent>> constraintViolations = validator.validateValue(
                Parent.class, "name", "123456");
        final ConstraintViolationConverter converter = new ItemNamedConstraintViolationConverterFactory()
                .create("parent");
        final List<Message> messages = converter.convert(constraintViolations);

        // -------------------------------------------------- assert
        assertThat("項目名は付加されないこと",
                messages, contains(MessageMatcher.is("parent.name", "5文字以内で入力してください。")));

    }

    /**
     * クラスレベルバリデーションでもメッセージ変換ができること
     */
    @Test
    public void convertMessageClassLevelValidation() throws Exception {
        // -------------------------------------------------- execute
        final Validator validator = ValidatorUtil.getValidator();
        final Set<ConstraintViolation<ClassLevelValidation>> constraintViolations =
                validator.validate(new ClassLevelValidation());
        final ConstraintViolationConverter converter = new ItemNamedConstraintViolationConverterFactory().create();
        final List<Message> messages = converter.convert(constraintViolations);

        // -------------------------------------------------- assert
        assertThat("クラスレベルのバリデーションなので項目名やプロパティの情報はもたない",
                messages, contains(MessageMatcher.is("", "クラスレベルエラー")));
    }

    /**
     * メッセージの各情報が取得できること。
     * @throws Exception
     */
    @Test
    public void getMessage() throws Exception {
        // -------------------------------------------------- setup
        final Parent parent = new Parent();
        parent.number = "1";
        parent.children = Collections.singletonList(new Child("name"));

        // -------------------------------------------------- execute
        final Validator validator = ValidatorUtil.getValidator();
        final Set<ConstraintViolation<Parent>> constraintViolations = validator.validate(parent);
        final ConstraintViolationConverter converter = new ItemNamedConstraintViolationConverterFactory().create();
        final List<Message> messages = converter.convert(constraintViolations);

        // -------------------------------------------------- assert
        final Message message = messages.get(0);
        assertThat(message.getMessageId(), is("nablarch.core.validation.ee.Required"));
    }

    // -------------------------------------------------- bean definition
    private static class Parent {

        @Required
        @Domain("名前")
        private String name;

        @Digits(integer = 2)
        private String number;

        @Valid
        private List<Child> children;
    }

    private static class Child {

        @Required
        private String name;

        public Child(final String name) {
            this.name = name;
        }
    }

    @ClassLevel
    private static class ClassLevelValidation {

        private String name = "";
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = ClassLevelValidator.class)
    @interface ClassLevel {

        String message() default "クラスレベルエラー";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }

    public static class ClassLevelValidator implements ConstraintValidator<ClassLevel, ClassLevelValidation> {

        @Override
        public void initialize(final ClassLevel level) {
            // nop
        }

        @Override
        public boolean isValid(final ClassLevelValidation validation,
                final ConstraintValidatorContext context) {
            return validation.name.length() == 1;
        }
    }

    // -------------------------------------------------- domain definition
    private static class DomainManagerImpl implements DomainManager<DomainBean> {

        @Override
        public Class<DomainBean> getDomainBean() {
            return DomainBean.class;
        }
    }

    private static class DomainBean {

        @Length(max = 5)
        public String 名前;
    }

    // -------------------------------------------------- helper
    private static class MessageMatcher extends CustomTypeSafeMatcher<Message> {

        private final String name;

        private final String expectedMessage;

        public static MessageMatcher is(final String name, final String message) {
            return new MessageMatcher(name, message);
        }

        public MessageMatcher(final String name, final String expectedMessage) {
            super(name + ":" + expectedMessage);
            this.name = name;
            this.expectedMessage = expectedMessage;
        }

        @Override
        protected void describeMismatchSafely(final Message item, final Description mismatchDescription) {
            ValidationResultMessage validationResultMessage = (ValidationResultMessage) item;
            mismatchDescription.appendValue(validationResultMessage.getPropertyName())
                               .appendText(":")
                               .appendValue(item.formatMessage());
        }

        @Override
        protected boolean matchesSafely(final Message message) {
            ValidationResultMessage validationResultMessage = (ValidationResultMessage) message;
            return message.formatMessage()
                          .equals(expectedMessage)
                    && validationResultMessage.getPropertyName()
                                              .equals(name);
        }
    }
}