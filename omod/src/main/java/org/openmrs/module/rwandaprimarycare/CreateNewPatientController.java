package org.openmrs.module.rwandaprimarycare;

import com.google.gson.JsonObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import static org.springframework.util.StringUtils.hasText;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.IdentifierNotUniqueException;
import org.openmrs.api.context.Context;
import org.openmrs.module.idgen.IdentifierSource;
import org.openmrs.web.WebConstants;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/module/rwandaprimarycare/createNewPatient")
public class CreateNewPatientController {

    protected final Log log = LogFactory.getLog(getClass());
    private boolean isCommingFromNIDA = false;

    @RequestMapping(method = RequestMethod.GET)
    public String confirmIdNumber(
            @RequestParam(required = false, value = "addNationalIdentifier") String addNationalIdentifier,
            @RequestParam("givenName") String givenName,
            @RequestParam("familyName") String familyName,
            @RequestParam("gender") String gender,
            @RequestParam("age") Integer age,
            @RequestParam(required = false, value = "birthdateDay") Integer birthdateDay,
            @RequestParam(required = false, value = "birthdateMonth") Integer birthdateMonth,
            @RequestParam(required = false, value = "birthdateYear") Integer birthdateYear,
            @RequestParam("country") String country,
            @RequestParam("province") String province,
            @RequestParam("district") String district,
            @RequestParam("sector") String sector,
            @RequestParam("cell") String cell,
            @RequestParam("address1") String address1,
            @RequestParam("mothersName") String mothersName,
            @RequestParam("fathersName") String fathersName,
            @RequestParam("educationLevel") String educationLevel,
            @RequestParam("profession") String profession,
            @RequestParam("religion") String religion,
            @RequestParam("phoneNumber") String phoneNumber,
            HttpSession session, ModelMap map) throws PrimaryCareException {

        //LK: Need to ensure that all primary care methods only throw a PrimaryCareException
        //So that errors will be directed to a touch screen error page
        try {
            IdentifierSource is = PrimaryCareUtil.getPrimaryIdentifierTypeSource();
            if (is == null) {
                map.addAttribute("addIdentifier", "");
            } else {
                MessageSourceAccessor msa = new MessageSourceAccessor(Context.getMessageSourceService().getActiveMessageSource());
                map.addAttribute("addIdentifier", msa.getMessage("rwandaprimarycare.automaticallyassigned"));
                map.addAttribute("idSource", is.getId());
            }

            map.addAttribute("nationalIdIdentifierType", PrimaryCareUtil.getNationalIdIdentifierType());
        } catch (Exception e) {
            throw new PrimaryCareException(e);
        }
        return "/module/rwandaprimarycare/createNewPatient";
    }

    //TODO:  save all ID numbers -- need global properties for these...
    // save person address, using address hierarchy
    @RequestMapping(method = RequestMethod.POST)
    public String processSubmit(
            @RequestParam(required = false, value = "addNationalIdentifier") String addNationalIdentifier,
            @RequestParam(required = false, value = "addPCIdentifier") String addPCIdentifier,
            @RequestParam("givenNameCreate") String givenName,
            @RequestParam("familyNameCreate") String familyName,
            @RequestParam("gender") String gender, //NOTE: uses original search param, not 'Create' param
            @RequestParam("age") Integer age, //uses original search param
            @RequestParam(required = false, value = "birthdateDayCreate") Integer birthdateDay,
            @RequestParam(required = false, value = "birthdateMonthCreate") Integer birthdateMonth,
            @RequestParam(required = false, value = "birthdateYearCreate") Integer birthdateYear,
            @RequestParam("COUNTRY") String country,
            @RequestParam("PROVINCE") String province,
            @RequestParam("DISTRICT") String district,
            @RequestParam("SECTOR") String sector,
            @RequestParam("CELL") String cell,
            @RequestParam("UMUDUGUDU") String address1,
            @RequestParam("idSourceIdCreate") Integer sourceId,
            @RequestParam("mothersNameCreate") String mothersName,
            @RequestParam("fathersNameCreate") String fathersName,
            @RequestParam("educationLevelCreate") String educationLevel,
            @RequestParam("professionCreate") String profession,
            @RequestParam("religionCreate") String religion,
            @RequestParam("phoneNumberCreate") String phoneNumber,
            HttpSession session) throws PrimaryCareException {

        return createPatient(
                addNationalIdentifier,
                addPCIdentifier,
                givenName,
                familyName,
                gender,
                age,
                birthdateDay,
                birthdateMonth,
                birthdateYear,
                country,
                province,
                district,
                sector,
                cell,
                address1,
                sourceId,
                mothersName,
                fathersName,
                educationLevel,
                profession,
                religion,
                phoneNumber,

                session
        );
    }

    /*
        This function is the final one which create the patient from json object
        
     */
    public String createPatient(JsonObject patientObject, HttpSession session, boolean isNidaData) throws PrimaryCareException, ParseException {
        String addNationalIdentifier = patientObject.get("documentNumber").getAsString();
        String addPCIdentifier = "";//Will be generetd
        String givenName = patientObject.get("foreName").getAsString();
        String familyName = patientObject.get("surnames").getAsString();
        String gender = patientObject.get("sex").getAsString(); //NOTE: uses original search param = patientObject.get("PCIdentifier").getAsString(); not 'Create' param

        Date birthdate = new SimpleDateFormat("dd/MM/yyyy").parse(patientObject.get("dateOfBirth").getAsString());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(birthdate);
        Integer birthdateDay = calendar.get(Calendar.DAY_OF_MONTH);
        Integer birthdateMonth = calendar.get(Calendar.MONTH) + 1;
        Integer birthdateYear = calendar.get(Calendar.YEAR);

        Integer age = getAge(birthdate);

        String country = "Rwanda";//TODO Take into accounte other
        String province = patientObject.get("province").getAsString();
        String district = patientObject.get("district").getAsString();
        String sector = patientObject.get("sector").getAsString();
        String cell = patientObject.get("cell").getAsString();
        String address1 = patientObject.get("village").getAsString();//Village = Umudugou
        Integer sourceId = patientObject.get("villageId").getAsInt(); //TODO is this a villageId ?
        String mothersName = patientObject.get("motherNames").getAsString();
        String fathersName = patientObject.get("fatherNames").getAsString();
        String educationLevel = patientObject.get("educationLevel").getAsString();
        String profession = patientObject.get("profession").getAsString();
        String religion = patientObject.get("religion").getAsString();
        String phoneNumber = patientObject.get("phoneNumber").getAsString();

        isCommingFromNIDA = isNidaData;

        return createPatient(
                addNationalIdentifier,
                addPCIdentifier,
                givenName,
                familyName,
                gender,
                age,
                birthdateDay,
                birthdateMonth,
                birthdateYear,
                country,
                province,
                district,
                sector,
                cell,
                address1,
                sourceId,
                mothersName,
                fathersName,
                educationLevel,
                profession,
                religion,
                phoneNumber,


                session
        );
    }

    public int getAge(Date dateOfBirth) {
        int age = 0;
        Calendar born = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        if (dateOfBirth != null) {
            now.setTime(new Date());
            born.setTime(dateOfBirth);
            if (born.after(now)) {
                throw new IllegalArgumentException("Can't be born in the future");
            }
            age = now.get(Calendar.YEAR) - born.get(Calendar.YEAR);
            if (now.get(Calendar.DAY_OF_YEAR) < born.get(Calendar.DAY_OF_YEAR)) {
                age -= 1;
            }
        }
        return age;
    }

    /*
        This function is the final one which create the patient
     */
    public String createPatient(
            String addNationalIdentifier,
            String addPCIdentifier,
            String givenName,
            String familyName,
            String gender, //NOTE: uses original search param, not 'Create' param
            Integer age, //uses original search param
            Integer birthdateDay,
            Integer birthdateMonth,
            Integer birthdateYear,
            String country,
            String province,
            String district,
            String sector,
            String cell,
            String address1,
            Integer sourceId,
            String mothersName,
            String fathersName,
            String educationLevel,
            String profession,
            String religion,
            String phoneNumber,


            HttpSession session
    ) throws PrimaryCareException {
        //LK: Need to ensure that all primary care methods only throw a PrimaryCareException
        //So that errors will be directed to a touch screen error page
        try {
            if (!hasText(givenName) || !hasText(familyName) || !hasText(gender) || age == null) {
                throw new RuntimeException("Programming error: this shouldn't happen because params are required");
            }
            Patient newPatient = new Patient();
            newPatient.setGender(gender);
            {
                Calendar c = Calendar.getInstance();
                c.add(Calendar.YEAR, -age);
                c.add(Calendar.DATE, -183);
                if (birthdateDay != null) {
                    c.set(Calendar.DAY_OF_MONTH, birthdateDay);
                }
                if (birthdateMonth != null) {
                    c.set(Calendar.MONTH, birthdateMonth - 1);
                }
                if (birthdateYear != null) {
                    c.set(Calendar.YEAR, birthdateYear);
                }
                newPatient.setBirthdate(c.getTime());
                if (birthdateDay == null || birthdateMonth == null) {
                    newPatient.setBirthdateEstimated(true);
                }
                //parse national ID for year:
            }

            //captialize first letter of christian name
            givenName = capitalizeFirstLetterOfString(givenName);

            //capitalize after space character in name
            int pos = givenName.trim().indexOf(" ");
            if (pos > 0) {
                try {
                    if (givenName.charAt(pos + 1) != ' ') {
                        String firstPart = givenName.substring(0, pos + 1);
                        String secondPart = givenName.substring(pos + 1);
                        secondPart = capitalizeFirstLetterOfString(secondPart);
                        givenName = firstPart + secondPart;
                    }
                } catch (Exception ex) {
                    log.error("Exception ocurred: ", ex);
                }
            }

            PersonName pn = new PersonName(givenName, null, familyName);
            pn.setPreferred(true);
            newPatient.addName(pn);
            {
                PersonAddress pa = new PersonAddress();
                pa.setCountry(country);
                pa.setStateProvince(province);
                pa.setCountyDistrict(district);
                pa.setCityVillage(sector);
                pa.setNeighborhoodCell(cell);
                pa.setAddress1(address1);
                pa.setPreferred(true);
                newPatient.addAddress(pa);
            }

            //save Education Level person attribute type
            PersonAttributeType educationLevelAttributeType=Context.getPersonService().getPersonAttributeTypeByName(PrimaryCareConstants.GLOBAL_PROPERTY_EDUCATION_LEVEL_CONCEPT);
            PersonAttribute educationLevelAttribute=new PersonAttribute();
            educationLevelAttribute.setAttributeType(educationLevelAttributeType);
            educationLevelAttribute.setValue(educationLevel);
            newPatient.addAttribute(educationLevelAttribute);

            //save Profession person attribute type
            PersonAttributeType professionAttributeType=Context.getPersonService().getPersonAttributeTypeByName(PrimaryCareConstants.GLOBAL_PROPERTY_PROFESSION_CONCEPT);
            PersonAttribute professionAttribute=new PersonAttribute();
            professionAttribute.setAttributeType(professionAttributeType);
            professionAttribute.setValue(profession);
            newPatient.addAttribute(professionAttribute);

            //save Religion person attribute type
            PersonAttributeType religionAttributeType=Context.getPersonService().getPersonAttributeTypeByName(PrimaryCareConstants.GLOBAL_PROPERTY_RELIGION_CONCEPT);
            PersonAttribute religionAttribute=new PersonAttribute();
            religionAttribute.setAttributeType(religionAttributeType);
            religionAttribute.setValue(religion);
            newPatient.addAttribute(religionAttribute);

            //save Phone Number person attribute type
            PersonAttributeType phoneNumberAttributeType=Context.getPersonService().getPersonAttributeTypeByName(PrimaryCareConstants.GLOBAL_PROPERTY_PHONE_NUMBER_CONCEPT);
            PersonAttribute phoneNumberAttribute=new PersonAttribute();
            phoneNumberAttribute.setAttributeType(phoneNumberAttributeType);
            phoneNumberAttribute.setValue(phoneNumber);
            newPatient.addAttribute(phoneNumberAttribute);


            //new patients get a new ID:
            try {

                boolean newIdNeeded = true;
                //TODO:  i think this is wrong.  this could wreck sync?
                if (addPCIdentifier != null && !addPCIdentifier.equals("") && addPCIdentifier.length() > 3) {
                    //if the passed-in identifier looks like a valid id for the id type:
                    if (PrimaryCareUtil.isIdentifierStringAValidIdentifier(addPCIdentifier, PrimaryCareWebLogic.getCurrentLocation(session))) {
                        //if no patient already exists with this id.  TODO:  what if the patient already does exist??
                        if (Context.getPatientService().getPatients(null, addPCIdentifier, Collections.singletonList(PrimaryCareBusinessLogic.getPrimaryPatientIdentiferType()), true).size() == 0) {
                            //if the ID's 3 digits correspond to the current location:
                            if (addPCIdentifier.substring(0, 3).equals(PrimaryCareUtil.getPrimaryCareLocationCode())) {
                                PatientIdentifier pi = new PatientIdentifier(addPCIdentifier, PrimaryCareBusinessLogic.getPrimaryPatientIdentiferType(), PrimaryCareWebLogic.getCurrentLocation(session));
                                pi.setPreferred(true);
                                newPatient.addIdentifier(pi);
                                newIdNeeded = false;
                            } else {

                                //the location code is different than the local location code, i.e. the id is from somwhere else and is not in the db
                                //case:  we can find a location out of our location list for this id:
                                Location thisIdsLocation = PrimaryCareUtil.getPrimaryCareLocationFromCodeList(addPCIdentifier.substring(0, 3));
                                if (thisIdsLocation != null) {
                                    PatientIdentifier pi = new PatientIdentifier(addPCIdentifier, PrimaryCareBusinessLogic.getPrimaryPatientIdentiferType(), thisIdsLocation);
                                    pi.setPreferred(true);
                                    newPatient.addIdentifier(pi);
                                } else {
                                    //case:  we can't find a location out of our location list for this id:
                                    //TODO:  when id.location stops being mandatory, we can create...
                                    // for now, pass
                                }

                            }

                        }
                    }
                }
                if (newIdNeeded) {
                    String addIdentifier = PrimaryCareBusinessLogic.getNewPrimaryIdentifierString();
                    PatientIdentifier pi = new PatientIdentifier(addIdentifier, PrimaryCareBusinessLogic.getPrimaryPatientIdentiferType(), PrimaryCareWebLogic.getCurrentLocation(session));
                    pi.setPreferred(true);
                    newPatient.addIdentifier(pi);
                }

            } catch (Exception ex) {
                log.error("Exception ocurred: ", ex);
                throw new RuntimeException("Couldn't generate new ID.  Check idgen settings.");
            }

            PersonAttributeType pat = PrimaryCareUtil.getHealthCenterAttributeType();
            //TODO:  is this right -- verify with Cheryl?
            if (pat != null) {
                PersonAttribute pa = PrimaryCareUtil.newPersonAttribute(pat, PrimaryCareWebLogic.getCurrentLocation(session).getLocationId().toString(), newPatient);
                newPatient.addAttribute(pa);
            }

            if (addNationalIdentifier != null && !addNationalIdentifier.trim().equals("")) {
                //PatientIdentifierType natIdType = PrimaryCareUtil.getNationalIdIdentifierType();
                PatientIdentifierType natIdType = new PatientIdentifierType(5);//5 is the PatientIdentifierType id
                if (natIdType != null) {
                    newPatient.addIdentifier(new PatientIdentifier(PrimaryCareUtil.getIdNumFromNationalId(addNationalIdentifier),
                            natIdType, PrimaryCareWebLogic.getCurrentLocation(session)));

                    if (!isCommingFromNIDA) {
                        String givenNameNI = PrimaryCareUtil.getGivenNameFromNationalId(addNationalIdentifier);
                        String familyNameNI = PrimaryCareUtil.getFamilyNameFromNationalId(addNationalIdentifier);

                        //if the name the patient gave doens't match their ID card, create a secondary person name: 
                        if (!givenNameNI.equals(newPatient.getGivenName()) || familyNameNI.equals(newPatient.getFamilyName())) {
                            PersonName pnNI = new PersonName(givenNameNI, null, familyNameNI);
                            pnNI.setPreferred(false);
                            newPatient.addName(pnNI);
                        }
                    }
                }
            }
            try {
                PrimaryCareUtil.setupParentNames(newPatient, mothersName, fathersName);
                PrimaryCareBusinessLogic.preferredIdentifierSafeSavePatient(newPatient);
            } catch (IdentifierNotUniqueException ex) {
                MessageSourceAccessor msa = new MessageSourceAccessor(Context.getMessageSourceService().getActiveMessageSource());
                session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, msa.getMessage("rwandaprimarycare.idAlreadyUsed"));
                return "/module/rwandaprimarycare/createNewPatient";
            } catch (Exception ex) {
                log.error("Exception ocurred: ", ex);
                throw new RuntimeException(ex.getMessage());
            }

            return "redirect:/module/rwandaprimarycare/patient.form?skipPresentQuestion=false&patientId=" + newPatient.getPatientId();
        } catch (Exception e) {
            throw new PrimaryCareException(e);
        }
    }

    private String capitalizeFirstLetterOfString(String givenName) {
        if (givenName != null && givenName.length() > 0) {
            String firstLetter = givenName.substring(0, 1).toUpperCase();
            String rest = "";
            if (givenName.length() > 1) {
                rest = givenName.substring(1);
            }
            givenName = firstLetter + rest;
        }
        return givenName;
    }

}
