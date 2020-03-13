package com.axelor.apps.event.management.web;

import com.axelor.apps.event.management.db.Discount;
import com.axelor.apps.event.management.db.Event;
import com.axelor.apps.event.management.db.EventRegistration;
import com.axelor.apps.event.management.db.repo.DiscountRepository;
import com.axelor.apps.event.management.db.repo.EventRegistrationRepository;
import com.axelor.apps.event.management.service.EventService;
import com.axelor.apps.message.db.EmailAccount;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.repo.EmailAccountRepository;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventController {

  public void setEventRegistrations(ActionRequest request, ActionResponse response) {
    Event event = request.getContext().asType(Event.class);
    List<EventRegistration> eventRegistrations = event.getEventRegistrationList();
    try {
      if (event.getCapacity() < eventRegistrations.size()
          || event.getRegistrationOpenDate().isAfter(LocalDate.now())
          || event.getRegistrationCloseDate().isBefore(LocalDate.now())
          || event.getRegistrationCloseDate() == null
          || event.getRegistrationOpenDate() == null) {
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
    List<Discount> discountList =
        Beans.get(DiscountRepository.class)
            .all()
            .filter("self.event = :event")
            .bind("event", event.getId())
            .order("-discountPercent")
            .fetch();
    /*
     * if(discountList.size() > 0 && discountList != null) { discountList =
     * Beans.get(DiscountRepository.class).all().filter("self.event = :event")
     * .bind("event", event.getId()).order("-discountPercent").fetch(); }
     */

    List<EventRegistration> eventRegistrations = event.getEventRegistrationList();
    if (eventRegistrations.size() > 0 && eventRegistrations != null) {

      EventRegistration eventRegistration = eventRegistrations.get(eventRegistrations.size() - 1);

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
          eventRegistration.setAmount(offerAppliedAmount);
          eventRegistrations.set(eventRegistrations.size() - 1, eventRegistration);
        } else {
          eventRegistration.setAmount(event.getEventFees());
          eventRegistrations.set(eventRegistrations.size() - 1, eventRegistration);
        }

      } else {
        eventRegistration.setAmount(event.getEventFees());
        eventRegistrations.set(eventRegistrations.size() - 1, eventRegistration);
      }
      BigDecimal amountCollected = BigDecimal.ZERO;
      BigDecimal totalDiscount = BigDecimal.ZERO;
      for (EventRegistration er : eventRegistrations) {
        amountCollected = amountCollected.add(er.getAmount());
        totalDiscount = totalDiscount.add(event.getEventFees().subtract(er.getAmount()));
      }
      response.setValue("amountCollected", amountCollected);
      response.setValue("totalDiscount", totalDiscount);
      response.setValue("eventRegistrationList", eventRegistrations);
    }
  }

  @Transactional
  public void sendMail(ActionRequest request, ActionResponse response) {
    Event event = request.getContext().asType(Event.class);
    List<EventRegistration> eventRegistrations = event.getEventRegistrationList();

    Message message = new Message();

    EmailAccount emailAccount =
        Beans.get(EmailAccountRepository.class)
            .all()
            .filter("self.isDefault = true AND self.isValid = true")
            .fetchOne();

    message.setFromEmailAddress(new EmailAddress("nsa.axelor@gmail.com"));
    message.setContent("Test Content");
    message.setSubject("Test");
    Set<EmailAddress> addresses = new HashSet<>();

    for (EventRegistration eventRegistration : eventRegistrations) {
      if (!eventRegistration.isSelected()) {
        addresses.add(new EmailAddress(eventRegistration.getEmail()));
      }
      eventRegistration =
          Beans.get(EventRegistrationRepository.class).find(eventRegistration.getId());
      eventRegistration.setIsEmailSent(true);
      Beans.get(EventRegistrationRepository.class).save(eventRegistration);
    }

    message.setToEmailAddressSet(addresses);
    message.setMailAccount(emailAccount);
    message.setTypeSelect(MessageRepository.TYPE_SENT);
    message.setMediaTypeSelect(MessageRepository.MEDIA_TYPE_EMAIL);
    message.setSentByEmail(true);

    Beans.get(MessageRepository.class).save(message);
    Beans.get(EventService.class).sendEmail(message);
  }
}
