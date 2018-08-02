package com.mycompany.controller.account;

import javax.annotation.Resource;

import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.domain.CustomerImpl;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.social.connect.Connection;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.User;

public class MediDocSocialSaveDataService {

	@Value("${use.email.for.site.login:true}")
	protected boolean useEmailForLogin;

	@Resource(name = "blCustomerService")
	private CustomerService customerService;

	public String saveOrUpdate(Connection<?> connection) {
		Facebook facebook = (Facebook) connection.getApi();
		String[] fields = { "id", "email", "first_name", "last_name" };
		User userProfile = facebook.fetchObject("me", User.class, fields);
		Customer customerInDb = customerService.readCustomerByEmail(userProfile.getEmail());

		System.out.println(customerInDb);
		if (customerInDb != null) {

			customerInDb.setFirstName(userProfile.getFirstName());
			customerInDb.setLastName(userProfile.getLastName());
			customerInDb.setEmailAddress(userProfile.getEmail());
			if (isUseEmailForLogin()) {
				customerInDb.setUsername(userProfile.getEmail());
			} else {
				customerInDb.setUsername(userProfile.getName());
			}

			customerService.saveCustomer(customerInDb);

			return userProfile.getEmail();
		} else {
			Customer customer = new CustomerImpl();
			if (isUseEmailForLogin()) {
				customer.setUsername(userProfile.getEmail());
			} else {
				customer.setUsername(userProfile.getName());
			}
			customer.setEmailAddress(userProfile.getEmail());
			customer.setFirstName(userProfile.getFirstName());
			customer.setLastName(userProfile.getLastName());
			customer.setId(customerService.findNextCustomerId());
			customer.setPassword(Double.toString(Math.random()));

			customerService.registerCustomer(customer, customer.getPassword(), customer.getPassword(), "ROLE_USER");

			return customer.getEmailAddress();
		}
	}

	public boolean isUseEmailForLogin() {
		return useEmailForLogin;
	}

	public void setUseEmailForLogin(boolean useEmailForLogin) {
		this.useEmailForLogin = useEmailForLogin;
	}

}
