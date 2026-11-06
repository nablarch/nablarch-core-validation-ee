package nablarch.core.validation.ee;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;

import org.junit.Before;
import org.junit.Test;

/**
 * 各制約アノテーションをコンテナ要素({@code List<@Length String>} など)に指定した場合のテストクラス。
 * <p>
 * {@link java.lang.annotation.ElementType#TYPE_USE} を {@code @Target} に持つことで、
 * Bean Validation のコンテナ要素制約として要素ごとに検証されることを確認する。
 * </p>
 */
public class ContainerElementValidationTest extends BeanValidationTestCase {

    @Before
    public void setUp() {
        prepareSystemRepository();
    }

    private enum Color {
        RED, BLUE
    }

    /** {@link java.util.List} の要素に各制約を指定した Bean */
    private static class ListBean {
        List<@Length(max = 3) String> lengths;

        List<@SystemChar(charsetDef = "英大文字") String> systemChars;

        List<@EnumElement(Color.class) String> enumElements;

        List<@Required String> requireds;

        List<@DateFormat String> dateFormats;

        List<@Digits(integer = 3) BigDecimal> digits;

        List<@NumberRange(min = 1, max = 10) Integer> numberRanges;

        List<@DecimalRange(min = "0.1", max = "0.5") BigDecimal> decimalRanges;

        List<@Size(max = 2) List<String>> sizes;

        List<@Domain("name") String> domains;
    }

    /** {@link java.util.Map} のキーと値に制約を指定した Bean */
    private static class MapBean {
        Map<@Length(max = 3) String, @NumberRange(max = 10) Integer> map;
    }

    /** {@link java.util.Optional} の値に制約を指定した Bean */
    private static class OptionalBean {
        Optional<@SystemChar(charsetDef = "数字") String> optional;
    }

    /** {@link java.util.List} の要素に {@link Valid} を指定し、要素の Bean をカスケード検証する Bean */
    private static class CascadeBean {
        List<@Valid InnerForm> forms;
    }

    /** {@link CascadeBean} の要素となる Bean */
    private static class InnerForm {
        @Length(max = 3)
        String name;

        InnerForm(String name) {
            this.name = name;
        }
    }

    @Test
    public void testLength() {
        ListBean bean = new ListBean();
        bean.lengths = Arrays.asList("abc", "");
        assertThat(validator.validate(bean).isEmpty(), is(true));

        bean.lengths = Arrays.asList("abc", "abcd");
        assertSingleViolation(validator.validate(bean), "lengths[1].<list element>", "3文字以内で入力してください。");
    }

    @Test
    public void testSystemChar() {
        ListBean bean = new ListBean();
        bean.systemChars = Arrays.asList("ABC", "XYZ");
        assertThat(validator.validate(bean).isEmpty(), is(true));

        bean.systemChars = Arrays.asList("ABC", "abc");
        assertSingleViolation(validator.validate(bean), "systemChars[1].<list element>", "英大文字でないですよ。");
    }

    @Test
    public void testEnumElement() {
        ListBean bean = new ListBean();
        bean.enumElements = Arrays.asList("RED", "blue");
        assertThat(validator.validate(bean).isEmpty(), is(true));

        bean.enumElements = Arrays.asList("RED", "GREEN");
        Set<ConstraintViolation<ListBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));
        ConstraintViolation<ListBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("enumElements[1].<list element>"));
        assertThat(v.getMessage(), containsString("のいずれの要素とも一致しません。"));
    }

    @Test
    public void testRequired() {
        ListBean bean = new ListBean();
        bean.requireds = Arrays.asList("a", "b");
        assertThat(validator.validate(bean).isEmpty(), is(true));

        bean.requireds = Arrays.asList("a", "");
        assertSingleViolation(validator.validate(bean), "requireds[1].<list element>", "必須項目です。");

        bean.requireds = Arrays.asList("a", null);
        assertSingleViolation(validator.validate(bean), "requireds[1].<list element>", "必須項目です。");
    }

    @Test
    public void testDateFormat() {
        ListBean bean = new ListBean();
        bean.dateFormats = Arrays.asList("20260918", "20260101");
        assertThat(validator.validate(bean).isEmpty(), is(true));

        bean.dateFormats = Arrays.asList("20260918", "2026-09-18");
        assertSingleViolation(validator.validate(bean), "dateFormats[1].<list element>", "日付書式に一致しません。");
    }

    @Test
    public void testDigits() {
        ListBean bean = new ListBean();
        bean.digits = Arrays.asList(new BigDecimal("123"), new BigDecimal("1"));
        assertThat(validator.validate(bean).isEmpty(), is(true));

        bean.digits = Arrays.asList(new BigDecimal("123"), new BigDecimal("1234"));
        assertSingleViolation(validator.validate(bean), "digits[1].<list element>", "整数部は3桁以内で入力してください。");
    }

    @Test
    public void testNumberRange() {
        ListBean bean = new ListBean();
        bean.numberRanges = Arrays.asList(1, 10);
        assertThat(validator.validate(bean).isEmpty(), is(true));

        bean.numberRanges = Arrays.asList(1, 11);
        assertSingleViolation(validator.validate(bean), "numberRanges[1].<list element>", "1以上10以内で入力してください。");
    }

    @Test
    public void testDecimalRange() {
        ListBean bean = new ListBean();
        bean.decimalRanges = Arrays.asList(new BigDecimal("0.1"), new BigDecimal("0.5"));
        assertThat(validator.validate(bean).isEmpty(), is(true));

        bean.decimalRanges = Arrays.asList(new BigDecimal("0.1"), new BigDecimal("0.6"));
        assertSingleViolation(validator.validate(bean), "decimalRanges[1].<list element>", "0.1以上0.5以内で入力してください。");
    }

    @Test
    public void testSize() {
        ListBean bean = new ListBean();
        bean.sizes = Arrays.asList(Arrays.asList("a", "b"), Arrays.asList("a"));
        assertThat(validator.validate(bean).isEmpty(), is(true));

        bean.sizes = Arrays.asList(Arrays.asList("a", "b"), Arrays.asList("a", "b", "c"));
        assertSingleViolation(validator.validate(bean), "sizes[1].<list element>", "2以内で入力してください。");
    }

    /**
     * {@link Domain} をコンテナ要素に指定した場合、
     * ドメイン定義 Bean({@link SampleDomain#name})の制約で要素が検証されること。
     */
    @Test
    public void testDomain() {
        ListBean bean = new ListBean();
        bean.domains = Arrays.asList("ABCDE", "A");
        assertThat(validator.validate(bean).isEmpty(), is(true));

        bean.domains = Arrays.asList("ABCDE", "ABCDEF");
        assertSingleViolation(validator.validate(bean), "domains[1].<list element>", "5文字以内で入力してください。");

        bean.domains = Arrays.asList("ABCDE", "abc");
        assertSingleViolation(validator.validate(bean), "domains[1].<list element>", "英大文字でないですよ。");
    }

    @Test
    public void testMapKeyAndValue() {
        MapBean bean = new MapBean();
        bean.map = new HashMap<>();
        bean.map.put("abc", 10);
        assertThat(validator.validate(bean).isEmpty(), is(true));

        bean.map.put("abcd", 11);
        List<ConstraintViolation<MapBean>> violations = sort(validator.validate(bean));
        assertThat(violations.size(), is(2));
        assertThat(violations.get(0).getPropertyPath().toString(), is("map<K>[abcd].<map key>"));
        assertThat(violations.get(0).getMessage(), is("3文字以内で入力してください。"));
        assertThat(violations.get(1).getPropertyPath().toString(), is("map[abcd].<map value>"));
        assertThat(violations.get(1).getMessage(), is("10以内で入力してください。"));
    }

    @Test
    public void testOptional() {
        OptionalBean bean = new OptionalBean();
        bean.optional = Optional.of("123");
        assertThat(validator.validate(bean).isEmpty(), is(true));

        bean.optional = Optional.of("12a");
        assertSingleViolation(validator.validate(bean), "optional", "数字でないですよ。");
    }

    /**
     * コンテナ要素に {@link Valid} を指定した場合、
     * 要素の Bean に指定した制約で要素ごとに検証されること。
     */
    @Test
    public void testCascadeValidationOfContainerElement() {
        CascadeBean bean = new CascadeBean();
        bean.forms = Arrays.asList(new InnerForm("abc"), new InnerForm(""));
        assertThat(validator.validate(bean).isEmpty(), is(true));

        bean.forms = Arrays.asList(new InnerForm("abc"), new InnerForm("abcd"));
        assertSingleViolation(validator.validate(bean), "forms[1].name", "3文字以内で入力してください。");
    }

    private static <T> void assertSingleViolation(Set<ConstraintViolation<T>> violations,
                                                  String expectedPath, String expectedMessage) {
        assertThat(violations.size(), is(1));
        ConstraintViolation<T> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is(expectedPath));
        assertThat(v.getMessage(), is(expectedMessage));
    }
}
