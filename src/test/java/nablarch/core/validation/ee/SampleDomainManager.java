package nablarch.core.validation.ee;

/**
 * @author kawasima
 */
public class SampleDomainManager implements DomainManager<SampleDomain> {

    /** {@inheritDoc} */
    @Override
    public Class<SampleDomain> getDomainBean() {
        return SampleDomain.class;
    }
}
