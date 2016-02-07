package org.openhds.task.support;

import java.io.File;

public interface FileResolver {

    File resolveIndividualFile();

    File resolveLocationFile();

    File resolveRelationshipFile();

    File resolveSocialGroupFile();

    File resolveMembershipFile();

    File resolveVisitFile();

    File resolveFieldWorkerFile();

    File resolveLocationHierarchyFile();

    File getFileForTask(String taskName);
}
