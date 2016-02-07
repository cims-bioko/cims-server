package org.openhds.controller.idgeneration;

import org.openhds.controller.util.OpenHDSResult;
import org.openhds.domain.service.SitePropertiesService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;

public class IdValidator {
	
	private static final String BAD_ID_FORMAT = "Bad Id Format: An Id can only contain alphanumeric characters";
    private static final String INVALID_CHECKCHAR_LOCATION = "Check character doesn't match the provided Location id";
    private static final String INVALID_CHECKCHAR_VISIT = "Check character doesn't match the provided Visit id";
    private static final String INVALID_CHECKCHAR_SOCIALGROUP = "Check character doesn't match the provided Social Group id";
    private static final String INVALID_CHECKCHAR_INDIVIDUAL = "Check character doesn't match the provided Individual id";
    private static final String INVALID_CHECKCHAR_FIELDWORKER = "Check character doesn't match the provided Field Worker id";

    @Autowired
    private LuhnValidator luhnValidator;
    @Autowired
    private SitePropertiesService siteProperties;
    @Autowired
    private IdSchemeResource resource;
    
    public IdValidator(LuhnValidator luhnValidator, SitePropertiesService siteProperties, IdSchemeResource resource) {
    	this.luhnValidator = luhnValidator;
    	this.siteProperties = siteProperties;
    	this.resource = resource;
    }

	public OpenHDSResult evaluateCheckDigits(HashMap<String, List<String>> map) {
		OpenHDSResult result = new OpenHDSResult();
		try {
			result.setSuccess(true);
			for (String item : map.keySet()) {
				if (checkIdSchemeForCheckDigit(item) && !validateId(map.get(item))) {
					result.setSuccess(false);
					switch (item) {
						case "Location":
							result.setFailureReason(INVALID_CHECKCHAR_LOCATION);
							break;
						case "FieldWorker":
							result.setFailureReason(INVALID_CHECKCHAR_FIELDWORKER);
							break;
						case "Visit":
							result.setFailureReason(INVALID_CHECKCHAR_VISIT);
							break;
						case "SocialGroup":
							result.setFailureReason(INVALID_CHECKCHAR_SOCIALGROUP);
							break;
						case "Individual":
							result.setFailureReason(INVALID_CHECKCHAR_INDIVIDUAL);
							break;
					}
				}
			}
		} catch (Exception e) {
			result.setSuccess(false);
			result.setFailureReason(BAD_ID_FORMAT);
		}
		return result;
	}

	public boolean validateId(List<String> ids) {
    	for (String item : ids) {
    		
    		if (item.equals(siteProperties.getUnknownIdentifier()))
    			continue;
    		
    		if (!luhnValidator.validateCheckCharacter(item))
        		return false;
    	}	
    	return true;
    }
    
    private boolean checkIdSchemeForCheckDigit(String name) {
    	return resource.getIdSchemeByName(name).isCheckDigit();
    }
}
