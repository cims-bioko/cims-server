package com.github.cimsbioko.server.service;

public interface StoredProcService {
    void callProcedure(String procName, Object... args);
    Object callFunction(String funcName, Object... args);
}
