package com.github.cimsbioko.server.hibernate;

import com.github.cimsbioko.server.security.SecureTokenGenerator;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;

/**
 * A custom id generator based on {@link com.github.cimsbioko.server.security.SecureTokenGenerator} that can be used
 * with Hibernate.
 */
public class TokenGenerator implements IdentifierGenerator {

    private final com.github.cimsbioko.server.security.TokenGenerator generator;

    public TokenGenerator() {
        generator = new SecureTokenGenerator();
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        return generator.generate();
    }
}
