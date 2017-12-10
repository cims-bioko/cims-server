package com.github.cimsbioko.server.controller.service.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import com.github.cimsbioko.server.controller.service.EntityService;
import com.github.cimsbioko.server.controller.service.SocialGroupService;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.controller.idgeneration.SocialGroupGenerator;
import com.github.cimsbioko.server.domain.model.Individual;
import com.github.cimsbioko.server.domain.model.Membership;
import com.github.cimsbioko.server.domain.model.SocialGroup;
import org.springframework.transaction.annotation.Transactional;

public class SocialGroupServiceImpl implements SocialGroupService {

    private EntityService service;
    private SocialGroupGenerator generator;

    public SocialGroupServiceImpl(EntityService service, SocialGroupGenerator generator) {
        this.service = service;
        this.generator = generator;
    }

    public SocialGroup generateId(SocialGroup entityItem) throws ConstraintViolations {
        entityItem.setExtId(generator.generateId(entityItem));
        return entityItem;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteSocialGroup(SocialGroup group) throws SQLException {

        if (group.getMemberships() != null) {

            Set<Membership> mems = group.getMemberships();
            for (Membership item : mems)
                service.delete(item);
        }
        service.delete(group);
    }

    @Transactional(rollbackFor = Exception.class)
    public void modifySocialGroupHead(SocialGroup group, Individual selectedSuccessor,
                                      List<Membership> memberships) throws Exception {

        group.setHead(selectedSuccessor);

        // Remove all Memberships from the Social Group
        Set<Membership> mems = group.getMemberships();

        for (Membership item : mems) {
            item.setDeleted(true);
            service.save(item);
        }

        // Create new Memberships
        for (Membership item : memberships)
            service.create(item);

        service.save(group);
    }
}
