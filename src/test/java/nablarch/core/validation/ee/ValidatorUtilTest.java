package nablarch.core.validation.ee;

import nablarch.core.message.ApplicationException;
import nablarch.core.repository.ObjectLoader;
import nablarch.core.repository.SystemRepository;
import nablarch.test.support.SystemRepositoryResource;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * {@link ValidatorUtil}のテスト。
 */
public class ValidatorUtilTest {

    @Rule
    public SystemRepositoryResource repositoryResource = new SystemRepositoryResource("nablarch/core/validation/ee/beanValidation.xml");

    @After
    public void tearDown() {
        SystemRepository.clear();
    }

    /**
     * {@link ValidatorFactoryBuilder}を設定していない場合、デフォルトの{@link javax.validation.ValidatorFactory}が取得できること。
     */
    @Test
    public void testGetDefaultValidatorFactory() {
        assertThat(ValidatorUtil.getValidatorFactory(), is(notNullValue()));
    }

    /**
     * {@link ValidatorFactoryBuilder}を設定した場合、カスタムの{@link javax.validation.ValidatorFactory}が取得できること。
     */
    @Test
    public void testGetCustomValidatorFactory() {
        repositoryResource.addComponent("validatorFactoryBuilder", new CustomValidatorFactoryBuilder());
        assertThat(ValidatorUtil.getValidatorFactory(), is(instanceOf(CustomValidatorFactory.class)));
    }

    /**
     * Beanを指定してvalidationが簡単に実行できることを確認する。
     *
     * バリデーションエラーの項目があるので、{@link ApplicationException}が送出されること
     */
    @Test
    public void testValidateToErrorObject() throws Exception {
        SampleBean bean = new SampleBean();
        bean.lengthTest = "123456";
        try {
            ValidatorUtil.validate(bean);
            fail("バリデーションエラーが発生するはず");
        } catch (ApplicationException e) {
            assertThat("エラーになるプロパティは2つなので、エラー数は2", e.getMessages(), hasSize(2));
            List<String> messages = Arrays.asList(
                    e.getMessages().get(0).formatMessage(),
                    e.getMessages().get(1).formatMessage()
            );
            assertThat("次のエラーメッセージが含まれているはず", messages, is(containsInAnyOrder("必須項目です。", "1文字以上5文字以内で入力してください。")));
        }
    }

    /**
     * 項目名を付加するコンバータを使った場合、メッセージの先頭に項目名が付加されること。
     */
    @Test
    public void testValidateToErrorObjectWithItemName() throws Exception {
        addItemNamedConverter();

        SampleBean bean = new SampleBean();
        bean.lengthTest = "123456";
        try {
            ValidatorUtil.validate(bean);
            fail("バリデーションエラーが発生するはず");
        } catch (ApplicationException e) {
            assertThat("エラーになるプロパティは2つなので、エラー数は2", e.getMessages(), hasSize(2));
            List<String> messages = Arrays.asList(
                    e.getMessages()
                     .get(0)
                     .formatMessage(),
                    e.getMessages()
                     .get(1)
                     .formatMessage()
            );
            assertThat("次のエラーメッセージが含まれているはず", messages, is(
                    containsInAnyOrder(
                            "[必須の項目]必須項目です。",
                            "[長さテストの項目]1文字以上5文字以内で入力してください。"
                    )));
        }
    }

    /**
     * {@link ValidatorUtil#validate(Object, String...)}のテスト。
     * <p/>
     * 指定したプロパティのみがバリデーションされ、{@link ApplicationException}が送出されること
     *
     */
    @Test
    public void testValidateToErrorObjectSpecifiedProperty() throws Exception{

        SampleBean sampleBean = new SampleBean();
        sampleBean.lengthTest = "123456";
        try{
            ValidatorUtil.validate(sampleBean, "requiredTest");
            fail("バリデーションエラーが発生するはず");
        }catch (ApplicationException e) {
            assertThat("プロパティを1つ指定したので、エラー数は1", e.getMessages(), hasSize(1));
            assertThat("必須項目エラー", e.getMessages().get(0).formatMessage(), containsString("必須項目です。"));
        }
    }
    
    /**
     * {@link ValidatorUtil#validate(Object, String...)}のテスト。
     * <p/>
     * 指定したプロパティのみがバリデーションされ、{@link ApplicationException}が送出されること
     * 項目名を付加するコンバータを使っているので、メッセージに項目名が設定されること。
     *
     */
    @Test
    public void testValidateToErrorObjectSpecifiedPropertyWithItemName() throws Exception{
        addItemNamedConverter();
        SampleBean sampleBean = new SampleBean();
        sampleBean.lengthTest = "123456";
        try{
            ValidatorUtil.validate(sampleBean, "requiredTest");
            fail("バリデーションエラーが発生するはず");
        }catch (ApplicationException e) {
            assertThat("プロパティを1つ指定したので、エラー数は1", e.getMessages(), hasSize(1));
            assertThat("必須項目エラー", e.getMessages().get(0).formatMessage(), containsString("[必須の項目]必須項目です。"));
        }
    }

    /**
     * {@link ValidatorUtil#validate(Object, String...)}のテスト。
     * <p/>
     * 第１引数が{@code null}の場合、実行時例外が発生すること。
     */
    @Test
    public void testValidateToNullObject(){
        try{
            ValidatorUtil.validate(null, "test1");
            fail("");
        }catch (Exception e){
            assertTrue(e instanceof IllegalArgumentException);
            assertEquals("HV000116: The object to be validated must not be null.", e.getMessage());
        }
    }

    /**
     * {@link ValidatorUtil#validate(Object, String...)}のテスト。
     * <p/>
     * 第１引数が無効なオブジェクトの場合、エラーメッセージが生成されないこと。
     */
    @Test
    public void testValidateToInValidObject(){
        try{
            ValidatorUtil.validate("", "test1");
            ValidatorUtil.validate(1, "test1");
        }catch (Exception e){
            fail("例外は発生しないはず");
        }
    }

    /**
     * {@link ValidatorUtil#validate(Object, String...)}のテスト。
     * <p/>
     * 第２引数が{@code null}または空文字の場合、何もしないこと。
     */
    @Test
    public void testValidateToVarargsNullOrEmpty(){
        try {
            ValidatorUtil.validate(new SampleBean(), (String[]) null);
            ValidatorUtil.validate(new SampleBean(), "");
        }catch(ApplicationException e){
            fail("例外は発生しないはず");
        }
    }

    /**
     * {@link ValidatorUtil#validate(Object, String...)}のテスト。
     * <p/>
     * 第２引数に{@code null}が含まれている場合、実行時例外が発生すること。
     */
    @Test
    public void testValidateToVarargsIncludesNullOrEmpty(){
        try {
            ValidatorUtil.validate(new SampleBean(), null, "lengthTest", null);
            fail("例外が発生するはず");
        }catch(Exception e){
            assertTrue(e instanceof IllegalArgumentException);
            assertEquals(e.getMessage(), "HV000038: Invalid property path.");
        }
    }

    /**
     * {@link ValidatorUtil#validate(Object, String...)}のテスト。
     * <p/>
     * 第2引数に存在しないプロパティが指定された場合、実行時例外が発生すること。
     */
    @Test
    public void testValidateToNotExistProperty() {
        try {
            ValidatorUtil.validate(new SampleBean(), "test1");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            assertTrue(e.getMessage().startsWith("HV000039"));
        }
    }

    /**
     * {@link ValidatorUtil#validate(Object, String...)}のテスト。
     * <p/>
     * バリデーションエラーになるプロパティを重複して指定した場合、バリデーション結果が１つになること。
     */
    @Test
    public void testValidateToDuplicatedProperty(){
        try{
            ValidatorUtil.validate(new SampleBean(), "requiredTest", "requiredTest");
            fail("バリデーションエラーが発生するはず");
        }catch(ApplicationException e){
            assertThat("重複するプロパティを指定しても結果は１つ", e.getMessages(), hasSize(1));
            assertThat("必須項目エラー", e.getMessages().get(0).formatMessage(), containsString("必須項目です。"));
        }
    }

    /**
     * Beanを指定したvalidationが簡単に実行できることを確認する。
     *
     * バリデーションエラーの項目がない場合、例外などが発生せずに正常に終わること。
     */
    @Test
    public void testValidateToValidObject() throws Exception {
        SampleBean bean = new SampleBean();
        bean.lengthTest = "12345";
        bean.requiredTest = "test";
        ValidatorUtil.validate(bean);
    }

    /**
     * グループを指定したBean Validationが実行できることを確認する。
     * Test1グループの検証に成功する場合、例外などが発生せずに正常に終わること。
     */
    @Test
    public void testGroupTest1() {
        SampleBean bean = new SampleBean();
        bean.groupTest = "ABCDEFG";

        try {
            ValidatorUtil.validateWithGroup(bean, SampleBean.Test1.class);
        } catch (Exception e) {
            fail("バリデーションに成功するはず");
        }
    }

    /**
     * グループを指定したBean Validationが実行できることを確認する。
     * Test2グループの検証に失敗する場合、バリデーションエラーとして検出できること。
     */
    @Test
    public void testGroupTest2() {
        SampleBean bean = new SampleBean();
        bean.groupTest = "ABCDEFG";

        try {
            ValidatorUtil.validateWithGroup(bean, SampleBean.Test2.class);
            fail("バリデーションに失敗するはず");
        } catch (ApplicationException e) {
            assertThat("エラーになるプロパティは1つなので、エラー数は1", e.getMessages(), hasSize(1));
            assertThat("次のエラーメッセージが含まれているはず", e.getMessages().get(0).formatMessage(), is("数字でないですよ。"));
        }
    }

    /**
     * グループを指定したBean Validationが実行できることを確認する。
     * 複数のグループを指定して検証できること。
     */
    @Test
    public void testMultiGroupSpecified() {
        SampleBean bean = new SampleBean();
        bean.multiGroupTest = "ABCDEFG";

        try {
            ValidatorUtil.validateWithGroup(bean, SampleBean.Test1.class, SampleBean.Test2.class);
            fail("バリデーションに失敗するはず");
        } catch (ApplicationException e) {
            assertThat("エラーになるプロパティは1つで、2つのグループの検証でエラーになっているので、エラー数は2", e.getMessages(), hasSize(2));
            List<String> messages = Arrays.asList(
                    e.getMessages().get(0).formatMessage(),
                    e.getMessages().get(1).formatMessage()
            );
            assertThat("次のエラーメッセージが含まれているはず", messages, is(containsInAnyOrder("5文字で入力してください。", "数字でないですよ。")));
        }
    }

    /**
     * プロパティとグループを指定したBean Validationが実行できることを確認する。
     * 指定したプロパティが指定したグループでの検証ルールで検証できること。
     */
    @Test
    public void testSpecifyPropertyAndGroup() {
        SampleBean bean = new SampleBean();
        bean.specifiedPropertyTest = "abcd";
        bean.ignoredPropertyTest = "123";

        try {
            ValidatorUtil.validateProperty(bean, "specifiedPropertyTest", SampleBean.Test1.class);
            fail("検証に失敗するはず");
        } catch (ApplicationException e) {
            assertThat("エラーになるプロパティは1つ、エラーになる検証ルールは1つなので、エラー数は1", e.getMessages(), hasSize(1));
            assertThat("次のエラーメッセージが含まれているはず", e.getMessages().get(0).formatMessage(), is("2文字で入力してください。"));
        }
    }

    /**
     * プロパティ指定したBean Validationが実行できることを確認する。
     * 指定したプロパティがデフォルトグループ指定時は検証されないこと。
     */
    @Test
    public void testSpecifyPropertyWithoutGroup() {
        SampleBean bean = new SampleBean();
        bean.specifiedPropertyTest = "abcd";
        bean.ignoredPropertyTest = "123";

        try {
            ValidatorUtil.validateProperty(bean, "specifiedPropertyTest");
        } catch (ApplicationException e) {
            fail("検証はスキップするはず");
        }
    }

    /**
     * 存在しないプロパティを指定した場合に実行時エラーが発生することを確認する。
     */
    @Test
    public void testSpecifyNonExistentPropertyName() {
        SampleBean bean = new SampleBean();
        bean.specifiedPropertyTest = "abcd";
        bean.ignoredPropertyTest = "123";

        try {
            ValidatorUtil.validateProperty(bean, "foobar", SampleBean.Test1.class);
            fail("検証に失敗するはず");
        } catch (IllegalArgumentException e) {
            assertThat("次のエラーメッセージが含まれているはず", e.getMessage(), is("HV000039: Invalid property path. Either there is no property foobar in entity nablarch.core.validation.ee.SampleBean or it is not possible to cascade to the property."));
        }
    }

    public static final class CustomValidatorFactory implements ValidatorFactory {
        @Override
        public Validator getValidator() { return null; }
        @Override
        public ValidatorContext usingContext() { return null; }
        @Override
        public MessageInterpolator getMessageInterpolator() { return null; }
        @Override
        public TraversableResolver getTraversableResolver() { return null; }
        @Override
        public ConstraintValidatorFactory getConstraintValidatorFactory() { return null; }
        @Override
        public ParameterNameProvider getParameterNameProvider() { return null; }
        @Override
        public <T> T unwrap(Class<T> aClass) { return null; }
        @Override
        public void close() {}
    }
    public static final class CustomValidatorFactoryBuilder extends ValidatorFactoryBuilder {
        @Override
        protected ValidatorFactory build() {
            return new CustomValidatorFactory();
        }
    }

    private static void addItemNamedConverter() {
        SystemRepository.load(new ObjectLoader() {
            @Override
            public Map<String, Object> load() {
                final HashMap<String, Object> result = new HashMap<String, Object>();
                result.put("constraintViolationConverterFactory", new ItemNamedConstraintViolationConverterFactory());
                return result;
            }
        });
    }
}
