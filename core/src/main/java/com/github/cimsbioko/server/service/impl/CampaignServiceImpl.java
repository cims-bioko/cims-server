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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
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
        loadEnabledCampaigns();
    }

    private void loadEnabledCampaigns() {

        Map<String, JsConfig> newlyLoaded = new LinkedHashMap<>();

        log.info("pre-loading enabled campaign definitions");
        for (String name : getEnabledCampaignNames()) {
            getCampaignFile(name).ifPresent(file -> {
                try {
                    JsConfig config = new JsConfig(file, ctx).load();
                    newlyLoaded.put(name, config);
                } catch (MalformedURLException | URISyntaxException e) {
                    log.warn("failed to load config for campaign " + name, e);
                }
            });
        }

        log.info("unloading old campaigns");
        for (Map.Entry<String, JsConfig> existing : loadedConfigs.entrySet()) {
            eventPublisher.publishEvent(new CampaignUnloaded(existing.getKey(), existing.getValue()));
        }

        loadedConfigs = newlyLoaded;

        log.info("loading new campaigns");
        for (Map.Entry<String, JsConfig> newConfig : loadedConfigs.entrySet()) {
            eventPublisher.publishEvent(new CampaignLoaded(newConfig.getKey(), newConfig.getValue()));
        }
    }

    @EventListener
    public void loadUploadedCampaign(CampaignUploaded event) {
        String name = event.getName();
        if (repo.findActiveByName(name).isPresent()) {
            try {
                log.info("pre-loading new definition for campaign '{}'", name);
                JsConfig config = new JsConfig(event.getFile(), ctx).load();

                if (loadedConfigs.containsKey(name)) {
                    log.info("unloading old definition for campaign '{}'", name);
                    eventPublisher.publishEvent(new CampaignUnloaded(name, loadedConfigs.get(name)));
                }

                loadedConfigs.put(name, config);

                log.info("loading new definition for campaign '{}'", name);
                eventPublisher.publishEvent(new CampaignLoaded(name, config));
            } catch (URISyntaxException | MalformedURLException e) {
                log.error("failed to load uploaded campaign", e);
            }
        } else {
            log.info("not loading campaign {} because it is inactive", name);
        }
    }

    private Iterable<String> getEnabledCampaignNames() {
        return repo.findActive().stream().map(Campaign::getName).collect(Collectors.toList());
    }

    @Override
    public void uploadCampaignFile(String name, MultipartFile file) throws IOException {
        Path target = getCampaignFilePath(name);
        file.transferTo(target);
        eventPublisher.publishEvent(new CampaignUploaded(name, target.toFile()));
    }

    @Override
    public Optional<File> getCampaignFile(String name) {
        File archive = getCampaignFilePath(name).toFile();
        return archive.canRead() ? Optional.of(archive) : Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMember(String campaign, Authentication auth) {
        // FIXME: query directly for membership rather than fetching all objects
        if (auth instanceof TokenAuthentication) {
            TokenAuthentication tokenAuth = (TokenAuthentication) auth;
            if (tokenAuth.isDevice()) {
                return repo.findActiveByName(campaign)
                        .map(Campaign::getDevices)
                        .map(devices -> devices.stream().map(Device::getName).collect(Collectors.toSet()))
                        .map(deviceNames -> deviceNames.contains(tokenAuth.getName())).orElse(false);
            }
        }
        return repo.findActiveByName(campaign)
                .map(Campaign::getUsers)
                .map(users -> users.stream().map(User::getUsername).collect(Collectors.toSet()))
                .map(userNames -> userNames.contains(auth.getName())).orElse(false);
    }

    private Path getCampaignFilePath(String name) {
        String fileName = Optional.ofNullable(name).map(n -> n + ".zip").orElse("default.zip");
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

    private final String name;
    private final File file;

    CampaignUploaded(String name, File file) {
        this.name = name;
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }
}

class CampaignLoaded implements CampaignEvent {

    private final String name;
    private final JsConfig config;

    CampaignLoaded(String name, JsConfig config) {
        this.name = name;
        this.config = config;
    }

    public String getName() {
        return name;
    }

    public JsConfig getConfig() {
        return config;
    }
}

class CampaignUnloaded implements CampaignEvent {

    private final String name;
    private final JsConfig config;

    CampaignUnloaded(String name, JsConfig config) {
        this.name = name;
        this.config = config;
    }

    public String getName() {
        return name;
    }

    public JsConfig getConfig() {
        return config;
    }
}