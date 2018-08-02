/*
 * Copyright 2008-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mycompany.worklow.checkout;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.email.domain.EmailTargetImpl;
import org.broadleafcommerce.common.email.service.EmailService;
import org.broadleafcommerce.common.email.service.info.EmailInfo;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.checkout.service.workflow.CheckoutSeed;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderItemAttribute;
import org.broadleafcommerce.core.workflow.BaseActivity;
import org.broadleafcommerce.core.workflow.ProcessContext;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.springframework.beans.factory.annotation.Value;

/**
 * Send order confirmation email
 *
 * @author Phillip Verheyden (phillipuniverse)
 * @author Joshua Skorton (jskorton)
 */
public class SendAppointmentConfirmationEmailToDoctorActivity extends BaseActivity<ProcessContext<CheckoutSeed>> {

	protected static final Log LOG = LogFactory.getLog(SendAppointmentConfirmationEmailToDoctorActivity.class);

	@Value("${site.emailAddress}")
	protected String fromEmailAddress;

	@Resource(name = "blEmailService")
	protected EmailService emailService;

	@Resource(name = "blCatalogService")
	protected CatalogService catalogService;

	@Override
	public ProcessContext<CheckoutSeed> execute(ProcessContext<CheckoutSeed> context) throws Exception {
		Order order = context.getSeedData().getOrder();
		if (order.getDiscreteOrderItems().get(0).getProduct().getProSerSeg() == 1) {
			sendToDocServiceEmail(order);
		}

		return context;
	}

	private void sendToDocServiceEmail(Order order) {
		HashMap<String, Object> vars = new HashMap<String, Object>();

		Customer clinic = catalogService.readCustomerByOrder(order);
		String clinicEmailAddressTo = clinic.getEmailAddress();
		EmailInfo emailInfo = new EmailInfo();

		emailInfo.setFromAddress(fromEmailAddress);
		emailInfo.setSubject("[Appointment Number:" + order.getOrderNumber() + "]");
		emailInfo.setMessageBody("-------------------------<br />新しい予約はきました。<br />患者様：" + order.getCustomer().getLastName() + " " + order.getCustomer().getFirstName() + 
				"<br />予約希望日（月/日 時:分）：<br />" + getDateFromOptions(order.getOrderItems().get(0).getOrderItemAttributes()) + 
				"<br />症状説明：" + order.getOrderItems().get(0).getOrderItemAttributes().get("Symptom Description").getValue() +
				"<br />患者様に分かりやすい日本語で返信してください。登録メールのままをお願いします。<br />よろしくお願いします。<br />-------------------------");
		EmailTargetImpl emailTarget = new EmailTargetImpl();
		emailTarget.setEmailAddress(clinicEmailAddressTo);

		// Email service failing should not trigger rollback
		try {
			emailService.sendBasicEmail(emailInfo, emailTarget, vars);
		} catch (Exception e) {
			LOG.error(e);
		}
	}

	private String getDateFromOptions(Map<String, OrderItemAttribute> options) {
		return "第一希望日：" + ((OrderItemAttribute) options.get("1st Preferred Date")).getValue() + "<br />第二希望日：" + ((OrderItemAttribute) options.get("2nd Preferred Date")).getValue()+ "<br />第三希望日：" + ((OrderItemAttribute) options.get("3rd Preferred Date")).getValue();
	}

}
