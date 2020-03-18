package com.axelor.csv.script;

import java.util.Map;

import com.axelor.apps.event.management.db.Event;
import com.axelor.apps.event.management.db.EventRegistration;
import com.axelor.apps.event.management.db.repo.EventRepository;
import com.axelor.apps.event.management.service.EventRegistrationService;
import com.axelor.apps.event.management.service.EventService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class ImportEventRegistration {

	@Inject
	EventRegistrationService eventRegistrationService;

	private static int counter;

	public static boolean isCounterInitiated = false;

	@Inject
	EventService eventService;

	public Object importEventRegistrationData(Object bean, Map<String, Object> values) {
		assert bean instanceof EventRegistration;

		try {
			EventRegistration eventRegistration = (EventRegistration) bean;
			Event event = (Beans.get(EventRepository.class).find(Long.parseLong(values.get("event").toString())));
			int totalBeforeAddition = event.getEventRegistrationList().size();
			int capacity = event.getCapacity();
			if ((!isCounterInitiated) && (capacity > totalBeforeAddition)) {
				counter = totalBeforeAddition;
				isCounterInitiated = true;
			}

			if (capacity <= totalBeforeAddition) {
				return null;
			} else {
				if (counter < capacity
						&& (event.getRegistrationOpenDate()
								.isBefore(eventRegistration.getRegistrationDateT().toLocalDate())
								|| event.getRegistrationOpenDate()
										.isEqual(eventRegistration.getRegistrationDateT().toLocalDate()))
						&& (event.getRegistrationCloseDate()
								.isAfter(eventRegistration.getRegistrationDateT().toLocalDate())
								|| event.getRegistrationCloseDate()
										.isEqual(eventRegistration.getRegistrationDateT().toLocalDate()))) {
					eventRegistration.setEvent(event);
					eventRegistration.setAmount(eventRegistrationService.calculateAmount(eventRegistration, event));
					eventRegistrationService.saveEventTotalCalculations(event, eventRegistration);
					counter++;
					return eventRegistration;
				} else {
					return null;
				}

			}
		} catch (Exception e) {
		}
		return null;
	}
}
