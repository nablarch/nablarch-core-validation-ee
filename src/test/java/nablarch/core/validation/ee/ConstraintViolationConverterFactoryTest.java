package nablarch.core.validation.ee;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import nablarch.core.repository.ObjectLoader;
import nablarch.core.repository.SystemRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * {@link ConstraintViolationConverterFactory}のテストクラス。
 */
public class ConstraintViolationConverterFactoryTest {

    private final ConstraintViolationConverterFactory sut = new ConstraintViolationConverterFactory();

    @After
    @Before
    public void clear() throws Exception {
        SystemRepository.clear();
    }

    /**
     * デフォルト設定の場合は、{@link ConstraintViolationConverter}が戻されること。
     *
     * @throws Exception
     */
    @Test
    public void defaultSetting() throws Exception {

        assertThat("ConstraintViolationConverterが返されること",
                sut.create().getClass().getName(), is(ConstraintViolationConverter.class.getName()));

        assertThat("ConstraintViolationConverterが返されること",
                sut.create("prefix").getClass().getName(), is(ConstraintViolationConverter.class.getName()));
    }

    /**
     * ファクトリクラスをリポジトリに設定した場合、そのファクトリから生成した{@link ConstraintViolationConverter}実装がかえされること。
     * @throws Exception
     */
    @Test
    public void customSetting() throws Exception {
        SystemRepository.load(new ObjectLoader() {
            @Override
            public Map<String, Object> load() {
                final HashMap<String, Object> result = new HashMap<String, Object>();
                result.put("constraintViolationConverterFactory", new CustomConstraintViolationConverterFactory());
                return result;
            }
        });
        
        assertThat("CustomConstraintViolationConverterが返されること",
                sut.create().getClass().getName(), is(CustomConstraintViolationConverter.class.getName()));

        assertThat("CustomConstraintViolationConverterが返されること",
                sut.create("prefix").getClass().getName(), is(CustomConstraintViolationConverter.class.getName()));
    }

    /**
     * カスタムなファクトリ実装
     */
    private static class CustomConstraintViolationConverterFactory extends ConstraintViolationConverterFactory {

        @Override
        public ConstraintViolationConverter create() {
            return new CustomConstraintViolationConverter();
        }

        @Override
        public ConstraintViolationConverter create(final String prefix) {
            return new CustomConstraintViolationConverter(prefix);
        }
    }
    
    private static class CustomConstraintViolationConverter extends ConstraintViolationConverter {

        public CustomConstraintViolationConverter() {
            super();
        }

        public CustomConstraintViolationConverter(final String prefix) {
            super(prefix);
        }
    }
}