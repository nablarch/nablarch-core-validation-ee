package nablarch.core.validation.ee;

import org.junit.Assert;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import java.math.BigDecimal;
import java.util.Set;

public class IncludedInValidatorTest extends BeanValidationTestCase {

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
    public void 入力値がString以外の場合にEnum要素名と比較する場合は検証失敗する() {
        EnumBean bean = new EnumBean();
        bean.invalidEnumName = 1;

        Set<ConstraintViolation<EnumBean>> violations = validator.validate(bean);

        Assert.assertEquals(1, violations.size());

        ConstraintViolation<EnumBean> v1 = violations.iterator().next();
        Assert.assertEquals("指定した列挙型class nablarch.core.validation.ee.IncludedInValidatorTest$DirectEnumの定数のフィールド型と入力値の型が一致しません。", v1.getMessage());
    }

    @Test
    public void 入力値がEnum要素名のいずれかに大文字小文字の区別なく一致する場合は検証成功する() {
        EnumBean bean = new EnumBean();
        bean.enumNameString = "ON";

        Set<ConstraintViolation<EnumBean>> violations = validator.validate(bean);

        Assert.assertTrue(violations.isEmpty());
    }

    @Test
    public void 入力値がEnum要素名のいずれにも一致しない場合は検証失敗する() {
        EnumBean bean = new EnumBean();
        bean.enumNameString = "HOGE";

        Set<ConstraintViolation<EnumBean>> violations = validator.validate(bean);

        Assert.assertEquals(1, violations.size());

        ConstraintViolation<EnumBean> v = violations.iterator().next();
        Assert.assertEquals("enumNameString", v.getPropertyPath().toString());
        Assert.assertEquals("指定した列挙型class nablarch.core.validation.ee.IncludedInValidatorTest$DirectEnumのいずれの要素とも一致しません。", v.getMessage());
    }


    @Test
    public void 入力値がEnum要素名のいずれかに大文字小文字区別して一致する場合は検証成功する() {
        EnumBean bean = new EnumBean();
        bean.enumNameCaseSensitiveName = "ON";

        Set<ConstraintViolation<EnumBean>> violations = validator.validate(bean);

        Assert.assertTrue(violations.isEmpty());
    }

    @Test
    public void 入力値がEnum要素名のいずれかに大文字小文字区別して一致しない場合は検証失敗する() {
        EnumBean bean = new EnumBean();
        bean.enumNameCaseSensitiveName = "On";

        Set<ConstraintViolation<EnumBean>> violations = validator.validate(bean);

        Assert.assertEquals(1, violations.size());

        ConstraintViolation<EnumBean> v = violations.iterator().next();
        Assert.assertEquals("enumNameCaseSensitiveName", v.getPropertyPath().toString());
        Assert.assertEquals("指定した列挙型class nablarch.core.validation.ee.IncludedInValidatorTest$DirectEnumのいずれの要素とも一致しません。", v.getMessage());
    }

    @Test
    public void 入力値がEnum要素のString型フィールドのいずれかに一致する場合は検証成功する() {
        EnumBean bean = new EnumBean();
        bean.enumValueString = "1";

        Set<ConstraintViolation<EnumBean>> violations = validator.validate(bean);

        Assert.assertTrue(violations.isEmpty());
    }

    @Test
    public void 入力値がEnum要素のString型フィールドのいずれにも一致しない場合は検証失敗する() {
        EnumBean bean = new EnumBean();
        bean.enumValueString = "HOGE";

        Set<ConstraintViolation<EnumBean>> violations = validator.validate(bean);

        Assert.assertEquals(1, violations.size());

        ConstraintViolation<EnumBean> v = violations.iterator().next();
        Assert.assertEquals("enumValueString", v.getPropertyPath().toString());
        Assert.assertEquals("指定した列挙型class nablarch.core.validation.ee.IncludedInValidatorTest$EnValueStringEnumのいずれの要素とも一致しません。", v.getMessage());
    }

    @Test
    public void 入力値がEnum要素のBigDecimal型フィールドのいずれかに一致する場合は検証成功する() {
        EnumBean bean = new EnumBean();
        bean.enumValueBigDecimal = new BigDecimal("1");

        Set<ConstraintViolation<EnumBean>> violations = validator.validate(bean);

        Assert.assertTrue(violations.isEmpty());
    }

    @Test
    public void 入力値がEnum要素のBigDecimal型フィールドのいずれにも一致しない場合は検証失敗する() {
        EnumBean bean = new EnumBean();
        bean.enumValueBigDecimal = new BigDecimal("2");

        Set<ConstraintViolation<EnumBean>> violations = validator.validate(bean);

        Assert.assertEquals(1, violations.size());

        ConstraintViolation<EnumBean> v = violations.iterator().next();
        Assert.assertEquals("enumValueBigDecimal", v.getPropertyPath().toString());
        Assert.assertEquals("指定した列挙型class nablarch.core.validation.ee.IncludedInValidatorTest$EnValueBigDecimalEnumのいずれの要素とも一致しません。", v.getMessage());
    }

    @Test
    public void 入力値がEnum要素のNumber型フィールドのいずれかに一致する場合は検証成功する() {
        EnumBean bean = new EnumBean();
        bean.enumValueNumber = 1;

        Set<ConstraintViolation<EnumBean>> violations = validator.validate(bean);

        Assert.assertTrue(violations.isEmpty());
    }

    @Test
    public void 入力値がEnum要素のNumber型フィールドのいずれにも一致しない場合は検証失敗する() {
        EnumBean bean = new EnumBean();
        bean.enumValueNumber = 2;

        Set<ConstraintViolation<EnumBean>> violations = validator.validate(bean);

        Assert.assertEquals(1, violations.size());

        ConstraintViolation<EnumBean> v = violations.iterator().next();
        Assert.assertEquals("enumValueNumber", v.getPropertyPath().toString());
        Assert.assertEquals("指定した列挙型class nablarch.core.validation.ee.IncludedInValidatorTest$EnValueNumberEnumのいずれの要素とも一致しません。", v.getMessage());
    }

    private enum DirectEnum {
        ON,
        OFF
    }

    private enum EnValueStringEnum implements EnValue {
        ON("1"),
        OFF("0");

        private final String value;

        EnValueStringEnum(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    private enum EnValueBigDecimalEnum implements EnValue {
        ON(new BigDecimal("1")),
        OFF(new BigDecimal("0"));

        private final BigDecimal value;

        EnValueBigDecimalEnum(BigDecimal value) {
            this.value = value;
        }

        @Override
        public BigDecimal getValue() {
            return value;
        }
    }


    private enum EnValueNumberEnum implements EnValue {
        ON(1),
        OFF(0);

        private final Number value;

        EnValueNumberEnum(Number value) {
            this.value = value;
        }

        @Override
        public Number getValue() {
            return value;
        }
    }

    private static class EnumBean {

        @IncludedIn(DirectEnum.class)
        String enumNameString;

        @IncludedIn(value = DirectEnum.class, caseInsensitive = false)
        String enumNameCaseSensitiveName;

        @IncludedIn(EnValueStringEnum.class)
        String enumValueString;

        @IncludedIn(EnValueBigDecimalEnum.class)
        BigDecimal enumValueBigDecimal;

        @IncludedIn(EnValueNumberEnum.class)
        Number enumValueNumber;

        @IncludedIn(DirectEnum.class)
        Integer invalidEnumName;
    }
}