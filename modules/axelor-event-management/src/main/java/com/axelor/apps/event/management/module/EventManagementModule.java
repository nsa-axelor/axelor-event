package com.axelor.apps.event.management.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.event.management.service.EventRegistrationService;
import com.axelor.apps.event.management.service.EventRegistrationServiceImpl;
import com.axelor.apps.event.management.service.EventService;
import com.axelor.apps.event.management.service.EventServiceImpl;

public class EventManagementModule extends AxelorModule {
	@Override
	protected void configure() {
		bind(EventService.class).to(EventServiceImpl.class);
		bind(EventRegistrationService.class).to(EventRegistrationServiceImpl.class);
	}
}
