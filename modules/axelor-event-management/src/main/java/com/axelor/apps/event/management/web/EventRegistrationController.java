package com.axelor.apps.event.management.web;

import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class EventRegistrationController {

  public void closeForm(ActionRequest request, ActionResponse response) {
    response.setCanClose(true);
  }
}
