package com.github.cimsbioko.server.service.impl;

import com.github.cimsbioko.server.dao.CampaignRepository;
import com.github.cimsbioko.server.domain.Campaign;
import com.github.cimsbioko.server.domain.FormSubmission;
import com.github.cimsbioko.server.scripting.FormProcessor;
import com.github.cimsbioko.server.scripting.JsConfig;
import com.github.cimsbioko.server.service.FormProcessorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FormProcessorServiceImpl implements FormProcessorService {

    private static final Logger log = LoggerFactory.getLogger(FormProcessorServiceImpl.class);

    private final Map<String, Map<String, FormProcessor>> campaignProcessors = new HashMap<>();
    private final CampaignRepository campaignRepo;

    public FormProcessorServiceImpl(CampaignRepository campaignRepo) {
        this.campaignRepo = campaignRepo;
    }

    @EventListener
    public void onCampaignLoaded(CampaignLoaded event) {
        JsConfig config = event.getConfig();
        Optional.ofNullable(config.getFormProcessors()).ifPresent(fps -> {
            campaignProcessors.put(event.getUuid(), fps);
            log.info("added {} form processors for campaign '{}'", fps.size(), event.getUuid());
        });
    }

    @EventListener
    public void onCampaignUnloaded(CampaignUnloaded event) {
        if (campaignProcessors.containsKey(event.getUuid())) {
            campaignProcessors.remove(event.getUuid());
            log.info("removed form processors for campaign '{}'", event.getUuid());
        }
    }

    @Override
    @Transactional
    public void process(FormSubmission submission) {
        String campaign = campaignRepo.findDefault()
                .map(Campaign::getUuid)
                .orElseThrow(() -> new RuntimeException("no default campaign found"));
        FormProcessor processor = Optional.ofNullable(campaignProcessors.get(campaign))
                .map(pm -> pm.get(submission.getFormBinding()))
                .orElse((fs) -> { log.info("ignoring submission {}: no processor for binding '{}'",
                                           submission.getInstanceId(), submission.getFormBinding()); });
        processor.process(submission);
    }
}
