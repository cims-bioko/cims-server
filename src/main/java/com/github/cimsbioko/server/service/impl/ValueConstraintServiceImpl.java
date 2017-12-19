package com.github.cimsbioko.server.service.impl;

import java.io.IOException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import com.github.cimsbioko.server.service.ValueConstraintService;
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
}
