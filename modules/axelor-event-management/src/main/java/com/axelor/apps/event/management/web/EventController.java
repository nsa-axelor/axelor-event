package com.axelor.apps.event.management.web;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.axelor.apps.event.management.db.Discount;
import com.axelor.apps.event.management.db.Event;
import com.axelor.apps.event.management.db.EventRegistration;
import com.axelor.apps.event.management.error.IErrorMessage;
import com.axelor.apps.event.management.service.EventService;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.service.MessageService;
import com.axelor.csv.script.ImportEventRegistration;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.persist.Transactional;

public class EventController {

	public void setEventRegistrations(ActionRequest request, ActionResponse response) {
		try {
			Event event = request.getContext().asType(Event.class);
			response.setValue("eventRegistrationList",
					Beans.get(EventService.class).removeLastLineByValidations(event));
		} catch (Exception e) {
			TraceBackService.trace(e);
		}
	}

	public void applyDiscount(ActionRequest request, ActionResponse response) {
		try {
			Event event = request.getContext().asType(Event.class);
			EventService eventService = Beans.get(EventService.class);
			List<EventRegistration> eventRegistrations = event.getEventRegistrationList();
			if (eventRegistrations.size() > 0 && eventRegistrations != null) {
				eventRegistrations = eventService.getCalculatedEventRegistrationList(event);
				event = eventService.calculateTotal(event, eventRegistrations);
				response.setValue("amountCollected", event.getAmountCollected());
				response.setValue("totalDiscount", event.getTotalDiscount());
				response.setValue("totalEntry", eventRegistrations.size());
				response.setValue("eventRegistrationList", eventRegistrations);
			}
		} catch (Exception e) {
			TraceBackService.trace(e);
		}
	}

	@Transactional
	public void sendMail(ActionRequest request, ActionResponse response) {
		try {
			Event event = request.getContext().asType(Event.class);
			EventService eventService = Beans.get(EventService.class);
			if (!eventService.areAllMailSent(event.getEventRegistrationList())) {
				Message message = eventService.setUpMessage(event);
				Beans.get(MessageRepository.class).save(message);
				Beans.get(MessageService.class).sendByEmail(message);
				response.setReload(true);
			}
		} catch (Exception e) {
			TraceBackService.trace(e);
		}
	}

	@SuppressWarnings("unchecked")
	public void importData(ActionRequest request, ActionResponse response) {
		try {
			Integer id = (Integer) request.getContext().get("_id");
			Long eventId = id.longValue();

			Map<String, Object> map = new HashMap<>();
			map = (HashMap<String, Object>) request.getContext().get("fileUpload");

			if (map.get("fileType").toString().equals("text/csv")) {
				MetaFile metaFile = Beans.get(MetaFileRepository.class).find(Long.parseLong(map.get("id").toString()));
				Beans.get(EventService.class).importDataFromCsvFile(metaFile, eventId);
				response.setFlash("Data imported !");
				ImportEventRegistration.isCounterInitiated = false;
			} else {
				response.setError(I18n.get(IErrorMessage.CSV_EXTENSION_ERROR));
			}
			response.setCanClose(true);
		} catch (Exception e) {
			TraceBackService.trace(e);
		}
	}

	public void setDiscounts(ActionRequest request, ActionResponse response) {
		try {
			Event event = request.getContext().asType(Event.class);
			List<Discount> discountList = event.getDiscountList();
			if (discountList != null)
				for (Discount discount : discountList) {
					discount.setDiscountAmount(
							event.getEventFees().multiply(discount.getDiscountPercent()).divide(new BigDecimal(100)));
				}
			response.setValue("discountList", discountList);
		} catch (Exception e) {
			TraceBackService.trace(e);
		}
	}
}
