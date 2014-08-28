package org.openhds.controller.exception;

import java.util.ArrayList;
import java.util.List;

public class ConstraintViolations extends Exception {

    public static final String INVALID_FIELD_WORKER_EXT_ID = "Invalid Field Worker Ext Id";
    public static final String INVALID_INDIVIDUAL_EXT_ID = "Invalid Individual Ext Id";
    public static final String INVALID_VISIT_EXT_ID = "Invalid Visit Ext Id";
    public static final String INVALID_LOCATION_EXT_ID = "Invalid Location Ext Id";


    private static final long serialVersionUID = 4392790814928552607L;
	private List<String> violations = new ArrayList<String>();
	
	public ConstraintViolations() {}
	
	public ConstraintViolations(String msg, List<String> violations) {
		super(msg);
		this.violations = violations;
	}
	
	public ConstraintViolations(String msg) {
		super(msg);
		violations.add(msg);
	}
	
	public List<String> getViolations() {
		return violations;
	}

	public void addViolations(String invalidFieldWorkerId) {
		violations.add(invalidFieldWorkerId);
	}

	public boolean hasViolations() {
		return violations != null && violations.size() > 0;
	}
}

