package com.mycompany.web.email;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.email.domain.EmailTargetImpl;
import org.broadleafcommerce.common.email.service.EmailService;
import org.broadleafcommerce.common.email.service.info.EmailInfo;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.OrderService;
import org.springframework.beans.factory.annotation.Value;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.mycompany.worklow.checkout.SendAppointmentConfirmationEmailToDoctorActivity;

public class TransferApptEmail {

	protected static final Log LOG = LogFactory.getLog(SendAppointmentConfirmationEmailToDoctorActivity.class);

	@Value("${site.emailAddress}")
	protected String fromEmailAddress;

	@Resource(name = "blEmailService")
	protected EmailService emailService;

	@Resource(name = "blCatalogService")
	protected CatalogService catalogService;

	@Resource(name = "blOrderService")
	protected OrderService orderService;

	public void translateAndSendEmail(MimeMessage mimeMessage) {
		try {
			Matcher matcher = Pattern.compile("(Appointment Number:)([0-9]+)").matcher(mimeMessage.getSubject());
			String orderNumber = "";
			if(matcher.find()) {
				orderNumber = matcher.group(2);
			} else {
				return;
			}
			Order order = orderService.findOrderByOrderNumber(orderNumber);
			if (order == null) {
				sendBackEmail(mimeMessage);
			} else {
				forwardEmail(mimeMessage, order);
			}
		} catch (Exception e) {
			LOG.error(e);
		}
	}

	private void sendBackEmail(MimeMessage mimeMessage) throws Exception {
		HashMap<String, Object> vars = new HashMap<String, Object>();

		EmailInfo emailInfo = new EmailInfo();

		emailInfo.setFromAddress(fromEmailAddress);
		emailInfo.setSubject(mimeMessage.getSubject());
		EmailTargetImpl emailTarget = new EmailTargetImpl();

		emailInfo.setMessageBody("-------------------------<br />この予約は見つかりません。" + fromEmailAddress + "に問い合わせてください。<br />The appointment cannot be found. Please contact " + fromEmailAddress
				+ ".<br />-------------------------");
		emailTarget.setEmailAddress(mimeMessage.getFrom()[0].toString());

		// Email service failing should not trigger rollback
		emailService.sendBasicEmail(emailInfo, emailTarget, vars);
	}

	private void forwardEmail(MimeMessage mimeMessage, Order order) throws Exception {
		String patientEmailAddress = order.getCustomer().getEmailAddress();
		String clinicEmailAddress = catalogService.readCustomerByOrder(order).getEmailAddress();

		HashMap<String, Object> vars = new HashMap<String, Object>();

		EmailInfo emailInfo = new EmailInfo();

		emailInfo.setFromAddress(fromEmailAddress);
		emailInfo.setSubject(mimeMessage.getSubject());
		EmailTargetImpl emailTarget = new EmailTargetImpl();

		String translateMsg = mimeMessage.getContent().toString().split("[-]{15,}")[0].replaceAll("(\r\n|\n)", "<br />");
		if (mimeMessage.getFrom()[0].toString().contains(patientEmailAddress)) {
			translateMsg = "-------------------------<br />[---患者様に分かりやすい日本語で返信してください。登録メールのままをお願いします。---]<br />" + translate(translateMsg, "en") + "<br />-------------------------";
			emailInfo.setMessageBody(translateMsg);
			emailTarget.setEmailAddress(clinicEmailAddress);
		} else if (mimeMessage.getFrom()[0].toString().contains(clinicEmailAddress)) {
			translateMsg = "-------------------------<br />[---Please reply this email by your registered email. Contact your doctor in easy understanding English please.---]<br />" + translate(translateMsg, "ja")
					+ "<br />-------------------------";
			emailInfo.setMessageBody(translateMsg);
			emailTarget.setEmailAddress(patientEmailAddress);
		} else {
			translateMsg = "-------------------------<br />[---患者様に分かりやすい日本語で返信してください。登録メールのままをお願いします。---]<br />" + 
					"[---Please reply this email by your registered email. Contact your doctor in easy understanding English please.---]<br />-------------------------";
			emailInfo.setMessageBody(translateMsg);
			emailTarget.setEmailAddress(mimeMessage.getFrom()[0].toString());
		}

		// Email service failing should not trigger rollback
		emailService.sendBasicEmail(emailInfo, emailTarget, vars);
	}

	private String translate(String translateText, String sourceLanguage) {
		Translate translate = TranslateOptions.newBuilder().setApiKey("AIzaSyDq18Z3RIAR_PKn0dLmsYwUkbRjRZKu4Bs").build().getService();
		String result = translateText;
		Translation translation = null;
		if ("ja".equals(sourceLanguage)) {
			translation = translate.translate(translateText, TranslateOption.sourceLanguage("ja"), TranslateOption.targetLanguage("en"));
		} else if ("en".equals(sourceLanguage)) {
			translation = translate.translate(translateText, TranslateOption.sourceLanguage("en"), TranslateOption.targetLanguage("ja"));
		}
		if (translation != null) {
			result = translation.getTranslatedText();
		}
		return result;
	}

}
