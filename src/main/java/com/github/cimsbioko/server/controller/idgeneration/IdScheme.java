package com.github.cimsbioko.server.controller.idgeneration;

import java.util.Map;

public class IdScheme implements Comparable<IdScheme> {

    String name;
    String prefix;
    Map<String, Integer> fields;
    boolean checkDigit;
    int incrementBound;
    int length;

    public IdScheme() {
    }

    public IdScheme(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Map<String, Integer> getFields() {
        return fields;
    }

    public void setFields(Map<String, Integer> fields) {
        this.fields = fields;
    }

    public boolean isCheckDigit() {
        return checkDigit;
    }

    public void setCheckDigit(boolean checkDigit) {
        this.checkDigit = checkDigit;
    }

    public int getIncrementBound() {
        return incrementBound;
    }

    public void setIncrementBound(int incrementBound) {
        this.incrementBound = incrementBound;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int compareTo(IdScheme o) {
        return name.compareTo(o.name);
    }
}
