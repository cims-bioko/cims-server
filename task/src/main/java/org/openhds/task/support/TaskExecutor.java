package org.openhds.task.support;

public interface TaskExecutor {

    void executeIndividualTask();

    void executeLocationTask();

    void executeRelationshipTask();

    void executeSocialGroupTask();

    void executeMembershipTask();

    void executeVisitTask(int roundNumber);

    void executeFieldWorkerTask();

    void executeLocationHierarchyTask();

    void executeMobileDBTask();

}
