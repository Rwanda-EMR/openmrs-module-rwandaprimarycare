package org.openmrs.module.rwandaprimarycare;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
import org.openmrs.module.addresshierarchy.AddressValidator;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HtmlFormEntryAddressHierarchyController {
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Gets the addresses for the patient specified in the patientId request parameter.
	 * {
	 * 	 "results": 2,
	 * 	 "rows": [
	 *     { "id": 1, "firstname": "Bill", occupation: "Gardener" },         // a row object
	 *     { "id": 2, "firstname": "Ben" , occupation: "Horticulturalist" }  // another row object
	 * 	 ]
	 * }
	 *
	 */
	@RequestMapping("/module/rwandaprimarycare/patientAddress.form")
	protected void patientAddress(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("patientId") Integer patientId) throws Exception {
		Set<PersonAddress> addressSet = null;
		Map<String, Set<PersonAddress>> map = new HashMap<String,Set<PersonAddress>>();

		Patient patient = Context.getPatientService().getPatient(patientId);
		if (patient != null) {
			addressSet = patient.getAddresses();
			map.put("1", addressSet);

			Collection<Set<PersonAddress>> addressList = map.values();
			PersonAddress pa = null;
			AddressValidator av = new AddressValidator();
			StringBuffer sb = new StringBuffer();
			sb.append("{");
			Iterator<Set<PersonAddress>> setIterator= addressList.iterator();
			Iterator<PersonAddress> addressIterator = null;
			if(setIterator.hasNext()){
				addressIterator = setIterator.next().iterator();
			}
			sb.append("\"addresses\":");
			sb.append("[");
			while(addressIterator != null && addressIterator.hasNext()){
				sb.append("{");
				pa = addressIterator.next();
				if (pa != null) {
					sb.append("\"structured\":");
					sb.append("\""+av.isAddressStructured(pa)+"\"");
					sb.append(",");

					sb.append("\"country\":");
					sb.append("\""+pa.getCountry()+"\"");
					sb.append(",");

					sb.append("\"stateProvince\":");
					sb.append("\""+pa.getStateProvince()+"\"");
					sb.append(",");

					sb.append("\"countyDistrict\":");
					sb.append("\""+pa.getCountyDistrict()+"\"");
					sb.append(",");

					sb.append("\"cityVillage\":");
					sb.append("\""+pa.getCityVillage()+"\"");
					sb.append(",");

					sb.append("\"address3\":");
					sb.append("\""+pa.getAddress3()+"\"");
					sb.append(",");

					sb.append("\"address1\":");
					sb.append("\""+pa.getAddress1()+"\"");
					sb.append("");

					sb.append("}");
					if(addressIterator.hasNext()){
						sb.append(",");
					}
				}
			}
			sb.append("]");

			sb.append("}");

			response.setContentType("text/html");
			OutputStreamWriter osw = new OutputStreamWriter(response.getOutputStream());
			osw.write(sb.toString());
			osw.flush();
		}
	}

	@RequestMapping("/module/rwandaprimarycare/locations.form")
	protected void locations(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "locationId", required = false) Integer locationId) throws Exception {

		AddressHierarchyService ahs = Context.getService(AddressHierarchyService.class);
		List<AddressHierarchyEntry> locationList;

		if (locationId == null || locationId == -1){
			locationList = ahs.getTopOfHierarchyList();
		}
		else {
			locationList = ahs.getNextComponent(locationId);
		}

		Map<String, List<AddressHierarchyEntry>> map = new HashMap<String,List<AddressHierarchyEntry>>();
		map.put(Integer.toString(1), locationList);

		AddressHierarchyEntry ah = null;

		StringBuffer sb = new StringBuffer();
		sb.append("{");
		if (map.values().iterator().hasNext()){
			List<AddressHierarchyEntry> currList = map.values().iterator().next();
			sb.append("\"addresses\":");
			sb.append("[");
			Iterator<AddressHierarchyEntry> locIterator = currList.iterator();
			while(locIterator.hasNext()){
				sb.append("{");
				ah = locIterator.next();

				sb.append("\"id\":");
				String idString = "";
				Integer tempId = -1;
				tempId = ah.getAddressHierarchyEntryId();
				if(tempId!=null && tempId != -1){
					idString = tempId.toString();

				}
				sb.append("\""+idString+"\"");
				sb.append(",");

				sb.append("\"type\":");
				AddressHierarchyLevel type = ah.getLevel();
				String typeName = "";
				if(type!=null){
					typeName = type.getName();
				}
				sb.append("\""+typeName+"\"");
				sb.append(",");

				String childTypeName = "";
				AddressHierarchyLevel tempChildType = ahs.getChildAddressHierarchyLevel(type);
				if(tempChildType != null){
					childTypeName = tempChildType.getName();
				}
				sb.append("\"childType\":");
				sb.append("\""+childTypeName+"\"");
				sb.append(",");
				sb.append("\"display\":");
				sb.append("\""+ah.getLocationName()+"\"");
				sb.append("}");
				if(locIterator.hasNext()){
					sb.append(",");
				}
			}
			sb.append("]");
		}
		sb.append("}");

		response.setContentType("text/html");
		OutputStreamWriter osw = new OutputStreamWriter(response.getOutputStream());
		osw.write(sb.toString());
		osw.flush();
	}

	@RequestMapping("/module/rwandaprimarycare/ahValidateAddress.form")
	public void ahValidateAddress(HttpServletRequest request, HttpServletResponse response) throws Exception {
		PersonAddress pa = new PersonAddress();

		pa.setCountry(ServletRequestUtils.getStringParameter(request, "country", null).trim());
		pa.setStateProvince(ServletRequestUtils.getStringParameter(request, "province", null).trim());
		pa.setCountyDistrict(ServletRequestUtils.getStringParameter(request, "district", null).trim());
		pa.setCityVillage(ServletRequestUtils.getStringParameter(request, "sector", null).trim());
		pa.setAddress3(ServletRequestUtils.getStringParameter(request, "cell", null).trim());
		pa.setAddress1(ServletRequestUtils.getStringParameter(request, "umudugudu", null).trim());
		boolean validity = new AddressValidator().isAddressStructured(pa);

		HashMap<String, Integer> validityMap = new HashMap<String, Integer>();
		if (validity) {
			validityMap.put("1", 1);
		} else {
			validityMap.put("1", 0);
		}

		Collection<Integer> list = validityMap.values();

		StringBuffer sb = new StringBuffer();
		sb.append("{");
		sb.append("\"values\":");
		sb.append("[");
		int  lastPass = list.size()-1;
		int count = 0;
		for(Integer i:list) {
			sb.append("{");
			sb.append("\"value\":");
			sb.append("\"" + i + "\"");
			sb.append("}");
			if (lastPass > count)
				sb.append(",");
			count++;
		}
		sb.append("]");
		sb.append("}");

		response.setContentType("text/html");
		OutputStreamWriter osw = new OutputStreamWriter(response.getOutputStream());
		osw.write(sb.toString());
		osw.flush();
	}
}
