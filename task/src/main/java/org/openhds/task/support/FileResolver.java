package org.openhds.task.support;

import java.io.File;

public interface FileResolver {

    File resolveIndividualXmlFile();

    File resolveLocationXmlFile();

    String resolveLocationXmlFilename();

    File resolveRelationshipXmlFile();

    File resolvesocialGroupXmlFile();

    File resolveResidencyXmlFile();

    File resolveMembershipXmlFile();

    File resolveVisitXmlFile();

}
