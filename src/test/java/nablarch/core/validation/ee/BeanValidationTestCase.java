package nablarch.core.validation.ee;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;

import org.junit.After;

/**
 * TODO write document comment.
 *
 * @author T.Kawasaki
 */
public abstract class BeanValidationTestCase {

    protected final Validator validator = ValidatorUtil.getValidator();

    @After
    public void tearDown() {
        SystemRepository.clear();
    }

    void prepareSystemRepository() {
        SystemRepository.load(new DiContainer(new XmlComponentDefinitionLoader
                                                      ("nablarch/core/validation/ee/beanValidation.xml")));
    }

    protected <T> List<ConstraintViolation<T>> sort(Set<ConstraintViolation<T>> violations) {
        List<ConstraintViolation<T>> sorted = new ArrayList<ConstraintViolation<T>>(violations);
        Collections.sort(sorted, new Comparator<ConstraintViolation<T>>() {
            @Override
            public int compare(ConstraintViolation<T> o1, ConstraintViolation<T> o2) {
                int ret = o1.getPropertyPath().toString().compareTo(o2.getPropertyPath().toString());
                if (ret != 0) {
                    return ret;
                }
                return o1.getMessage().compareTo(o2.getMessage());
            }
        });
        return sorted;
    }
}
