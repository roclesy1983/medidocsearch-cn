/*
 * Copyright 2008-2012 the original author or authors.
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

package com.mycompany.controller.account;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.web.controller.account.BroadleafOrderHistoryController;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CountryService;
import org.broadleafcommerce.profile.core.service.StateService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/account/orders")
public class OrderHistoryController extends BroadleafOrderHistoryController {

    @Resource(name = "blStateService")
    StateService stateService;

    @Resource(name = "blCountryService")
    CountryService countryService;

    @RequestMapping(method = RequestMethod.GET)
    public String viewOrderHistory(HttpServletRequest request, Model model) {
    	String orderHistoryView = super.viewOrderHistory(request, model);
    	if(request.isUserInRole("ROLE_DOCTOR")){
    		Customer customer = (Customer) request.getAttribute("customer");
    		Long productId = catalogService.readProductByCustomerId(customer.getId()).getId();
    		List<Order> orders = orderService.findOrdersByProductId(productId);
    		model.addAttribute("orders", orders);
    	}
        return orderHistoryView; 
    }

    @RequestMapping(value = "/{orderNumber}", method = RequestMethod.GET)
    public String viewOrderDetails(HttpServletRequest request, Model model, @PathVariable("orderNumber") String orderNumber) {
        return super.viewOrderDetails(request, model, orderNumber);
    }

    @RequestMapping(value = "/complete/{orderNumber}", method = RequestMethod.GET)
    public String completeOrder(HttpServletRequest request, Model model, @PathVariable("orderNumber") String orderNumber) {
    	Order order = orderService.findOrderByOrderNumber(orderNumber);
        Order orderModel = orderService.completeOrder(order);
        if (orderModel == null) {
            throw new IllegalArgumentException("The orderNumber provided is not valid");
        }
        return "redirect:/account/orders";
    }
    
    @RequestMapping(value = "/cancel/{orderNumber}", method = RequestMethod.GET)
    public String cancelOrder(HttpServletRequest request, Model model, @PathVariable("orderNumber") String orderNumber) {
    	Order order = orderService.findOrderByOrderNumber(orderNumber);
        Order orderModel = orderService.cancelOrderByDoctor(order);
        if (orderModel == null) {
            throw new IllegalArgumentException("The orderNumber provided is not valid");
        }
        return "redirect:/account/orders";
    }

}
