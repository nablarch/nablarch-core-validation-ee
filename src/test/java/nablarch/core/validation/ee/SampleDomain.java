package nablarch.core.validation.ee;

import java.math.BigDecimal;

/**
 * @author kawasima
 */
public class SampleDomain {

    @Length(max = 5)
    @SystemChar(charsetDef = "英大文字")
    String name;

    @Digits(integer = 3, fraction=0)
    BigDecimal money;

    @Digits(integer = 10, fraction=0)
    BigDecimal bigMoney;
}
