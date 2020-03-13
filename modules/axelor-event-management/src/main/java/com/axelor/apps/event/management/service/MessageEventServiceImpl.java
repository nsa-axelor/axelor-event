package com.axelor.apps.event.management.service;

import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.service.MessageServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.repo.MetaAttachmentRepository;
import com.google.inject.Inject;
import javax.mail.MessagingException;

public class MessageEventServiceImpl extends MessageServiceImpl {

  @Inject
  public MessageEventServiceImpl(
      MetaAttachmentRepository metaAttachmentRepository, MessageRepository messageRepository) {
    super(metaAttachmentRepository, messageRepository);
  }

  @Override
  public Message sendByEmail(Message message) throws MessagingException, AxelorException {
    return super.sendByEmail(message);
  }
}
