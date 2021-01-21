package com.github.cimsbioko.server.service.impl;

import com.github.cimsbioko.server.service.StoredProcService;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.sql.CallableStatement;
import java.sql.Types;
import java.util.Collections;

public class StoredProcServiceImpl implements StoredProcService {

    private final EntityManager em;

    public StoredProcServiceImpl(EntityManager em) {
        this.em = em;
    }

    private Session getSession() {
        return em.unwrap(Session.class);
    }

    private String getParamString(int numArgs) {
        return String.join(",", Collections.nCopies(numArgs, "?"));
    }

    @Override
    @Transactional
    public void callProcedure(String procName, Object... args) {
        Session s = getSession();
        // ensure any pending updates/deletes are applied before we call the procedure
        if (s.isDirty()) {
            s.flush();
        }
        s.doWork(
                c -> {
                    try (CallableStatement f = c.prepareCall(getProcedureCall(procName, getParamString(args.length)))) {
                        for (int a = 0; a < args.length; a++) {
                            f.setObject(a + 1, args[a]);
                        }
                        f.execute();
                    }
                }
        );
    }

    private String getProcedureCall(String procName, String params) {
        return String.format("{ call %s(%s) }", procName, params);
    }

    @Override
    @Transactional
    public Object callFunction(String funcName, Object... args) {
        return getSession().doReturningWork(
                c -> {
                    try (CallableStatement f = c.prepareCall(getFunctionCall(funcName, getParamString(args.length)))) {
                        f.registerOutParameter(1, Types.VARCHAR);
                        for (int a = 0; a < args.length; a++) {
                            f.setObject(a + 2, args[a]);
                        }
                        f.execute();
                        return f.getObject(1);
                    }
                }
        );
    }

    private String getFunctionCall(String funcName, String params) {
        return String.format("{ ? = call %s(%s) }", funcName, params);
    }
}
