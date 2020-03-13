package com.axelor.apps.event.management.service;

import com.axelor.apps.message.db.Message;

public interface EventService {
  public Message sendEmail(Message message);
}
