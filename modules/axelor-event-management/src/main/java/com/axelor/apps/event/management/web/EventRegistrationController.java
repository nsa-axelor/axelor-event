package com.axelor.apps.event.management.web;

import com.axelor.apps.event.management.db.Discount;
import com.axelor.apps.event.management.db.Event;
import com.axelor.apps.event.management.db.EventRegistration;
import com.axelor.apps.event.management.db.repo.DiscountRepository;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class EventRegistrationController {

  public void closeForm(ActionRequest request, ActionResponse response) {
    response.setCanClose(true);
  }

  public void setAmount(ActionRequest request, ActionResponse response) {
    EventRegistration eventRegistration = request.getContext().asType(EventRegistration.class);
    System.err.println("call");
    Event event = eventRegistration.getEvent();
    List<Discount> discountList =
        Beans.get(DiscountRepository.class)
            .all()
            .filter("self.event = :event")
            .bind("event", event.getId())
            .order("-discountPercent")
            .fetch();
    if (discountList.size() > 0 && discountList != null) {
      Discount discount = discountList.get(0);

      LocalDate registrationDate = eventRegistration.getRegistrationDateT().toLocalDate();
      LocalDate offerLastDate =
          event.getRegistrationCloseDate().minusDays(discount.getBeforeDays().longValue());

      for (int i = 0; i < discountList.size(); i++) {
        discount = discountList.get(i);
        offerLastDate =
            event.getRegistrationCloseDate().minusDays(discount.getBeforeDays().longValue());
        if (ChronoUnit.DAYS.between(registrationDate, offerLastDate) >= 0) {
          break;
        }
      }

      if (ChronoUnit.DAYS.between(registrationDate, offerLastDate) >= 0) {
        BigDecimal eventFees = event.getEventFees();
        BigDecimal discountAmount = discount.getDiscountAmount();
        BigDecimal offerAppliedAmount = eventFees.subtract(discountAmount);
        response.setValue("amount", offerAppliedAmount);
      } else {
        response.setValue("amount", event.getEventFees());
      }
    } else {
      response.setValue("amount", event.getEventFees());
    }
  }
}
