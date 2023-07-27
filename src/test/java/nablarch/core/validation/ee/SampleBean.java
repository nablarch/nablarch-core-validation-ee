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

    Collection<?> sizeTest;

    @SystemChar(charsetDef = "ascii")
    String systemCharTest;

    @Domain("name")
    String domainTest;

    @javax.validation.constraints.Digits(integer = 5, fraction = 2)
    String digitsTest;

    @SystemChar.List({
            @SystemChar(charsetDef = "英大文字", groups = Test1.class),
            @SystemChar(charsetDef = "数字", groups = Test2.class)
    })
    String groupTest;


    @SystemChar(charsetDef = "数字", groups = Test1.class)
    @Length(max = 5, min = 5, groups = Test2.class)
    String multiGroupTest;

    @Length(min = 2, max = 2, groups = Test1.class)
    @SystemChar(charsetDef = "数字", groups = Test2.class)
    String specifiedPropertyTest;

    @SystemChar(charsetDef = "英大文字", groups = Test1.class)
    String ignoredPropertyTest;


    public interface Test1 {}
    public interface Test2 {}
}
