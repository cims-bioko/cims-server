package com.github.cimsbioko.server.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.jdom2.Document;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import static com.github.cimsbioko.server.util.JDOMUtil.docFromObj;
import static com.github.cimsbioko.server.util.JDOMUtil.stringFromDoc;

public class XMLType implements UserType {

    @Override
    public int[] sqlTypes() {
        return new int[]{Types.SQLXML};
    }

    @Override
    public Class returnedClass() {
        return Document.class;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return x == y || stringFromDoc((Document) x).equals(stringFromDoc((Document) y));
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return x == null ? 0 : stringFromDoc((Document) x).hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException {
        try {
            return docFromObj(rs.getObject(names[0]));
        } catch (JDOMException | IOException e) {
            throw new HibernateException("failed to convert object during get", e);
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, Types.SQLXML);
        } else {
            st.setObject(index, stringFromDoc((Document) value), Types.SQLXML);
        }
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        try {
            return docFromObj(value);
        } catch (SQLException | JDOMException | IOException e) {
            throw new HibernateException("failed to convert object for deep copy", e);
        }
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        if (value == null) {
            return null;
        } else if (value instanceof Document) {
            return stringFromDoc((Document) value);
        } else {
            throw new HibernateException("expected jdom document, received " + value.getClass());
        }
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        try {
            return docFromObj(cached);
        } catch (SQLException | JDOMException | IOException e) {
            throw new HibernateException("failed to assemble from cached object", e);
        }
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return deepCopy(original);
    }
}
