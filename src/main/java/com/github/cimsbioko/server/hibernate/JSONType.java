package com.github.cimsbioko.server.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.json.JSONObject;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class JSONType implements UserType {

    @Override
    public int[] sqlTypes() {
        return new int[]{Types.OTHER};
    }

    @Override
    public Class returnedClass() {
        return JSONObject.class;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return x == y || (x instanceof JSONObject && y instanceof JSONObject && x.toString().equals(y.toString()));
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return x == null ? 0 : x.toString().hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException {
        Object value = rs.getObject(names[0]);
        return value == null ? null : new JSONObject(value.toString());
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            st.setObject(index, value.toString(), Types.OTHER);
        }
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return value == null ? null : new JSONObject(value.toString());
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return value == null ? null : value.toString();
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return new JSONObject(cached.toString());
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return deepCopy(original);
    }
}
