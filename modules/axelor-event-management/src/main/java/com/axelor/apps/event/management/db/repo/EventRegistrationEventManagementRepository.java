package com.axelor.apps.event.management.db.repo;

import java.util.List;

import com.axelor.apps.event.management.db.Event;
import com.axelor.apps.event.management.db.EventRegistration;
import com.axelor.apps.event.management.service.EventService;
import com.axelor.inject.Beans;

public class EventRegistrationEventManagementRepository extends EventRegistrationRepository{
	
	@Override
	public void remove(EventRegistration entity) {
		EventRepository eventRepository = Beans.get(EventRepository.class);
		EventService eventService = Beans.get(EventService.class);
		Long entityId = entity.getId();
		super.remove(entity);
		Event event = eventRepository.find(entity.getEvent().getId());
		List<EventRegistration> eventRegistrations = event.getEventRegistrationList();
		eventRegistrations.removeIf(eventRegistration -> eventRegistration.getId() == entityId);
		event = eventService.calculateTotal(event, event.getEventRegistrationList());
		eventRepository.save(event);
	}
	
	@Override
	public EventRegistration save(EventRegistration entity) {
		EventRepository eventRepository = Beans.get(EventRepository.class);
		EventService eventService = Beans.get(EventService.class);
		entity = super.save(entity);
		Event event = eventRepository.find(entity.getEvent().getId());
		event = eventService.calculateTotal(event, event.getEventRegistrationList());
		return entity;
	}

}
