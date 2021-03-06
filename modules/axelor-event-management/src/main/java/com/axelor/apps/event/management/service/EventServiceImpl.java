package com.axelor.apps.event.management.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.axelor.apps.event.management.db.Discount;
import com.axelor.apps.event.management.db.Event;
import com.axelor.apps.event.management.db.EventRegistration;
import com.axelor.apps.event.management.db.repo.DiscountRepository;
import com.axelor.apps.event.management.db.repo.EventRegistrationRepository;
import com.axelor.apps.message.db.EmailAccount;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.repo.EmailAccountRepository;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.data.ImportTask;
import com.axelor.data.csv.CSVImporter;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.google.common.io.Files;
import com.google.inject.persist.Transactional;

public class EventServiceImpl implements EventService {

	@Override
	public Event calculateTotal(Event event, List<EventRegistration> eventRegistrations) {
		BigDecimal amountCollected = BigDecimal.ZERO;
		BigDecimal totalDiscount = BigDecimal.ZERO;
		for (EventRegistration er : eventRegistrations) {
			amountCollected = amountCollected.add(er.getAmount());
			if(!er.getAmount().equals(BigDecimal.ZERO))
			totalDiscount = totalDiscount.add(event.getEventFees().subtract(er.getAmount()));
		}
		event.setAmountCollected(amountCollected);
		event.setTotalDiscount(totalDiscount);
		return event;
	}

	@Override
	public List<EventRegistration> removeLastLine(List<EventRegistration> eventRegistrations) {
		eventRegistrations.remove(eventRegistrations.size() - 1);
		return eventRegistrations;
	}

	@Override
	public List<EventRegistration> removeLastLineByValidations(Event event) {
		List<EventRegistration> eventRegistrations = event.getEventRegistrationList();
		try {
			if (event.getRegistrationCloseDate() == null
					|| event.getRegistrationOpenDate() == null) {
				return this.removeLastLine(eventRegistrations);
			}
		} catch (Exception e) {
			return this.removeLastLine(eventRegistrations);
		}
		return eventRegistrations;
	}

	@Override
	public long countDays(LocalDate startDate, LocalDate endDate) {
		return ChronoUnit.DAYS.between(startDate, endDate);
	}

	@Override
	public List<EventRegistration> getCalculatedEventRegistrationList(Event event) {
		List<Discount> discountList = Beans.get(DiscountRepository.class).all().filter("self.event = :event")
				.bind("event", event.getId()).order("-discountPercent").fetch();
		List<EventRegistration> eventRegistrations = event.getEventRegistrationList();
		
		for(int i = 0; i < eventRegistrations.size(); i++){
			
			EventRegistration reg = eventRegistrations.get(i);
			
			if (discountList.size() > 0 && discountList != null) {
				Discount discount = discountList.get(0);
				if (reg.getRegistrationDateT() == null) {
					eventRegistrations.set(i, reg);
					continue;
				}
				LocalDate registrationDate = reg.getRegistrationDateT().toLocalDate();
				LocalDate offerLastDate = event.getRegistrationCloseDate().minusDays(discount.getBeforeDays().longValue());

				for (int j = 0; j < discountList.size(); j++) {
					discount = discountList.get(j);
					offerLastDate = event.getRegistrationCloseDate().minusDays(discount.getBeforeDays().longValue());
					if (this.countDays(registrationDate, offerLastDate) >= 0)
						break;
				}

				if (this.countDays(registrationDate, offerLastDate) >= 0) {
					BigDecimal eventFees = event.getEventFees();
					BigDecimal discountAmount = discount.getDiscountAmount();
					BigDecimal offerAppliedAmount = eventFees.subtract(discountAmount);
					reg.setAmount(offerAppliedAmount);
					eventRegistrations.set(i, reg);
				} else {
					reg.setAmount(event.getEventFees());
					eventRegistrations.set(i, reg);
				}

			} else {
				reg.setAmount(event.getEventFees());
				eventRegistrations.set(i, reg);
			}
		}
		
//		EventRegistration eventRegistration = eventRegistrations.get(eventRegistrations.size() - 1);
//
//		if (discountList.size() > 0 && discountList != null) {
//			Discount discount = discountList.get(0);
//			LocalDate registrationDate = eventRegistration.getRegistrationDateT().toLocalDate();
//			LocalDate offerLastDate = event.getRegistrationCloseDate().minusDays(discount.getBeforeDays().longValue());
//
//			for (int i = 0; i < discountList.size(); i++) {
//				discount = discountList.get(i);
//				offerLastDate = event.getRegistrationCloseDate().minusDays(discount.getBeforeDays().longValue());
//				if (this.countDays(registrationDate, offerLastDate) >= 0)
//					break;
//			}
//
//			if (this.countDays(registrationDate, offerLastDate) >= 0) {
//				BigDecimal eventFees = event.getEventFees();
//				BigDecimal discountAmount = discount.getDiscountAmount();
//				BigDecimal offerAppliedAmount = eventFees.subtract(discountAmount);
//				eventRegistration.setAmount(offerAppliedAmount);
//				eventRegistrations.set(eventRegistrations.size() - 1, eventRegistration);
//			} else {
//				eventRegistration.setAmount(event.getEventFees());
//				eventRegistrations.set(eventRegistrations.size() - 1, eventRegistration);
//			}
//
//		} else {
//			eventRegistration.setAmount(event.getEventFees());
//			eventRegistrations.set(eventRegistrations.size() - 1, eventRegistration);
//		}
		return eventRegistrations;
	}

	@Transactional
	@Override
	public Message setUpMessage(Event event) {
		List<EventRegistration> eventRegistrations = event.getEventRegistrationList();
		EmailAddress address = new EmailAddress("nsa.axelor@gmail.com");
		Message message = new Message();

		EmailAccount emailAccount = Beans.get(EmailAccountRepository.class).all()
				.filter("self.isDefault = true AND self.isValid = true").fetchOne();

		message.setFromEmailAddress(address);
		message.setContent("Test Content");
		message.setSubject("Test");

		for (EventRegistration eventRegistration : eventRegistrations) {
			if (!eventRegistration.getIsEmailSent()) {
				message.addToEmailAddressSetItem(new EmailAddress(eventRegistration.getEmail()));
			}
			eventRegistration = Beans.get(EventRegistrationRepository.class).find(eventRegistration.getId());
			eventRegistration.setIsEmailSent(true);
			Beans.get(EventRegistrationRepository.class).save(eventRegistration);
		}

		message.addCcEmailAddressSetItem(address);
		message.addBccEmailAddressSetItem(address);

		message.setMailAccount(emailAccount);
		message.setTypeSelect(MessageRepository.TYPE_SENT);
		message.setMediaTypeSelect(MessageRepository.MEDIA_TYPE_EMAIL);
		message.setSentByEmail(true);
		return message;
	}

	@Override
	public boolean areAllMailSent(List<EventRegistration> eventRegistrations) {
		for (EventRegistration eventRegistration : eventRegistrations)
			if (!eventRegistration.getIsEmailSent())
				return false;
		return true;
	}

	@Override
	public void importDataFromCsvFile(MetaFile metaFile, Long eventId) {

		this.getCsvFile(metaFile);
		String configFilePath = this.getConfigFile().getAbsolutePath();
		String csvFilePath = this.getCsvFilePath(metaFile);
		CSVImporter importer = new CSVImporter(configFilePath);
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("event", eventId);
		importer.setContext(context);
		importer.run(new ImportTask() {

			@Override
			public void configure() throws IOException {
				input("event.csv", new File(csvFilePath));
			}
		});
		deleteFile(metaFile);
	}

	@Transactional
	public void deleteFile(MetaFile metaFile) {
		Beans.get(MetaFileRepository.class).remove(metaFile);
		File tmpDir = Files.createTempDir();
		File csvFile = new File(tmpDir.getPath() + "event.csv");
		File meta = MetaFiles.getPath(metaFile).toFile();
		meta.delete();
		csvFile.delete();
	}

	public File getConfigFile() {
		File importConfigFile = null;
		try {
			importConfigFile = File.createTempFile("csv-config", ".xml");
			InputStream is = this.getClass().getResourceAsStream("/data-demo/input-config2.xml");
			FileOutputStream os = new FileOutputStream(importConfigFile);
			IOUtils.copy(is, os);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return importConfigFile;
	}

	public File getCsvFile(MetaFile metaFile) {
		File csvFile = null;
		try {
			File tempDir = Files.createTempDir();
			csvFile = new File(tempDir, "event.csv");
			Files.copy(MetaFiles.getPath(metaFile).toFile(), csvFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return csvFile;
	}

	public String getCsvFilePath(MetaFile metaFile) {
		String path = MetaFiles.getPath(metaFile).toString();
		return path;
	}

}
