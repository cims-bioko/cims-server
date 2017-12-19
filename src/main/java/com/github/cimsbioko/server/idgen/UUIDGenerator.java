package com.github.cimsbioko.server.idgen;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.service.ServiceRegistry;
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
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        entityName = params.getProperty(ENTITY_NAME);
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        EntityPersister ep = session.getEntityPersister(entityName, object);
        Serializable id = ep.getIdentifier(object, session);
        return id != null ? id : generate();
    }
}
