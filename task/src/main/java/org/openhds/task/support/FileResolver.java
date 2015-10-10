package org.openhds.task.support;

import java.io.File;

public interface FileResolver {

    File resolveIndividualXmlFile();

    File resolveLocationXmlFile();

    File resolveRelationshipXmlFile();

    File resolveSocialGroupXmlFile();

    File resolveResidencyXmlFile();

    File resolveMembershipXmlFile();

    File resolveVisitXmlFile();

    File getFileForTask(String taskName);
}
