package com.github.cimsbioko.server.domain.constraint;

import java.util.Calendar;

public interface GenericStartEndDateConstraint {

    Calendar getStartDate();

    Calendar getEndDate();
}