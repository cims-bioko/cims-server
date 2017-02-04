package com.github.cimsbioko.server.domain.util;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.util.Properties;

import static java.util.UUID.randomUUID;

/**
 * A custom uuid generator based on {@link java.util.UUID} that can be used with Hibernate. It only assigns identifiers
 * if the object has a null identifier.
 */
public class UUIDGenerator implements IdentifierGenerator, Configurable {

    public static String generate() {
        return randomUUID().toString().replace("-", "");
    }

    private String entityName;

    @Override
    public void configure(Type type, Properties params, Dialect d) throws MappingException {
        entityName = params.getProperty(ENTITY_NAME);
    }

    @Override
    public Serializable generate(SessionImplementor session, Object object) throws HibernateException {
        EntityPersister ep = session.getEntityPersister(entityName, object);
        Serializable id = ep.getIdentifier(object, session);
        return id != null ? id : generate();
    }
}
