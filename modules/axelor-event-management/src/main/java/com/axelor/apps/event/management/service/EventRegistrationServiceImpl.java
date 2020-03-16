package com.axelor.apps.event.management.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.axelor.apps.event.management.db.Discount;
import com.axelor.apps.event.management.db.Event;
import com.axelor.apps.event.management.db.EventRegistration;
import com.axelor.apps.event.management.db.repo.DiscountRepository;
import com.axelor.apps.event.management.db.repo.EventRepository;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class EventRegistrationServiceImpl implements EventRegistrationService {

	@Inject
	EventService eventService;

	@Override
	public BigDecimal calculateAmount(EventRegistration eventRegistration) {
		Event event = eventRegistration.getEvent();

		List<Discount> discountList = Beans.get(DiscountRepository.class).all().filter("self.event = :event")
				.bind("event", event.getId()).order("-discountPercent").fetch();

		if (discountList.size() > 0 && discountList != null) {
			Discount discount = discountList.get(0);

			LocalDate registrationDate = eventRegistration.getRegistrationDateT().toLocalDate();
			LocalDate offerLastDate = event.getRegistrationCloseDate().minusDays(discount.getBeforeDays().longValue());

			for (int i = 0; i < discountList.size(); i++) {
				discount = discountList.get(i);
				offerLastDate = event.getRegistrationCloseDate().minusDays(discount.getBeforeDays().longValue());
				if (eventService.countDays(registrationDate, offerLastDate) >= 0)
					break;
			}

			if (eventService.countDays(registrationDate, offerLastDate) >= 0) {
				BigDecimal eventFees = event.getEventFees();
				BigDecimal discountAmount = discount.getDiscountAmount();
				BigDecimal offerAppliedAmount = eventFees.subtract(discountAmount);
				return offerAppliedAmount;
			} else {
				return event.getEventFees();
			}
		} else {
			return event.getEventFees();
		}
	}

	@Transactional
	@Override
	public void saveEventTotalCalculations(Event event, EventRegistration registration) {
		event = Beans.get(EventRepository.class).find(event.getId());
		BigDecimal totalDiscount = event.getTotalDiscount();
		BigDecimal amountCollected = event.getAmountCollected();
		BigDecimal amountToAdd = registration.getAmount();
		if (!event.getEventFees().equals(amountToAdd)) {
			totalDiscount = totalDiscount.add(event.getEventFees().subtract(amountToAdd));
		}
		amountCollected = amountCollected.add(amountToAdd);
		event.setTotalDiscount(totalDiscount);
		event.setAmountCollected(amountCollected);
		Beans.get(EventRepository.class).save(event);
	}

}
