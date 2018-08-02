package com.mycompany.web.exception;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
class ExceptionHandlingController {

	public static final String DEFAULT_ERROR_VIEW = "/error/errorPage";

	@ExceptionHandler(NoHandlerFoundException.class)
	public ModelAndView handleError405(HttpServletRequest request, Exception exp) {
		ModelAndView mav = new ModelAndView("/error/pageNotFound");
		mav.addObject("exception", exp.getStackTrace().toString());
		exp.getStackTrace();
		return mav;
	}

	@ExceptionHandler(value = Exception.class)
	public ModelAndView defaultErrorHandler(HttpServletRequest request, Exception exp) throws Exception {

		// Otherwise setup and send the user to a default error-view.
		ModelAndView mav = new ModelAndView(DEFAULT_ERROR_VIEW);
		mav.addObject("exception", exp.getStackTrace().toString());
		mav.addObject("url", request.getRequestURL());
		exp.getStackTrace();
		return mav;
	}

}
