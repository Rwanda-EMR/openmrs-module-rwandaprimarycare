<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="resources/touchscreenHeader.jsp"%>

<openmrs:globalProperty var="skipInsuranceInfo" key="registration.skipInsuranceInformation"  />
<form method="get">
<table>

<c:if test="${skipInsuranceInfo == 'false'}">

	<tr><td></td>
		<td>
			
			<select optional="false" name="insuranceType" label="<spring:message code='rwandaprimarycare.touchscreen.insuranceType'/>" helpText="<spring:message code='rwandaprimarycare.touchscreen.insuranceType'/>">
					<c:forEach var="answer" items="${patientInsuranceTypes}">
						<option value="${answer.conceptId}"
							<c:if test="${!empty answer && !empty mostRecentType && answer == mostRecentType}"> SELECTED </c:if>
						>${answer.name}</option>
					</c:forEach>
			</select>
		</td>
	</tr>
	<tr>
		<td></td>
		<%-- <td>
			<c:set var="insStr"><spring:message code='rwandaprimarycare.touchscreen.insuranceNumber'/></c:set>
			<touchscreen:textInput   required="false" label="${insStr}"  field_name="insuranceNumber" value="${mostRecentInsuranceNumber}" />
		</td> --%>

		<td>
        		<select option="false" name="insuranceNumber" label="<spring:message code='rwandaprimarycare.touchscreen.insuranceNumber'/>" helpText="<spring:message code='rwandaprimarycare.touchscreen.insuranceNumber'/>">
        		<c:forEach var="patientPolicy" items="${mostRecentInsuranceNumber}">
        		<option value="${patientPolicy.getInsuranceCardNo()}">${patientPolicy.getInsuranceCardNo()}</option>
        		</c:forEach>
        		</select>
        		</td>

	</tr>
</c:if>
	<tr>
		<td></td>
		<td>
			<input type="hidden" value="${patient.patientId}" name="patientId"/>
			<input type="hidden" value="${visitDate}" name="visitDate"/>
			<input type="submit" value="Done"/>
		</td>
	</tr>
</table>
</form>

<%@ include file="resources/touchscreenFooter.jsp"%>