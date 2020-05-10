package com.github.cimsbioko.server.service.impl;

import com.github.cimsbioko.server.dao.CampaignRepository;
import com.github.cimsbioko.server.domain.Campaign;
import com.github.cimsbioko.server.domain.Device;
import com.github.cimsbioko.server.domain.User;
import com.github.cimsbioko.server.scripting.JsConfig;
import com.github.cimsbioko.server.security.TokenAuthentication;
import com.github.cimsbioko.server.service.CampaignService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CampaignServiceImpl implements CampaignService, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(CampaignServiceImpl.class);

    private ApplicationContext ctx;

    private final CampaignRepository repo;
    private final File campaignsDir;
    private final ApplicationEventPublisher eventPublisher;
    private Map<String, JsConfig> loadedConfigs;

    public CampaignServiceImpl(CampaignRepository repo, File campaignsDir, ApplicationEventPublisher publisher) {
        this.repo = repo;
        this.campaignsDir = campaignsDir;
        eventPublisher = publisher;
        loadedConfigs = Collections.emptyMap();
    }

    @EventListener
    public void onStartup(ApplicationStartedEvent event) {
        loadInitialCampaigns();
    }

    private void loadInitialCampaigns() {

        Map<String, JsConfig> loaded = new LinkedHashMap<>();

        log.info("loading enabled campaign configs");
        Map<String, Campaign> enabledCampaigns = getEnabledCampaigns();
        for (Map.Entry<String, Campaign> campaignEntry : enabledCampaigns.entrySet()) {
            String campaignUuid = campaignEntry.getKey();
            Campaign campaign = campaignEntry.getValue();
            getCampaignFile(campaignUuid).ifPresent(file -> {
                try {
                    JsConfig config = new JsConfig(file, ctx).load();
                    loaded.put(campaignUuid, config);
                } catch (MalformedURLException | URISyntaxException e) {
                    log.warn("failed to load config for campaign " + campaign.getName() + " (" + campaignUuid + ")", e);
                }
            });
        }

        loadedConfigs = loaded;

        log.info("loading new campaigns");
        for (Map.Entry<String, JsConfig> newConfig : loadedConfigs.entrySet()) {
            String campaignName = enabledCampaigns.get(newConfig.getKey()).getName();
            eventPublisher.publishEvent(new CampaignLoaded(newConfig.getKey(), campaignName, newConfig.getValue()));
        }
    }

    @EventListener
    public void loadUploadedCampaign(CampaignUploaded event) {
        String uuid = event.getUuid();
        Optional<Campaign> optionalActiveCampaign = repo.findActiveByUuid(uuid);
        if (optionalActiveCampaign.isPresent()) {
            Campaign campaign = optionalActiveCampaign.get();
            try {
                log.info("pre-loading new config for campaign '{}' ({})", campaign.getName(), uuid);
                JsConfig config = new JsConfig(event.getFile(), ctx).load();

                Path campaignFilePath = getCampaignFilePath(uuid);
                log.info("pre-loading succeeded, installing to {}", campaignFilePath);
                Files.copy(event.getFile().toPath(), campaignFilePath, StandardCopyOption.REPLACE_EXISTING);

                if (loadedConfigs.containsKey(uuid)) {
                    log.info("unloading old config for campaign '{}'", uuid);
                    try (JsConfig oldConfig = loadedConfigs.get(uuid)) {
                        eventPublisher.publishEvent(new CampaignUnloaded(uuid, campaign.getName(), oldConfig));
                    }
                }

                loadedConfigs.put(uuid, config);

                log.info("loading new config for campaign '{}' ({})", campaign.getName(), uuid);
                eventPublisher.publishEvent(new CampaignLoaded(uuid, campaign.getName(), config));
            } catch (URISyntaxException | IOException e) {
                log.error("failed to load uploaded campaign", e);
            }
        } else {
            log.info("not loading campaign {} because it is inactive", uuid);
        }
    }

    private Map<String, Campaign> getEnabledCampaigns() {
        return repo.findActive().stream().collect(Collectors.toMap(Campaign::getUuid, Function.identity()));
    }

    @Override
    public void uploadCampaignFile(String uuid, MultipartFile file) throws IOException {
        Path target = Files.createTempFile("campaign_upload", null);
        file.transferTo(target);
        eventPublisher.publishEvent(new CampaignUploaded(uuid, target.toFile()));
    }

    @Override
    public Optional<File> getCampaignFile(String uuid) {
        File archive = getCampaignFilePath(uuid).toFile();
        return archive.canRead() ? Optional.of(archive) : Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMember(String uuid, Authentication auth) {

        if ("default".equals(uuid)) {
            uuid = repo.findDefault().map(Campaign::getUuid).orElse(uuid);
        }

        if (auth instanceof TokenAuthentication) {
            TokenAuthentication tokenAuth = (TokenAuthentication) auth;
            if (tokenAuth.isDevice()) {
                return repo.isDeviceActiveMember(uuid, tokenAuth.getName());
            }
        }
        return repo.isUserActiveMember(uuid, auth.getName());
    }

    @Override
    public List<Campaign> getMyCampaigns(Authentication auth) {
        if (auth instanceof TokenAuthentication) {
            TokenAuthentication tokenAuth = (TokenAuthentication) auth;
            if (tokenAuth.isDevice()) {
                return repo.findActiveForDevice(tokenAuth.getName());
            }
        }
        return repo.findActiveForUser(auth.getName());
    }

    private Path getCampaignFilePath(String uuid) {
        String fileName = Optional.ofNullable(uuid).map(n -> n + ".zip").orElse("default.zip");
        return campaignsDir.toPath().resolve(Paths.get(fileName));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext;
    }
}

interface CampaignEvent {
}

class CampaignUploaded implements CampaignEvent {

    private final String uuid;
    private final File file;

    CampaignUploaded(String uuid, File file) {
        this.uuid = uuid;
        this.file = file;
    }

    public String getUuid() {
        return uuid;
    }

    public File getFile() {
        return file;
    }
}

class CampaignLoaded implements CampaignEvent {

    private final String uuid;
    private final String name;
    private final JsConfig config;

    CampaignLoaded(String uuid, String name, JsConfig config) {
        this.uuid = uuid;
        this.name = name;
        this.config = config;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public JsConfig getConfig() {
        return config;
    }
}

class CampaignUnloaded implements CampaignEvent {

    private final String uuid;
    private final String name;
    private final JsConfig config;

    CampaignUnloaded(String uuid, String name, JsConfig config) {
        this.uuid = uuid;
        this.name = name;
        this.config = config;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public JsConfig getConfig() {
        return config;
    }
}