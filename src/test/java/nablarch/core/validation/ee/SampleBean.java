package nablarch.core.validation.ee;

import javax.validation.Valid;
import javax.validation.groups.ConvertGroup;
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

    @javax.validation.constraints.Digits(integer = 5, fraction = 2)
    String digitsTest;

    @SystemChar.List({
            @SystemChar(charsetDef = "英大文字", groups = Test1.class),
            @SystemChar(charsetDef = "数字", groups = Test2.class)
    })
    String groupTest;


    @Valid
    @ConvertGroup(from = Test1.class, to = Test2.class)
    NestedGroupTestBean nestedGroupTestBean;

    public interface Test1 {}
    public interface Test2 {}

    public static class NestedGroupTestBean {

        @Length(min = 2, max = 2, groups = Test2.class)
        String nestedGroupTestString;

        @SystemChar(charsetDef = "英大文字", groups = Test2.class)
        String ignoredProperty;

    }


}
