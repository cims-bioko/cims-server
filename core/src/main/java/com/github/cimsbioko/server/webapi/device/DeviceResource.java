package com.github.cimsbioko.server.webapi.device;

import com.github.cimsbioko.server.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import static com.github.cimsbioko.server.webapi.device.DeviceResource.DEVICE_API_PATH;

@RestController
@RequestMapping(DEVICE_API_PATH)
public class DeviceResource {

    static final String DEVICE_API_PATH = "/api/device";

    private final DeviceService deviceService;

    @Autowired
    public DeviceResource(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping("/token")
    @PreAuthorize("hasAuthority('CREATE_DEVICE_TOKEN')")
    public Map<String, String> createToken(Principal principal) {
        Map<String, String> result = new HashMap<>();
        result.put("access_token", deviceService.generateToken(principal.getName()));
        return result;
    }

    @PostMapping("/registration")
    @PreAuthorize("hasAuthority('REGISTER_DEVICE')")
    public Map<String, String> completeRegistration(Principal principal, @RequestBody DeviceRegistrationRequest request) {
        return deviceService.completeRegistration(principal.getName(), request.getDescription());
    }
}

class DeviceRegistrationRequest {

    private String description;

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
