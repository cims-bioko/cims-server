package org.openhds.domain.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.openhds.domain.service.ValueConstraintService;
import org.springframework.core.io.ClassPathResource;

public class ValueConstraintServiceImpl implements ValueConstraintService {

    private Document doc;

    public ValueConstraintServiceImpl() {
        SAXBuilder builder = new SAXBuilder();
        try {
            ClassPathResource res = new ClassPathResource("value-constraint.xml");
            doc = builder.build(res.getInputStream());
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConstraintDefined(String constraintName) {
        Element ele = findConstraintByName(constraintName);
        return ele != null;
    }

    public Element findConstraintByName(String constraintName) {
        for (Element ele : doc.getRootElement().getChildren()) {
            if (ele.getAttribute("id").getValue().equals(constraintName))
                return ele;
        }
        return null;
    }

    public boolean isValidConstraintValue(String constraintName, Object value) {
        // Get children of the root element whose "id" attribute is constraintName...
        for (Element elt : doc.getRootElement().getChildren()) {
            if (elt.getAttribute("id").getValue().equals(constraintName)) {
                // ...and return true if the value of some child matches the input
                for (Element o2 : elt.getChildren()) {
                    if (o2.getValue().toLowerCase().equals(value.toString().toLowerCase()))
                        return true;
                }
                return false;
            }
        }
        return false;
    }

    public List<String> getAllConstraintNames() {
        List<String> output = new ArrayList<>();
        for (Element ele : doc.getRootElement().getChildren()) {
            output.add(ele.getAttribute("id").getValue());
        }
        return output;
    }

    public Map<String, String> getMapForConstraint(String constraintName) {
        Map<String, String> keyValues = new TreeMap<>();
        Element constraint = findConstraintByName(constraintName);
        for (Element child : constraint.getChildren()) {
            keyValues.put(child.getValue(), child.getAttributeValue("description"));
        }
        return keyValues;
    }
}
