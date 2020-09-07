package com.github.cimsbioko.server.webapi.odk;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class RepeatExtractorImpl implements RepeatExtractor {

    private static final Namespace XFORMS_NS = Namespace.getNamespace("xforms", "http://www.w3.org/2002/xforms");
    private final XPathExpression<Attribute> repeatExpression;

    public RepeatExtractorImpl() {
        XPathFactory xPathFactory = XPathFactory.instance();
        repeatExpression = xPathFactory.compile("//xforms:repeat/@nodeset", Filters.attribute(), null, XFORMS_NS);
    }

    @Override
    public List<String[]> extractRepeats(Document formDoc) {
        return repeatExpression.evaluate(formDoc)
                .stream()
                .map(Attribute::getValue)
                .filter(path -> path.startsWith("/"))
                .map(path -> path.substring(1).split("/"))
                .collect(toList());
    }
}
