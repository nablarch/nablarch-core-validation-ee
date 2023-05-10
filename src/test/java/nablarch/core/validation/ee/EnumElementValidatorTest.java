package nablarch.core.validation.ee;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import java.math.BigDecimal;
import java.util.Set;

public class EnumElementValidatorTest extends BeanValidationTestCase {


    @Rule
    public ExpectedException expectedException  = ExpectedException.none();

    @Test
    public void 入力値がnullの場合は検証成功する() {
        EnumBean bean = new EnumBean();

        Set<ConstraintViolation<EnumBean>> violations = validator.validate(bean);

        Assert.assertTrue(violations.isEmpty());
    }

    @Test
    public void 入力値が空文字列の場合は検証成功する() {
        EnumBean bean = new EnumBean();
        bean.enumNameString = "";

        Set<ConstraintViolation<EnumBean>> violations = validator.validate(bean);

        Assert.assertTrue(violations.isEmpty());
    }

    @Test
    public void 列挙型定数のフィールド型がStringでもNumberでもない場合例外が発生する(){
        ErrorBean bean = new ErrorBean();
        bean.invalidEnumValueObject = "hoge";

        expectedException.expect(ValidationException.class);
        expectedException.expectCause(Matchers.allOf(
            Matchers.instanceOf(IllegalArgumentException.class),
            Matchers.hasProperty("message", Matchers.is("The return type of EnumElement.WithValue#getValue() must be String or Number."))));

        validator.validate(bean);
    }

    @Test
    public void 入力値がString以外の場合に列挙型定数名と比較する場合は検証失敗する() {
        EnumBean bean = new EnumBean();
        bean.invalidEnumName = 1;

        Set<ConstraintViolation<EnumBean>> violations = validator.validate(bean);

        Assert.assertEquals(1, violations.size());

        ConstraintViolation<EnumBean> v1 = violations.iterator().next();
        Assert.assertEquals("指定した列挙型class nablarch.core.validation.ee.EnumElementValidatorTest$DirectEnumのいずれの要素とも一致しません。", v1.getMessage());
    }

    @Test
    public void 入力値がStringの場合に列挙型定数のNumber型フィールドと比較する場合は検証失敗する() {
        EnumBean bean = new EnumBean();
        bean.invalidEnumValueString = "hOgE";

        Set<ConstraintViolation<EnumBean>> violations = validator.validate(bean);

        Assert.assertEquals(1, violations.size());

        ConstraintViolation<EnumBean> v1 = violations.iterator().next();
        Assert.assertEquals("指定した列挙型class nablarch.core.validation.ee.EnumElementValidatorTest$WithValueNumberEnumのいずれの要素とも一致しません。", v1.getMessage());
    }

    @Test
    public void 入力値がIntegerの場合に列挙型定数のString型フィールドと比較する場合は検証失敗する() {
        EnumBean bean = new EnumBean();
        bean.invalidEnumValueInteger = 1;

        Set<ConstraintViolation<EnumBean>> violations = validator.validate(bean);

        Assert.assertEquals(1, violations.size());

        ConstraintViolation<EnumBean> v1 = violations.iterator().next();
        Assert.assertEquals("指定した列挙型class nablarch.core.validation.ee.EnumElementValidatorTest$WithValueStringEnumのいずれの要素とも一致しません。", v1.getMessage());
    }

    @Test
    public void 入力値が列挙型定数名のいずれかに大文字小文字の区別なく一致する場合は検証成功する() {
        EnumBean bean = new EnumBean();
        bean.enumNameString = "ON";

        Set<ConstraintViolation<EnumBean>> violations = validator.validate(bean);

        Assert.assertTrue(violations.isEmpty());
    }

    @Test
    public void 入力値が列挙型定数名のいずれにも一致しない場合は検証失敗する() {
        EnumBean bean = new EnumBean();
        bean.enumNameString = "HOGE";

        Set<ConstraintViolation<EnumBean>> violations = validator.validate(bean);

        Assert.assertEquals(1, violations.size());

        ConstraintViolation<EnumBean> v = violations.iterator().next();
        Assert.assertEquals("enumNameString", v.getPropertyPath().toString());
        Assert.assertEquals("指定した列挙型class nablarch.core.validation.ee.EnumElementValidatorTest$DirectEnumのいずれの要素とも一致しません。", v.getMessage());
    }


    @Test
    public void caseInsensitiveがfalseの時_入力値が列挙型定数名のいずれかに大文字小文字区別して一致する場合は検証成功する() {
        EnumBean bean = new EnumBean();
        bean.enumNameCaseSensitiveString = "ON";

        Set<ConstraintViolation<EnumBean>> violations = validator.validate(bean);

        Assert.assertTrue(violations.isEmpty());
    }

    @Test
    public void caseInsensitiveがfalseの時_入力値が列挙型定数名のいずれにも大文字小文字区別して一致しない場合は検証失敗する() {
        EnumBean bean = new EnumBean();
        bean.enumNameCaseSensitiveString = "On";

        Set<ConstraintViolation<EnumBean>> violations = validator.validate(bean);

        Assert.assertEquals(1, violations.size());

        ConstraintViolation<EnumBean> v = violations.iterator().next();
        Assert.assertEquals("enumNameCaseSensitiveString", v.getPropertyPath().toString());
        Assert.assertEquals("指定した列挙型class nablarch.core.validation.ee.EnumElementValidatorTest$DirectEnumのいずれの要素とも一致しません。", v.getMessage());
    }

    @Test
    public void messageが指定された場合検証失敗メッセージが指定されたメッセージとなる() {
        EnumBean bean = new EnumBean();
        bean.enumNameStringWithMessage = "hoge";

        Set<ConstraintViolation<EnumBean>> violations = validator.validate(bean);

        Assert.assertEquals(1, violations.size());

        ConstraintViolation<EnumBean> v = violations.iterator().next();
        Assert.assertEquals("enumNameStringWithMessage", v.getPropertyPath().toString());
        Assert.assertEquals("てすとめっせーじ", v.getMessage());
    }

    @Test
    public void 入力値が列挙型定数のString型フィールド値のいずれかに一致する場合は検証成功する() {
        EnumBean bean = new EnumBean();
        bean.enumValueString = "1";

        Set<ConstraintViolation<EnumBean>> violations = validator.validate(bean);

        Assert.assertTrue(violations.isEmpty());
    }

    @Test
    public void 入力値が列挙型定数のString型フィールド値のいずれにも一致しない場合は検証失敗する() {
        EnumBean bean = new EnumBean();
        bean.enumValueString = "HOGE";

        Set<ConstraintViolation<EnumBean>> violations = validator.validate(bean);

        Assert.assertEquals(1, violations.size());

        ConstraintViolation<EnumBean> v = violations.iterator().next();
        Assert.assertEquals("enumValueString", v.getPropertyPath().toString());
        Assert.assertEquals("指定した列挙型class nablarch.core.validation.ee.EnumElementValidatorTest$WithValueStringEnumのいずれの要素とも一致しません。", v.getMessage());
    }

    @Test
    public void 入力値が列挙型定数のBigDecimal型フィールド値のいずれかに一致する場合は検証成功する() {
        EnumBean bean = new EnumBean();
        bean.enumValueBigDecimal = new BigDecimal("1");

        Set<ConstraintViolation<EnumBean>> violations = validator.validate(bean);

        Assert.assertTrue(violations.isEmpty());
    }

    @Test
    public void 入力値が列挙型定数のBigDecimal型フィールド値のいずれにも一致しない場合は検証失敗する() {
        EnumBean bean = new EnumBean();
        bean.enumValueBigDecimal = new BigDecimal("2");

        Set<ConstraintViolation<EnumBean>> violations = validator.validate(bean);

        Assert.assertEquals(1, violations.size());

        ConstraintViolation<EnumBean> v = violations.iterator().next();
        Assert.assertEquals("enumValueBigDecimal", v.getPropertyPath().toString());
        Assert.assertEquals("指定した列挙型class nablarch.core.validation.ee.EnumElementValidatorTest$WithValueBigDecimalEnumのいずれの要素とも一致しません。", v.getMessage());
    }

    @Test
    public void 入力値が列挙型定数のDouble型フィールド値のいずれかに一致する場合は検証成功する() {
        EnumBean bean = new EnumBean();


        Set<ConstraintViolation<EnumBean>> violations = validator.validate(bean);

        Assert.assertTrue(violations.isEmpty());
    }

    @Test
    public void 入力値が列挙型定数のDouble型フィールド値のいずれにも一致しない場合は検証失敗する() {
        EnumBean bean = new EnumBean();
        bean.enumValueDouble = 2.0;

        Set<ConstraintViolation<EnumBean>> violations = validator.validate(bean);

        Assert.assertEquals(1, violations.size());

        ConstraintViolation<EnumBean> v = violations.iterator().next();
        Assert.assertEquals("enumValueDouble", v.getPropertyPath().toString());
        Assert.assertEquals("指定した列挙型class nablarch.core.validation.ee.EnumElementValidatorTest$WithValueDoubleEnumのいずれの要素とも一致しません。", v.getMessage());
    }

    @Test
    public void 入力値が列挙型定数のNumber型フィールド値のいずれかに一致する場合は検証成功する() {
        EnumBean bean = new EnumBean();
        bean.enumValueNumber = 1;

        Set<ConstraintViolation<EnumBean>> violations = validator.validate(bean);

        Assert.assertTrue(violations.isEmpty());
    }

    @Test
    public void 入力値が列挙型定数のNumber型フィールド値のいずれにも一致しない場合は検証失敗する() {
        EnumBean bean = new EnumBean();
        bean.enumValueNumber = 2;

        Set<ConstraintViolation<EnumBean>> violations = validator.validate(bean);

        Assert.assertEquals(1, violations.size());

        ConstraintViolation<EnumBean> v = violations.iterator().next();
        Assert.assertEquals("enumValueNumber", v.getPropertyPath().toString());
        Assert.assertEquals("指定した列挙型class nablarch.core.validation.ee.EnumElementValidatorTest$WithValueNumberEnumのいずれの要素とも一致しません。", v.getMessage());
    }

    @Test
    public void リストとグループが正しく動作する() {
        ListAndGroupsBean bean = new ListAndGroupsBean();
        bean.listEnumNameString = "oN";

        Set<ConstraintViolation<ListAndGroupsBean>> violations1 = validator.validate(bean, Test1.class);
        Assert.assertTrue(violations1.isEmpty());

        Set<ConstraintViolation<ListAndGroupsBean>> violations2 = validator.validate(bean, Test2.class);
        Assert.assertEquals(1, violations2.size());
        ConstraintViolation<ListAndGroupsBean> v = violations2.iterator().next();
        Assert.assertEquals("listEnumNameString", v.getPropertyPath().toString());
        Assert.assertEquals("指定した列挙型class nablarch.core.validation.ee.EnumElementValidatorTest$DirectEnumのいずれの要素とも一致しません。", v.getMessage());
    }

    private enum DirectEnum {
        ON,
        OFF
    }

    private enum WithValueStringEnum implements EnumElement.WithValue<String> {
        ON("1"),
        OFF("0");

        private final String value;

        WithValueStringEnum(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    private enum WithValueDoubleEnum implements EnumElement.WithValue<Double> {
        ON(1.0),
        OFF(0.0);

        private final Double value;

        WithValueDoubleEnum(Double value) {
            this.value = value;
        }

        @Override
        public Double getValue() {
            return value;
        }
    }

    private enum WithValueBigDecimalEnum implements EnumElement.WithValue<BigDecimal> {
        ON(new BigDecimal("1")),
        OFF(new BigDecimal("0"));

        private final BigDecimal value;

        WithValueBigDecimalEnum(BigDecimal value) {
            this.value = value;
        }

        @Override
        public BigDecimal getValue() {
            return value;
        }
    }


    private enum WithValueNumberEnum implements EnumElement.WithValue<Number> {
        ON(1),
        OFF(0);

        private final Number value;

        WithValueNumberEnum(Number value) {
            this.value = value;
        }

        @Override
        public Number getValue() {
            return value;
        }
    }

    private enum WithValueObjectEnum implements EnumElement.WithValue<Object> {
        ON(new Object()),
        OFF(new Object());

        private final Object value;

        WithValueObjectEnum(Object value) {
            this.value = value;
        }

        @Override
        public Object getValue() {
            return value;
        }
    }

    private interface Test1 {
    }

    private interface Test2 {
    }

    private static class EnumBean {

        @EnumElement(DirectEnum.class)
        String enumNameString;

        @EnumElement(value = DirectEnum.class, caseSensitive = true)
        String enumNameCaseSensitiveString;

        @EnumElement(value = DirectEnum.class, message = "てすとめっせーじ")
        String enumNameStringWithMessage;

        @EnumElement(WithValueStringEnum.class)
        String enumValueString;

        @EnumElement(WithValueBigDecimalEnum.class)
        BigDecimal enumValueBigDecimal;

        @EnumElement(WithValueDoubleEnum.class)
        Double enumValueDouble;

        @EnumElement(WithValueNumberEnum.class)
        Number enumValueNumber;

        @EnumElement(DirectEnum.class)
        Integer invalidEnumName;

        @EnumElement(WithValueNumberEnum.class)
        String invalidEnumValueString;

        @EnumElement(WithValueStringEnum.class)
        Integer invalidEnumValueInteger;
    }

    private static class ErrorBean {
        @EnumElement(WithValueObjectEnum.class)
        Object invalidEnumValueObject;
    }

    private static class ListAndGroupsBean {
        @EnumElement.List({
            @EnumElement(value = DirectEnum.class, groups = {Test1.class}),
            @EnumElement(value = DirectEnum.class, caseSensitive = true, groups = {Test2.class})
        })
        private String listEnumNameString;
    }

}
