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
import org.broadleafcommerce.core.checkout.service.workflow.CheckoutSeed;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderItemAttribute;
import org.broadleafcommerce.core.workflow.BaseActivity;
import org.broadleafcommerce.core.workflow.ProcessContext;
import org.springframework.beans.factory.annotation.Value;

/**
 * Send order confirmation email
 *
 * @author Phillip Verheyden (phillipuniverse)
 * @author Joshua Skorton (jskorton)
 */
public class SendOrderConfirmationEmailActivity extends BaseActivity<ProcessContext<CheckoutSeed>> {

	protected static final Log LOG = LogFactory.getLog(SendOrderConfirmationEmailActivity.class);

	@Value("${site.reception.emailAddress}")
	protected String fromEmailAddress;
	
	@Resource(name = "blEmailService")
	protected EmailService emailService;

	@Resource(name = "blOrderConfirmationEmailInfo")
	protected EmailInfo orderConfirmationEmailInfo;

	@Override
	public ProcessContext<CheckoutSeed> execute(ProcessContext<CheckoutSeed> context) throws Exception {
		Order order = context.getSeedData().getOrder();

		if (order.getDiscreteOrderItems().get(0).getProduct().getProSerSeg() == 1) {
			sendServiceEmail(order);
		} else {
			sendGoodsEmail(order);
		}

		return context;
	}

	public EmailInfo getOrderConfirmationEmailInfo() {
		return orderConfirmationEmailInfo;
	}

	public void setOrderConfirmationEmailInfo(EmailInfo orderConfirmationEmailInfo) {
		this.orderConfirmationEmailInfo = orderConfirmationEmailInfo;
	}

	private void sendGoodsEmail(Order order) {
		HashMap<String, Object> vars = new HashMap<String, Object>();
		vars.put("customer", order.getCustomer());
		vars.put("orderNumber", order.getOrderNumber());
		vars.put("order", order);

		// Email service failing should not trigger rollback
		try {
			emailService.sendTemplateEmail(order.getEmailAddress(), getOrderConfirmationEmailInfo(), vars);
		} catch (Exception e) {
			LOG.error(e);
		}
	}

	private void sendServiceEmail(Order order) {
		HashMap<String, Object> vars = new HashMap<String, Object>();
		EmailInfo emailInfo = new EmailInfo();

		emailInfo.setFromAddress(fromEmailAddress);
		emailInfo.setSubject("[Appointment Number:" + order.getOrderNumber() + "]");
		emailInfo.setMessageBody("-------------------------<br />医院已将收到您的就诊预约。<br />医院名：" + order.getDiscreteOrderItems().get(0).getProduct().getName() + 
				"<br />预约候选日（月/日 时：分）：<br />" + getDateFromOptions(order.getOrderItems().get(0).getOrderItemAttributes()) + 
				"<br />病症说明：" + order.getOrderItems().get(0).getOrderItemAttributes().get("Symptom Description").getValue() + 
				"<br />医院稍后会通过邮件向您确认预约事宜，请稍后。请用登陆的电子邮件地址回复邮件。<br />祝安康。<br />-------------------------");
		EmailTargetImpl emailTarget = new EmailTargetImpl();
		emailTarget.setEmailAddress(order.getEmailAddress());

		// Email service failing should not trigger rollback
		try {
			emailService.sendBasicEmail(emailInfo, emailTarget, vars);
		} catch (Exception e) {
			LOG.error(e);
		}
	}

	private String getDateFromOptions(Map<String, OrderItemAttribute> options) {
		return "第一候选日：" + ((OrderItemAttribute) options.get("1st Preferred Date")).getValue() + "<br />第二候选日：" + ((OrderItemAttribute) options.get("2nd Preferred Date")).getValue()+ "<br />第三候选日：" + ((OrderItemAttribute) options.get("3rd Preferred Date")).getValue();
	}

}
