package com.axelor.apps.event.management.web;

import com.axelor.apps.event.management.db.Discount;
import com.axelor.apps.event.management.db.Event;
import com.axelor.apps.event.management.db.EventRegistration;
import com.axelor.apps.event.management.db.repo.DiscountRepository;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.time.LocalDate;
import java.util.List;

public class EventController {

	public void setEventRegistrations(ActionRequest request, ActionResponse response) {
		Event event = request.getContext().asType(Event.class);
		List<EventRegistration> eventRegistrations = event.getEventRegistrationList();
		try {
			if (event.getCapacity() < eventRegistrations.size()
					|| event.getRegistrationOpenDate().isAfter(LocalDate.now())
					|| event.getRegistrationCloseDate().isBefore(LocalDate.now())
					|| event.getRegistrationCloseDate() == null || event.getRegistrationOpenDate() == null) {
				eventRegistrations.remove(eventRegistrations.size() - 1);
				response.setValue("eventRegistrationList", eventRegistrations);
			}
		} catch (Exception e) {
			eventRegistrations.remove(eventRegistrations.size() - 1);
			response.setValue("eventRegistrationList", eventRegistrations);
		}
	}
	
	public void applyDiscount(ActionRequest request, ActionResponse response) {
		Event event = request.getContext().asType(Event.class);
		List<Discount> discountList = Beans.get(DiscountRepository.class).all().filter("self.event = :event").bind("event",event).order("-discountPercent").fetch();
		Discount discount = discountList.get(0); //TODO: apply this discount to registration of events
	}
}
