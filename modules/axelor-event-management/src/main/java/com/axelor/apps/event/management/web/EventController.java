package com.axelor.apps.event.management.web;

import java.math.BigDecimal;
import java.util.List;

import javax.mail.MessagingException;

import com.axelor.apps.event.management.db.Discount;
import com.axelor.apps.event.management.db.Event;
import com.axelor.apps.event.management.db.EventRegistration;
import com.axelor.apps.event.management.service.EventService;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.service.MessageService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.persist.Transactional;

public class EventController {

	public void setEventRegistrations(ActionRequest request, ActionResponse response) {
		Event event = request.getContext().asType(Event.class);
		response.setValue("eventRegistrationList", Beans.get(EventService.class).removeLastLineByValidations(event));
	}

	public void applyDiscount(ActionRequest request, ActionResponse response) {
		Event event = request.getContext().asType(Event.class);
		EventService eventService = Beans.get(EventService.class); 
		List<EventRegistration> eventRegistrations = event.getEventRegistrationList();
		if (eventRegistrations.size() > 0 && eventRegistrations != null) {
			eventRegistrations = eventService.lastModifiedEventRegistrationList(event);
			event = eventService.calculateTotal(event, eventRegistrations);
			response.setValue("amountCollected", event.getAmountCollected());
			response.setValue("totalDiscount", event.getTotalDiscount());
			response.setValue("totalEntry", eventRegistrations.size());
			response.setValue("eventRegistrationList", eventRegistrations);
		}
	}

	@Transactional
	public void sendMail(ActionRequest request, ActionResponse response) {
		try {
			Event event = request.getContext().asType(Event.class);
			EventService eventService = Beans.get(EventService.class);
			if(!eventService.areAllMailSent(event.getEventRegistrationList())) {
				Message message = eventService.setUpMessage(event);
				Beans.get(MessageRepository.class).save(message);
				Beans.get(MessageService.class).sendByEmail(message);
				response.setReload(true);
			}
		} catch (MessagingException | AxelorException e) {
			e.printStackTrace();
		}
	}

	public void importData(ActionRequest request, ActionResponse response) {

	}

	public void setDiscounts(ActionRequest request, ActionResponse response) {
		Event event = request.getContext().asType(Event.class);
		List<Discount> discountList = event.getDiscountList();
		for (Discount discount : discountList) {
			discount.setDiscountAmount(
					event.getEventFees().multiply(discount.getDiscountPercent()).divide(new BigDecimal(100)));
		}
		response.setValue("discountList", discountList);
	}
}
