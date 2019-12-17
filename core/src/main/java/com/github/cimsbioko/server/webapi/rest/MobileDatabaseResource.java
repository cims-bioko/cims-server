package com.github.cimsbioko.server.webapi.rest;

import com.github.batkinson.jrsync.Metadata;
import com.github.cimsbioko.server.dao.CampaignRepository;
import com.github.cimsbioko.server.domain.Campaign;
import com.github.cimsbioko.server.service.SyncService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.context.request.WebRequest;

import java.io.File;
import java.util.Optional;

import static com.github.cimsbioko.server.config.WebConfig.CACHED_FILES_PATH;


/**
 * Endpoint for tablet database synchronization. It uses content negotiation and awareness of sync metadata to help
 * tablets optimize bandwidth consumption:
 * <p>
 * <li>skip syncing when its local content is identical</li>
 * <li>use zsync to efficiently synchronize with existing content</li>
 * <li>perform a full-download if zsync is not possible</li>
 */
@Controller
public class MobileDatabaseResource {

    private static final String ACCEPT = "Accept";
    static final String SQLITE_MIME_TYPE = "application/x-sqlite3";

    private final SyncService service;
    private final CampaignRepository campaignRepo;

    public MobileDatabaseResource(SyncService service, CampaignRepository campaignRepo) {
        this.service = service;
        this.campaignRepo = campaignRepo;
    }

    @GetMapping(value = "/api/rest/mobiledb", produces = {SQLITE_MIME_TYPE, Metadata.MIME_TYPE})
    @PreAuthorize("hasAuthority('MOBILE_SYNC') and @campaignService.isMember('default', #auth)")
    public String mobileDB(WebRequest request, Authentication auth) {
        return mobileDb(request, "default");
    }

    @GetMapping(value = "/api/rest/mobiledb/{name}", produces = {SQLITE_MIME_TYPE, Metadata.MIME_TYPE})
    @PreAuthorize("hasAuthority('MOBILE_SYNC') and @campaignService.isMember(#name, #auth)")
    public String mobileDB(WebRequest request, @PathVariable String name, Authentication auth) {
        return mobileDb(request, name);
    }

    private String mobileDb(WebRequest request, String uuid) {
        if ("default".equals(uuid)) {
            uuid = campaignRepo.findDefault().map(Campaign::getUuid).orElse(uuid);
        }
        File cacheFile = service.getOutput(uuid);
        File metadataFile = new File(cacheFile.getParentFile(), cacheFile.getName() + "." + Metadata.FILE_EXT);
        String accept = request.getHeader(ACCEPT);

        if (request.checkNotModified(service.getTask(uuid).getDescriptor())) {
            return null;
        }

        if (accept != null && accept.contains(Metadata.MIME_TYPE) && metadataFile.exists()) {
            return "forward:" + CACHED_FILES_PATH + "/" + metadataFile.getName();
        }
        return "forward:" + CACHED_FILES_PATH + "/" + cacheFile.getName();
    }
}
