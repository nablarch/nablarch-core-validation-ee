package com.example;

import nablarch.core.validation.ee.DomainManager;

/**
 * TODO write document comment.
 *
 * @author T.Kawasaki
 */
public class SampleDomainManager implements DomainManager<SampleDomain> {
    @Override
    public Class<SampleDomain> getDomainBean() {
        return SampleDomain.class;
    }
}
