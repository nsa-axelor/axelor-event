package com.axelor.csv.script;

import java.util.Map;

import com.axelor.apps.event.management.db.Event;
import com.axelor.apps.event.management.db.EventRegistration;
import com.axelor.apps.event.management.db.repo.EventRepository;
import com.axelor.apps.event.management.service.EventRegistrationService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class ImportEventRegistration {

	@Inject
	EventRegistrationService eventRegistrationService;

	private static int counter;

	private static boolean isCounterInitiated = false;

	public Object importEventRegistrationData(Object bean, Map<String, Object> values) {
		assert bean instanceof EventRegistration;

		EventRegistration eventRegistration = (EventRegistration) bean;
		Event event = (Beans.get(EventRepository.class).find(Long.parseLong(values.get("event").toString())));
		System.out.println(event.getEventRegistrationList().size());
		int totalBeforeAddition = event.getEventRegistrationList().size();
		int capacity = event.getCapacity();

		if (!isCounterInitiated && capacity > totalBeforeAddition) {
			counter = totalBeforeAddition;
			isCounterInitiated = true;
		}

		if (capacity <= totalBeforeAddition) {
			return null;
		} else {
			if (counter < capacity) {
				eventRegistration.setEvent(event);
				eventRegistration.setAmount(eventRegistrationService.calculateAmount(eventRegistration));
				eventRegistrationService.saveEventRegistration(eventRegistration);
				eventRegistrationService.saveEventTotalCalculations(event, eventRegistration);
				counter++;
				return eventRegistration;
			} else {
				return null;
			}

		}
	}
}
