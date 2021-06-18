package nablarch.core.validation.ee;

import java.util.Collection;

/**
 * こいつらのバリデーションができること。
 *
 * @author T.Kawasaki
 */
public class SampleBean {

    @Length(max = 5, min = 1)
    String lengthTest;

    @Required
    String requiredTest;

    //@NumberRange(min = -1.2, max = 10.1)
    //Number numberRangeTest;

    //@Digits(integer = 4, fraction = 1, commaSeparated = true)
    //String digitsTest;

    //@Size(min = 1, max = 10)
    Collection<?> sizeTest;

    @SystemChar(charsetDef = "ascii")
    String systemCharTest;

    @Domain("name")
    String domainTest;

    @jakarta.validation.constraints.Digits(integer = 5, fraction = 2)
    String digitsTest;
}
