package com.axelor.apps.event.management.service;

import com.axelor.apps.event.management.db.Event;
import com.axelor.apps.event.management.db.EventRegistration;
import java.math.BigDecimal;

public interface EventRegistrationService {
	public BigDecimal calculateAmount(EventRegistration registration);

	public void saveEventTotalCalculations(Event event, EventRegistration registration);
}
