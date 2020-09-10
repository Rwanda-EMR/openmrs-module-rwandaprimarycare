package org.openmrs.module.rwandaprimarycare.dwr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;

public class PrimaryCareDWRService {

    
    public Integer getAddressHierarchyId(Integer parentId, String searchString){
        Integer ret = null;
        if (Context.getAuthenticatedUser() != null && parentId != null && searchString != null && !searchString.equals("")){
            AddressHierarchyService ahs = Context.getService(AddressHierarchyService.class);
            if (parentId.intValue() == 0){
                List<AddressHierarchyEntry> topList = ahs.getTopOfHierarchyList();
                for (AddressHierarchyEntry ah : topList){
                    if (ah != null && ah.getLocationName().equals(searchString))
                        return ah.getAddressHierarchyEntryId();
                }
            } else {
                
                    List<AddressHierarchyEntry> childList = ahs.getNextComponent(parentId);
                    for (AddressHierarchyEntry ahTmp : childList){
                        if (ahTmp != null && ahTmp.getLocationName().equals(searchString))
                            return ahTmp.getAddressHierarchyEntryId();
                    }
                    
                
            }
        }
        return ret;
    }
    
    public String getMessage(String message){
    	return Context.getMessageSourceService().getMessage(message);
    }
    
    public Map<String,String> getMessages(List<String> messageCodes, String prefix){
    	
    	Map<String,String> messages = new HashMap<String,String>();
    	
    	MessageSourceService messageSourceService = Context.getMessageSourceService();
    	
    	for(String message:messageCodes){
    		if(prefix != null && StringUtils.isNotEmpty(prefix)){
    			messages.put(message, messageSourceService.getMessage(prefix + "." + message));
    		}
    		else{
    			messages.put(message, messageSourceService.getMessage(message));
    		}
    	}
    		
    	return messages;
    }
}
