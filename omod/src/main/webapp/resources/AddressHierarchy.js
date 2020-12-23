$j = jQuery.noConflict();


function getPatientAddress(patientId) {
	$j.getJSON(openmrsContextPath  + "/module/rwandaprimarycare/patientAddress.form", {
		patientId :patientId
	}, function(data) {
		var count = -1;
		$j.each(data.addresses, function(i, address) {
			count++;
			$j("#cnt_" + count).val(address.country);
			$j("#sp_" + count).val(address.stateProvince);
			$j("#cd_" + count).val(address.countyDistrict);
			$j("#cv_" + count).val(address.cityVillage);
			$j("#nc_" + count).val(address.address3);
			$j("#a1_" + count).val(address.address1);
			$j("#structured_" + count).text(address.structured);
		});

	});
}

//this is a hack for google chrome, for which :hidden selector doesn't work...
function isVisible(a){
	//this block is a hack for google chrome.  
	for (var i = 0 ; i < $j(a).parents().size(); i++){
		var x = $j(a).parents()[i];
		//if ($j(x).hasClass("tabBox") || ( $j(x).attr("id", "addressPortlet") && $j(x).is("div")) ){
			var style = new String($j(x).attr("style"));
			if (style.indexOf("display") > -1 && style.indexOf("none") > -1){
				return false;
			}
		//}
	}
	return true;
}

function changeLocation(parentLocationId, nextElement) {
	var locIdString = "" + parentLocationId;
	var parsedParentLocationId = locIdString.substring(3);
	$j.getJSON(openmrsContextPath  + "/module/rwandaprimarycare/locations.form", {
		locationId :parsedParentLocationId
	}, function(data) {
		var options = "<option value='--'>--</option>";
		$j.each(data.addresses, function(i, address) {
			options += "<option value='ah_" + address.id  + "' id = '" + address.id +"'>"+address.display+"</option>";
		});
		$j(nextElement).append(options);
		
		if ($j(nextElement).attr('class') != undefined){
			if (getSiblingWithinValueFromClass(nextElement,$j(nextElement).attr('class').replace("Class","SaveClass"))){
				
				//set selected value 
				var textBoxValue = getSiblingWithinValueFromClass($j(nextElement),$j(nextElement).attr('class').replace("Class","SaveClass"));
				textBoxValue = new String(textBoxValue);
				var op = $j(nextElement).children('option').filter(function() {
					if (($j(this).text().trim() + "") == textBoxValue && $j(this).val() != "--" && isVisible(this)) {  //hidden works in firefox, fails in chrome && !$j(this).is(":hidden")
				    	return this;
				    }
				});
				$j(op[0]).attr("selected", "selected");
				var list = $j(nextElement).find("option:selected").filter(function(){
					if ($j(this).val() != '--' && isVisible(this) ) return this;  //same chrome problem
				});
				if ($j(list).text() == getSiblingWithinValueFromClass($j(nextElement),$j(nextElement).attr('class').replace("Class","SaveClass"))){
					var nextSelect = findNextSelect(nextElement);
					if (nextSelect != null)
						setTimeout(function() {changeLocation($j(nextElement).val(),$j(nextSelect))}, 10);
				}
			}
		}	
	});
}

function findNextSelect(nextElement){
	var inputs = $j(nextElement).closest("table:visible").find(':input:visible');
	var winningPos = null;
	for (var i = 0; i < inputs.length - 2; i++){
		if ($j(inputs[i]).attr("name") == $j(nextElement).attr("name")){
			winningPos = i + 2;
			break;
		}	
	}
	if (winningPos != null)
		return $j(inputs[winningPos]);
}


function validateSingleAddress(someElement,country, province, district, sector, cell, umudugudu, targetElement){
	//alert(country + province + district + sector + cell + umudugudu);
	$j.getJSON(openmrsContextPath+"/module/rwandaprimarycare/ahValidateAddress.form", {
		country:country, province:province, district:district, sector:sector, cell:cell, umudugudu:umudugudu
		},  function (json){
			$j.each(json.values, function(i, value) {
				if(value.value == 1){
					$j(someElement).empty();
					$j(someElement).append($j(document.createElement("img")).attr("src",openmrsContextPath+"/images/checkmark.png"));
				} else {
					$j(someElement).empty();
					$j(someElement).append($j(document.createElement("img")).attr("src",openmrsContextPath+"/images/error.gif"));
				}
			});
		});
	}

function updateStructuredIcon(data){
	alert(data);
}


function validateAddressesOnPage(){
	
	var structuredElements = $j(".isstructured");
	var country, province, district, sector, cell, umudugudu;
	
	for(var i = 0; i < structuredElements.length; i++){
		
		country = getSiblingWithinValueFromClass(structuredElements[i],"countrySaveClass");
		province = getSiblingWithinValueFromClass(structuredElements[i],"provinceSaveClass");
		district = getSiblingWithinValueFromClass(structuredElements[i],"districtSaveClass");
		sector = getSiblingWithinValueFromClass(structuredElements[i],"sectorSaveClass");
		cell = getSiblingWithinValueFromClass(structuredElements[i],"cellSaveClass");
		umudugudu = getSiblingWithinValueFromClass(structuredElements[i],"address1SaveClass");

		if (country, province, district, sector, cell, umudugudu)
			validateSingleAddress(structuredElements[i],country, province, district, sector, cell, umudugudu, null);

	}
}

function getSiblingWithinValueFromClass(someElement,className){
	
	var td = $j(someElement).closest("table:visible")
	.children("tbody")
	.children("tr")
	.children("td");
	//alert("returning " + $j(td).children("."+className).val());
	return $j(td).children("."+className).val().trim();
}

function getSiblingWithinTableFromClass(someElement,className){
	
	var td = $j(someElement).closest("table:visible")
	.children("tbody:visible")
	.children("tr:visible")
	.children("td:visible");
	return $j(td).children("."+className);
}

function validateAddressOnChange(targetElement){
	
	var structuredElement = getSiblingWithinTableFromClass(targetElement,"isstructured");
	
	var country = getSiblingWithinValueFromClass(structuredElement,"countrySaveClass");
	var province = getSiblingWithinValueFromClass(structuredElement,"provinceSaveClass");
	var district = getSiblingWithinValueFromClass(structuredElement,"districtSaveClass");
	var sector = getSiblingWithinValueFromClass(structuredElement,"sectorSaveClass");
	var cell = getSiblingWithinValueFromClass(structuredElement,"cellSaveClass");
	var umudugudu = getSiblingWithinValueFromClass(structuredElement,"address1SaveClass");
	
	validateSingleAddress(structuredElement, country, province, district, sector, cell, umudugudu, targetElement);
}


$j(document).ready(

		function() {
			
			$j(".countryClass").live(
					"change",
					function() {
						if ($j(this).val() != "--") {
							$j(".provinceClass:visible").children().remove();
							$j(".districtClass:visible").children().remove();
							$j(".sectorClass:visible").children().remove();
							$j(".cellClass:visible").children().remove();
							$j(".address1Class:visible").children().remove();
							changeLocation($j(this).val(),
									$j(".provinceClass:visible"));
							$j(".countrySaveClass:visible").val(
									$j(this).children(":selected").text());
						}
						validateAddressOnChange(this);
					});

			$j(".provinceClass").live(
					"change",
					function() {
						if ($j(this).val() != "--") {
							$j(".districtClass:visible ").children().remove();
							$j(".sectorClass:visible ").children().remove();
							$j(".cellClass:visible").children().remove();
							$j(".address1Class:visible").children().remove();
							changeLocation($j(this).val(),
									$j(".districtClass:visible"));
							$j(".provinceSaveClass:visible").val(
									$j(this).children(":selected").text());
						}
						validateAddressOnChange(this);
					});

			$j(".districtClass").live(
					"change",
					function() {
						if ($j(this).val() != "--") {
							$j(".sectorClass:visible").children().remove();
							$j(".cellClass:visible").children().remove();
							$j(".address1Class:visible").children().remove();
							changeLocation($j(this).val(),
									$j(".sectorClass:visible"));
							$j(".districtSaveClass:visible").val(
									$j(this).children(":selected").text());
						}
						validateAddressOnChange(this)
					});

			$j(".sectorClass").live(
					"change",
					function() {
						if ($j(this).val() != "--") {
							$j(".cellClass:visible").children().remove();
							$j(".address1Class:visible").children().remove();
							changeLocation($j(this).val(),
									$j(".cellClass:visible"));
							$j(".sectorSaveClass:visible").val(
									$j(this).children(":selected").text());
						}
						validateAddressOnChange(this)
					});

			$j(".cellClass").live(
					"change",
					function() {
						if ($j(this).val() != "--") {
							$j(".address1Class:visible").children().remove();
							changeLocation($j(this).val(),
									$j(".address1Class:visible"));
							$j(".cellSaveClass:visible").val(
									$j(this).children(":selected").text());
						}
						validateAddressOnChange(this)
					});

			$j(".address1Class").live(
					"change",
					function() {
						
						if ($j(this).val() != "--") {
							$j(".address1SaveClass:visible").val(
									$j(this).children(":selected").text());
						}
						validateAddressOnChange(this);
					});
			
			
			
			
			// ===================== handlers for changes in the text box ================== //
			$j(".address1SaveClass").live(
					"keyup",
					function() {
						validateAddressOnChange(this);
					});
			
			$j(".cellSaveClass").live(
					"keyup",
					function() {
						validateAddressOnChange(this);
					});
			
			$j(".sectorSaveClass").live(
					"keyup",
					function() {
						validateAddressOnChange(this);
					});
			$j(".districtSaveClass").live(
					"keyup",
					function() {
						validateAddressOnChange(this);
					});

			$j(".provinceSaveClass").live(
					"keyup",
					function() {
						validateAddressOnChange(this);
					});
			$j(".countrySaveClass").live(
					"keyup",
					function() {
						validateAddressOnChange(this);
					});
			
			
			changeLocation(-1, $j(".countryClass"));
			validateAddressesOnPage();		
			
			$j(".voided").live(
					"change",
					function() {
						var voidedReasonRow = $j(this).closest("table")
								.siblings("table").children("tbody").children(
										".voidedReasonRowClass");
						if (voidedReasonRow.css("display") == "none") {
							voidedReasonRow.show("fast");
						} else {
							voidedReasonRow.hide("fast");
						}
					});

			
		});
