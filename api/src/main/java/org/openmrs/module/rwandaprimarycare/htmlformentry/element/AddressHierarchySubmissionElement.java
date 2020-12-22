/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.rwandaprimarycare.htmlformentry.element;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.api.context.Context;
import org.openmrs.module.htmlformentry.FormEntryContext;
import org.openmrs.module.htmlformentry.FormEntryContext.Mode;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.htmlformentry.FormSubmissionError;
import org.openmrs.module.htmlformentry.action.FormSubmissionControllerAction;
import org.openmrs.module.htmlformentry.element.HtmlGeneratorElement;
import org.openmrs.module.htmlformentry.widget.ErrorWidget;
import org.openmrs.module.htmlformentry.widget.Widget;
import org.openmrs.module.rwandaprimarycare.htmlformentry.widget.AddressHierarchyWidget;
import org.openmrs.web.WebConstants;

/**
 * Holds a list of widgets which represent the address hierarchy, and serves as both the HtmlGeneratorElement 
 * and the FormSubmissionControllerAction
 */
public class AddressHierarchySubmissionElement implements HtmlGeneratorElement,
		FormSubmissionControllerAction {

	protected final Log log = LogFactory.getLog(AddressHierarchySubmissionElement.class);
	
	public static final String FIELD_TYPES = "types";
	
	private List<Widget> addressWidgetList;
	private ErrorWidget errorWidget = null;
    
	public AddressHierarchySubmissionElement(FormEntryContext context, HttpServletRequest request,
	    Map<String, String> parameters) {
		Patient existingPatient = context.getExistingPatient();
		addressWidgetList = new ArrayList<Widget>();
		errorWidget = new ErrorWidget();
		
		// check parameter types (optional)
		String types = parameters.get(FIELD_TYPES);
		if (types != null && types.length() > 1) {
			StringTokenizer  tokenizer = new StringTokenizer(types, ",");
			while (tokenizer.hasMoreElements()) {
				String element = ((String) tokenizer.nextElement()).trim();
				if (element.equalsIgnoreCase(AddressHierarchyWidget.TYPE_COUNTRY)
				        || element.equalsIgnoreCase(AddressHierarchyWidget.TYPE_PROVINCE)
				        || element.equalsIgnoreCase(AddressHierarchyWidget.TYPE_DISTRICT)
				        || element.equalsIgnoreCase(AddressHierarchyWidget.TYPE_SECTOR)
				        || element.equalsIgnoreCase(AddressHierarchyWidget.TYPE_CELL)
				        || element.equalsIgnoreCase(AddressHierarchyWidget.TYPE_UMUDUGUDU)) {
					addressWidgetList.add(new AddressHierarchyWidget(element, existingPatient.getPersonAddress()));
				} else {
					throw new IllegalArgumentException("You must provide a valid type for example country in " + parameters);
				}
            }
		} else {
			addressWidgetList.add(new AddressHierarchyWidget(AddressHierarchyWidget.TYPE_COUNTRY, existingPatient.getPersonAddress()));
			addressWidgetList.add(new AddressHierarchyWidget(AddressHierarchyWidget.TYPE_PROVINCE, existingPatient.getPersonAddress()));
			addressWidgetList.add(new AddressHierarchyWidget(AddressHierarchyWidget.TYPE_DISTRICT, existingPatient.getPersonAddress()));
			addressWidgetList.add(new AddressHierarchyWidget(AddressHierarchyWidget.TYPE_SECTOR, existingPatient.getPersonAddress()));
			addressWidgetList.add(new AddressHierarchyWidget(AddressHierarchyWidget.TYPE_CELL, existingPatient.getPersonAddress()));
			addressWidgetList.add(new AddressHierarchyWidget(AddressHierarchyWidget.TYPE_UMUDUGUDU, existingPatient.getPersonAddress()));
		}

		// Register widgets
		registerWidgetList(addressWidgetList, context);
		context.registerWidget(errorWidget);
	}

	/**
	 * @should return HTML snippet
	 * @see org.openmrs.module.htmlformentry.element.HtmlGeneratorElement#generateHtml(org.openmrs.module.htmlformentry.FormEntryContext)
	 */
	public String generateHtml(FormEntryContext context) {
		StringBuilder ret = new StringBuilder();
		
		ret.append("<script type=\"text/javascript\" src=\"/" + WebConstants.WEBAPP_NAME
		        + "/moduleResources/rwandaprimarycare/AddressHierarchy.js\"></script>");
		if (context.getMode() != Mode.VIEW) {
			ret.append(errorWidget.generateHtml(context));
		}
		ret.append("<table class=\"tableClass\"><tbody>");
		
		for (Widget widget : addressWidgetList) {
			AddressHierarchyWidget addressWidget =(AddressHierarchyWidget)widget;
			if (addressWidget != null) {
				ret.append("<tr>");
				ret.append(addressWidget.generateHtml(context));		
				ret.append("</tr>");
			}	        
        }

		ret.append("<tr><td>Structured:</td><td><span class=\"isstructured\" id=\"structured_\">--</span></td></tr></tbody></table>");
		
		return ret.toString();
	}

	/**
	 * handleSubmission modifies person address 
	 * @see org.openmrs.module.htmlformentry.action.FormSubmissionControllerAction#handleSubmission(org.openmrs.module.htmlformentry.FormEntrySession, HttpServletRequest)
	 */
	public void handleSubmission(FormEntrySession session, HttpServletRequest request) {
		Patient patient = (Patient) session.getSubmissionActions().getCurrentPerson();

		if (patient == null) {
			throw new RuntimeException("Person shouldn't be null");
		}

		FormEntryContext context = session.getContext();
		if (context.getMode() != Mode.VIEW) {

			PersonAddress currentAddress = patient.getPersonAddress();
			//  validate if current address equals new address
			if (currentAddressEqualsNewAddress(context, request, currentAddress)) {
				log.info("current address equals new address, no entities will be changed");
			} else {
				PersonAddress personAddress = new PersonAddress();
				Date today = new Date();
				personAddress.setDateCreated(today);
				personAddress.setCreator(Context.getAuthenticatedUser());
				if (personAddress.getUuid() == null)
					personAddress.setUuid(UUID.randomUUID().toString());
				for (Widget widget : addressWidgetList) {
					AddressHierarchyWidget addressWidget =(AddressHierarchyWidget)widget;

					if (addressWidget.getType().equals(AddressHierarchyWidget.TYPE_COUNTRY)) {
						personAddress.setCountry(request.getParameter(context.getFieldName(addressWidget)).trim());
					}
					if (addressWidget.getType().equals(AddressHierarchyWidget.TYPE_PROVINCE)) {
						personAddress.setStateProvince(request.getParameter(context.getFieldName(addressWidget)).trim());
					}
					if (addressWidget.getType().equals(AddressHierarchyWidget.TYPE_DISTRICT)) {
						personAddress.setCountyDistrict(request.getParameter(context.getFieldName(addressWidget)).trim());
					}
					if (addressWidget.getType().equals(AddressHierarchyWidget.TYPE_SECTOR)) {
						personAddress.setCityVillage(request.getParameter(context.getFieldName(addressWidget)).trim());
					}
					if (addressWidget.getType().equals(AddressHierarchyWidget.TYPE_CELL)) {
						personAddress.setAddress3(request.getParameter(context.getFieldName(addressWidget)).trim());
					}
					if (addressWidget.getType().equals(AddressHierarchyWidget.TYPE_UMUDUGUDU)) {
						personAddress.setAddress1(request.getParameter(context.getFieldName(addressWidget)).trim());
					}
				}
				if (currentAddress != null) {
					currentAddress.setPreferred(false);
					currentAddress.setVoided(true);
					currentAddress.setVoidedBy(Context.getAuthenticatedUser());
					currentAddress.setDateVoided(today);
					currentAddress.setDateChanged(today);
					currentAddress.setChangedBy(Context.getAuthenticatedUser());
				}
				personAddress.setPreferred(true);
				patient.addAddress(personAddress);
				log.info("new address added to patient " + personAddress);
			}
		}
	}

	/**
	 * @should return validation errors
	 * @see org.openmrs.module.htmlformentry.action.FormSubmissionControllerAction#validateSubmission(org.openmrs.module.htmlformentry.FormEntryContext,
	 *      HttpServletRequest)
	 */
	public Collection<FormSubmissionError> validateSubmission(FormEntryContext context, HttpServletRequest request) {
		List<FormSubmissionError> ret = new ArrayList<FormSubmissionError>();
		return ret;
	}


	/**
	 * Returns if current address equals new address
	 * 
	 * @param context
	 * @param request
	 * @param currentAddress
	 * @return true if current address equals new address
	 */
	private boolean currentAddressEqualsNewAddress(FormEntryContext context, HttpServletRequest request,
	                                               PersonAddress currentAddress) {
		
		boolean equals = true;
		if (currentAddress == null) {
			return false;
		}
	    for (Widget widget: addressWidgetList) {
			AddressHierarchyWidget addressWidget =(AddressHierarchyWidget)widget;
			if (addressWidget.getType().equals(AddressHierarchyWidget.TYPE_COUNTRY)) {
				if (currentAddress.getCountry()!=null && !currentAddress.getCountry().trim().equalsIgnoreCase(request.getParameter(context.getFieldName(addressWidget)).trim())) {
					equals = false;
					break;
				}
			}
			if (addressWidget.getType().equals(AddressHierarchyWidget.TYPE_PROVINCE)) {
				if (currentAddress.getStateProvince()!=null && !currentAddress.getStateProvince().trim().equalsIgnoreCase(request.getParameter(context.getFieldName(addressWidget)).trim())) {
					equals = false;
					break;
				}
			}
			if (addressWidget.getType().equals(AddressHierarchyWidget.TYPE_DISTRICT)) {
				if (currentAddress.getCountyDistrict()!=null && !currentAddress.getCountyDistrict().trim().equalsIgnoreCase(request.getParameter(context.getFieldName(addressWidget)).trim())) {
					equals = false;
					break;
				}
			}
			if (addressWidget.getType().equals(AddressHierarchyWidget.TYPE_SECTOR)) {
				if (currentAddress.getCityVillage()!=null && !currentAddress.getCityVillage().trim().equalsIgnoreCase(request.getParameter(context.getFieldName(addressWidget)).trim())) {
					equals = false;
					break;
				}
			}
			if (addressWidget.getType().equals(AddressHierarchyWidget.TYPE_CELL)) {
				if (currentAddress.getAddress3()!=null && !currentAddress.getAddress3().trim().equalsIgnoreCase(request.getParameter(context.getFieldName(addressWidget)).trim())) {
					equals = false;
					break;
				}
			}
			if (addressWidget.getType().equals(AddressHierarchyWidget.TYPE_UMUDUGUDU)) {
				if (currentAddress.getAddress1()!=null && !currentAddress.getAddress1().trim().equalsIgnoreCase(request.getParameter(context.getFieldName(addressWidget)).trim())) {
					equals = false;
					break;
				}
			}
        }
		
	    return equals;
    }
	
	protected void registerWidgetList(List<Widget> widgetList, FormEntryContext context) {
		for (Widget widget : widgetList) {
			context.registerWidget(widget);
        }
	}

}
