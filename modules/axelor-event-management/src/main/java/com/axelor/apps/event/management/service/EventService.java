package com.axelor.apps.event.management.service;

import java.time.LocalDate;
import java.util.List;

import com.axelor.apps.event.management.db.Event;
import com.axelor.apps.event.management.db.EventRegistration;
import com.axelor.apps.message.db.Message;
import com.axelor.meta.db.MetaFile;

public interface EventService {
	public Event calculateTotal(Event event, List<EventRegistration> eventRegistrations);

	public List<EventRegistration> removeLastLine(List<EventRegistration> eventRegistrations);

	public List<EventRegistration> removeLastLineByValidations(Event event);

	public long countDays(LocalDate startDate, LocalDate endDate);

	public List<EventRegistration> getCalculatedEventRegistrationList(Event event);

	public Message setUpMessage(Event event);

	public boolean areAllMailSent(List<EventRegistration> eventRegistrations);

	public void importDataFromCsvFile(MetaFile metaFile, Long eventId);
}
