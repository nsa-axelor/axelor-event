package com.axelor.apps.event.management.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.event.management.service.EventService;
import com.axelor.apps.event.management.service.EventServiceImpl;
import com.axelor.apps.event.management.service.MessageEventServiceImpl;
import com.axelor.apps.message.service.MessageServiceImpl;

public class EventManagementModule extends AxelorModule {
  @Override
  protected void configure() {
    bind(EventService.class).to(EventServiceImpl.class);
    bind(MessageServiceImpl.class).to(MessageEventServiceImpl.class);
  }
}
