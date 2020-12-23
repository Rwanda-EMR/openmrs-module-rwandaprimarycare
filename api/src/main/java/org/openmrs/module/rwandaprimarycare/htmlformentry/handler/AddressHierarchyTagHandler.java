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
package org.openmrs.module.rwandaprimarycare.htmlformentry.handler;

import java.util.Map;

import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.htmlformentry.FormSubmissionController;
import org.openmrs.module.htmlformentry.handler.SubstitutionTagHandler;
import org.openmrs.module.rwandaprimarycare.htmlformentry.element.AddressHierarchySubmissionElement;

/**
 * Handles the {@code <addressHierarchyRwanda>} tag <br/>
 * Usage: <br/>
 * - default <addressHierarchyRwanda /> shows all Rwanda relevant input fields <br/>
 * - to change the default add the types parameter e.g. <addressHierarchyRwanda types="country" /> <br/>
 * - this tag will include the script tag for AddressHierarchy.js
 * 
 * <br/> In OpenMRS 1.9 the tag could be moved to HTMLFormEntry module, see http://tickets.openmrs.org/browse/TRUNK-1924
 */
public class AddressHierarchyTagHandler extends SubstitutionTagHandler {
	
	@Override
	protected String getSubstitution(FormEntrySession session, FormSubmissionController controller,
	                                 Map<String, String> parameters) {
		
		AddressHierarchySubmissionElement element = new AddressHierarchySubmissionElement(session.getContext(),
		        controller.getLastSubmission(), parameters);
		session.getSubmissionController().addAction(element);
		
		return element.generateHtml(session.getContext());
	}
	
}
