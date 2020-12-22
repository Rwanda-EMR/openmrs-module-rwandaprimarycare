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
package org.openmrs.module.rwandaprimarycare.htmlformentry.widget;

import org.openmrs.PersonAddress;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.htmlformentry.FormEntryContext;
import org.openmrs.module.htmlformentry.FormEntryContext.Mode;
import org.openmrs.module.htmlformentry.widget.TextFieldWidget;

/**
 * AddressHierarchyWidget adds a CSS class and a select input field which will interact with
 * the textfield
 */
public class AddressHierarchyWidget extends TextFieldWidget {

    public static final String TYPE_UMUDUGUDU = "Umudugudu";
    public static final String TYPE_CELL = "Cell";
    public static final String TYPE_SECTOR = "Sector";
    public static final String TYPE_DISTRICT = "District";
    public static final String TYPE_PROVINCE = "Province";
    public static final String TYPE_COUNTRY = "Country";

    private String type;
	
	/**
	 * @param type
	 * @param personAddress
	 * @should set type and initial Value
	 */
	public AddressHierarchyWidget(String type, PersonAddress personAddress) {
		this.type = type;
		
		if (personAddress != null) {
			if (type.equalsIgnoreCase(TYPE_COUNTRY)) {
				setInitialValue(personAddress.getCountry());
			} else if (type.equalsIgnoreCase(TYPE_PROVINCE)) {
				setInitialValue(personAddress.getStateProvince());
			} else if (type.equalsIgnoreCase(TYPE_DISTRICT)) {
				setInitialValue(personAddress.getCountyDistrict());
			} else if (type.equalsIgnoreCase(TYPE_SECTOR)) {
				setInitialValue(personAddress.getCityVillage());
			} else if (type.equalsIgnoreCase(TYPE_CELL)) {
				setInitialValue(personAddress.getAddress3());
			} else if (type.equalsIgnoreCase(TYPE_UMUDUGUDU)) {
				setInitialValue(personAddress.getAddress1());
			}
		}
	}
	
	/**
	 * @should return HTML snippet
	 * @see org.openmrs.module.htmlformentry.element.HtmlGeneratorElement#generateHtml(org.openmrs.module.htmlformentry.FormEntryContext)
	 */
	public String generateHtml(FormEntryContext context) {
		StringBuilder sb = new StringBuilder();
		MessageSourceService mss = Context.getMessageSourceService();
		String inputCssClass = "";
		String selectCssClass = "";
		String selectName = "";
			
		if (getInitialValue() == null)
			setInitialValue("");
		
		sb.append("<td>");
		if (type.equalsIgnoreCase(TYPE_COUNTRY)) {
			sb.append(mss.getMessage("Location.country") + "</td>");
			 inputCssClass = "countrySaveClass";
			 selectCssClass = "countryClass";
			 selectName = "countryselect";
		} else if (type.equalsIgnoreCase(TYPE_PROVINCE)) {
			sb.append(mss.getMessage("Location.stateProvince") + "</td>");
			 inputCssClass = "provinceSaveClass";
			 selectCssClass = "provinceClass";
			 selectName = "stateProvinceselect";
		} else if (type.equalsIgnoreCase(TYPE_DISTRICT)) {
			sb.append(mss.getMessage("Location.district") + "</td>");
			 inputCssClass = "districtSaveClass";
			 selectCssClass = "districtClass";
			 selectName = "countryDistrictselect";
		} else if (type.equalsIgnoreCase("sector")) {
			sb.append(mss.getMessage("Location.cityVillage") + "</td>");
			 inputCssClass = "sectorSaveClass";
			 selectCssClass = "sectorClass";
			 selectName = "cityVillageselect";
		} else if (type.equalsIgnoreCase("cell")) {
			sb.append(mss.getMessage("Location.cell") + "</td>");
			 inputCssClass = "cellSaveClass";
			 selectCssClass = "cellClass";
			 selectName = "cellselect";
		} else if (type.equalsIgnoreCase(TYPE_UMUDUGUDU)) {
			sb.append(mss.getMessage("Location.rwandanNeighborhood") + "</td>");
			 inputCssClass = "address1SaveClass";
			 selectCssClass = "address1Class";
			 selectName = "address1select";
		}		

		if (context.getMode().equals(Mode.VIEW)) {
			// hidden input field is included just to make the structured icon work (see AddressHierarchy.js)
			sb.append("<td><input type=\"hidden\" class=\"" + inputCssClass + "\" value=\"" + super.getInitialValue()
			        + "\"/>" + super.getInitialValue() + "</td>");
		} else {
			sb.append("<td><input type=\"text\" name=\"" + context.getFieldName(this) + "\"	value=\"" + getInitialValue().trim()
			        + " \" 	class=\"" + inputCssClass + "\" />" + "</td>	<td><select name=\""
			        + selectName + "\" class=\"" + selectCssClass + "\"></select></td>");
		}
		return sb.toString();
	}
	
    /**
     * @return the type
     */
    public String getType() {
    	return type;
    }

	
    /**
     * @param type the type to set
     */
    public void setType(String type) {
    	this.type = type;
    }
}
