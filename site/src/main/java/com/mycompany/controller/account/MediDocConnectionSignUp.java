package com.mycompany.controller.account;

import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionSignUp;

public class MediDocConnectionSignUp implements ConnectionSignUp {

	private MediDocSocialSaveDataService mdSocialSaveDataService;

	@Override
	public String execute(final Connection<?> connection) {
		return getMdSocialSaveDataService().saveOrUpdate(connection);
	}

	public MediDocSocialSaveDataService getMdSocialSaveDataService() {
		return mdSocialSaveDataService;
	}

	public void setMdSocialSaveDataService(MediDocSocialSaveDataService mdSocialSaveDataService) {
		this.mdSocialSaveDataService = mdSocialSaveDataService;
	}

}
