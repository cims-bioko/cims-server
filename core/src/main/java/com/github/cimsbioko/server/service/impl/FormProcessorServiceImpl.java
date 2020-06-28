package com.github.cimsbioko.server.service.impl;

import com.github.cimsbioko.server.dao.CampaignRepository;
import com.github.cimsbioko.server.domain.Campaign;
import com.github.cimsbioko.server.domain.FormSubmission;
import com.github.cimsbioko.server.scripting.FormProcessor;
import com.github.cimsbioko.server.scripting.JsConfig;
import com.github.cimsbioko.server.service.FormProcessorService;
import com.github.cimsbioko.server.service.impl.campaign.CampaignLoadedEvent;
import com.github.cimsbioko.server.service.impl.campaign.CampaignUnloadedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

public class FormProcessorServiceImpl implements FormProcessorService {

    private static final Logger log = LoggerFactory.getLogger(FormProcessorServiceImpl.class);

    private final Map<String, Map<String, FormProcessor>> campaignProcessors = new HashMap<>();
    private final CampaignRepository campaignRepo;

    public FormProcessorServiceImpl(CampaignRepository campaignRepo) {
        this.campaignRepo = campaignRepo;
    }

    @EventListener
    public void onCampaignLoaded(CampaignLoadedEvent event) {
        JsConfig config = event.getConfig();
        Optional.ofNullable(config.getFormProcessors()).ifPresent(fps -> {
            campaignProcessors.put(event.getUuid(), fps);
            log.info("added {} form processors for campaign '{}' ({})", fps.size(), event.getName(), event.getUuid());
        });
    }

    @EventListener
    public void onCampaignUnloaded(CampaignUnloadedEvent event) {
        if (campaignProcessors.containsKey(event.getUuid())) {
            campaignProcessors.remove(event.getUuid());
            log.info("removed form processors for campaign '{}' ({})", event.getName(), event.getUuid());
        }
    }

    @Override
    public List<String> getBindings(String campaignUuid) {
        List<String> bindings = new ArrayList<>();
        if (campaignProcessors.containsKey(campaignUuid)) {
            bindings.addAll(campaignProcessors.get(campaignUuid).keySet());
            Collections.sort(bindings);
        }
        return bindings;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class})
    public void process(FormSubmission submission) {
        Optional.ofNullable(submission.getCampaignId())
                .flatMap(campaignRepo::findActiveByUuid)
                .map(Campaign::getUuid)
                .map(campaignProcessors::get)
                .map(processorMap -> processorMap.get(submission.getFormBinding()))
                .orElse(new DefaultProcessor())
                .process(submission);
    }

    static class DefaultProcessor implements FormProcessor {
        @Override
        public void process(FormSubmission submission) {
            log.info("ignoring submission {}: no processor for binding '{}'", submission.getInstanceId(), submission.getFormBinding());
        }
    }
}