package nablarch.core.validation.ee;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hamcrest.collection.IsCollectionWithSize;

import org.junit.Test;

/**
 * {@link nablarch.core.validation.ee.Digits.DigitsValidator}のテストクラス。
 *
 * @author Sumida
 */
public class DigitsTest extends BeanValidationTestCase {

    /** バリデータ動作検証用クラス */
    private static class DigitsBean {
        @Digits(integer = 3, fraction = 4)
        String digitsString;

        @Digits(integer = 2, fraction = 3)
        Number digitsNumber;

        @Digits(integer = 1, fraction = 2)
        String digitsString2;

        @Digits(integer = 4, fraction = 5)
        BigDecimal digitsBigDecimal;

        @Digits(integer = 4, fraction = 0)
        BigDecimal digitsBigDecimal2;

        @Digits(integer = 4, fraction = 0)
        Boolean digitsBoolean;
        
        @Digits(integer = 2, message = "正しい値をいれてください。")
        Integer digitsInteger;

        @Digits(integer = 3)
        String integerString;
    }

    /** バリデータ動作検証用インスタンス */
    private DigitsBean bean = new DigitsBean();

    /** 整数部、小数部とも最大桁の場合、バリデーションエラーが発生しない(文字列型プロパティ) */
    @Test
    public void testEqualLimitForString() {
        bean.digitsString = "123.4567";  // OK
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.isEmpty(), is(true));
    }

    /** 整数部、小数部とも最大桁の場合、バリデーションエラーが発生しない(数値型プロパティ)  */
    @Test
    public void testEqualLimitForNumber() {
        bean.digitsNumber = 12.345;    // OK
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.isEmpty(), is(true));
    }

    /** 整数部、小数部とも最大桁の場合、バリデーションエラーが発生しない(数値型プロパティ - BigDecimal)  */
    @Test
    public void testEqualLimitForNumberBigDecimal() {
        bean.digitsBigDecimal = new BigDecimal("1234.56789");    // OK
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.isEmpty(), is(true));
    }

    /** 整数部、小数部とも最大桁を下回る場合、バリデーションエラーが発生しない(文字列型プロパティ) */
    @Test
    public void testUnderLimitForString() {
        bean.digitsString = "12.345";  // OK
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.isEmpty(), is(true));
    }

    /** 整数部、小数部とも最大桁を下回る場合、バリデーションエラーが発生しない(数値型プロパティ) */
    @Test
    public void testUnderLimitForNumber() {
        bean.digitsNumber = 1.23;    // OK
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.isEmpty(), is(true));
    }

    /** 整数部が最大桁を超える場合、バリデーションエラーが発生する(文字列型プロパティ) */
    @Test
    public void testOverLimitIntegerForString() {
        bean.digitsString = "1234.5678";  // NG
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));

        ConstraintViolation<DigitsBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("digitsString"));
        assertThat(v.getMessage(), is("整数部は3桁以内、小数部は4桁以内で入力してください。"));
    }

    /** 整数部が最大桁を超える場合、バリデーションエラーが発生する(数値型プロパティ) */
    @Test
    public void testOverLimitIntegerForNumber() {
        bean.digitsNumber = 123.456;  // NG
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));

        ConstraintViolation<DigitsBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("digitsNumber"));
        assertThat(v.getMessage(), is("整数部は2桁以内、小数部は3桁以内で入力してください。"));
    }

    /** 小数部が最大桁を超える場合、バリデーションエラーが発生する(文字列型プロパティ) */
    @Test
    public void testOverLimitFractionForString() {
        bean.digitsString = "123.45678";  // NG
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));

        ConstraintViolation<DigitsBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("digitsString"));
        assertThat(v.getMessage(), is("整数部は3桁以内、小数部は4桁以内で入力してください。"));
    }

    /** 小数部が最大桁を超える場合、バリデーションエラーが発生する(数値型プロパティ) */
    @Test
    public void testOverLimitFractionForNumber() {
        bean.digitsNumber = 12.3456;  // NG
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));

        ConstraintViolation<DigitsBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("digitsNumber"));
        assertThat(v.getMessage(), is("整数部は2桁以内、小数部は3桁以内で入力してください。"));
    }

    /** 整数部が最大桁を下回る場合、バリデーションエラーが発生しない(文字列型プロパティ) */
    @Test
    public void testUnderLimitIntegerForString() {
        bean.digitsString = "12.5678";  // OK
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.isEmpty(), is(true));
    }

    /** 整数部が最大桁を下回る場合、バリデーションエラーが発生しない(数値型プロパティ) */
    @Test
    public void testUnderLimitIntegerForNumber() {
        bean.digitsNumber = 1.456;  // OK
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.isEmpty(), is(true));
    }

    /** 小数部が最大桁を下回る場合、バリデーションエラーが発生しない(文字列型プロパティ) */
    @Test
    public void testUnderLimitFractionForString() {
        bean.digitsString = "123.456";  // OK
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.isEmpty(), is(true));
    }

    /** 小数部が最大桁を下回る場合、バリデーションエラーが発生しない(数値型プロパティ) */
    @Test
    public void testUnderLimitFractionForNumber() {
        bean.digitsNumber = 12.34;  // OK
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.isEmpty(), is(true));
    }

    /** 整数のみの場合で、整数部が最大桁を超えない場合、バリデーションエラーが発生しない(文字列型プロパティ) */
    @Test
    public void testOnlyIntegerForString() {
        bean.digitsString = "123";  // OK
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.isEmpty(), is(true));
    }

    /** 整数のみの場合で、整数部が最大桁を超えない場合、バリデーションエラーが発生しない(数値型プロパティ) */
    @Test
    public void testOnlyIntegerForNumber() {
        bean.digitsNumber = 12;  // OK
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.isEmpty(), is(true));
    }

    /** 小数のみの場合で、小数部が最大桁を超えない場合、バリデーションエラーが発生しない(文字列型プロパティ) */
    @Test
    public void testOnlyFractionForString() {
        bean.digitsString = ".1234";  // OK
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.isEmpty(), is(true));
    }

    /** 小数のみの場合で、小数部が最大桁を超えない場合、バリデーションエラーが発生しない(数値型プロパティ) */
    @Test
    public void testOnlyFractionForNumber() {
        bean.digitsNumber = .123;  // OK
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.isEmpty(), is(true));
    }

    /** NULLの場合、バリデーションエラーが発生しない(文字列型プロパティ) */
    @Test
    public void testNullForString() {
        bean.digitsString = null;  // OK
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.isEmpty(), is(true));
    }

    /** NULLの場合、バリデーションエラーが発生しない(数値型プロパティ) */
    @Test
    public void testNullForNumber() {
        bean.digitsNumber = null;  // OK
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.isEmpty(), is(true));
    }

    /** 空文字の場合、バリデーションエラーが発生する */
    @Test
    public void testEmptyString() {
        bean.digitsString = "";  // OK
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.isEmpty(), is(true));
    }

    /** 数値でないの場合、バリデーションエラーが発生する */
    @Test
    public void testNotNumberForString() {
        bean.digitsString = "あ";  // NG
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));

        ConstraintViolation<DigitsBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("digitsString"));
        assertThat(v.getMessage(), is("整数部は3桁以内、小数部は4桁以内で入力してください。"));
    }

    /** 数値として不正な値の場合、バリデーションエラーが発生する */
    @Test
    public void testIncorrectNumberForString() {
        bean.digitsString = "1..1";  // NG
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));

        ConstraintViolation<DigitsBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("digitsString"));
        assertThat(v.getMessage(), is("整数部は3桁以内、小数部は4桁以内で入力してください。"));
    }

    /** 全角数字（整数部が最大桁）の場合、バリデーションエラーが発生する */
    @Test
    public void testFullWidthDigitForString() {
        bean.digitsString = "１２３";  // NG
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));

        ConstraintViolation<DigitsBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("digitsString"));
        assertThat(v.getMessage(), is("整数部は3桁以内、小数部は4桁以内で入力してください。"));
    }

    /** 全角数字（整数部が最大桁オーバー）の場合、バリデーションエラーが発生する */
    @Test
    public void testFullWidthDigitOverLimitIntegerForString() {
        bean.digitsString = "１２３４";  // NG
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));

        ConstraintViolation<DigitsBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("digitsString"));
        assertThat(v.getMessage(), is("整数部は3桁以内、小数部は4桁以内で入力してください。"));
    }

    /** 全角数字（小数点は半角）の場合、バリデーションエラーが発生する */
    @Test
    public void testFullWidthDigitFullForStringHalfDecimalPoint() {
        bean.digitsString = "１２３.４５６７";  // NG
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));

        ConstraintViolation<DigitsBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("digitsString"));
        assertThat(v.getMessage(), is("整数部は3桁以内、小数部は4桁以内で入力してください。"));
    }

    /** 全角数字（小数点も全角）の場合、バリデーションエラーが発生する */
    @Test
    public void testFullWidthDigitForStringFullDecimalPoint() {
        bean.digitsString = "１２３．４５６７";  // NG
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));

        ConstraintViolation<DigitsBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("digitsString"));
        assertThat(v.getMessage(), is("整数部は3桁以内、小数部は4桁以内で入力してください。"));
    }

    /** 明示的に+を指定した正数値（最大桁）の場合、バリデーションエラーが発生しない(文字列型プロパティ) */
    @Test
    public void testPlusForString() {
        bean.digitsString = "+123.4567";  // OK
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.isEmpty(), is(true));
    }

    /** マイナス値（最大桁）の場合、バリデーションエラーが発生しない(文字列型プロパティ) */
    @Test
    public void testMinusForString() {
        bean.digitsString = "-123.4567";  // OK
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.isEmpty(), is(true));
    }

    /** マイナス値（最大桁）の場合、バリデーションエラーが発生しない(数値型プロパティ) */
    @Test
    public void testMinusForNumber() {
        bean.digitsNumber = -12.345;  // OK
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.isEmpty(), is(true));
    }

    /** マイナス値（整数部の桁数オーバー）の場合、バリデーションエラーが発生する(文字列型プロパティ) */
    @Test
    public void testOverLimitIntegerForStringMinus() {
        bean.digitsString = "-1234.567";  // NG
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));

        ConstraintViolation<DigitsBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("digitsString"));
        assertThat(v.getMessage(), is("整数部は3桁以内、小数部は4桁以内で入力してください。"));
    }

    /** マイナス値（整数部の桁数オーバー）の場合、バリデーションエラーが発生する(数値型プロパティ) */
    @Test
    public void testOverLimitIntegerForNumberMinus() {
        bean.digitsNumber = -123.45;  // NG
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));

        ConstraintViolation<DigitsBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("digitsNumber"));
        assertThat(v.getMessage(), is("整数部は2桁以内、小数部は3桁以内で入力してください。"));
    }

    /** マイナス値（小数部の桁数オーバー）の場合、バリデーションエラーが発生する(文字列型プロパティ) */
    @Test
    public void testOverLimitFractionForStringMinus() {
        bean.digitsString = "-12.45678";  // NG
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));

        ConstraintViolation<DigitsBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("digitsString"));
        assertThat(v.getMessage(), is("整数部は3桁以内、小数部は4桁以内で入力してください。"));
    }

    /** マイナス値（小数部の桁数オーバー）の場合、バリデーションエラーが発生する(数値型プロパティ) */
    @Test
    public void testOverLimitFractionForNumberMinus() {
        bean.digitsNumber = -1.2345;  // NG
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));

        ConstraintViolation<DigitsBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("digitsNumber"));
        assertThat(v.getMessage(), is("整数部は2桁以内、小数部は3桁以内で入力してください。"));
    }

    //
    /** 複数項目に{@link Digits}アノテーションが付加され、桁数が不正の場合、バリデーションエラーがその数分、発生する */
    @Test
    public void testOverLimitForStringMinus() {
        bean.digitsString  = "123.45678";  // NG
        bean.digitsString2 = "1234.5678";  // NG
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(2));
    }

    /** BigDecimalのスケールがマイナスの場合、小数部の桁数は0扱いになる  */
    @Test
    public void testBigDecimalScaleMinus() {
        BigDecimal bd = new BigDecimal("1234.56789");
        bean.digitsBigDecimal2 = bd.setScale(-1, RoundingMode.DOWN);
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.isEmpty(), is(true));
    }

    /**
     * 小数値の場合にバリデーションエラーとなること。
     */
    @Test
    public void testInteger() throws Exception {
        bean.integerString = "123.45";
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);

        assertThat(violations.size(), is(1));
        ConstraintViolation<DigitsBean> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), is("integerString"));
        assertThat(violation.getMessage(), is("整数部は3桁以内で入力してください。"));
    }

    /** 数値型、文字列型以外のフィールドにアノテーションが設定された場合、バリデーションエラーが発生する */
    @Test
    public void testBoolean() {
        bean.digitsBoolean = true;  // NG
        Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));

        ConstraintViolation<DigitsBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("digitsBoolean"));
        assertThat(v.getMessage(), is("整数部は4桁以内で入力してください。"));
    }

    /** バリデータ動作検証用クラス */
    private static class DigitsBeanOnlyInteger {
        @Digits(integer = 3)
        String digitsString;
    }

    /**
     * {@link Digits}のinteger属性のみが指定されたときには
     * エラーメッセージにfraction属性に関する文言が出力されない.
     */
    @Test
    public void testOutputMessageOnlyInteger() {
    	DigitsBeanOnlyInteger bean = new DigitsBeanOnlyInteger();
        bean.digitsString = "1234.5678";  // NG
        Set<ConstraintViolation<DigitsBeanOnlyInteger>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));

        ConstraintViolation<DigitsBeanOnlyInteger> v = violations.iterator().next();
        assertThat(v.getMessage(), is("整数部は3桁以内で入力してください。"));
    }

    /**
     * message属性を指定した場合、そのメッセージが設定されること。
     */
    @Test
    public void testSpecifyMessage() throws Exception {
        final DigitsBean bean = new DigitsBean();
        bean.digitsInteger = 100;

        final Set<ConstraintViolation<DigitsBean>> violations = validator.validate(bean);
        assertThat(violations, IsCollectionWithSize.hasSize(1));

        ConstraintViolation<DigitsBean> v = violations.iterator().next();
        assertThat(v.getMessage(), is("正しい値をいれてください。"));
    }

    private interface NormalUser {}
    private interface PremiumUser {}

    private class ListAndGroupsBean {
        @Digits.List({
                @Digits(integer = 3, groups = {NormalUser.class}),
                @Digits(integer = 10, groups = {PremiumUser.class})
        })
        private String test;
    }

    /**
     * Listとgroupsが正しく定義されていること。
     */
    @Test
    public void testListAndGroups() {

        ListAndGroupsBean bean = new ListAndGroupsBean();
        bean.test = "12345";

        Set<ConstraintViolation<ListAndGroupsBean>> violations = validator.validate(bean, NormalUser.class);
        assertThat(violations.size(), is(1));
        assertThat(violations.iterator().next().getMessage(), is("整数部は3桁以内で入力してください。"));

        assertThat(validator.validate(bean, PremiumUser.class).size(), is(0));
    }
}
