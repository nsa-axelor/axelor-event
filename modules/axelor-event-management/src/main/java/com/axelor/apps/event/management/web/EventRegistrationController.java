package com.axelor.apps.event.management.web;

import java.math.BigDecimal;

import com.axelor.apps.event.management.db.Event;
import com.axelor.apps.event.management.db.EventRegistration;
import com.axelor.apps.event.management.service.EventRegistrationService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class EventRegistrationController {

	public void closeForm(ActionRequest request, ActionResponse response) {
		try {
			response.setCanClose(true);
		} catch (Exception e) {
			TraceBackService.trace(e);
		}
	}

	public void setAmount(ActionRequest request, ActionResponse response) {
		try {
			EventRegistration eventRegistration = request.getContext().asType(EventRegistration.class);
			BigDecimal amount = Beans.get(EventRegistrationService.class).calculateAmount(eventRegistration);
			response.setValue("amount", amount);
		} catch (Exception e) {
			TraceBackService.trace(e);
		}
	}

	public void setEventCalculations(ActionRequest request, ActionResponse response) {
		try {
			EventRegistration eventRegistration = request.getContext().asType(EventRegistration.class);
			Event event = eventRegistration.getEvent();
			Beans.get(EventRegistrationService.class).saveEventTotalCalculations(event, eventRegistration);
		} catch (Exception e) {
			TraceBackService.trace(e);
		}
	}
}
