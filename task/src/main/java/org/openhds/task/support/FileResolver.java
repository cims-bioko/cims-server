package org.openhds.task.support;

import org.openhds.domain.annotations.Authorized;
import org.openhds.domain.model.PrivilegeConstants;

import java.io.File;

public interface FileResolver {

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    File resolveIndividualXmlFile();

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    File resolveLocationXmlFile();

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    File resolveRelationshipXmlFile();

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    File resolvesocialGroupXmlFile();

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    File resolveResidencyXmlFile();

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    File resolveMembershipXmlFile();

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    File resolveVisitXmlFile();

}
