package com.axelor.apps.event.management.service;

import com.axelor.apps.message.db.Message;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import javax.mail.MessagingException;

public class EventServiceImpl implements EventService {

  @Inject MessageEventServiceImpl messageService;

  @Override
  public Message sendEmail(Message message) {
    try {
      messageService.sendByEmail(message);
    } catch (MessagingException | AxelorException e) {
      e.printStackTrace();
    }
    return message;
  }
}
