package nablarch.core.validation.ee;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;

import org.hamcrest.collection.IsCollectionWithSize;

import org.junit.Test;

/**
 * {@link SizeValidatorTest}のテストクラス。
 *
 * @author T.Kawasaki
 */
public class SizeValidatorTest extends BeanValidationTestCase {

    private static class ArraySizeBean {
        @Size(max = 3)
        String[] arrayMax;

        @Size(min = 1)
        Object[] arrayMin;

        @Size(min = 1, max = 3)
        Object[] arrayMinAndMax;
        
        @Size(max = 2, message = "２を超えないように。")
        Object[] arrayWithMessage;
    }

    ArraySizeBean arrayBean = new ArraySizeBean();

    /** 配列の要素数が妥当である場合、バリデーションエラーが0件であること */
    @Test
    public void testArrayValid() {
        arrayBean.arrayMax = new String[3];
        arrayBean.arrayMin = new Object[1];
        arrayBean.arrayMinAndMax = new Object[2];
        Set<ConstraintViolation<ArraySizeBean>> violations = validator.validate(arrayBean);
        assertThat(violations.size(), is(0));
    }

    /** サイズ最小値を下回る場合、バリデーションエラーが発生すること。 */
    @Test
    public void testArrayMin() {
        arrayBean.arrayMin = new Object[0];
        Set<ConstraintViolation<ArraySizeBean>> violations = validator.validate(arrayBean);
        assertThat(violations.size(), is(1));
        ConstraintViolation<ArraySizeBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("arrayMin"));
        assertThat(v.getMessage(), is("1以上で入力してください。"));
    }

    /** サイズ最大値を上回る場合、バリデーションエラーが発生すること。 */
    @Test
    public void testArrayMax() {
        arrayBean.arrayMax = new String[4];
        Set<ConstraintViolation<ArraySizeBean>> violations = validator.validate(arrayBean);
        assertThat(violations.size(), is(1));
        ConstraintViolation<ArraySizeBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("arrayMax"));
        assertThat(v.getMessage(), is("3以内で入力してください。"));
    }

    /** サイズ最小値～最大値に当てはまらない場合、バリデーションエラーが発生すること。 */
    @Test
    public void testArrayMinAndMax() {
        arrayBean.arrayMinAndMax = new Object[4];
        Set<ConstraintViolation<ArraySizeBean>> violations = validator.validate(arrayBean);
        assertThat(violations.size(), is(1));
        ConstraintViolation<ArraySizeBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("arrayMinAndMax"));
        assertThat(v.getMessage(), is("1以上3以内で入力してください。"));
    }

    /**
     * メッセージを指定した場合、そのメッセージが設定されること。
     */
    @Test
    public void testSpecifyMessage() throws Exception {
        arrayBean.arrayWithMessage = new Object[3];
        
        Set<ConstraintViolation<ArraySizeBean>> violations = validator.validate(arrayBean);
        assertThat(violations, IsCollectionWithSize.hasSize(1));
        
        ConstraintViolation<ArraySizeBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("arrayWithMessage"));
        assertThat(v.getMessage(), is("２を超えないように。"));
    }

    private static class CollectionSizeBean {

        @Size(max = 3)
        Collection<?> colMax;

        @Size(min = 1)
        Collection<Integer> colMin;
        @Size(min = 1, max = 3)
        Collection<Object> colMinAndMax;
    }

    CollectionSizeBean colBean = new CollectionSizeBean();

    /** コレクションの要素数が妥当である場合、バリデーションエラーが0件であること */
    @Test
    public void testCollectionValid() {
        colBean.colMax = newList(3);
        colBean.colMin = newList(1);
        colBean.colMinAndMax = newList(2);
        Set<ConstraintViolation<CollectionSizeBean>> violations = validator.validate(colBean);
        assertThat(violations.size(), is(0));
    }

    @Test
    public void testCollectionMin() {
        colBean.colMin = newList(0);
        Set<ConstraintViolation<CollectionSizeBean>> violations = validator.validate(colBean);
        assertThat(violations.size(), is(1));
        ConstraintViolation<CollectionSizeBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("colMin"));
        assertThat(v.getMessage(), is("1以上で入力してください。"));
    }


    @Test
    public void testCollectionMax() {
        colBean.colMax = newList(4);
        Set<ConstraintViolation<CollectionSizeBean>> violations = validator.validate(colBean);
        assertThat(violations.size(), is(1));
        ConstraintViolation<CollectionSizeBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("colMax"));
        assertThat(v.getMessage(), is("3以内で入力してください。"));
    }

    @Test
    public void testCollectionMinAndMax() {
        colBean.colMinAndMax = newList(4);
        Set<ConstraintViolation<CollectionSizeBean>> violations = validator.validate(colBean);
        assertThat(violations.size(), is(1));
        ConstraintViolation<CollectionSizeBean> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is("colMinAndMax"));
        assertThat(v.getMessage(), is("1以上3以内で入力してください。"));
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> newList(int size) {
        List<Integer> list = new ArrayList<Integer>(size);
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
        return (List<T>) list;
    }

    /** メッセージ出力検証用bean */
    private static class ArraySizeBeanOnlyMin {
        @Size(min = 3)
        String[] array;
    }

    /**
     * {@link Size}のmax属性のみが指定されたときには
     * エラーメッセージにmin属性に関する文言が出力されない.
     */
    @Test
    public void testOutputMessageOnlyMin() {
    	ArraySizeBeanOnlyMin bean = new ArraySizeBeanOnlyMin();
        bean.array = new String[2];
        Set<ConstraintViolation<ArraySizeBeanOnlyMin>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));
        ConstraintViolation<ArraySizeBeanOnlyMin> v = violations.iterator().next();
        assertThat(v.getMessage(), is("3以上で入力してください。"));
    }

    /** メッセージ出力検証用bean */
    private static class ArraySizeBeanOnlyMax {
        @Size(max = 5)
        String[] array;
    }

    /**
     * {@link Size}のmin属性のみが指定されたときには
     * エラーメッセージにmax属性に関する文言が出力されない.
     */
    @Test
    public void testOutputMessageOnlyMax() {
    	ArraySizeBeanOnlyMax bean = new ArraySizeBeanOnlyMax();
        bean.array = new String[6];
        Set<ConstraintViolation<ArraySizeBeanOnlyMax>> violations = validator.validate(bean);
        assertThat(violations.size(), is(1));
        ConstraintViolation<ArraySizeBeanOnlyMax> v = violations.iterator().next();
        assertThat(v.getMessage(), is("5以内で入力してください。"));
    }

    private interface NormalUser {}
    private interface PremiumUser {}

    private class ListAndGroupsBean {
        @Size.List({
                @Size(max = 5, groups = NormalUser.class),
                @Size(max = 10, groups = PremiumUser.class)
        })
        private String[] test;
    }

    /**
     * Listとgroupsが正しく定義されていること。
     */
    @Test
    public void testListAndGroups() {

        ListAndGroupsBean bean = new ListAndGroupsBean();
        bean.test = new String[] {"1", "2", "3", "4", "5", "6"};

        Set<ConstraintViolation<ListAndGroupsBean>> violations = validator.validate(bean, NormalUser.class);
        assertThat(violations.size(), is(1));
        assertThat(violations.iterator().next().getMessage(), is("5以内で入力してください。"));

        assertThat(validator.validate(bean, PremiumUser.class).size(), is(0));
    }
}