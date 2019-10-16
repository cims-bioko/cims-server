package com.github.cimsbioko.server.service.impl;

import com.github.cimsbioko.server.scripting.JsConfig;
import com.github.cimsbioko.server.service.CampaignService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CampaignServiceImpl implements CampaignService {

    private static final Logger log = LoggerFactory.getLogger(CampaignServiceImpl.class);

    private final File campaignsDir;
    private final ApplicationEventPublisher eventPublisher;
    private Map<String, JsConfig> loadedConfigs;

    public CampaignServiceImpl(File campaignsDir, ApplicationEventPublisher publisher) {
        this.campaignsDir = campaignsDir;
        eventPublisher = publisher;
        loadedConfigs = Collections.emptyMap();
    }

    @EventListener
    public void onStartup(ApplicationStartedEvent event) {
        loadEnabledCampaigns();
    }

    public void loadEnabledCampaigns() {

        Map<String, JsConfig> newlyLoaded = new LinkedHashMap<>();

        // pre-load enabled campaign definitions
        for (String name : getEnabledCampaigns()) {
            File campaignFile = getCampaignFile(name).orElseThrow(() -> new IllegalStateException(
                    String.format("no campaign file for '%s'", name)));
            try {
                JsConfig config = new JsConfig(campaignFile).load();
                newlyLoaded.put(name, config);
            } catch (MalformedURLException | URISyntaxException e) {
                log.warn("failed to (re)load config for campaign " + name, e);
            }
        }

        // send unload events for all loaded campaigns
        for (Map.Entry<String, JsConfig> existing : loadedConfigs.entrySet()) {
            eventPublisher.publishEvent(new CampaignUnloaded(existing.getKey(), existing.getValue()));
        }

        // install the newly loaded configs
        loadedConfigs = newlyLoaded;

        // send load events for newly loaded configs
        for (Map.Entry<String, JsConfig> newConfig : loadedConfigs.entrySet()) {
            eventPublisher.publishEvent(new CampaignLoaded(newConfig.getKey(), newConfig.getValue()));
        }
    }

    @EventListener
    public void loadUploadedCampaign(CampaignUploaded event) {
        String name = event.getName();
        try {
            // pre-load new definition so a failing update leaves running intact
            JsConfig config = new JsConfig(event.getFile()).load();
            // if config loads, notify components that existing config is unloading
            if (loadedConfigs.containsKey(name)) {
                eventPublisher.publishEvent(new CampaignUnloaded(name, loadedConfigs.get(name)));
            }
            // install the new config
            loadedConfigs.put(name, config);
            // notify components that the new campaign config is loaded
            eventPublisher.publishEvent(new CampaignLoaded(name, config));
        } catch (URISyntaxException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Just a stub for data-managed campaign management. Currently, default is always enabled.
     */
    private Iterable<String> getEnabledCampaigns() {
        return Arrays.asList(new String[]{null});
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

    private Path getCampaignFilePath(String name) {
        String fileName = Optional.ofNullable(name).map(n -> n + ".zip").orElse("default.zip");
        return campaignsDir.toPath().resolve(Paths.get(fileName));
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