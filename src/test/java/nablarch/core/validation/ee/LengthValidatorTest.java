package nablarch.core.validation.ee;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsEmptyCollection.*;
import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Test;

/**
 * {@link LengthValidatorTest}のテストクラス。
 *
 * @author T.Kawasaki
 */
public class LengthValidatorTest extends BeanValidationTestCase {


    LengthBean bean = new LengthBean();

    private static class LengthBean {
        @Length(max = 5, min = 1)
        public String lengthTest;

        @Length(min = 2)
        public String lengthTest2;
        
        @Length(max = 10, message = "１０文字をこえないでください。")
        public String lengthTest3;

        @Length(min = 10, max = 10)
        public String fixedLength;
    }

    @Test
    public void testMax() {
        bean.lengthTest = "12345";  // OK
        bean.lengthTest2 = "123456789012345678901234567890123456789012345678901234567890"
                + "1234567890123456789012345678901234567890123456789012345678901234567890"
                + "1234567890123456789012345678901234567890123456789012345678901234567890"
                + "1234567890123456789012345678901234567890123456789012345678901234567890"
                + "1234567890123456789012345678901234567890123456789012345678901234567890"
                + "1234567890123456789012345678901234567890123456789012345678901234567890"
                + "1234567890123456789012345678901234567890123456789012345678901234567890"; // OK
        Set<ConstraintViolation<LengthBean>> violations = validator.validate(bean);
        assertThat(violations.isEmpty(), is(true));
    }

    private String str(long length) {
        StringBuilder sb = new StringBuilder();
        for (long i = 0; i < length; i++) {
            sb.append("a");
        }
        return sb.toString();
    }

    @Test
    public void testOverLimit() {
        bean.lengthTest = "123456";  // NG
        bean.lengthTest2 = "1"; // NG
        Set<ConstraintViolation<LengthBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(2));

        ConstraintViolation<LengthBean> v = find(violations, "lengthTest");
        assertThat(v, is(notNullValue()));
        assertThat(v.getMessage(), is("1文字以上5文字以内で入力してください。"));

        v = find(violations, "lengthTest2");
        assertThat(v, is(notNullValue()));
        assertThat(v.getMessage(), is("2文字以上で入力してください。"));
    }

    /**
     * {@link Length#message()}を指定したばあい、そのメッセージが送出されること。
     * @throws Exception
     */
    @Test
    public void testSpecifyMessage() throws Exception {
        bean.lengthTest3 = str(11);

        final Set<ConstraintViolation<LengthBean>> violations = validator.validate(bean);

        assertThat(violations, IsCollectionWithSize.hasSize(1));
        final ConstraintViolation<LengthBean> v = find(violations, "lengthTest3");
        assertThat(v.getMessage(), is("１０文字をこえないでください。"));
    }

    @Test
    public void testFixedLength() throws Exception {
        bean.fixedLength = "123456789";
        
        final Set<ConstraintViolation<LengthBean>> violations = validator.validate(bean);

        assertThat(violations, IsCollectionWithSize.hasSize(1));
        final ConstraintViolation<LengthBean> v = find(violations, "fixedLength");
        assertThat(v.getMessage(), is("10文字で入力してください。"));
    }

    private ConstraintViolation<LengthBean> find(Set<ConstraintViolation<LengthBean>> violations, String propertyPath) {
        Iterator<ConstraintViolation<LengthBean>> itr = violations.iterator();
        while (itr.hasNext()) {
            ConstraintViolation<LengthBean> v = itr.next();
            if (propertyPath.equals(v.getPropertyPath().toString())) {
                return v;
            }
        }
        return null;
    }

    @Test
    public void testMin() {
        bean.lengthTest = "0";  // NG
        Set<ConstraintViolation<LengthBean>> violations = validator.validate(bean);
        assertThat(violations.isEmpty(), is(true));
    }

    @Test
    public void testUnderLimit() {
        bean.lengthTest = "";  // OK
        Set<ConstraintViolation<LengthBean>> violations = validator.validate(bean);
        assertThat(violations.isEmpty(), is(true));
    }

    /** メッセージ出力検証用bean */
    private static class LengthBeanOnlyMax {
        @Length(max = 5)
        public String lengthTest;
    }

    /**
     * {@link Length}のmax属性のみが指定されたときには
     * エラーメッセージにmin属性に関する文言が出力されない.
     */
    @Test
    public void testOutputMessageOnlyMax() {
    	LengthBeanOnlyMax bean = new LengthBeanOnlyMax();
    	bean.lengthTest = "123456";  // NG
        Set<ConstraintViolation<LengthBeanOnlyMax>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));

        ConstraintViolation<LengthBeanOnlyMax> v = violations.iterator().next();
        assertThat(v.getMessage(), is("5文字以内で入力してください。"));
    }

    /** メッセージ出力検証用bean */
    private static class LengthBeanOnlyMin {
        @Length(min = 4)
        public String lengthTest;
    }

    /**
     * {@link Length}のmin属性のみが指定されたときには
     * エラーメッセージにmax属性に関する文言が出力されない.
     */
    @Test
    public void testOutputMessageOnlyMin() {
    	LengthBeanOnlyMin bean = new LengthBeanOnlyMin();
    	bean.lengthTest = "123";  // NG
        Set<ConstraintViolation<LengthBeanOnlyMin>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));

        ConstraintViolation<LengthBeanOnlyMin> v = violations.iterator().next();
        assertThat(v.getMessage(), is("4文字以上で入力してください。"));
    }

    private interface NormalUser {}
    private interface PremiumUser {}

    private class ListAndGroupsBean {
        @Length.List({
            @Length(max = 10, groups = NormalUser.class),
            @Length(max = 100, groups = PremiumUser.class)
        })
        private String test;
    }

    /**
     * Listとgroupsが正しく定義されていること。
     */
    @Test
    public void testListAndGroups() {

        ListAndGroupsBean bean = new ListAndGroupsBean();
        bean.test = "123456789012";

        Set<ConstraintViolation<ListAndGroupsBean>> violations = validator.validate(bean, NormalUser.class);
        assertThat(violations.size(), is(1));
        assertThat(violations.iterator().next().getMessage(), is("10文字以内で入力してください。"));

        assertThat(validator.validate(bean, PremiumUser.class).size(), is(0));
    }

    private class SurrogatePair {
        @Length(min = 5, max = 10)
        private String test;
    }

    /**
     * サロゲートペアを含む文字列の長さがmaxを超えた場合
     */
    @Test
    public void testValidateLongerWithSurrogatePair() {
        Set<?> violations = validator.validateValue(SurrogatePair.class, "test", "1234567890\uD867\uDE3D");
        assertThat(violations, not(empty()));
    }

    /**
     * サロゲートペアを含む文字列の長さがminを下回った場合
     */
    @Test
    public void testValidateShorterWithSurrogatePair() {
        Set<?> violations = validator.validateValue(SurrogatePair.class, "test", "123\uD867\uDE3D");
        assertThat(violations, empty());
    }

    /**
     * サロゲートペアを含む文字列の長さが妥当な場合
     */
    @Test
    public void testValidateSuccessWithSurrogatePair() {
        Set<?> violationsForJustMax = validator.validateValue(SurrogatePair.class, "test", "123456789\uD867\uDE3D");
        assertThat("Just max", violationsForJustMax, empty());
        Set<?> violationsForJustMin = validator.validateValue(SurrogatePair.class, "test", "1234\uD867\uDE3D");
        assertThat("Just min", violationsForJustMin, empty());
    }
}