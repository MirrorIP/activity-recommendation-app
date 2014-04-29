/**
 * JavaScript module for the MIRROR Spaces SDK.
 * @namespace SpacesSDK
 */
var SpacesSDK = (function() {
	/**
	 * Version of the SDK. This value is set when the SDK is packaged.
	 * @memberOf SpacesSDK 
	 */
	var VERSION = '1.3.2-SNAPSHOT';
	
	/**
	 * Collection of utility functions for the SDK to be used internally.
	 * @name Utils
	 * @namespace Utils
	 * @private
	 */
	var Utils = (function() {
		var getISO8601ForDate = function(date) {
			function pad(number) {
				var r = String(number);
				if ( r.length === 1 ) {
					r = '0' + r;
				}
				return r;
			}
			var isoString = date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate());
			isoString += 'T' + pad(date.getHours()) + ':' + pad(date.getMinutes()) + ':' + pad(date.getSeconds()) + '.' + String((date.getMilliseconds()/1000).toFixed(3)).slice(2,5);
			var offset = - date.getTimezoneOffset();
			isoString += (offset >= 0 ? '+' : '-') + pad(Math.floor(offset / 60)) + ':' + pad(offset % 60);
			
			return isoString;
		};
		
		var getDateForISO8601 = function(dateString) {
			var rx=/^(\d{4}\-\d\d\-\d\d([tT][\d:\.]*)?)([zZ]|([+\-])(\d\d):(\d\d))?$/;
			var p = rx.exec(dateString) || [];
			if (p[1]) {
				var day= p[1].split(/\D/);
				for(var i = 0; i < day.length; i++) {
					day[i] = parseInt(day[i], 10) || 0;
				}
				day[1] -= 1;
				if (p[5]) {
					var tz = (parseInt(p[5], 10) * 60);
					if (p[6]) tz += parseInt(p[6], 10);
					if (p[4] == '+') tz *= -1;
					if (tz) day[4] += tz;
				}
				var date = new Date(Date.UTC(day[0], day[1], day[2], day[3], day[4], day[5]));
				if (day[6]) date.setMilliseconds(day[6]);
				return date;
			} else {
				return NaN;	       	
			}
		};
		
		/**
		 * Trims a string, i.e., removes heading and tailing whitespaces. 
		 * @memberOf Utils
		 * @function
		 * @private
		 * @param {string} str String to trim.
		 * @return {string} Trimmed string.
		 */
		var trimString = function(str) {
			return str.replace(/^\s+|\s+$/g, '');
		};
		
		/**
		 * Generates an XML DOM document based on the given XML string.
		 * Supports both IE and other browsers.
		 * @memberOf Utils
		 * @function
		 * @private
		 * @param {string} xmlString XML string to parse.
		 * @return {Document} XML DOM document object.
		 */
		var generateXMLDocumentFromString = function(xmlString) {
			var xmlDoc;
			if (window.DOMParser){
				var parser = new DOMParser();
			  	xmlDoc = parser.parseFromString(xmlString, 'text/xml');
			} else { // Internet Explorer
				xmlDoc = new ActiveXObject('MSXML.DOMDocument');
				xmlDoc.async = false;
				xmlDoc.loadXML(xmlString);
			}
			return xmlDoc;
		};
		
		/**
		 * Creates an XML document including a root element.
		 * @memberOf Utils
		 * @function
		 * @private
		 * @param {string} elementName Tag name of the root element.
		 * @param {string} namespace Namespace URI string.
		 * @return {Document} XML document object.
		 */
		var createXMLDocument = function(elementName, namespace) {
			if (!elementName) {
				throw {
					name: 'IllegalArgumentException',
					message: 'A element name is required.'
				};
			}
			
			var xmlDoc = null;
			if (typeof (ActiveXObject) != 'undefined') {
				xmlDoc = new ActiveXObject('MSXML.DOMDocument');
				var rootElement = (namespace) ? xmlDoc.createNode(1, elementName, namespace) : xmlDoc.createElement(elementName);
				xmlDoc.appendChild(rootElement);  
			} else if (document.implementation.createDocument) {
				xmlDoc = document.implementation.createDocument(namespace, elementName, null);
			} else {
				throw {
					name: 'NotSupportedException',
					message: 'The browser does not support the required XML operations.'
				};
			}
			
			return xmlDoc;
		};
		
		/**
		 * Creates a new XML document with the given XML DOM element as root element.
		 * @memberOf Utils
		 * @function
		 * @private
		 * @param {Element} element XML DOM element to use as root element.
		 * @return {Document} XML DOM document.
		 */
		var createXMLDocumentWithElement = function(element) {
			if (!element || (typeof element) != 'object') {
				throw {
					name: 'IllegalArgumentException',
					message: 'A element name is required.'
				};
			}
			var xmlElementString = module.generateXMLString(element);
			return module.generateXMLDocumentFromString(xmlElementString);
		};
		
		/**
		 * Generates a string form the given XML DOM element (or document).
		 * @memberOf Utils
		 * @function
		 * @private
		 * @param {Element|Document} xmlObject XML DOM object.
		 * @return {string} XML object as string.
		 */
		var generateXMLString = function(xmlObject) {
			return Strophe.serialize(xmlObject);
			/*
			if (xmlObject.xml) {
				return xmlObject.xml;
			} else if (typeof(XMLSerializer) !== 'undefined') {
				var serializer = new XMLSerializer();
				return serializer.serializeToString(xmlObject);
			}
			return null;
			*/
		};
		
		/**
		 * Creates an object tree from an xml element as used in the Strophe.js library.
		 * @memberOf Utils
		 * @function
		 * @private
		 * @param {XMLElement} element XML DOM element.
		 * @param {object} parent Parent tree to attach branch. May be null.
		 * @return {object} Object tree as consumed by strophe.js.
		 */
		var elementToStopheTree = function(element, parent) {
			var i;
			var tree = null;
			var attributesObject = {};
			for (i = 0; i < element.attributes.length; i++) {
				attributesObject[element.attributes.item(i).name] = element.attributes.item(i).value; 
			}
			if (element.namespaceURI) {
				attributesObject['xmlns'] = element.namespaceURI;
			}
			tree = $build(element.nodeName, attributesObject);
			for (i = 0; i < element.childNodes.length; i++) {
				var childNode = element.childNodes[i];
				if (childNode.nodeType == 3) {
					tree.t(childNode.nodeValue);
				} else if (childNode.nodeType == 4) {
					tree.cnode(childNode);
					tree.up();
				} else {
					module.elementToStopheTree(childNode, tree);
				}
			}
			if (parent) {
				parent.cnode(tree.tree());
				parent.up();
			}
			return tree;
		};
		
		var module = {
			'getDateForISO8601' : getDateForISO8601,
			'getISO8601ForDate' : getISO8601ForDate,
			'trimString' : trimString,
			'generateXMLDocumentFromString' : generateXMLDocumentFromString,
			'createXMLDocument' : createXMLDocument,
			'createXMLDocumentWithElement' : createXMLDocumentWithElement,
			'generateXMLString' : generateXMLString,
			'elementToStopheTree' : elementToStopheTree,
		};
		
		return module; 
	})();
	
	/**
	 * Enumeration for possible connection statuses.
	 * @namespace ConnectionStatus
	 * @name ConnectionStatus
	 * @memberOf SpacesSDK
	 */
	var ConnectionStatus = {
			/**
			 * The connection is not established.
			 * @constant
			 * @name OFFLINE
			 * @memberOf SpacesSDK.ConnectionStatus
			 */
			OFFLINE: "OFFLINE",
			/**
			 * The connection is established.
			 * @constant
			 * @name ONLINE
			 * @memberOf SpacesSDK.ConnectionStatus
			 */
			ONLINE : "ONLINE",
			/**
			 * An error occurred trying to establish the connection.
			 * The connection is not established.
			 * @constant 
			 * @name ERROR
			 * @memberOf SpacesSDK.ConnectionStatus
			 */
			ERROR : "ERROR",
			/**
			 * The connection is pending, i.e., a connection is trying to be established or closed.
			 * @constant
			 * @name PENDING
			 * @memberOf SpacesSDK.ConnectionStatus
			 */
			PENDING : "PENDING"
	};
	
	/**
	 * Enumeration for roles a space member can take.
	 * @namespace Role
	 * @name Role
	 * @memberOf SpacesSDK
	 */
	var Role = {
		/**
		 * @constant
		 * @name MEMBER
		 * @memberOf SpacesSDK.Role
		 */
		MEMBER: "MEMBER",
		/**
		 * @constant 
		 * @name MODERATOR
		 * @memberOf SpacesSDK.Role
		 */
		MODERATOR: "MODERATOR"
	};
	
	/**
	 * Enumeration for space types.
	 * @namespace Type
	 * @name Type
	 * @memberOf SpacesSDK
	 */
	var Type = {
		/**
		 * @constant 
		 * @name PRIVATE
		 * @memberOf SpacesSDK.Type
		 */
		PRIVATE : "PRIVATE",
		/**
		 * @constant 
		 * @name TEAM
		 * @memberOf SpacesSDK.Type
		 */
		TEAM : "TEAM",
		/**
		 * @constant 
		 * @name ORGA
		 * @memberOf SpacesSDK.Type
		 */
		ORGA : "ORGA",
		/**
		 * Returns the space type for the given string.
		 * @function
		 * @name getTypeForString
		 * @memberOf SpacesSDK.Type
		 * @param {string} str String to return type for.
		 * @return {SpacesSDK.Type} Related type or <code>null</code> if the mapping fails.
		 */
		getTypeForString: function(str) {
			for (var type in SpacesSDK.Type) {
				if (typeof SpacesSDK.Type[type] === "string") {
					if (SpacesSDK.Type[type].toLowerCase() === str.toLowerCase()) {
						return type;
					}
				}
			}
			return null;
		}
	};
	
	/**
	 * Enumeration for space persistence types.
	 * @namespace PersistenceType
	 * @name PersistenceType
	 * @memberOf SpacesSDK
	 */
	var PersistenceType = {
		/**
		 * Data objects published on the space are persisted. 
		 * @constant
		 * @name ON
		 * @memberOf SpacesSDK.PersistenceType
		 */
		ON : "ON",
		
		/**
		 * Published data objects are not persisted.
		 * @constant
		 * @name OFF
		 * @memberOf SpacesSDK.PersistenceType
		 */
		OFF : "OFF",
		
		/**
		 * Data objects are persisted for a specific period from their publishing time.
		 * @constant
		 * @name DURATION
		 * @memberOf SpacesSDK.PersistenceType
		 */
		DURATION : "DURATION"
	};
	
	
	/**
	 * @class Exception thrown if an error occurs during space creation, modification or deletion.
	 * @name SpaceManagementException
	 * @memberOf SpacesSDK
	 * @param {string} msg Error message string.
	 */
	var SpaceManagementException = function(msg) {
		return {
			name: "SpaceManagementException",
			message: msg
		};
	};
	
	/**
	 * @class Exception thrown when an unknown entity is addressed.
	 * @name UnknownEntityException
	 * @memberOf SpacesSDK
	 * @param {string} msg Error message string.
	 */
	var UnknownEntityException = function(msg) {
		return {
			name: "UnknownEntityException",
			message: msg
		};
	};

	/**
	 * CDM data structures.
	 * @namespace SpacesSDK.cdm
	 * @name SpacesSDK.cdm
	 */
	var cdm = (function() {
		/**
		 * Enumeration for available versions of the Common Data Model (CDM).
		 * @namespace CDMVersion
		 * @name CDMVersion 
		 * @memberOf SpacesSDK.cdm
		 */
		var CDMVersion = {
			/**
			 * @constant
			 * @name CDM_0_1
			 * @memberOf SpacesSDK.cdm.CDMVersion
			 */
			CDM_0_1 : "0.1",
			/**
			 * @constant 
			 * @name CDM_0_2
			 * @memberOf SpacesSDK.cdm.CDMVersion
			 */
			CDM_0_2 : "0.2",
			/**
			 * @constant 
			 * @name CDM_1_0
			 * @memberOf SpacesSDK.cdm.CDMVersion
			 */
			CDM_1_0 : "1.0",
			/**
			 * @constant 
			 * @name CDM_2_0
			 * @memberOf SpacesSDK.cdm.CDMVersion
			 */
			CDM_2_0 : "2.0",
			/**
			 * Returns the CDM version for the given string.
			 * @function
			 * @name getVersionForString
			 * @memberOf SpacesSDK.cdm.CDMVersion
			 * @param {string} str String to return version for.
			 * @return {SpacesSDK.cdm.CDMVersion} Related version type or <code>null</code> if the mapping fails.
			 */
			getVersionForString: function(str) {
				for (var cdmVersion in SpacesSDK.cdm.CDMVersion) {
					if (typeof SpacesSDK.cdm.CDMVersion[cdmVersion] === "string") {
						if (SpacesSDK.cdm.CDMVersion[cdmVersion] === str) {
							return cdmVersion;
						}
					}
				}
				return null;
			}
		};
		
		/**
		 * @class Model for a reference as used in the CDM 2.0.
		 * @name Reference
		 * @memberOf SpacesSDK.cdm
		 * @param {string} referenceObjectId Identifier of the data object to reference.
		 * @param {SpacesSDK.cdm.Reference.ReferenceType} [type] Type of the reference. Optional.
		 */
		var Reference =  function(referenceObjectId, type) {
			/**
			 * Returns the data object identifier of the reference.
			 * @memberOf SpacesSDK.cdm.Reference.prototype
			 * @function
			 * @return {string} Data object identifier.
			 */
			var getId = function() {
				return referenceObjectId;
			};
			
			/**
			 * Returns the type of the reference. A reference is either WEAK or marked as DEPENDENCY.
			 * Dependencies are taken into consideration for the data object lifetime management.
			 * Weak dependencies indicate a minor connection between two data objects.
			 * @memberOf SpacesSDK.cdm.Reference.prototype
			 * @function
			 * @return {SpacesSDK.cdm.Reference.ReferenceType} Reference type. Defaults to DEPENDENCY.
			 */
			var getReferenceType = function() {
				return type ? type : SpacesSDK.cdm.Reference.ReferenceType.DEPENDENCY;
			};
			
			/**
			 * Adds the the reference as new child to the given element.
			 * @memberOf SpacesSDK.cdm.Reference.prototype
			 * @function
			 * @private
			 * @param {XMLElement} Element to append the reference as child.
			 */
			var addToElement = function(targetElement) {
				var xmlDocument = targetElement.ownerDocument;
				var referenceElement = xmlDocument.createElement('reference');
				referenceElement.setAttribute('id', referenceObjectId);
				if (SpacesSDK.cdm.Reference.ReferenceType.WEAK == type) {
					referenceElement.setAttribute('type', SpacesSDK.cdm.Reference.ReferenceType.WEAK);
				}
				targetElement.appendChild(referenceElement);
			};
			
			var funcs = {
				'getId' : getId,
				'getReferenceType' : getReferenceType,
				'addToElement' : addToElement
			};
			
			return funcs;
		};
		
		/**
		 * Type of an object reference.
		 * @namespace SpacesSDK.cdm.Reference.ReferenceType
		 */
		Reference.ReferenceType = {
			/**
			 * @constant 
			 * @name DEPENDENCY
			 * @memberOf SpacesSDK.cdm.Reference.ReferenceType
			 */
			DEPENDENCY : 'dependency',
			/**
			 * @constant 
			 * @name WEAK
			 * @memberOf SpacesSDK.cdm.Reference.ReferenceType
			 */
			WEAK : 'weak',
			/**
			 * Returns the reference type for the given XML attribute value.
			 * @function
			 * @name getVersionForString
			 * @memberOf SpacesSDK.cdm.getReferenceTypeForString
			 * @param {string} str String to return reference type for.
			 * @return {SpacesSDK.cdm.Reference.ReferenceType} Related reference type or <code>null</code> if the mapping fails.
			 */
			getReferenceTypeForString: function(str) {
				for (var referenceType in SpacesSDK.cdm.Reference.ReferenceType) {
					if (typeof SpacesSDK.cdm.Reference.ReferenceType[referenceType] === 'string') {
						if (SpacesSDK.cdm.Reference.ReferenceType[referenceType] === str) {
							return referenceType;
						}
					}
				}
				return null;
			}
		};
		
		/**
		 * @class Model for the creation information as available with CDM 2.0.
		 * @name CreationInfo
		 * @memberOf SpacesSDK.cdm
		 * @param {string} [creationDate] Date of the data object creation as ISO 8601 string (e.g. created with date.toISOString()). Optional.
		 * @param {string} [creator] Identifier for the creator of the data object, e.g. the bare JID of an user. Optional.
		 * @param {string} [application] Identifier of the application used to create the data obhect, e.g. the application namespace URI.
		 */
		var CreationInfo = function(creationDate, creator, application) {
			/**
			 * Returns the identifier for the application used to create the data object.
			 * @memberOf SpacesSDK.cdm.CreationInfo.prototype
			 * @function
			 * @return {string} Application identifier if set, otherwise <code>null</code>.
			 */
			var getApplication = function() {
				return application;
			};
			
			/**
			 * Returns the creator of the data object.
			 * @memberOf SpacesSDK.cdm.getCreator.prototype
			 * @function
			 * @return {string} Identifier, e.g. bare-JID, for the creator. May be <code>null</code> if no creator is set.
			 */
			var getCreator = function() {
				return creator;
			};

			/**
			 * Returns the date when the data object was created.
			 * @memberOf SpacesSDK.cdm.CreationInfo.prototype
			 * @function
			 * @return {string} Creation date as ISO 8601 string if set, otherwise <code>null</code>.
			 */
			var getCreationDate = function() {
				return creationDate;
			};
			
			/**
			 * Adds the the creation info as new child to the given element.
			 * @memberOf SpacesSDK.cdm.CreationInfo.prototype
			 * @function
			 * @private
			 * @param {XMLElement} Element to append creation info as child.
			 */
			var addToElement = function(targetElement) {
				var xmlDocument = targetElement.ownerDocument;
				var creationInfoElement = xmlDocument.createElement('creationInfo');
				if (creationDate) {
					var dateElement = xmlDocument.createElement('date');
					dateElement.textContent = creationDate;
					creationInfoElement.appendChild(dateElement);
				}
				if (creator) {
					var personElement = xmlDocument.createElement('person');
					personElement.textContent = creator;
					creationInfoElement.appendChild(personElement);
				}
				if (application) {
					var applicationElement = xmlDocument.createElement('application');
					applicationElement.textContent = application;
					creationInfoElement.appendChild(applicationElement);
				}
				targetElement.appendChild(creationInfoElement);
			};

			var funcs = {
				'getApplication' : getApplication,
				'getCreator' : getCreator,
				'getCreationDate' : getCreationDate,
				'addToElement' : addToElement
			};
			
			return funcs;
		};
		
		var modules = {
			'CDMVersion' : CDMVersion,
			'Reference' : Reference,
			'CreationInfo' : CreationInfo
		};
		
		return modules;
	})();


	/**
	 * Use {@link SpacesSDK.CDMDataBuilder} for creation.
	 * @class Wrapper for the data provided by the Common Data Model (CDM).
	 * @name CDMData
	 * @memberOf SpacesSDK 
	 * @param {XMLElement} rootElement XML DOM document containing the CDM data.
	 */
	var CDMData = function(rootElement) {
		var xmlDocument = rootElement.ownerDocument;
		var funcs = {};
		var fields = {};
		
		(function(){ // constructor
			if (!rootElement) {
				throw {
					name: 'IllegalArgumentException',
					message: 'XML document containing the CDM data is required.'
				};
			}
			fields['cdmVersion'] = rootElement.getAttribute('cdmVersion') ? rootElement.getAttribute('cdmVersion') : SpacesSDK.cdm.CDMVersion.CDM_2_0;
			fields['id'] = rootElement.getAttribute('id');
			fields['timestamp'] = rootElement.getAttribute('timestamp');
			switch (fields['cdmVersion']) {
			case SpacesSDK.cdm.CDMVersion.CDM_0_1:
				initializeCDM_0_1(rootElement);
				break;
			case SpacesSDK.cdm.CDMVersion.CDM_0_2:
				initializeCDM_0_2(rootElement);
				break;
			case SpacesSDK.cdm.CDMVersion.CDM_1_0:
				initializeCDM_1_0(rootElement);
				break;
			case SpacesSDK.cdm.CDMVersion.CDM_2_0:
				initializeCDM_2_0(rootElement);
				break;
			}		
		})();
		
		/**
		 * Applies the CDM data types common to all versions to a XML element.
		 * @param {object} targetElement XML element to apply CDM data types to.
		 * @private
		 */
		function applyCommonTypes(targetElement) {
			targetElement.setAttribute('cdmVersion', fields['cdmVersion']);
			if (fields['id']) {
				targetElement.setAttribute('id', fields['id']);
			}
			if (fields['timestamp']) {
				targetElement.setAttribute('timestamp', fields['timestamp']);
			} 
		}
		
		/**
		 * Adds specific getters for CDM version 0.1.
		 * @param {XMLElement} rootElement XML Element containing the CDM data.
		 * @private
		 */
		function initializeCDM_0_1(rootElement) {
			fields['creator'] = rootElement.getAttribute('creator');
			
			/**
			 * [CDM 0.1] Returns the creator of the data object.
			 * @memberOf SpacesSDK.CDMData.prototype
			 * @function
			 * @return {string} Full JID of the creator of the data object. May be <code>null</code>.
			 */
			var getCreator = function() {
				return fields['creator'];
			};
			
			/**
			 * [CDM 0.1] Applies the CDM data to the given XML element.
			 * @memberOf SpacesSDK.CDMData.prototype
			 * @private
			 * @function
			 * @param {Element} targetElement XML DOM element to apply CDM data to.
			 */
			var applyToElement = function(targetElement){
				applyCommonTypes(targetElement);
				if (fields['creator']) {
					targetElement.setAttribute('creator', fields['creator']);
				}
			};
			funcs.getCreator = getCreator;
			funcs.applyToElement = applyToElement;
		}
		
		/**
		 * Adds specific getters for CDM version 0.2.
		 * @param {XMLElement} rootElement XML Element containing the CDM data.
		 * @private 
		 */
		function initializeCDM_0_2(rootElement) {
			fields['creator'] = rootElement.getAttribute('creator');
			fields['ref'] = rootElement.getAttribute('ref');
			
			/**
			 * [CDM 0.2] Returns the creator of the data object.  
			 * @memberOf SpacesSDK.CDMData.prototype
			 * @function
			 * @return {string} Full JID of the creator of the data object. May be <code>null</code>.
			 */
			var getCreator = function() {
				return fields['creator'];
			};

			/**
			 * [CDM 0.2] Returns the URI the object refers to, e.g., a the id of a parent object.  
			 * @memberOf SpacesSDK.CDMData.prototype
			 * @function
			 * @return {string} URI string if a object reference is set, otherwise <code>null</code>.
			 */
			var getRef = function() {
				return fields['ref'];
			};
			
			/**
			 * [CDM 0.2] Applies the CDM data to the given XML element.
			 * @memberOf SpacesSDK.CDMData.prototype
			 * @private
			 * @function
			 * @param {Element} targetElement XML element to apply CDM data to.
			 */
			var applyToElement = function(targetElement){
				applyCommonTypes(targetElement);
				if (fields['creator']) {
					targetElement.setAttribute('creator', fields['creator']);
				}
				if (fields['ref']) {
					targetElement.setAttribute('ref', fields['ref']);
				}
			};
			
			funcs.getCreator = getCreator;
			funcs.getRef = getRef;
			funcs.applyToElement = applyToElement;
		}
		
		/**
		 * Adds specific getters for CDM version 1.0.
		 * @param {XMLElement} rootElement XML Element containing the CDM data.
		 * @private 
		 */
		function initializeCDM_1_0(rootElement) {
			fields['customId'] = rootElement.getAttribute('customId');
			fields['modelVersion'] = rootElement.getAttribute('modelVersion');
			fields['publisher'] = rootElement.getAttribute('publisher');
			fields['ref'] = rootElement.getAttribute('ref');
			
			/**
			 * [CDM 1.0] Returns the custom identifier of the data object.  
			 * @memberOf SpacesSDK.CDMData.prototype
			 * @function
			 * @return {string} Custom object identifier or <code>null</code> if not set. 
			 */
			var getCustomId = function() {
				return fields['customId'];
			};
			
			/**
			 * [CDM 1.0] Returns the version of the data model the object instantiates.  
			 * @memberOf SpacesSDK.CDMData.prototype
			 * @function
			 * @return {string} Model version string.
			 */
			var getModelVersion = function() {
				return fields['modelVersion'];
			};
			
			/**
			 * [CDM 1.0] Returns the publisher of the data object. 
			 * This attribute is optional, but is verified by the service if set.  
			 * @memberOf SpacesSDK.CDMData.prototype
			 * @function
			 * @return {string} Full-JID of the data object publisher as string.
			 */
			var getPublisher = function() {
				return fields['publisher'];
			};
			
			/**
			 * [CDM 1.0] Returns the URI the object refers to, e.g., a the id of a parent object.  
			 * @memberOf SpacesSDK.CDMData.prototype
			 * @function
			 * @return {string} URI string if a object reference is set, otherwise <code>null</code>.
			 */
			var getRef = function() {
				return fields['ref'] ;
			};
			
			/**
			 * [CDM 1.0] Applies the CDM data to the given XML element.
			 * @private
			 * @memberOf SpacesSDK.CDMData.prototype
			 * @function
			 * @param {Element} targetElement XML element to apply CDM data to.
			 */
			var applyToElement = function(targetElement) {
				applyCommonTypes(targetElement);
				if (fields['customId']) {
					targetElement.setAttribute('customId', fields['customId']);
				}
				if (fields['modelVersion']) {
					targetElement.setAttribute('modelVersion', fields['modelVersion']);
				}
				if (fields['ref']) {
					targetElement.setAttribute('ref', fields['ref']);
				}
				if (fields['publisher']) {
					targetElement.setAttribute('publisher', fields['publisher']);
				}
			};
			
			funcs.getCustomId = getCustomId;
			funcs.getModelVersion = getModelVersion;
			funcs.getPublisher = getPublisher;
			funcs.getRef = getRef;
			funcs.applyToElement = applyToElement;
		}
		
		/**
		 * Adds specific getters for CDM version 2.0.
		 * @param {XMLElement} rootElement XML Element containing the CDM data.
		 * @private 
		 */
		function initializeCDM_2_0(rootElement) {
			fields['customId'] = rootElement.getAttribute('customId');
			fields['modelVersion'] = rootElement.getAttribute('modelVersion');
			fields['publisher'] = rootElement.getAttribute('publisher');
			fields['ref'] = rootElement.getAttribute('ref');
			fields['copyOf'] = rootElement.getAttribute('copyOf');
			fields['updates'] = rootElement.getAttribute('updates');
			for (var i = 0; i < rootElement.childNodes.length; i++) {
					var childNode = rootElement.childNodes[i];
					if (childNode.nodeType == 1) switch (childNode.nodeName) {
						case 'summary':
							fields['summary'] = childNode.textContent;
							break;
						case 'references':
							fields['references'] = [];
							for (var j = 0; j < childNode.childNodes.length; j++) {
								var referenceElement = childNode.childNodes[j];
								if (referenceElement.nodeName == 'reference') {
									var referenceObjectId = referenceElement.getAttribute('id');
									var referenceType = SpacesSDK.cdm.Reference.ReferenceType.getReferenceTypeForString(referenceElement.getAttribute('type'));
									fields['references'].push(new SpacesSDK.cdm.Reference(referenceObjectId, referenceType));
								}
							}
							break;
						case 'creationInfo':
							var creationDate = null;
							var creator = null;
							var application = null;
							
							for (var j = 0; j < childNode.childNodes.length; j++) {
								var subElement = childNode.childNodes[j];
								switch (subElement.nodeName) {
									case 'date':
										creationDate = subElement.textContent;
										break;
									case 'person':
										creator = subElement.textContent;
										break;
									case 'application' :
										application = subElement.textContent;
										break;
								}
							}
							fields['creationInfo'] = SpacesSDK.cdm.CreationInfo(creationDate, creator, application);
							break;
					}
				}

			
			/**
			 * [CDM 2.0] Returns the custom identifier of the data object.  
			 * @memberOf SpacesSDK.CDMData.prototype
			 * @function
			 * @return {string} Custom object identifier or <code>null</code> if not set. 
			 */
			var getCustomId = function() {
				return fields['customId'];
			};
			
			/**
			 * [CDM 2.0] Returns the version of the data model the object instantiates.  
			 * @memberOf SpacesSDK.CDMData.prototype
			 * @function
			 * @return {string} Model version string.
			 */
			var getModelVersion = function() {
				return fields['modelVersion'];
			};
			
			/**
			 * [CDM 2.0] Returns the publisher of the data object. 
			 * This attribute is optional, but is verified by the service if set.  
			 * @memberOf SpacesSDK.CDMData.prototype
			 * @function
			 * @return {string} Full-JID of the data object publisher as string.
			 */
			var getPublisher = function() {
				return fields['publisher'];
			};
			
			/**
			 * [CDM 2.0] Returns the URI the object refers to, e.g., a the id of a parent object.  
			 * @memberOf SpacesSDK.CDMData.prototype
			 * @function
			 * @return {string} URI string if a object reference is set, otherwise <code>null</code>.
			 */
			var getRef = function() {
				return fields['ref'];
			};
			
			/**
			 * [CDM 2.0] Returns the identifier of the data object copied by this object.
			 * @memberOf SpacesSDK.CDMData.prototype
			 * @function
			 * @return {string} Identifier of the data object this one is a copy of, otherwise <code>null</code>.
			 */
			var getCopyOf = function() {
				return fields['copyOf'];
			};
			
			/**
			 * [CDM 2.0] Checks if this data object is a copy of another one.
			 * @memberOf SpacesSDK.CDMData.prototype
			 * @function
			 * @return {boolean} <code>true</code> if this data object states itself to be a copy, otherwise <code>false</code>. 
			 */
			var isCopy = function() {
				return fields['copyOf'] ? true : false;
			};
			
			/**
			 * [CDM 2.0] Returns the summary, i.e. textual description, for this data object.
			 * @memberOf SpacesSDK.CDMData.prototype
			 * @function
			 * @return {string} Summary of the data objects content, or null if not set.
			 */
			var getSummary = function() {
				return fields['summary'];
			};
			
			/**
			 * [CDM 2.0] Return the identifier of the data object updated by this object.
			 * @memberOf SpacesSDK.CDMData.prototype
			 * @function
			 * @return {string} Identifier of the data object this one updates, or <code>null</code> if this is no update.
			 */
			var getUpdatedObjectId = function() {
				return fields['updates'];
			};
			
			
			/**
			 * [CDM 2.0] Checks if this data object is an update for another one.
			 * @memberOf SpacesSDK.CDMData.prototype
			 * @function
			 * @return {boolean} <code>true</code> if this data object states itself to be an update, otherwise <code>false</code>. 
			 */
			var isUpdate = function() {
				return fields['updates'] ? true : false;
			};
			
			
			/**
			 * [CDM 2.0] Returns the references for the data object.
			 * @memberOf SpacesSDK.CDMData.prototype
			 * @function
			 * @return {SpaceSDK.cdm.Reference[]} List of references or <code>null</code> if not set. 
			 */
			var getReferences = function() {
				return fields['references'];
			};
			
			/**
			 * [CDM 2.0] Returns the creation information of this data object.
			 * @memberOf SpacesSDK.CDMData.prototype
			 * @function
			 * @return {SpaceSDK.cdm.CreationInfo} Information on the data object creation, or <code>null</code> if not available. 
			 */
			var getCreationInfo = function() {
				return fields['creationInfo'];
			};
			
			/**
			 * [CDM 2.0] Applies the CDM data to the given XML element.
			 * @private
			 * @memberOf SpacesSDK.CDMData.prototype
			 * @function
			 * @param {Element} targetElement XML element to apply CDM data to.
			 */
			var applyToElement = function(targetElement) {
				applyCommonTypes(targetElement);
				var attributeFields = ['customId', 'modelVersion', 'ref', 'publisher', 'copyOf', 'updates'];
				for (var i = 0; i < attributeFields.length; i++) {
					if (fields[attributeFields[i]]) {
						targetElement.setAttribute(attributeFields[i], fields[attributeFields[i]]);
					}
				}
				var xmlDoc = targetElement.ownerDocument;
				if (fields['summary']) {
					var summaryElement = xmlDoc.createElement('summary');
					summaryElement.textContent = fields['summary'];
					targetElement.appendChild(summaryElement);
				}
				if (fields['references']) {
					var referencesElement = xmlDoc.createElement('references');
					for (var i = 0; i < fields['references'].length; i++) {
						fields['references'][i].addToElement(referencesElement);
					}
					targetElement.appendChild(referencesElement);
				}
				if (fields['creationInfo']) {
					fields['creationInfo'].addToElement(targetElement);
				}
			};
			
			funcs.getCustomId = getCustomId;
			funcs.getModelVersion = getModelVersion;
			funcs.getPublisher = getPublisher;
			funcs.getRef = getRef;
			funcs.getCopyOf = getCopyOf;
			funcs.getSummary = getSummary;
			funcs.getReferences = getReferences;
			funcs.getCreationInfo = getCreationInfo;
			funcs.applyToElement = applyToElement;
		}
		
		/**
		 * Returns the version information of common data model implemented.
		 * The CDM version information is only available in all objects with support for CDM version 1.0 or higher.  
		 * @memberOf SpacesSDK.CDMData.prototype
		 * @function
		 * @return {string} Version information string if a CDM version is set, otherwise <code>null</code>.
		 */
		var getCDMVersion = function() {
			return fields['cdmVersion'];
		};
		
		/**
		 * Returns the ID of the data object.
		 * The ID is unique within the domain and set by the server.  
		 * @memberOf SpacesSDK.CDMData.prototype
		 * @function
		 * @return {string} Data object identifier.
		 */
		var getId = function() {
			return fields['id'];
		};
		
		/**
		 * Returns the time the object was published.
		 * The point in time is determined by the server.  
		 * @memberOf SpacesSDK.CDMData.prototype
		 * @function
		 * @return {Date} Point in time the data object was published as date object.
		 */
		var getTimeStamp = function() {
			return (fields['timestamp']) ? Utils.getDateForISO8601(fields['timestamp']) : null;
		};
		
		funcs.getCDMVersion = getCDMVersion;
		funcs.getId = getId;
		funcs.getTimeStamp = getTimeStamp;
		
		return funcs;
	};
	
	/**
	 * Creates a new builder for the CDM information.
	 * @class Builder for information provided by the Common Data Model.
	 * @name CDMDataBuilder
	 * @memberOf SpacesSDK 
	 * @param {SpacesSDK.cdm.CDMVersion} cdmDataVersion The version of the CDM object to generate. If not set, the latest version is used.
	 */
	var CDMDataBuilder = function(cdmVersion) {
		var fields = {};
		
		(function() { // constructor
			fields['cdmVersion'] = cdmVersion ? cdmVersion : SpacesSDK.cdm.CDMVersion.CDM_2_0;
		})();
		
		/**
		 * Builds and returns a CDMDataObject with the given parameters.
		 * @memberOf SpacesSDK.CDMDataBuilder.prototype
		 * @function
		 * @return {SpacesSDK.CDMData} A typed CDMData object with the information provided to this builder.
		 */
		var build = function() {
			var xmlDocument = Utils.createXMLDocument('root');
			var rootElement = xmlDocument.childNodes[0];
			var attributesToSet = ['cdmVersion', 'customId', 'creator', 'customId', 'id', 'timestamp', 'modelVersion', 'publisher', 'ref', 'copyOf', 'updates'];
			for (var i = 0; i < attributesToSet.length; i++) {
				if (fields[attributesToSet[i]]) {
					rootElement.setAttribute(attributesToSet[i], fields[attributesToSet[i]]);
				}
			}
			if (fields['summary']) {
				var summaryElement = xmlDocument.createElement('summary');
				summaryElement.textContent = fields['summary'];
				rootElement.appendChild(summaryElement);
			}
			if (fields['references']) {
				var referencesElement = xmlDocument.createElement('references');
				for (var i = 0; i < fields['references'].length; i++) {
					fields['references'][i].addToElement(referencesElement);
				}
				rootElement.appendChild(referencesElement);
			}
			if (fields['creationInfo']) {
				fields['creationInfo'].addToElement(rootElement);
			}
			
			return new CDMData(rootElement);
		};
		
		/**
		 * Sets the creator of the data object.
		 * With CDM version 1.0, the creator information moved to the Common Data Types and is now part
		 * of the application specific data model.
		 * CDM version: 0.1, 0.2
		 * @memberOf SpacesSDK.CDMDataBuilder.prototype
		 * @function
		 * @param {string} cdmCreator Full-JID of the user who created the data object.
		 * @return {SpacesSDK.CDMDataBuilder} The CDMDataBuilder.
		 */
		var setCreator = function(cdmCreator) {
			fields['creator'] = cdmCreator;
			return funcs;
		};
		
		/**
		 * Adds a custom identifier for the data object.
		 * CDM version: 1.0 or greater
		 * @memberOf SpacesSDK.CDMDataBuilder.prototype
		 * @function
		 * @param {string} cdmCustomId String to be used as custom identifier.
		 * @return {SpacesSDK.CDMDataBuilder} The CDMDataBuilder.
		 */
		var setCustomId = function(cdmCustomId) {
			fields['customId'] = cdmCustomId;
			return funcs;
		};
		
		/**
		 * Adds an object identifier.
		 * The data object identifier will be automatically set/overwritten by the Spaces Service when the
		 * object is published on a space. Custom identifiers are provided with the <code>customId</code>
		 * attribute.
		 * CDM version: 0.1 or greater 
		 * @memberOf SpacesSDK.CDMDataBuilder.prototype
		 * @function
		 * @param {string} cdmId String to set as data object identifier.
		 * @return {SpacesSDK.CDMDataBuilder}The CDMDataBuilder.
		 */
		var setId = function(cdmId) {
			fields['id'] = cdmId;
			return funcs;
		};
		
		/**
		 * Sets the version of the data model instantiated by the data object.
		 * The model version information is REQUIRED since CDM version 1.0.
		 * CDM version: 1.0 or greater.
		 * @memberOf SpacesSDK.CDMDataBuilder.prototype
		 * @function
		 * @param {string} modelVersion Version of the data model as string.
		 * @return {SpacesSDK.CDMDataBuilder} The CDMDataBuilder.
		 */
		var setModelVersion = function(modelVersion) {
			fields['modelVersion'] = modelVersion;
			return funcs;
		};
		
		/**
		 * Sets the publisher of the data object.
		 * When published on a space, the publisher will be corrected by the Spaces Service if set and
		 * not correct.
		 * CDM version: 1.0 or greater
		 * @memberOf SpacesSDK.CDMDataBuilder.prototype
		 * @function
		 * @param {string} jid Full-JID of the user publishing the data object. 
		 * @return {SpacesSDK.CDMDataBuilder} The CDMDataBuilder.
		 */
		var setPublisher = function(jid) {
			fields['publisher'] = jid;
			return funcs;
		};

		/**
		 * Adds a reference to another data object.
		 * The reference indicates a "is child of" or "belongs to" relation. 
		 * CDM version: 0.2 or greater
		 * @memberOf SpacesSDK.CDMDataBuilder.prototype
		 * @function
		 * @param {string} objectId Data object identifier of the referenced object.
		 * @return {SpacesSDK.CDMDataBuilder} The CDMDataBuilder.
		 */
		var setRef = function(objectId) {
			fields['ref'] = objectId;
			return funcs;
		};
		
		/**
		 * Adds a publishing timestamp.
		 * The timestamp will be automatically set/overwritten by the Spaces Service when the object is
		 * published on a space.
		 * CDM version: 0.1 or greater
		 * @memberOf SpacesSDK.CDMDataBuilder.prototype
		 * @function
		 * @param {string} timestamp Timestamp as ISO 8601 string.
		 * @return {SpacesSDK.CDMDataBuilder} The CDMDataBuilder.
		 */
		var setTimestamp = function(timestamp) {
			fields['timestamp'] = timestamp;
			return funcs;
		};
		
		/**
		 * Indicate that this data object is a copy of another one.
		 * CDM version: 2.0 or greater
		 * @memberOf SpacesSDK.CDMDataBuilder.prototype
		 * @function
		 * @param {string} objectId Data object identifier of the copied object.
		 * @return {SpacesSDK.CDMDataBuilder} The CDMDataBuilder.
		 */
		var setCopyOf = function(objectId) {
			fields['copyOf'] = objectId;
			return funcs;
		};
		
		/**
		 * Indicate that this data object is a update for another one.
		 * CDM version: 2.0 or greater
		 * @memberOf SpacesSDK.CDMDataBuilder.prototype
		 * @function
		 * @param {string} objectId Data object identifier of the updated object.
		 * @return {SpacesSDK.CDMDataBuilder} The CDMDataBuilder.
		 */
		var setUpdates = function(objectId) {
			fields['updates'] = objectId;
			return funcs;
		};
		
		/**
		 * Adds a textual summary of the data object.
		 * CDM version: 2.0 or greater
		 * @memberOf SpacesSDK.CDMDataBuilder.prototype
		 * @function
		 * @param {string} summary Textual summary of the data object.
		 * @return {SpacesSDK.CDMDataBuilder} The CDMDataBuilder.
		 */
		var setSummary = function(summary) {
			fields['summary'] = summary;
			return funcs;
		};
		
		/**
		 * Adds a reference to another data object.
		 * CDM version: 2.0 or greater
		 * @memberOf SpacesSDK.CDMDataBuilder.prototype
		 * @function
		 * @param {SpacesSDK.cdm.Reference} reference Reference to another data object.
		 * @return {SpacesSDK.CDMDataBuilder} The CDMDataBuilder.
		 */
		var addReference = function(reference) {
			if (fields['references'] == null) {
				fields['references'] = [];
			}
			fields['references'].push(reference);
			return funcs;
		};
		
		/**
		 * Adds information about the data object creation.
		 * CDM version: 2.0 or greater
		 * @memberOf SpacesSDK.CDMDataBuilder.prototype
		 * @function
		 * @param {SpacesSDK.cdm.CreationInfo} creationInfo Creation information.
		 * @return {SpacesSDK.CDMDataBuilder} The CDMDataBuilder.
		 */
		var setCreationInfo = function(creationInfo) {
			fields['creationInfo'] = creationInfo;
			return funcs;
		};
		
		var funcs = {
			'build': build,
			'setCreator' : setCreator,
			'setCustomId' : setCustomId,
			'setId' : setId,
			'setModelVersion' : setModelVersion,
			'setPublisher' : setPublisher,
			'setRef' : setRef,
			'setTimestamp' : setTimestamp,
			'setCopyOf' : setCopyOf,
			'setUpdates' : setUpdates,
			'setSummary' : setSummary,
			'addReference' : addReference,
			'setCreationInfo' : setCreationInfo
		};

		return funcs;
	};

	/**
	 * It is recommended to use the {@link SpacesSDK.ConnectionConfigurationBuilder} to generate a connection configuration.
	 * @class Configuration for the XMPP connection.
	 * @name ConnectionConfiguration
	 * @memberOf SpacesSDK
	 * @param {string} domain XMPP domain.
	 * @param {string} host XMPP server host.
	 * @param {int} port Port of the XMPP server.
	 * @param {string} applicationId Resource identifier to use for the application.
	 * @param {boolean} isSecureConnection Set to <code>true</code> to enforce an TLS encrypted connection.
	 * @param {boolean} selfSigned Set to <code>true</code> to explicitly allow self-signed certificates for the connection.
	 */
	var ConnectionConfiguration = function(domain, host, port, timeout, applicationID, isSecureConnection, selfSigned) {
		/**
		 * Returns the application identifier.
		 * The identifier is an arbitrary string, which should be unique for the application.
		 * @function
		 * @memberOf SpacesSDK.ConnectionConfiguration.prototype
		 * @return {string} Application identifier.
		 */
		var getApplicationID = function() {
			return applicationID;
		};
		
		/**
		 * Returns the XMPP domain.
		 * In most cases, the XMPP domain equals the DNS domain.
		 * @function
		 * @memberOf SpacesSDK.ConnectionConfiguration.prototype
		 * @return {string} Domain domain name.
		 */
		var getDomain = function() {
			return domain;
		};
		
		/**
		 * Returns the configured host name of IPv4 address.
		 * By default, the hostname equals the XMPP domain name. 
		 * @function
		 * @memberOf SpacesSDK.ConnectionConfiguration.prototype
		 * @return {string} Hostname or IPv4 address.
		 */
		var getHost = function() {
			return host;
		};
		
		/**
		 * Returns the XMPP server port.
		 * Defaults to the standard port 5222.
		 * @function
		 * @memberOf SpacesSDK.ConnectionConfiguration.prototype
		 * @return {int} Server port.
		 */
		var getPort = function() {
			return port;
		};
		
		/**
		 * Checks if the connection is configured to be secure.
		 * Defaults to <code>true</code>. 
		 * @function
		 * @memberOf SpacesSDK.ConnectionConfiguration.prototype
		 * @return {boolean} <code>true</code> if the connection is configured to be TLS encrypted, otherwise <code>false</code>.
		 */
		var isSecureConnection = function() {
			return isSecureConnection;
		};
		
		/**
		 * Checks if the connection accepts self-signed certificates.
		 * Defaults to <code>true</code>. 
		 * @function
		 * @memberOf SpacesSDK.ConnectionConfiguration.prototype
		 * @return {boolean} <code>true</code> if self-signed certificates are accepted, otherwise <code>false</code>.
		 */
		var isSelfSignedCertificateEnabled = function() {
			return selfSigned;
		};
		
		/**
		 * Returns the duration until a request times out.
		 * Defaults to 2000 ms.
		 * @function
		 * @memberOf SpacesSDK.ConnectionConfiguration.prototype
		 * @return Timeout in milliseconds.
		 */
		var requestTimeout = function() {
			return timeout ? timeout : 2000;
		};
		
		var funcs = {
			'getApplicationID' : getApplicationID,
			'getDomain' : getDomain,
			'getHost' : getHost,
			'getPort' : getPort,
			'isSecureConnection' : isSecureConnection,
			'isSelfSignedCertificateEnabled' : isSelfSignedCertificateEnabled,
			'requestTimeout' : requestTimeout
		};
		
		return funcs;
	};

	/**
	 * Creates the builder with default values.
	 * @class Builder for connection configurations.
	 * @name ConnectionConfigurationBuilder
	 * @memberOf SpacesSDK
	 * @param {string} domain Name of the XMPP domain to connect to.
	 * @param {string} applicationID Arbitrary string identifying the application.
	 */
	var ConnectionConfigurationBuilder = function(domain, applicationID) {
		var portIntern = null;
		var isSecureConnectionIntern = null;
		var selfSignedCertificateEnabledIntern = null;
		var domainIntern = null;
		var applicationIDIntern = null;
		var hostIntern = null;
		var timeoutIntern = null;

		(function() { // constructor
			portIntern = 5222;
			isSecureConnectionIntern = true;
			selfSignedCertificateEnabledIntern = true;
			domainIntern = domain;
			applicationIDIntern = applicationID;
			timeoutIntern = 2000;
		})();

		/**
		 * Builds an connection configuration object based on the given settings.
		 * @function
		 * @memberOf SpacesSDK.ConnectionConfigurationBuilder.prototype
		 * @return {SpacesSDK.ConnectionConfiguration} A ConnectionConfiguration object with the inforamtion provided to this builder.
		 */
		 var build = function() {
			if (applicationIDIntern == null || Utils.trimString(applicationIDIntern).length == 0) {
				throw {
					name: "IllegalStateException",
					message: "The application identifier is not initialized or empty, but has to be set."
				};
			}
			var isDomainSet = (domainIntern != null) && (Utils.trimString(domainIntern).length != 0);
			var isHostSet = (hostIntern != null) && (Utils.trimString(hostIntern).length != 0);
			if (!isDomainSet && !isHostSet) {
				throw {
					name: "IllegalStateException",
					message: "Either hostname or domain has to be set."
				};
			}
			if (!isDomainSet && isHostSet) {
				domainIntern = hostIntern;
			} else if (!isHostSet && isDomainSet) {
				hostIntern = domainIntern;
			}
			return new ConnectionConfiguration(domainIntern, hostIntern, portIntern, timeoutIntern,
							applicationIDIntern, isSecureConnectionIntern, selfSignedCertificateEnabledIntern);
		};
		
		/**
		 * Sets the identifier for the application.
		 * The identifier is an arbitrary string, which should be unique for the application.
		 * @function
		 * @memberOf SpacesSDK.ConnectionConfigurationBuilder.prototype
		 * @param {string} applicationID Arbitrary string identifying the application.
		 * @return {SpacesSDK.ConnectionConfigurationBuilder} The ConnectionConfigurationBuilder.
		 */
		var setApplicationId = function(applicationID) {
			applicationIDIntern = applicationID;
			return funcs;
		};
		
		/**
		 * Sets the XMPP domain.
		 * In most cases, the XMPP domains equals the DNS domain.
		 * @function
		 * @memberOf SpacesSDK.ConnectionConfigurationBuilder.prototype
		 * @param {string} domain Domain name to set.
		 * @return {SpacesSDK.ConnectionConfigurationBuilder} The ConnectionConfigurationBuilder.
		 */
		var setDomain = function(domain) {
			domainIntern = domain;
			return funcs;
		};
		
		/**
		 * Sets the XMPP host name or IPv4 address.
		 * If not set, it is assumed the hostname equals the XMPP domain.
		 * @function
		 * @memberOf SpacesSDK.ConnectionConfigurationBuilder.prototype
		 * @param {string} host Hostname or IPv4 address of the XMPP server.
		 * @return {SpacesSDK.ConnectionConfigurationBuilder} The ConnectionConfigurationBuilder.
		 */
		var setHost = function(host) {
			hostIntern = host;
			return funcs;
		};
		
		/**
		 * Sets the port for the XMPP connection.
		 * If not set, the standard port (5222) is assumed.
		 * @function
		 * @memberOf SpacesSDK.ConnectionConfigurationBuilder.prototype
		 * @param {int} port Port for the XMPP connection.
		 * @return {SpacesSDK.ConnectionConfigurationBuilder} The ConnectionConfigurationBuilder.
		 */
		var setPort = function(port) {
			portIntern = port;
			return funcs;
		};
		
		/**
		 * Specifies if the connection should be TLS encrypted or not.
		 * @function
		 * @memberOf SpacesSDK.ConnectionConfigurationBuilder.prototype
		 * @param {boolean} secureConnection <code>true</code> to configure the connection to be encrypted, otherwise <code>false</code>.
		 * @return {SpacesSDK.ConnectionConfigurationBuilder} The ConnectionConfigurationBuilder.
		 */
		var setSecureConnection = function(secureConnection) {
			isSecureConnectionIntern = secureConnection;
			return funcs;
		};
		
		/**
		 * Specifies if self-signed certificates should be accepted or not.
		 * @function
		 * @memberOf SpacesSDK.ConnectionConfigurationBuilder.prototype
		 * @param {boolean} selfSignedCertificateEnabled <code>true</code> if self-signed certificates should be accepted, otherwise <code>false</code>.
		 * @return {SpacesSDK.ConnectionConfigurationBuilder} The ConnectionConfigurationBuilder.
		 */
		var setSelfSignedCertificateEnabled = function(selfSignedCertificateEnabled) {
			selfSignedCertificateEnabledIntern = selfSignedCertificateEnabled;
			return funcs;
		};
		
		/**
		 * Sets the time the client should wait for an response from the server
		 * @function
		 * @memberOf SpacesSDK.ConnectionConfigurationBuilder.prototype
		 * @param timeout Timeout for server requests.
		 * @return Builder instance.
		 */
		var setTimeout = function(timeout) {
			timeoutIntern = timeout;
			return funcs;	
		};
		
		var funcs = {
			'build' : build,
			'setApplicationId' : setApplicationId,
			'setDomain' : setDomain,
			'setHost' : setHost,
			'setPort' : setPort,
			'setSecureConnection' : setSecureConnection,
			'setSelfSignedCertificateEnabled' : setSelfSignedCertificateEnabled,
			'setTimeout' : setTimeout
		};
		return funcs;
	};
	
	/**
	 * @class Model for information about the XMPP network and its components.
	 * @name ConnectionHandler
	 * @memberOf SpacesSDK
	 * @param {string} spacesServiceJID JID of the spaces service component.
	 * @param {string} spacesServiceVersion Version of the space service component.
	 * @param {string} persistenceServiceJID JID of the persistence service component.
	 */
	var NetworkInformation = function(spacesServiceJID, spacesServiceVersion, persistenceServiceJID) {
		/**
		 * Returns the JID of the MIRROR Persistence Service registered in the network.
		 * @function
		 * @memberOf SpacesSDK.NetworkInformation.prototype
		 * @param {string} JID of the persistence service XMPP component.
		 */
		var getSpacesServiceJID = function() {
			return spacesServiceJID;
		};
		
		/**
		 * Returns the version of the MIRROR Spaces Service.
		 * @function
		 * @memberOf SpacesSDK.NetworkInformation.prototype
		 * @param {string} Version information string as provided by the service.
		 */
		var getSpacesServiceVersion = function() {
			return spacesServiceVersion;
		};
		
		/**
		 * Returns the JID of the MIRROR Spaces Service.
		 * @function
		 * @memberOf SpacesSDK.NetworkInformation.prototype
		 * @param {string} JID of the spaces service XMPP component.
		 */
		var getPersistenceServiceJID = function() {
			return persistenceServiceJID;
		};
		
		var funcs = {
			'getSpacesServiceJID' : getSpacesServiceJID,
			'getSpacesServiceVersion' : getSpacesServiceVersion,
			'getPersistenceServiceJID' : getPersistenceServiceJID
		};
		
		return funcs;
	};

	/**
	 * Instantiates a connection handler with the given connection configuration and user credentials.
	 * @class JavaScript implementation of the connection handler API. It is makes use of the Strophe.js XMPP library.
	 * @name ConnectionHandler
	 * @memberOf SpacesSDK
	 * @param {string} user XMPP username of the user to log in when connecting to the server.
	 * @param {string} password Password of the user to log in.
	 * @param {SpacesSDK.ConnectionConfiguration} connectionConfiguration Connection configuration.
	 * @param {string} service The relative or absolut path to the HTTP-Binding (BOSH) interface.
	 */
	var ConnectionHandler = function(user, password, connectionConfiguration, service) {
		var listeners = null;
		var status = null;
		var userInfo = null;
		var connection = null;
		var networkInfo = null;

		(function(){ // constructor
			userInfo = new UserInfo(user.toLowerCase(), connectionConfiguration.getDomain(),
						connectionConfiguration.getApplicationID());
			connection = new Strophe.Connection(service);
			listeners = {};
			Strophe.log = function(level, msg) {
				if (level === 2) {
					console.log("WARN: ", msg);
				} else if (level >= 3) {
					console.log("ERROR: ", msg);
				}
			};
		
			setConnectionStatus(SpacesSDK.ConnectionStatus.OFFLINE);
		})();

		/**
		 * @private
		 * @function
		 * @memberOf SpacesSDK.ConnectionHandler.prototype
		 * Sets the connection status and notifies all connection status listeners.
		 * @param {SpacesSDK.ConnectionStatus} newStatus The new ConnectionStatus.
		 */
		function setConnectionStatus(newStatus) {
			status = newStatus;
			for (var listenerName in listeners) {
				var listener = listeners[listenerName];
				listener.connectionStatusChanged(status);
			}
		}

		
		/**
		 * @function
		 * @memberOf SpacesSDK.ConnectionHandler.prototype
		 * Requests the network information from the XMPP server.
		 * @private
		 */
		var requestNetworkInformation = function(callback) {
			var spacesServiceJID = null;
			var persistenceServiceJID = null;
			var spacesServiceVersion = null;
			
			var attrs = {xmlns: Strophe.NS.DISCO_ITEMS};
			var itemsIQ = $iq({
				from: userInfo.getFullJID(),
				to: connectionConfiguration.getDomain(),
				type: 'get'
			}).c('query', attrs);
			
			var successHandler = function (iq) {
				if (!callback) return;
				var xml = $(iq);
				var itemElements = xml.find('item');
				for (var i=0; i < itemElements.length; i++) {
					var itemElement = itemElements[i];
					if ('MIRROR Spaces Service'.toLowerCase() == itemElement.getAttribute('name').toLowerCase()) {
						spacesServiceJID = itemElement.getAttribute('jid');
					} else if ('MIRROR Persistence Service'.toLowerCase() == itemElement.getAttribute('name').toLowerCase()) {
						persistenceServiceJID = itemElement.getAttribute('jid');
					}

				}
				if (spacesServiceJID) {
					var spacesVersionIQ = $iq({
						from: userInfo.getFullJID(),
						to: spacesServiceJID,
						type: 'get'
					}).c('spaces', {xmlns: Strophe.NS.SPACES}).c('version');
					
					connection.sendIQ(spacesVersionIQ, function (resultIQ) {
						spacesServiceVersion = resultIQ.getElementsByTagName('version')[0].textContent;
						callback(new NetworkInformation(spacesServiceJID, spacesServiceVersion, persistenceServiceJID));
					}, function (errorIQ) {
						if (errorIQ) {
							var errorElement = errorIQ.getElementsByTagName('error')[0];
							console.warn('Failed to retrieve spaces service version: ' + Utils.generateXMLString(errorElement));
						} else {
							console.warn('Failed to retrieve spaces service version.');
						}
						callback(new NetworkInformation(spacesServiceJID, null, persistenceServiceJID));
					}, connectionConfiguration.requestTimeout());
				} else {
					callback(new NetworkInformation(null, null, persistenceServiceJID));
				}
			};
			var errorHandler = function (errorIQ) {
				if (errorIQ) {
					var errorElement = $(errorIQ).find('error');
					console.warn('Failed to retrieve network information: ' + errorElement[0].getAttribute('code'), errorElement[0]);
				} else {
					console.log('Failed to retrieve network information.');
				}
				callback();
			};
			connection.sendIQ(itemsIQ, successHandler, errorHandler, connectionConfiguration.requestTimeout());
		};
		
		/**
		 * Adds a listener to be notified when the connection status changes.
		 * @function
		 * @memberOf SpacesSDK.ConnectionHandler.prototype
		 * @param {SpacesSDK.ConnectionStatusListener} listener Listener to add. 
		 */
		var addConnectionStatusListener = function(listener) {
			listeners[listener.name] = listener;
		};
		
		/**
		 * Tries to establish a connection and logs the user in.
		 * @function
		 * @memberOf SpacesSDK.ConnectionHandler.prototype
		 */
		var connect = function() {
			setConnectionStatus(SpacesSDK.ConnectionStatus.PENDING);
			connection.connect(userInfo.getFullJID(), password, function onConnect(code, message) {
				if (code == Strophe.Status.CONNECTED) {
					connection.send($pres());
					requestNetworkInformation(function(result) {
						networkInfo = result;
						setConnectionStatus(SpacesSDK.ConnectionStatus.ONLINE);
					});
				} else if (code == Strophe.Status.ERROR || code == Strophe.Status.CONNFAIL || code == Strophe.Status.AUTHFAIL) {
					setConnectionStatus(SpacesSDK.ConnectionStatus.ERROR);
				} else if (code == Strophe.Status.AUTHENTICATING || code == Strophe.Status.DISCONNECTING || code == Strophe.Status.CONNECTING) {
					setConnectionStatus(SpacesSDK.ConnectionStatus.PENDING);
				} else if (code == Strophe.Status.DISCONNECTED) {
					setConnectionStatus(SpacesSDK.ConnectionStatus.OFFLINE);
				}
			}	
			);
		};
		
		/**
		 * Tries to establish a connection, create the configured user, and perform the login. 
		 * @function
		 * @memberOf SpacesSDK.ConnectionHandler.prototype
		 */
		var connectAndCreateUser = function() {
			connection.register.connect(connectionConfiguration.getHost(), function onConnect(status) {
				if (status == Strophe.Status.REGISTER) {
					connection.register.fields.username = userInfo.getUsername().toLowerCase();
					connection.register.fields.password = password;
					connection.register.submit();
				} else if (status == Strophe.Status.REGISTERED) {
					// connection.register.authenticate();
					connection.authenticate();
				} else if (status == Strophe.Status.CONNECTED) {
					connection.send($pres());
					requestNetworkInformation(function(result) {
						networkInfo = result;
						setConnectionStatus(SpacesSDK.ConnectionStatus.ONLINE);
					});
				} else if (status == Strophe.Status.ERROR || status == Strophe.Status.CONNFAIL || status == Strophe.Status.AUTHFAIL || 
								status == Strophe.Status.SBMTFAIL || status == Strophe.Status.REGIFAIL) {
					setConnectionStatus(SpacesSDK.ConnectionStatus.ERROR);
				} else if (status == Strophe.Status.AUTHENTICATING || status == Strophe.Status.DISCONNECTING || status == Strophe.Status.CONNECTING || 
								status == Strophe.Status.REGISTERING || status == Strophe.Status.SUBMITTING) {
					setConnectionStatus(SpacesSDK.ConnectionStatus.PENDING);
				} else if (status == Strophe.Status.DISCONNECTED) {
					setConnectionStatus(SpacesSDK.ConnectionStatus.OFFLINE);
				}
			});
		};
		
		
		/**
		 * Disconnect the client from the XMPP server.
		 * @function
		 * @memberOf SpacesSDK.ConnectionHandler.prototype
		 */
		var disconnect = function() {
			if (status == SpacesSDK.ConnectionStatus.OFFLINE) {
				return;
			}
			setConnectionStatus(SpacesSDK.ConnectionStatus.PENDING);
			connection.disconnect();
			setConnectionStatus(SpacesSDK.ConnectionStatus.OFFLINE);
		};
		
		/**
		 * Returns the connection configuration.
		 * @function
		 * @memberOf SpacesSDK.ConnectionHandler.prototype
		 * @return {SpacesSDK.ConnectionConfiguration} Current connection configuration. 
		 */
		var getConfiguration = function() {
			return connectionConfiguration;
		};
		
		/**
		 * Returns the user information of the currently logged in user.
		 * @function
		 * @memberOf SpacesSDK.ConnectionHandler.prototype
		 * @return {SpacesSDK.UserInfo} User information, including the unique identifer within the XMPP network.
		 */
		var getCurrentUser = function() {
			return userInfo;
		};
		
		/**
		 * Returns information about the XMPP network.
		 * @function
		 * @memberOf SpacesSDK.ConnectionHandler.prototype
		 * @return {SpacesSDK.NetworkInformation} Network information object. 
		 */
		var getNetworkInformation = function() {
			return networkInfo;
		};
				
		/**
		 * Returns the connection status.
		 * @function
		 * @memberOf SpacesSDK.ConnectionHandler.prototype
		 * @return {SpacesSDK.ConnectionStatus} Current connection status of this handler.
		 */
		var getStatus = function() {
			return status;
		};
		
		/**
		 * Returns the connection object from the underlying Strophe.js library used by this implementation.
		 * With the connection object the features provided by the XMPP library can be accessed directly.
		 * @function
		 * @memberOf SpacesSDK.ConnectionHandler.prototype
		 * @return {Strophe.Connection} Strophe.js connection handler. 
		 */
		var getXMPPConnection = function() {
			return connection;
		};
		
		/**
		 * Removes a connection status listener.
		 * If the listener is not set, nothing will happen.
		 * @function
		 * @memberOf SpacesSDK.ConnectionHandler.prototype
		 * @param {SpacesSDK.ConnectionStatusListner} listener Listener to remove.
		 */
		var removeConnectionStatusListener = function(listenerToRemove) {
			var newListeners = [];
			for (var listenerName in listeners) {
				if (listenerToRemove.name === listenerName) continue;
				newListeners[listenerName] = listeners[listenerName];
			}
			listeners = newListeners;
		};
		
		var funcs = {
			'addConnectionStatusListener' : addConnectionStatusListener,
			'connect' : connect,
			'connectAndCreateUser' : connectAndCreateUser,
			'disconnect' : disconnect,
			'getConfiguration' : getConfiguration,
			'getCurrentUser' : getCurrentUser,
			'getNetworkInformation' : getNetworkInformation,
			'getStatus' : getStatus,
			'getXMPPConnection' : getXMPPConnection,
			'removeConnectionStatusListener' : removeConnectionStatusListener
		};
		
		return funcs;
	};

	/**
	 * Instantiates a connection status listener.
	 * @class Handler called when the connection status changes.
	 * @name ConnectionStatusListener
	 * @memberOf SpacesSDK
	 * @param {string} id Unique identifier for the listener.
	 * @param {SpacesSDK.ConnectionStatusListener~onEvent} onEvent The function to called when the status is changed.
	 */
	var ConnectionStatusListener = function(id, onEvent) {
		var listener = new Object();
		listener.name = id;
		listener.connectionStatusChanged = onEvent;
		return listener;
	};
	
	/**
	 * Callback for {@link SpacesSDK.ConnectionStatusListener}.
	 * @callback SpacesSDK.ConnectionStatusListener~onEvent
	 * @param {SpacesSDK.ConnectionStatus} newStatus New status of the connection handler.
	 */
	
	/**
	 * Creates a new data handler.
	 * @class Implementation of the data handler API.
	 * @name DataHandler
	 * @memberOf SpacesSDK
	 * @param {SpacesSDK.ConnectionHandler} connectionHandler The connection handler to be used for requests.
	 * @param {SpacesSDK.SpaceHandler} spaceHandler An instance of a space handler to be used for requesting space properties.
	 */
	var DataHandler = function(connectionHandler, spaceHandler) {
		var listeners = null;
		var handledSpaces = null;
		var dataObjectFilter = null;
		var connection = null;
		var userInfo = null;
		var dataObjects = null;
		var timeout = null;
		
		(function() { // constructor
			listeners = new Object();
			handledSpaces = new Object();
			connection = connectionHandler.getXMPPConnection();
			userInfo = connectionHandler.getCurrentUser();
			dataObjects = new Object();
			timeout = connectionHandler.getConfiguration().requestTimeout();
			connection.addHandler(handlePublishedItems, null, 'message', null, null, null);
		})();
		
		/**
		 * Gets the id of the space with the given nodeId.
		 * @private
		 * @function
		 * @memberOf SpacesSDK.DataHandler.prototype
		 * @param {string} nodeId The nodeId to look for.
		 * @return {string} A spaceId or <code>null</code> if no space was found.
		 */
		function getSpaceId(nodeId) {
			for (var spaceId in handledSpaces) {
				var space = handledSpaces[spaceId];
				var properties = space.getPubSubChannel().getProperties();
				if (properties.node === nodeId) {
					return spaceId;
				}
			}
			return null;
		};
		
		/**
		 * Adds a list of data objects to the local cache.
		 * @private
		 * @param {string} spaceId Identifier of the space the data objects were retrieved from.
		 * @param {SpacesSDK.DataObject[]} dataObjectsToAdd List of data objects to add. Duplicates are ignored. 
		 */
		function addDataObjectsToCache(spaceId, dataObjectsToAdd) {
			if (!dataObjects[spaceId]) {
				dataObjects[spaceId] = [];
			}
			
			for (var i = 0; i < dataObjectsToAdd.length; i++) {
				var dataObject = dataObjectsToAdd[i];
				if (!dataObject.getId) continue;
				var dataObjectFound = false;
				for (var j = 0; j < dataObjects[spaceId].length; j++) {
					var dataObjectStored = dataObjects[spaceId][j];
					if (!dataObjectStored.getId && (dataObject.getId() == dataObjectStored.getId())) {
						dataObjectFound = true;
						break;
					}
				}
				if (!dataObjectFound) {
					dataObjects[spaceId].push(dataObject);
				}
			}
		}

		
		/**
		 * Parses all received Items to DataObjects and requests the payloads if necessary.
		 * @private
		 * @function
		 * @memberOf SpacesSDK.DataHandler.prototype
		 * @param {XMLElement} message The message received.
		 * @return {SpacesSDK.DataObject[]} An array of all received data objects.
		 */
		function parseItems(message) {
			var itemIdsToRequest = [];
			var result = [];
			var nodeId = message.getElementsByTagName("items")[0];
			if (nodeId && nodeId != null) {
				var node = nodeId.attributes.getNamedItem('node');
				if (node && node.value) nodeId = node.value;
				else if (node && node.nodeValue) nodeId = node.nodeValue;
				else return null;
				var spaceId = getSpaceId(nodeId);
				if (spaceId == null || !handledSpaces[spaceId]) {
					return;
				}
				var items = message.getElementsByTagName("item");
				for (var i=0; i<items.length; i++) {
					if (items[i].firstChild == null) {
						if (items[i].attributes.getNamedItem('id').value) {
							itemIdsToRequest[itemIdsToRequest.length] = items[i].attributes.getNamedItem('id').value;
						} else itemIdsToRequest[itemIdsToRequest.length] = items[i].attributes.getNamedItem('id').nodeValue;
					} else {
						var item = items[i].firstChild;
						// create a XML document based on the element
						var xmlDocument = Utils.createXMLDocumentWithElement(item);
						var dataObject = new DataObject(xmlDocument.childNodes[0]);
						if (!dataObjectFilter || dataObjectFilter.isDataObjectValid(dataObject)) {
							addDataObjectsToCache(spaceId, [dataObject]);
						}
						result[result.length] = dataObject;
					}
				}
				if (itemIdsToRequest.length > 0) {
					requestPayload(nodeId, itemIdsToRequest);
				}
				return result;
			}
		};
		
		/**
		 * Item handler of this data handler implementation. Checks if received items are already cached,
		 * if not, it caches them. Ultimately it sends the items to the subscribed listeners.
		 * @private
		 * @function
		 * @memberOf SpacesSDK.DataHandler.prototype
		 */
		function handlePublishedItems(message) {
			var nodeId = message.getElementsByTagName('items')[0];
			if (nodeId && nodeId != null) {
				var node = nodeId.attributes.getNamedItem('node');
				if (node && node.value) nodeId = node.value;
				else if (node && node.nodeValue) nodeId = node.nodeValue;
				else return true;
				if (!nodeId || nodeId == null) return true;
				var spaceId = getSpaceId(nodeId);
				if (!spaceId || spaceId == null) return true;
				var dataObjects = parseItems(message);
				if (!dataObjects || dataObjects == null) return true;
				for (var listenerName in listeners) {
					var listener = listeners[listenerName];
					for (var i = 0; i < dataObjects.length; i++) {
						var dataObject = dataObjects[i];
						if (!dataObjectFilter || dataObjectFilter.isDataObjectValid(dataObject)) {
							listener.handleDataObject(dataObject, spaceId);
						}
					}
				}
			}
			return true;
		};
		
		/**
		 * Sends a request to the server to retrieve the payload of an item.
		 * @private
		 * @function
		 * @memberOf SpacesSDK.DataHandler.prototype
		 * @param {string} nodeId The id of the node the item is from.
		 * @param {string[]} itemsToRequest Array with all itemids to request the payload for.
		 */
		function requestPayload(nodeId, itemsToRequest) {
			for (var i=0; i<itemsToRequest.length; i++) {
				var itemToRequest = itemsToRequest[i];
				var iq = $iq({from:this.jid, to:this.service, type:'get'})
				.c('pubsub', { xmlns:Strophe.NS.PUBSUB })
				.c('items', {node:nodeId})
				.c('item', {id:itemToRequest});
				connection.sendIQ(iq.tree(), function(){}, function(){}, timeout);
			}
		};
		
		/**
		 * Adds a listener for data objects published on any space handled by this handler.
		 * @function
		 * @memberOf SpacesSDK.DataHandler.prototype
		 * @param {SpacesSDK.DataObjectListener} listener Listener to add.
		 *    
		 */
		var addDataObjectListener = function(listener) {
			listeners[listener.name] = listener;
		};
		

		/**
		 * Returns the list of spaces handled by this handler.
		 * Use registerSpace() and removeSpace() to modify this list. 
		 * @function
		 * @memberOf SpacesSDK.DataHandler.prototype
		 * @return {SpacesSDK.Space[]} An Object containing all Spaces currently handled. May be empty.
		 */
		var getHandledSpaces = function() {
			var result = new Object();
			for (var i in handledSpaces) {
				result[i] = handledSpaces[i];
			}
			return result;			
		};
		
		/**
		 * Publishes a data object on the space with the given id.
		 * The object is directly published on the related pubsub node.
		 * @function
		 * @memberOf SpacesSDK.DataHandler.prototype
		 * @param {SpacesSDK.DataObject} dataObject DataObject to publish.
		 * @param {string} spaceId Identifier if the space to publish.
		 * @param {SpacesSDK.DataHandler~publishDataObject$onSuccess} [onSuccess] Function called if publishing was successful. Optional.
		 * @param {SpacesSDK.DataHandler~publishDataObject$onError} [onError] Function called if the publishing failed. If not set, a console output is generated.
		 */
		var publishDataObject = function(dataObject, spaceId, onSuccess, onError) {
			// retrieve pubsub channel
			if (!onSuccess) onSuccess = function() {};
			if (!onError) onError = function(error) {
				console.warn('Failed to publish data object:', error);
			};
			spaceHandler.getSpace(spaceId, function(space) {
				if (space == null) {
					onError('A space with the given ID could not be retrieved.');
					return;
				}
				var nodeId = space.getPubSubChannel().getProperties().node;
				var element = dataObject.getElement();
				var item = Utils.elementToStopheTree(element);
				var items = new Array();
				items[0] = new Object();
				items[0].attrs = {};
				items[0].data = item.tree();
				connection.pubsub.publish(nodeId, items, function(resultIq) {
					switch (resultIq.getAttribute('type')) {
					case 'result':
						onSuccess();
						break;
					default:
						onError(resultIq);
						break;
					}
				});
			}, function(error) {
				onError(error);
			});
		};
		
		/**
		 * Function called if publishing was successful.
		 * Callback for {@link SpacesSDK.DataHandler#publishDataObject|DataHandler.publishDataObject()}.
		 * @callback SpacesSDK.DataHandler~publishDataObject$onSuccess
		 */
		
		/**
		 * Function called if the publishing of a data object failed.
		 * Callback for {@link SpacesSDK.DataHandler#publishDataObject|DataHandler.publishDataObject()}.
		 * @callback SpacesSDK.DataHandler~publishDataObject$onError
		 * @param {string|XMLElement} error Error message OR error IQ (if available).
		 */
		
		/**
		 * Adds a space to be observed by this data handler.
		 * If the space is already in the list of observed spaces, nothing happens.
		 * @function
		 * @memberOf SpacesSDK.DataHandler.prototype
		 * @param {string} spaceId Identifier of the space to observe.
		 * @param {SpacesSDK.DataHandler~registerSpace$onError} [onError] Function called when an error occures. If not set, the error is printed to the console.
		 */
		var registerSpace = function(spaceId, onError) {
			if (handledSpaces[spaceId]) {
				return;
			}
			if (!onError) onError = function(error) {
				console.warn('An error occured while the space was registered.', error);
			};
			spaceHandler.getSpace(spaceId, function(space) {
				handledSpaces[space.getId()] = space;
				dataObjects[space.getId()] = [];
			}, function(error) {
				onError(error);
			});
		};
		
		/**
		 * Function called when an error occures.
		 * Callback for {@link SpacesSDK.DataHandler#registerSpace|DataHandler.registerSpace()}
		 * @callback SpacesSDK.DataHandler~registerSpace$onError
		 * @param {string} error Error object. 
		 */
		
		/**
		 * Removes a data object listener.
		 * @function
		 * @memberOf SpacesSDK.DataHandler.prototype
		 * @param {SpacesSDK.DataObjectListener} listener Listener to remove.
		 */
		var removeDataObjectListener = function(listenerToRemove) {
			var newListeners = new Object();
			for (var listenerName in listeners) {
				if (listenerToRemove.name === listenerName) continue;
				newListeners[listenerName] = listeners[listenerName];
			}
			listeners = newListeners;
		};
		
		/**
		 * Removes a space from the list of observed spaces.
		 * If the space is not in the list of observed spaces, nothing happens.
		 * @function
		 * @memberOf SpacesSDK.DataHandler.prototype
		 * @param {string} spaceId Identifier of the space to observe.
		 */
		var removeSpace = function(spaceId) {
			if (!handledSpaces[spaceId]) {
				return;
			}
			else {
				var newHandledSpaces = [];
				for (var i in handledSpaces) {
					if (i === spaceId) continue;
					newHandledSpaces[i] = handledSpaces[i];
				}
				handledSpaces = newHandledSpaces;
				delete dataObjects[spaceId];
			}
		};
		
		/**
		 * Returns a unmodifiable list of data objects previously published on the space.
		 * The data objects are retrieved from the local cache and contain the latest items published.
		 * The amount of objects cache depend on the handler implementation.
		 * @function
		 * @memberOf SpacesSDK.DataHandler.prototype
		 * @deprecated
		 * @param {string} spaceId Identifier of the space to retrieve data objects for.
		 * @return {SpacesSDK.DataObject[]} List of data objects, in reverse order of their publishing time, i.e., the latest first. May be empty.
		 */
		var retrieveDataObjects = function(spaceId) {
			return dataObjects[spaceId] ? dataObjects[spaceId] : [];
		};
		
		/**
		 * Sets an data object filter to be applied for incoming data objects.
		 * @function
		 * @memberOf SpacesSDK.DataHandler.prototype
		 * @param {SpacesSDK.filter} filter Data object filter to set.
		 */
		var setDataObjectFilter = function(filter) {
			dataObjectFilter = filter;
		};
		
		/**
		 * Returns the data object filter applied to this data handler.
		 * @function
		 * @memberOf SpacesSDK.DataHandler.prototype
		 * @return {SpacesSDK.filter} Data object filter or null of no data object filter is set.
		 */
		var getDataObjectFilter = function() {
			return dataObjectFilter;
		};
		
		/**
		 * Performs a query to to persistence service.
		 * Utilized is queryDataObject* methods.
		 * @private 
		 * @memberOf SpacesSDK.DataHandler.prototype
		 */
		function performQuery(queryIq, onSuccess, onError) {
			var dataObjects = [];
			connection.sendIQ(queryIq, function (resultIQ) {
				var resultElement = resultIQ.getElementsByTagName('result')[0];
				for (var i = 0; i < resultElement.childNodes.length; i++) {
					var dataObjectElement = resultElement.childNodes[i];
					var dataObject = new DataObject(dataObjectElement);
					dataObjects.push(dataObject);
				}
				onSuccess(dataObjects);
			}, function (errorIQ) {
				if (errorIQ) {
					var errorElement = errorIQ.getElementsByTagName('error')[0];
					onError('Peristence service query failed: ' + Utils.generateXMLString(errorElement));
				} else {
					onError('Persistence service query failed.');
				}
			}, connectionHandler.getConfiguration().requestTimeout());
		}
		
		/**
		 * Queries a data object by its identifier.
		 * Requires a persistence service to be available.
		 * @function
		 * @memberOf SpacesSDK.DataHandler.prototype
		 * @param {string} objectId Identifier for the data object to query.
		 * @param {SpacesSDK.DataHandler~queryDataObjectById$onSuccess} onSuccess Function to call if the request succeeds.
		 * @param {SpacesSDK.DataHandler~queryDataObjectById$onError} onError Function to call if an error occures.
		 */
		var queryDataObjectById = function(objectId, onSuccess, onError) {
			if (!onError) {
				onError = function(message) {
					console.warn('[ERROR] ' + message);
				};
			}
			
			if (connectionHandler.getStatus() != ConnectionStatus.ONLINE) {
				onError('You must be online in order to query data.'); return;
			}
			var persistenceServiceJID = connectionHandler.getNetworkInformation().getPersistenceServiceJID();
			if (!persistenceServiceJID) {
				onError('No persistence service available.');
			}
			
			var queryIq = $iq({
				from: connectionHandler.getCurrentUser().getFullJID(),
				to: persistenceServiceJID,
				type: 'get'
			}).c('query', {xmlns: 'urn:xmpp:spaces:persistence'});
			queryIq.c('object', {id: objectId});
			
			performQuery(queryIq, function(dataObjects) {
				onSuccess(dataObjects.length > 0 ? dataObjects[0] : null);
			}, onError);
		};
		
		/**
		 * Function to call if the request succeeds.
		 * Callback for {SpacesSDK.DataHandler#queryDataObjectById|DataHandler.queryDataObjectById()}.
		 * @callback SpacesSDK.DataHandler~queryDataObjectById$onSuccess
		 * @param {SpacesSDK.DataObject} dataObject Data object for the given ID or <code>null</code> if no data object with the give id was found.
		 */
		
		/**
		 * Function to call if an error occures.
		 * Callback for {SpacesSDK.DataHandler#queryDataObjectById|DataHandler.queryDataObjectById()}.
		 * @callback SpacesSDK.DataHandler~queryDataObjectById$onError
		 * @param {string} error Error message.
		 */
		
		/**
		 * Queries multiple data objects by their identifier.
		 * Requires a persistence service to be available.
		 * @function
		 * @memberOf SpacesSDK.DataHandler.prototype
		 * @param {string[]} objectIds A list of data object identifiers.
		 * @param {SpacesSDK.filter[]} filters A list of filters to apply. All filters are chained, i.e. <code>AndFilter</code> and <code>OrFilter</code> are ignored. The list may be empty.
		 * @param {SpacesSDK.DataHandler~queryDataObjectsById$onSuccess} onSuccess Function to call if the request succeeds.
		 * @param {SpacesSDK.DataHandler~queryDataObjectsById$onError} onError Function to call if an error occures.
		 */
		var queryDataObjectsById = function(objectIds, filters, onSuccess, onError) {
			if (!onError) {
				onError = function(message) {
					console.warn('[ERROR] ' + message);
				};
			}
			
			if (connectionHandler.getStatus() != ConnectionStatus.ONLINE) {
				onError('You must be online in order to query data.'); return;
			}
			var persistenceServiceJID = connectionHandler.getNetworkInformation().getPersistenceServiceJID();
			if (!persistenceServiceJID) {
				onError('No persistence service available.');
			}
			
			var dataObjects = [];
			
			if (0 == objectIds.length) {
				onSuccess(dataObjects);
				return;
			}
			
			var queryIq = $iq({
				from: connectionHandler.getCurrentUser().getFullJID(),
				to: persistenceServiceJID,
				type: 'get'
			}).c('query', {xmlns: 'urn:xmpp:spaces:persistence'});
			queryIq.c('objects');
			for (var i = 0; i < objectIds.length; i++) {
				queryIq.c('object', {id: objectIds[i]}).up();
			}
			if (filters && filters.length > 0) {
				queryIq.up().c('filters');
				for (var i = 0; i < filters.length; i++) {
					var filter = filters[i];
					if (filter.getFilterAsXML) {
						var xmlElement = filter.getFilterAsXML('urn:xmpp:spaces:persistence');
						Utils.elementToStopheTree(xmlElement, queryIq);
					}
				}
			}
										
			performQuery(queryIq, onSuccess, onError);
		};
		
		/**
		 * Function to call if the request succeeds.
		 * Callback for {SpacesSDK.DataHandler#queryDataObjectsById|DataHandler.queryDataObjectsById()}.
		 * @callback SpacesSDK.DataHandler~queryDataObjectsById$onSuccess
		 * @param {SpacesSDK.DataObject[]} dataObjects List of data objects. May be empty.
		 */
		
		/**
		 * Function to call if an error occures.
		 * Callback for {SpacesSDK.DataHandler#queryDataObjectsById|DataHandler.queryDataObjectsById()}.
		 * @callback SpacesSDK.DataHandler~queryDataObjectsById$onError
		 * @param {string} error Error message.
		 */
		
		/**
		 * Fallback for queryDataObjectsBySpace if no persistence service is available.
		 * @function
		 * @private
		 * @memberOf SpacesSDK.DataHandler.prototype
		 * @param {string} spaceId Identifier of the space to request data objects from.
		 * @param {SpacesSDK.filter[]} filters A list of filters to apply. All filters are chained, i.e. <code>AndFilter</code> and <code>OrFilter</code> are ignored. The list may be empty.
		 * @param {function} onSuccess Function to call if the request succeeds. Parameters: A list of all data objects (SpacesSDK.DataObject) of the space which pass the filters, may be empty.
		 * @param {function} onError Function to call if an error occures. Parameters: Error message.
		 */
		function retrieveDataObjectsFromPubsubNode(spaceId, filters, onSuccess, onError) {
			var dataObjects = [];
			spaceHandler.getSpace(spaceId, function(space) {
				if (space.getPersistenceType() == PersistenceType.ON) {
					// request content of pubsub node
					connection.pubsub.items(space.getPubSubChannel().getProperties().node, function(message) {
						var itemIdsToRequest = [];
						var result = [];
						var nodeId = message.getElementsByTagName('items')[0];
						if (nodeId) {
							var node = nodeId.attributes.getNamedItem('node');
							if (node && node.value) {
								nodeId = node.value;
							} else if (node && node.nodeValue) {
								nodeId = node.nodeValue;
							} else {
								onError('Failed to retrieve items from pubsub node.');
							};
							var items = message.getElementsByTagName('item');
							for (var i=0; i<items.length; i++) {
								if (items[i].firstChild == null) {
									if (items[i].attributes.getNamedItem('id').value) {
										itemIdsToRequest[itemIdsToRequest.length] = items[i].attributes.getNamedItem('id').value;
									} else itemIdsToRequest[itemIdsToRequest.length] = items[i].attributes.getNamedItem('id').nodeValue;
								}
								else {
									var item = items[i].firstChild;
									// create a XML document based on the element
									var xmlDocument = Utils.createXMLDocumentWithElement(item);
									var dataObject = new DataObject(xmlDocument.firstChild);
									var isValid = true;
									for (var j = 0; j < filters.length; j++) {
										isValid &= filters[j].isDataObjectValid(dataObject);
									}
									if (isValid) dataObjects.push(dataObject);
								}
							}
							addDataObjectsToCache(spaceId, dataObjects);
							onSuccess(dataObjects);
						} else {
							onSuccess(dataObjects);
						}
					}, function(error) {
						onError('Failed to retrieve data objects from pubsub node.', error);
					}, timeout);						
				} else {
					// Nothing persisted - noting to return.
					onSuccess(dataObjects);
				}
			}, function(error){
				onError('Failed to retrieve space information. ', error);
			});
		}
		
		/**
		 * Queries all data objects of a single space which fits the given filters.
		 * If a persistence service is available, the data objects are requested from there and the filters are applied server-side. Otherwise all data is retrieved from the pubsub node and filtering is done at client-side.
		 * @function
		 * @memberOf SpacesSDK.DataHandler.prototype
		 * @param {string} spaceId Identifier of the space to request data objects from.
		 * @param {SpacesSDK.filter[]} filters A list of filters to apply. All filters are chained, i.e. <code>AndFilter</code> and <code>OrFilter</code> are ignored. The list may be empty.
		 * @param {SpacesSDK.DataHandler~queryDataObjectsBySpace$onSuccess} onSuccess Function to call if the request succeeds.
		 * @param {SpacesSDK.DataHandler~queryDataObjectsBySpace$onError} onError Function to call if an error occures.
		 */
		var queryDataObjectsBySpace = function(spaceId, filters, onSuccess, onError) {
			if (!onError) {
				onError = function(message) {
					console.warn('[ERROR] ' + message);
				};
			}
			
			if (connectionHandler.getStatus() != ConnectionStatus.ONLINE) {
				onError('You must be online in order to query data.'); return;
			}
			var persistenceServiceJID = connectionHandler.getNetworkInformation().getPersistenceServiceJID();
			if (!persistenceServiceJID) {
				// Fallback to pubsub node.
				retrieveDataObjectsFromPubsubNode(spaceId, filters, onSuccess, onError);
				return;
			}
			
			var dataObjects = [];
			
			var queryIq = $iq({
				from: connectionHandler.getCurrentUser().getFullJID(),
				to: persistenceServiceJID,
				type: 'get'
			}).c('query', {xmlns: 'urn:xmpp:spaces:persistence'});
			queryIq.c('objectsForSpace', {id: spaceId});
			if (filters && filters.length > 0) {
				queryIq.up().c('filters');
				for (var i = 0; i < filters.length; i++) {
					var filter = filters[i];
					if (filter.getFilterAsXML) {
						var xmlElement = filter.getFilterAsXML('urn:xmpp:spaces:persistence');
						Utils.elementToStopheTree(xmlElement, queryIq);
					}
				}
			}
			performQuery(queryIq, function(receivedDataObjects) {
				addDataObjectsToCache(spaceId, receivedDataObjects);
				onSuccess(receivedDataObjects);
			}, onError);
		};
		
		/**
		 * Function to call if the request succeeds.
		 * Callback for {SpacesSDK.DataHandler#queryDataObjectsBySpace|DataHandler.queryDataObjectsBySpace()}.
		 * @callback SpacesSDK.DataHandler~queryDataObjectsBySpace$onSuccess
		 * @param {SpacesSDK.DataObject[]} dataObjects A list of all data objects of the space which passed the filters. May be empty.
		 */
		
		/**
		 * Function to call if an error occures.
		 * Callback for {SpacesSDK.DataHandler#queryDataObjectsBySpace|DataHandler.queryDataObjectsBySpace()}.
		 * @callback SpacesSDK.DataHandler~queryDataObjectsBySpace$onError
		 * @param {string} error Error message.
		 */
		
		/**
		 * Queries all data objects from multiple spaces which fits the given filters.
		 * If a persistence service is available, the data objects are requested from there and the filters are applied server-side. Otherwise all data is retrieved from the pubsub node and filtering is done at client-side.
		 * @function
		 * @memberOf SpacesSDK.DataHandler.prototype
		 * @param {string[]} spaceIds List of identifiers for the spaces to request data objects from.
		 * @param {SpacesSDK.filter[]} filters A list of filters to apply. All filters are chained, i.e. <code>AndFilter</code> and <code>OrFilter</code> are ignored. The list may be empty.
		 * @param {SpacesSDK.DataHandler~queryDataObjectsBySpaces$onSuccess} onSuccess Function to call if the request succeeds.
		 * @param {SpacesSDK.DataHandler~queryDataObjectsBySpaces$onError} onError Function to call if an error occures.
		 */
		var queryDataObjectsBySpaces = function(spaceIds, filters, onSuccess, onError) {
			if (!onError) {
				onError = function(message) {
					console.warn('[ERROR] ' + message);
				};
			}
			
			if (connectionHandler.getStatus() != ConnectionStatus.ONLINE) {
				onError('You must be online in order to query data.'); return;
			}
			
			var dataObjects = [];
			if (spaceIds.length == 0) {
				// Nothing to do.
				onSuccess(dataObjects);
				return;
			}
			
			var persistenceServiceJID = connectionHandler.getNetworkInformation().getPersistenceServiceJID();
			if (!persistenceServiceJID) {
				var numReponses = 0;
				for (var i = 0; i < spaceIds.length; i++) {
					retrieveDataObjectsFromPubsubNode(spaceIds[i], filters, function(receivedDataObjects) {
						dataObjects = dataObjects.concat(receivedDataObjects);
						numReponses += 1;
						if (numReponses == spaceIds.length) {
							onSuccess(dataObjects);
						}
					}, function(error) {
						onError(error);
					});
				}
				return;
			}
			
			var queryIq = $iq({
				from: connectionHandler.getCurrentUser().getFullJID(),
				to: persistenceServiceJID,
				type: 'get'
			}).c('query', {xmlns: 'urn:xmpp:spaces:persistence'});
			queryIq.c('objectsForSpaces');
			for (var i = 0; i < spaceIds.length; i++) {
				queryIq.c('space', {id: spaceIds[i]}).up();
			}
			if (filters && filters.length > 0) {
				queryIq.up().c('filters');
				for (var i = 0; i < filters.length; i++) {
					var filter = filters[i];
					if (filter.getFilterAsXML) {
						var xmlElement = filter.getFilterAsXML('urn:xmpp:spaces:persistence');
						Utils.elementToStopheTree(xmlElement, queryIq);
					}
				}
			}
										
			performQuery(queryIq, onSuccess, onError);
		};
		
		/**
		 * Function to call if the request succeeds.
		 * Callback for {SpacesSDK.DataHandler#queryDataObjectsBySpaces|DataHandler.queryDataObjectsBySpaces()}.
		 * @callback SpacesSDK.DataHandler~queryDataObjectsBySpaces$onSuccess
		 * @param {SpacesSDK.DataObject[]} dataObjects A list of all data objects of the spaces which passed the filters. May be empty.
		 */
		
		/**
		 * Function to call if an error occures.
		 * Callback for {SpacesSDK.DataHandler#queryDataObjectsBySpaces|DataHandler.queryDataObjectsBySpaces()}.
		 * @callback SpacesSDK.DataHandler~queryDataObjectsBySpaces$onError
		 * @param {string} error Error message.
		 */
		
		/**
		 * Deletes a single data object from the storage. Only space moderators are allowed to perform this operation.
		 * @function
		 * @memberOf SpacesSDK.DataHandler.prototype
		 * @param {string} objectId Identifier of the data object to delete.
		 * @param {SpacesSDK.DataHandler~deleteDataObject$onSuccess} onSuccess Function to call if the request succeeds.
		 * @param {SpacesSDK.DataHandler~deleteDataObject$onError} onError Function to call if an error occures.
		 */
		var deleteDataObject = function(objectId, onSuccess, onError) {
			if (!onError) {
				onError = function(message) {
					console.warn('[ERROR] ' + message);
				};
			}
			
			if (connectionHandler.getStatus() != ConnectionStatus.ONLINE) {
				onError('You must be online in order to perform the query.'); return;
			}
			var persistenceServiceJID = connectionHandler.getNetworkInformation().getPersistenceServiceJID();
			if (!persistenceServiceJID) {
				onError('No persistence service available.'); return;
			}
			
			var deleteIq = $iq({
				from: connectionHandler.getCurrentUser().getFullJID(),
				to: persistenceServiceJID,
				type: 'set'
			}).c('delete', {xmlns: 'urn:xmpp:spaces:persistence'});
			deleteIq.c('object', {id: objectId});
										
			connection.sendIQ(deleteIq, function (resultIQ) {
				var deleteElement = resultIQ.getElementsByTagName('delete')[0];
				var deletedObjects = deleteElement.getAttribute('objectsDeleted');
				onSuccess(parseInt(deletedObjects) > 0);
			}, function (errorIQ) {
				if (errorIQ) {
					var errorElement = errorIQ.getElementsByTagName('error')[0];
					onError('Peristence service query failed: ' + Utils.generateXMLString(errorElement));
				} else {
					onError('Persistence service query failed.');
				}
			}, connectionHandler.getConfiguration().requestTimeout());
		};
		
		/**
		 * Function to call if the request succeeds.
		 * Callback for {SpacesSDK.DataHandler#deleteDataObject|DataHandler.deleteDataObject()}.
		 * @callback SpacesSDK.DataHandler~deleteDataObject$onSuccess
		 * @param {boolean} isDeleted <code>true</code> if the data object was found and deleted, otherwise <code>false</code>.
		 */
		
		/**
		 * Function to call if an error occures.
		 * Callback for {SpacesSDK.DataHandler#deleteDataObject|DataHandler.deleteDataObject()}.
		 * @callback SpacesSDK.DataHandler~deleteDataObject$onError
		 * @param {string} error Error message.
		 */
		
		/**
		 * Deletes multiple data objects from the storage. Only space moderators are allowed to perform this operation.
		 * @function
		 * @memberOf SpacesSDK.DataHandler.prototype
		 * @param {string[]} objectIds List of identifiers for the data objects to delete.
		 * @param {SpacesSDK.DataHandler~deleteDataObjects$onSuccess} onSuccess Function to call if the request succeeds.
		 * @param {SpacesSDK.DataHandler~deleteDataObjects$onError} onError Function to call if an error occures.
		 */
		var deleteDataObjects = function(objectIds, onSuccess, onError) {
			if (!onError) {
				onError = function(message) {
					console.warn('[ERROR] ' + message);
				};
			}
			
			if (objectIds.length == 0) {
				onSuccess(0);
				return;
			}
			
			if (connectionHandler.getStatus() != ConnectionStatus.ONLINE) {
				onError('You must be online in order to perform the query.'); return;
			}
			var persistenceServiceJID = connectionHandler.getNetworkInformation().getPersistenceServiceJID();
			if (!persistenceServiceJID) {
				onError('No persistence service available.'); return;
			}
			
			var deleteIq = $iq({
				from: connectionHandler.getCurrentUser().getFullJID(),
				to: persistenceServiceJID,
				type: 'set'
			}).c('delete', {xmlns: 'urn:xmpp:spaces:persistence'});
			deleteIq.c('objects');
			for (var i = 0; i < objectIds.length; i++) {
				deleteIq.c('object', {id: objectIds[i]}).up();
			}
										
			connection.sendIQ(deleteIq, function (resultIQ) {
				var deleteElement = resultIQ.getElementsByTagName('delete')[0];
				var deletedObjects = deleteElement.getAttribute('objectsDeleted');
				onSuccess(parseInt(deletedObjects));
			}, function (errorIQ) {
				if (errorIQ) {
					var errorElement = errorIQ.getElementsByTagName('error')[0];
					onError('Peristence service query failed: ' + Utils.generateXMLString(errorElement));
				} else {
					onError('Persistence service query failed.');
				}
			}, connectionHandler.getConfiguration().requestTimeout());
		};
		
		/**
		 * Function to call if the request succeeds.
		 * Callback for {SpacesSDK.DataHandler#deleteDataObjects|DataHandler.deleteDataObjects()}.
		 * @callback SpacesSDK.DataHandler~deleteDataObjects$onSuccess
		 * @param {number} numDeletedObjects Number of data objects deleted.
		 */
		
		/**
		 * Function to call if an error occures.
		 * Callback for {SpacesSDK.DataHandler#deleteDataObjects|DataHandler.deleteDataObjects()}.
		 * @callback SpacesSDK.DataHandler~deleteDataObjects$onError
		 * @param {string} error Error message.
		 */
		
		var funcs = {
			'addDataObjectListener' : addDataObjectListener,
			'getHandledSpaces' : getHandledSpaces,
			'publishDataObject' : publishDataObject,
			'registerSpace' : registerSpace,
			'removeDataObjectListener' : removeDataObjectListener,
			'removeSpace' : removeSpace,
			'retrieveDataObjects' : retrieveDataObjects,
			'setDataObjectFilter': setDataObjectFilter,
			'getDataObjectFilter': getDataObjectFilter,
			'queryDataObjectById' : queryDataObjectById,
			'queryDataObjectsById' : queryDataObjectsById,
			'queryDataObjectsBySpace' : queryDataObjectsBySpace,
			'queryDataObjectsBySpaces' : queryDataObjectsBySpaces,
			'deleteDataObject' : deleteDataObject,
			'deleteDataObjects' : deleteDataObjects
		};
		return funcs;
	};
	
	/**
	 * Creates a new data model.
	 * @class A data model consists of a name space and a location of a XML schema definition. The XSD file specifies the data model in detail.  
	 * @name DataModel
	 * @memberOf SpacesSDK
	 * @param {string} namespace The namespace of the data model.
	 * @param {string} schemaLocation The location of a XSD schema for the data model.
	 */
	var DataModel = function(namespace, schemaLocation) {
		
		/**
		 * Returns the namespace of this data model.
		 * @function
		 * @memberOf SpacesSDK.DataModel.prototype
		 * @return {string} Namespace URI
		 */
		var getNamespace = function() {
			return namespace;
		};
		
		/**
		 * Returns the location of the schema specifying the data model.
		 * @function
		 * @memberOf SpacesSDK.DataModel.prototype
		 * @return {string} URL of the related XSD file.
		 */
		var getSchemaLocation = function() {
			return schemaLocation;
		};
		
		/**
		 * @private
		 */
		var equals = function(e) {
			if (e == null) {
				return false;
			}
			if (e.getNamespace == null || typeof e.getNamespace !== "function") {
					return false;
			} else if (e.getSchemaLocation == null || typeof e.getSchemaLocation !== "function") {
					return false;
			} 
			else if (e.getSchemaLocation() === schemaLocation && e.getNamespace() === namespace) {
				return true;
			}
			return false;
		};
		
		var funcs = {
			'getNamespace' : getNamespace,
			'getSchemaLocation' : getSchemaLocation,
			'equals' : equals
		};
		
		return funcs;	
	};
	
	/**
	 * Data object filters.
	 * @namespace SpacesSDK.filter 
	 */
	var filter = (function() {
		/**
		 * @class This meta filter represents a logical AND. A data object has to be successfully validated by each child filter in order to pass this filter.
		 * @name AndFilter
		 * @memberOf SpacesSDK.filter
		 */
		var AndFilter = function() {
			var filters = [];
			
			/**
			 * Adds a child filter.
			 * @function
			 * @memberOf SpacesSDK.filter.AndFilter.prototype
			 * @param {SpacesSDK.filter} filter Filter to add.
			 */
			var addFilter = function(filter) {
				if (!filter.isDataObjectValid) {
					throw "Invalid data object filter.";
					return;
				}
				filters.push(filter);
			};
			
			/**
			 * Returns the list of child filters.
			 * @function
			 * @memberOf SpacesSDK.filter.AndFilter.prototype
			 * @return {SpacesSDK.filter[]} List of filters aggregated in this meta filter.
			 */
			var getFilters = function() {
				return filters;
			};
			
			/**
			 * Checks if a data object is valid, i.e. passes the filter.
			 * @function
			 * @memberOf SpacesSDK.filter.AndFilter.prototype
			 * @return {Boolean} <code>true</code> if the data object applies to the conditions of the filter, otherwise <code>false</code>.
			 */
			var isDataObjectValid = function(dataObject) {
				for (var i = 0; i < filters.length; i++) {
					if (!filters[i].isDataObjectValid(dataObject)) {
						return false;
					}
				}
				return true;
			};
			
			var funcs = {
				'addFilter' : addFilter,
				'getFilters' : getFilters,
				'isDataObjectValid' : isDataObjectValid
			};
			
			return funcs;
		};
		
		/**
		 * @class Filter for data model information.
		 * @name DataModelFilter
		 * @memberOf SpacesSDK.filter
		 * @param {string} namespace Namespace URI as string.
		 * @param {string} [version] Model version to filter for. Optional.
		 */
		var DataModelFilter = function(namespace, version) {
			
			/**
			 * Returns the namespace of the data model to filter for.
			 * @function
			 * @memberOf SpacesSDK.filter.DataModelFilter.prototype
			 * @return {string} Namespace URI as string.
			 */
			var getNamespace = function() {
				return namespace;
			};
			
			/**
			 * Returns the data model version to filter for.
			 * @function
			 * @memberOf SpacesSDK.filter.DataModelFilter.prototype
			 * @return {string} Version string or <code>null</code> if the filter accepts all versions of the data model.
			 */
			var getVersion = function() {
				return version;
			};
			
			/**
			 * Checks if a data object is valid, i.e. passes the filter.
			 * @function
			 * @memberOf SpacesSDK.filter.DataModelFilter.prototype
			 * @param {SpacesSDK.DataObject} dataObject Data object to validate.
			 * @return {Boolean} <code>true</code> if the data object applies to the conditions of the filter, otherwise <code>false</code>.
			 */
			var isDataObjectValid = function(dataObject) {
				if (namespace == dataObject.getNamespaceURI()) {
					if (version) {
						if (version == dataObject.getModelVersion()) {
							return true;
						} else {
							return false;
						}
					}
					return true;
				} else {
					return false;
				}
			};
			
			/**
			 * Returns the filter represented as XML element to be used in a query.
			 * @function
			 * @memberOf SpacesSDK.filter.DataModelFilter.prototype
			 * @param {string} queryNamespace Namespace URI the element should implement.
			 * @return {XMLElement} XML element to be added to a query.
			 */
			var getFilterAsXML = function(queryNamespace) {
				var dataModelElement = SpacesSDK.Utils.createXMLDocument('dummy').createElementNS(queryNamespace, 'dataModel');
				dataModelElement.setAttribute('namespace', namespace);
				if (version) {
					dataModelElement.setAttribute('version', version);
				}
				return dataModelElement;
			};
			
			var funcs = {
				'getNamespace': getNamespace,
				'getVersion': getVersion,
				'isDataObjectValid': isDataObjectValid,
				'getFilterAsXML': getFilterAsXML
			};
			
			return funcs;
		};
		
		/**
		 * @class Filter for data model information.
		 * @name NamespaceFilter
		 * @memberOf SpacesSDK.filter
		 * @param {string} compareString String to compare the namespace URI against.
		 * @param {SpacesSDK.filter.NamespaceFilter.CompareType} [compareType] Type of the comparison. Defaults to <code>CompareType.STRICT</code>.
		 */
		var NamespaceFilter = function(compareString, compareType) {
			
			(function() {
				if (!compareType) {
					compareType = NamespaceFilter.CompareType.STRICT;
				}
			})();
			
			/**
			 * Returns the string to compare the namespace URI against.
			 * @function
			 * @memberOf SpacesSDK.filter.NamespaceFilter.prototype
			 * @return {string} String for comparison.
			 */
			var  getCompareString = function() {
				return compareString;
			};
			
			/**
			 * Returns the type of the comparison.
			 * @function
			 * @memberOf SpacesSDK.filter.NamespaceFilter.prototype
			 * @return {SpacesSDK.filter.NamespaceFilter.CompareType} Compare type.
			 */
			var getCompareType = function() {
				return compareType;
			};
			
			/**
			 * Checks if a data object is valid, i.e. passes the filter.
			 * @function
			 * @memberOf SpacesSDK.filter.NamespaceFilter.prototype
			 * @param {SpacesSDK.DataObject} dataObject Data object to validate.
			 * @return {Boolean} <code>true</code> if the data object applies to the conditions of the filter, otherwise <code>false</code>.
			 */
			var isDataObjectValid = function(dataObject) {
				var namespace = dataObject.getNamespaceURI() ? dataObject.getNamespaceURI() : '';
				switch (compareType) {
					case NamespaceFilter.CompareType.STRICT:
						if (namespace != compareString) {
							return false;
						}
						break;
					case NamespaceFilter.CompareType.CONTAINS:
						if (namespace.indexOf(compareString) < 0) {
							return false;
						}
						break;
					case NamespaceFilter.CompareType.REGEX:
						var match = namespace.match(compareString);
						if (!match || match[0] != namespace) {
							return false;
						}
						break;
				}
				return true;
			};
			
			/**
			 * Returns the filter represented as XML element to be used in a query.
			 * @function
			 * @memberOf SpacesSDK.filter.NamespaceFilter.prototype
			 * @param {string} queryNamespace Namespace URI the element should implement.
			 * @return {XMLElement} XML element to be added to a query.
			 */
			var getFilterAsXML = function(queryNamespace) {
				var namespaceElement = SpacesSDK.Utils.createXMLDocument('dummy').createElementNS(queryNamespace, 'namespace');
				namespaceElement.setAttribute('compareType', compareType);
				namespaceElement.textContent = compareString;
				return namespaceElement;
			};
			
			var funcs = {
				'getCompareString': getCompareString,
				'getCompareType': getCompareType,
				'isDataObjectValid': isDataObjectValid,
				'getFilterAsXML': getFilterAsXML
			};
			
			return funcs;
		};
		
		/**
		 * Compare type for the namespace filter. 
		 * @namespace SpacesSDK.filter.NamespaceFilter.CompareType
		 */
		NamespaceFilter.CompareType = {
			/**
			 * The namespace has to be equal the given compare string.
			 * @constant 
			 * @name STRICT
			 * @memberOf SpacesSDK.filter.NamespaceFilter.CompareType
			 */
			STRICT: 'strict',
			
			/**
			 * The namespace must contain the given compare string.
			 * @constant 
			 * @name CONTAINS
			 * @memberOf SpacesSDK.filter.NamespaceFilter.CompareType
			 */
			CONTAINS: 'contains',
			
			/**
			 * The namespace must match the regex given with the compare string.
			 * @constant 
			 * @name REGEX
			 * @memberOf SpacesSDK.filter.NamespaceFilter.CompareType
			 */
			REGEX: 'regex'
		};
		
		/**
		 * @class This meta filter represents a logical OR. This filter will validate a data object if one ore more of the child filters does.
		 * @name OrFilter
		 * @memberOf SpacesSDK.filter
		 */
		var OrFilter = function() {
			var filters = [];
			
			/**
			 * Adds a child filter.
			 * @function
			 * @memberOf SpacesSDK.filter.OrFilter.prototype
			 * @param {SpacesSDK.filter} filter Filter to add.
			 */
			var addFilter = function(filter) {
				if (!filter.isDataObjectValid) {
					throw "Invalid data object filter.";
					return;
				}
				filters.push(filter);
			};
			
			/**
			 * Returns the list of child filters.
			 * @function
			 * @memberOf SpacesSDK.filter.OrFilter.prototype
			 * @return {SpacesSDK.filter[]} List of filters aggregated in this meta filter.
			 */
			var getFilters = function() {
				return filters;
			};
			
			/**
			 * Checks if a data object is valid, i.e. passes the filter.
			 * @function
			 * @memberOf SpacesSDK.filter.OrFilter.prototype
			 * @return {Boolean} <code>true</code> if the data object applies to the conditions of the filter, otherwise <code>false</code>.
			 */
			var isDataObjectValid = function(dataObject) {
				for (var i = 0; i < filters.length; i++) {
					if (filters[i].isDataObjectValid(dataObject)) {
						return true;
					}
				}
				return false;
			};
			
			var funcs = {
				'addFilter' : addFilter,
				'getFilters' : getFilters,
				'isDataObjectValid' : isDataObjectValid
			};
			
			return funcs;
		};
		
		/**
		 * @class Restricts the period in time the data object was published.
		 * @name PeriodFilter
		 * @memberOf SpacesSDK.filter
		 * @param {Date} [from] The earliest point in time the data object may have been published. Optional.
		 * @param {Date} [to] The latest point in time the data object may have been published. Optional.
		 */
		var PeriodFilter = function(from, to) {
			
			/**
			 * Returns the earliest point in time the data object may have been published in order to pass the filter.
			 * @function
			 * @memberOf SpacesSDK.filter.PeriodFilter.prototype
			 * @return {string} Date including time and time zone information.
			 */
			var getFrom = function() {
				return from;
			};
			
			/**
			 * Returns the latest point in time the data object may have been published in order to pass the filter.
			 * @function
			 * @memberOf SpacesSDK.filter.PeriodFilter.prototype
			 * @return {string} Date including time and time zone information.
			 */
			var getTo = function() {
				return to;
			};
			
			/**
			 * Checks if a data object is valid, i.e. passes the filter.
			 * @function
			 * @memberOf SpacesSDK.filter.PeriodFilter.prototype
			 * @param {SpacesSDK.DataObject} dataObject Data object to validate.
			 * @return {Boolean} <code>true</code> if the data object applies to the conditions of the filter, otherwise <code>false</code>.
			 */
			var isDataObjectValid = function(dataObject) {
				var isoTimeString = dataObject.getElement().getAttribute('timestamp');
				if (isoTimeString) {
					try {
						var publishingDate = Utils.getDateForISO8601(isoTimeString);
						if (from && to) {
							return (from <= publishingDate && publishingDate <= to);
						} else if (from) {
							return (from <= publishingDate);
						} else if (to) {
							return (publishingDate <= to);
						}
					} catch (e) {
						console.warn('Failed to parse data object timestamp. Object filter is NOT applied.');
						return true;
					}
				} else {
					return false;
				}
			};
			
			/**
			 * Returns the filter represented as XML element to be used in a query.
			 * @function
			 * @memberOf SpacesSDK.filter.PeriodFilter.prototype
			 * @param {string} queryNamespace Namespace URI the element should implement.
			 * @return {XMLElement} XML element to be added to a query.
			 */
			var getFilterAsXML = function(queryNamespace) {
				var periodElement = SpacesSDK.Utils.createXMLDocument('dummy').createElementNS(queryNamespace, 'period');
				if (from) {
					periodElement.setAttribute('from', SpacesSDK.Utils.getISO8601ForDate(from));
				}
				if (to) {
					periodElement.setAttribute('to', SpacesSDK.Utils.getISO8601ForDate(to));
				}
				return periodElement;
			};
			
			var funcs = {
				'getFrom': getFrom,
				'getTo': getTo,
				'isDataObjectValid': isDataObjectValid,
				'getFilterAsXML': getFilterAsXML
			};
			
			return funcs;
		};
		
		/**
		 * @class Only data objects from the given publisher (bare-JID or full-JID) are returned. Removes all non-personalized data objects.
		 * @name PublisherFilter
		 * @memberOf SpacesSDK.filter
		 * @param {String} publisher JID of the publisher to pass the filter.
		 */
		var PublisherFilter = function(publisher) {
			
			/**
			 * Returns the JID of the publisher filtered by this filter.
			 * @function
			 * @memberOf SpacesSDK.filter.PublisherFilter.prototype
			 * @return {string} Either bare-JID or full-JID as string.
			 */
			var getPublisher = function() {
				return publisher;
			};
			
			/**
			 * Checks if a data object is valid, i.e. passes the filter.
			 * @function
			 * @memberOf SpacesSDK.filter.PublisherFilter.prototype
			 * @param {SpacesSDK.DataObject} dataObject Data object to validate.
			 * @return {Boolean} <code>true</code> if the data object applies to the conditions of the filter, otherwise <code>false</code>.
			 */
			var isDataObjectValid = function(dataObject) {
				var dataObjectPublisher = dataObject.getElement().getAttribute('publisher');
				if (dataObjectPublisher) {
					return (dataObjectPublisher.indexOf(publisher) == 0);
				} else {
					return false;
				}
			};
			
			/**
			 * Returns the filter represented as XML element to be used in a query.
			 * @function
			 * @memberOf SpacesSDK.filter.PublisherFilter.prototype
			 * @param {string} queryNamespace Namespace URI the element should implement.
			 * @return {XMLElement} XML element to be added to a query.
			 */
			var getFilterAsXML = function(queryNamespace) {
				var publisherElement = SpacesSDK.Utils.createXMLDocument('dummy').createElementNS(queryNamespace, 'publisher');
				publisherElement.textContent = publisher;
				return publisherElement;
			};
			
			var funcs = {
				'getPublisher': getPublisher,
				'isDataObjectValid': isDataObjectValid,
				'getFilterAsXML': getFilterAsXML
			};
			
			return funcs;
		};
		
		/**
		 * @class Request only data objects which refer to a specific object.
		 * @name ReferencesFilter
		 * @memberOf SpacesSDK.filter
		 * @param {String} referenceId ID of the data object that should be referenced by passing data objects. 
		 */
		var ReferencesFilter = function(referenceId) {
			
			/**
			 * Returns the data object identifier of the reference a valid data object must contain.
			 * @function
			 * @memberOf SpacesSDK.filter.ReferencesFilter.prototype
			 * @return {string} Data object identifier.
			 */
			var getReferenceId = function() {
				return referenceId;
			};
			
			/**
			 * Checks if a data object is valid, i.e. passes the filter.
			 * @function
			 * @memberOf SpacesSDK.filter.ReferencesFilter.prototype
			 * @param {SpacesSDK.DataObject} dataObject Data object to validate.
			 * @return {Boolean} <code>true</code> if the data object applies to the conditions of the filter, otherwise <code>false</code>.
			 */
			var isDataObjectValid = function(dataObject) {
				var refString = dataObject.getElement().getAttribute('ref');
				if (refString) {
					return (refString == referenceId);
				} else {
					return false;
				}
			};
			
			/**
			 * Returns the filter represented as XML element to be used in a query.
			 * @function
			 * @memberOf SpacesSDK.filter.ReferencesFilter.prototype
			 * @param {string} queryNamespace Namespace URI the element should implement.
			 * @return {XMLElement} XML element to be added to a query.
			 */
			var getFilterAsXML = function(queryNamespace) {
				var referencesElement = SpacesSDK.Utils.createXMLDocument('dummy').createElementNS(queryNamespace, 'references');
				referencesElement.setAttribute('id', referenceId);
				return referencesElement;
			};
			
			var funcs = {
				'getReferenceId': getReferenceId,
				'isDataObjectValid': isDataObjectValid,
				'getFilterAsXML': getFilterAsXML
			};
			
			return funcs;
		};
		
		return {
			'AndFilter' : AndFilter,
			'DataModelFilter' : DataModelFilter,
			'NamespaceFilter' : NamespaceFilter,
			'OrFilter' : OrFilter,
			'PeriodFilter' : PeriodFilter,
			'PublisherFilter' : PublisherFilter,
			'ReferencesFilter' : ReferencesFilter
		};
	})();

	/**
	 * Use the {@link SpacesSDK.DataObjectBuilder} to create new data objects.
	 * @class A data object represents an item published on a pubsub node of a space.
	 * @name DataObject
	 * @memberOf SpacesSDK
	 * @param {XMLElement} rootElement XML DOM of the data object. Used internally by the {@link SpacesSDK.DataObjectBuilder}.
	 */
	var DataObject = function(rootElement) {
		var xmlDocument = rootElement.ownerDocument;
		var cdmData = null;
		
		(function() { // initialization
			if (!rootElement) {
				throw {
					name: 'IllegalArgumentException',
					message: 'The XML DOM must contain a root element.'
				};
			}
			var cdmVersion = rootElement.getAttribute('cdmVersion');
			if (cdmVersion && SpacesSDK.cdm.CDMVersion.getVersionForString(cdmVersion) != null) {
				cdmData = new CDMData(rootElement);
			} else {
				cdmData = null;
			}
		})();
		
		
		/**
		 * Returns the common data model data for this object.
		 * If no information is contained, i.e., the data object does not instantiate a
		 * MIRROR data model, <code>null</code> will be returned.
		 * @function
		 * @memberOf SpacesSDK.DataObject.prototype
		 * @return {SpacesSDK.CDMData} CDM information container or <code>null</code> if no CDM data is available.
		 */
		var getCDMData = function() {
			return cdmData;
		};
		
		/**
		 * Returns the set cdmversion of this DataObject.
		 * @function
		 * @memberOf SpacesSDK.DataObject.prototype
		 * @return {string} The set SpacesSDK.cdm.CDMVersion If it wasn't set this method returns null.
		 */
		var getCDMVersion = function() {
			return rootElement.getAttribute('cdmVersion');
		};
		
		/**
		 * Returns the data model for the data object.
		 * The returned model is not necessarily a MIRROR data model. No object verification is applied.
		 * @function
		 * @memberOf SpacesSDK.DataObject.prototype
		 * @return {SpacesSDK.DataModel} Model the data object claims to instantiate.
		 */
		var getDataModel = function() {
			var schema = rootElement.getAttribute('xsi:schemaLocation');
			if (schema != null) {
				return new DataModel(schema.split(" ")[0], schema.split(" ")[1]);
			}
			return null;
		};
		
		/**
		 * Returns the XML element for this data object.
		 * @function
		 * @memberOf SpacesSDK.DataObject.prototype
		 * @return {XMLElement} XML element. This is the payload published on the pubsub node.
		 */
		var getElement = function() {
			return rootElement;
		};
		
		/**
		 * Returns the name of the data object's root element. 
		 * @function
		 * @memberOf SpacesSDK.DataObject.prototype
		 * @return {string} The element name of the object.
		 */
		var getElementName = function() {
			return rootElement.tagName;
		};
		
		/**
		 * Returns the unique identifier of this data object provided by the CDM.
		 * @function
		 * @memberOf SpacesSDK.DataObject.prototype
		 * @return {string} Data object identifier.
		 */
		var getId = function() {
			return rootElement.getAttribute('id');
		};
		
		/**
		 * Returns the version string of the implemented data model, if available.
		 * @function
		 * @memberOf SpacesSDK.DataObject.prototype
		 * @return {string} The model version string or null if not set.
		 */
		var getModelVersion = function() {
			return rootElement.getAttribute('modelVersion');
		};
		
		/**
		 * Return the namespace of the data object's root element.
		 * @function
		 * @memberOf SpacesSDK.DataObject.prototype
		 * @return {string} The namespace URI of the object.
		 */
		var getNamespaceURI = function() {
			return rootElement.namespaceURI;
		};
		
		/**
		 * Checks if the data object claims to be an instance of a MIRROR data model.
		 * A simple namespace comparison is applied, but no verification.
		 * @function
		 * @memberOf SpacesSDK.DataObject.prototype
		 * @return {boolean} <code>true</code> if the XML object is from the MIRROR application namespace, otherwise <code>false</code>.
		 */
		var isMIRRORDataObject = function() {
			return rootElement.namespaceURI.indexOf("mirror:application:") == 0;
		};
		
		/**
		 * Generates an XML string representing this data object.
		 * @function
		 * @memberOf SpacesSDK.DataObject.prototype
		 * @return {string} The object as XML.
		 */
		var toString = function() {
			return Utils.generateXMLString(rootElement);
		};
		
		var funcs = {
			'getCDMData' : getCDMData,
			'getCDMVersion' : getCDMVersion,
			'getDataModel' : getDataModel,
			'getElement' : getElement,
			'getElementName' : getElementName,
			'getId' : getId,
			'getModelVersion' : getModelVersion,
			'getNamespaceURI' : getNamespaceURI,
			'isMIRRORDataObject' : isMIRRORDataObject,
			'toString' : toString
		};
		return funcs;
	};
	
	/**
	 * Instantiates a new data object builder.
	 * @class Builder for data objects.
	 * @name DataObjectBuilder
	 * @memberOf SpacesSDK
	 * @param {string} elementName The tag name of the root element.
	 * @param {string} ns The namespace of the data object.
	*/
	var DataObjectBuilder = function(elementName, ns) {
		var xmlDocument = null;
		
		(function(){ // constructor
			if (!elementName) {
				throw {
					name : 'IllegalArgumentException',
					message : 'Missing element name.'
				};
			}
			xmlDocument = Utils.createXMLDocument(elementName, ns);
		})();
		
		
		/**
		 * This method adds a CDT creationInfo element.
		 * All methods of the builder return the builder instance and can therefore be used in a chained way.
		 * @function
		 * @memberOf SpacesSDK.DataObjectBuilder.prototype
		 * @param {string} [person] Identifier for the person who updated the data, e.g., a JID, an email address, or a name. It is recommended to use a JID if available. May be null.
		 * @param {Date} [date] Date the object was created. May be null.
		 * @param {string} [applicationInfo] Identifier for the application which created the data, for instance the namespace of a MIRROR application. May be null.
		 * @return {SpacesSDK.DataObjectBuilder} The object on which the method was called.
		 */
		var addCDTCreationInfo = function(date, person, applicationInfo) {
			var rootElement = xmlDocument.childNodes[0];
			rootElement.setAttribute('xmlns:cdt', 'mirror:common:datatypes');
			var creationInfoElement = xmlDocument.createElement('creationInfo');
			if (date) {
				var year = date.getFullYear();
				var month = (date.getMonth()+1)<10? "0" + (date.getMonth()+1): (date.getMonth()+1);
				var day = date.getDate();
				var ms = date.getMilliseconds();
				ms = ms<10? "00" + ms: (ms<100? "0" + ms: ms);
				var time = date.toTimeString().match( /([0-9]{2}:[0-9]{2}:[0-9]{2}|[+-][0-9]{4})/g );
				time[1] = time[1].substr(0,3) + ":" + time[1].substr(3,2);
				var dateElement = xmlDocument.createElement('cdt:date');
				var dateText = xmlDocument.createTextNode(year +"-" +month +"-" + day +"T" +time[0] +"." +ms + time[1]);
				dateElement.appendChild(dateText);
				creationInfoElement.appendChild(dateElement);
			}
			if (person) {
				var personElement = xmlDocument.createElement('cdt:person');
				personElement.appendChild(xmlDocument.createTextNode(person));
				creationInfoElement.appendChild(personElement);
			}
			if (applicationInfo) {
				var applicationElement = xmlDocument.createElement('cdt:application');
				applicationElement.appendChild(xmlDocument.createTextNode(applicationInfo));
				creationInfoElement.appendChild(applicationElement);
			}
			rootElement.appendChild(creationInfoElement);
			
			return funcs;
		};
		
		/**
		 * This methods adds an CDT attachment element with a link.
		 * @function
		 * @memberOf SpacesSDK.DataObjectBuilder.prototype
		 * @param {string} url URL string to link to.
		 * @param {string} [type] Type of the attachment. Optional.
		 * @return {SpacesSDK.DataObjectBuilder} The object on which the method was called.
		 */
		var addCDTAttachmentWithLink = function(url, type) {
			var rootElement = xmlDocument.childNodes[0];
			var attachmentElement = xmlDocument.createElement('attachment');
			if (type) attachmentElement.setAttribute('type', type);
			var linkElement = xmlDocument.createElementNS('mirror:common:datatypes', 'cdt:link');
			linkElement.setAttribute('url', url);
			attachmentElement.appendChild(linkElement);
			rootElement.appendChild(attachmentElement);
		};
		
		/**
		 * This methods adds an CDT attachment element with a body.
		 * @function
		 * @memberOf SpacesSDK.DataObjectBuilder.prototype
		 * @param {string} body Body to include.
		 * @param {string} [mimeType] MIME type of the body. Optional.
		 * @param {string} [type] Type of the attachment. Optional.
		 * @return {SpacesSDK.DataObjectBuilder} The object on which the method was called.
		 */
		var addCDTAttachmentWithBody = function(body, mimeType, type) {
			var rootElement = xmlDocument.childNodes[0];
			var attachmentElement = xmlDocument.createElement('attachment');
			if (type) attachmentElement.setAttribute('type', type);
			var bodyElement = xmlDocument.createElementNS('mirror:common:datatypes', 'cdt:body');
			if (mimeType) bodyElement.setAttribute('mimeType', mimeType);
			bodyElement.appendChild(xmlDocument.createTextNode(body));
			attachmentElement.appendChild(bodyElement);
			rootElement.appendChild(attachmentElement);
		};
		
		/**
		 * Create a new XML element and adds it to the root element of the new data object. 
		 * @function
		 * @memberOf SpacesSDK.DataObjectBuilder.prototype
		 * @param {string} tagName Tag name of the new element. Required.
		 * @param {object} [attributes] Map of attributes to add to the new element. May be null.
		 * @param {string} [content] Content to add. May be null.
		 * @param {boolean} [parseContent] If set to true, the given content is parsed as XML, otherwise it is appended as text.
		 * @return {SpacesSDK.DataObjectBuilder} The object on which the method was called. 
		 */
		var addNewElement = function(tagName, attributes, content, parseContent) {
			if (!tagName) {
				throw {
					name: 'IllegalArgumentException',
					message: 'The tag name is required.'
				};
			}
			var rootElement = xmlDocument.childNodes[0];
			var newElement = xmlDocument.createElement(tagName);
			if (attributes) {
				for (var key in attributes) {
					newElement.setAttribute(key, attributes[key]);
				}
			}
			if (content) {
				if (parseContent) {
					var parsedXMLDoc = Utils.generateXMLDocumentFromString(content);
					newElement.appendChild(parsedXMLDoc.childNodes[0]);
				} else {
					var textContent = xmlDocument.createTextNode(content);
					newElement.appendChild(textContent);
				}
			}
			
			rootElement.appendChild(newElement);
			return funcs;
		};
		
		/**
		 * Sets an attribute for the root element. 
		 * @function
		 * @memberOf SpacesSDK.DataObjectBuilder.prototype
		 * @param {string} name Name of the attribute.
		 * @param {string} value Value of the attribute. 
		 * @return {SpacesSDK.DataObjectBuilder} The object on which the method was called.
		 */
		var setAttribute = function(name, value) {
			if (!name || !value) {
				throw {
					name: 'IllegalArgumentException',
					message: 'Attribute name and value are required.'
				};
			}
			var rootElement = xmlDocument.childNodes[0];
			rootElement.setAttribute(name, value);
			return funcs;
		};
		
		
		/**
		 * Builds the data object.
		 * @function
		 * @memberOf SpacesSDK.DataObjectBuilder.prototype
		 * @return {SpacesSDK.DataObject} A new data object based on the given information.
		 */
		var build = function() {
			return new DataObject(xmlDocument.childNodes[0]);
		};

		/**
		 * Sets the Common Data Model information for the object.
		 * The CDM data object can be generated using the {@link SpacesSDK.CDMDataBuilder}.
		 * @function
		 * @memberOf SpacesSDK.DataObjectBuilder.prototype
		 * @param {SpacesSDK.CDMData} cdmData The CDMData to set.
		 * @return {SpacesSDK.DataObjectBuilder} The object on which the method was called.
		 */
		var setCDMData = function(cdmData) {
			cdmData.applyToElement(xmlDocument.childNodes[0]);
			return funcs;
		};

		/**
		 * Sets the version of data model the object instantiates.
		 * @function
		 * @memberOf SpacesSDK.DataObjectBuilder.prototype
		 * @param {String} version The model version to set.
		 * @return {SpacesSDK.DataObjectBuilder} The object on which the method was called.
		 */
		var setModelVersion = function(modelVersion) {
			var rootElement = xmlDocument.childNodes[0];
			rootElement.setAttribute('modelVersion', modelVersion);
			return funcs;
		};
		
		/**
		 * Returns the root element of the building object.
		 * @return {XMLDocument} Root element of this builder.
		 */
		var getRootElement = function() {
			return xmlDocument.childNodes[0];
		};
		
		var funcs = {
			'addCDTAttachmentWithLink' : addCDTAttachmentWithLink,
			'addCDTAttachmentWithBody' : addCDTAttachmentWithBody,
			'addCDTCreationInfo' : addCDTCreationInfo,
			'addNewElement' : addNewElement,
			'setAttribute' : setAttribute,
			'build' : build,
			'setCDMData' : setCDMData,
			'setModelVersion' : setModelVersion,
			'getRootElement' : getRootElement
		};
		
		return funcs;
	};

	/**
	 * Creates a new listener which is called when a data object is received over the pubsub node from a MIRROR space.
	 * @class Listener for received data object. See {@link SpacesSDK.DataHandler#addDataObjectListener} for details.
	 * @name DataObjectListener
	 * @memberOf SpacesSDK
	 * @param {string} listenerName The name of the listener.
	 * @param {Function(pacesSDK.DataObjectListener~onEvent)} onEvent The function which will be called when a new data object was received. Parameters: {@link SpacesSDK.DataObject}, space identifier.
	 */
	var DataObjectListener = function(listenerName, onEvent) {
		var listener = new Object();
		listener.name = listenerName;
		listener.handleDataObject = onEvent;
		return listener;
	};
	
	/**
	 * This function is called each time a new item is published on the pubsub node of a MIRROR space.
	 * Callback of {@link SpacesSDK.DataObjectListener}.
	 * @callback SpacesSDK.DataObjectListener~onEvent
	 * @param {SpacesSDK.DataObject} dataObject Data object received.
	 * @param {string} spaceId Identifier of the space the data object was sent to. 
	 */
	

	/**
	 * Internally used. In order to create a new space use {@link SpacesSDK.SpaceHandler#createSpace}.
	 * @class Model for a MIRROR reflection space.The available functions depend on the given {@link SpacesSDK.Type|type}.
	 * @name Space
	 * @memberOf SpacesSDK
	 * @param {string} name The name of the space.
	 * @param {string} spaceId The id of the space.
	 * @param {string} domain The domain of the space.
	 * @param {SpacesSDK.DataModel[]} [dataModels] The datamodels this space supports. Only used when it is a Orgaspace.
	 * @param {SpacesSDK.Type} type The type of the space.
	 * @param {SpacesSDK.SpaceChannel[]} channels An array of all SpaceChannels of the space.
	 * @param {SpacesSDK.SpaceMember[]} members An array of all Members of this space.
	 * @param {boolean|string} persistent <code>true</code>, <code>false</code> or XSD duration string indicating the space persistence setting.	 
	 */
	var Space = function(name, spaceId, domain, dataModels, type, channels, members, persistenceType, persistenceDuration) {	
		var funcs = {};
			
		(function(){
			if (type == Type.ORGA && dataModels == null) {
				dataModels = [];
			}
			if (channels == null) {
				channels = [];
			}
			if (members == null) {
				members = [];
			}
			switch (type){
				case SpacesSDK.Type.PRIVATE:
					addPrivateSpaceMethods();
					break;
				case SpacesSDK.Type.ORGA:
					addOrgaSpaceMethods();
				case SpacesSDK.Type.TEAM:
					addTeamSpaceMethods();
					break;
			}
		})();
		
		function addPrivateSpaceMethods() {
			/**
			 * Returns the owner of the private space.
			 * Only available for spaces of type {@link SpacesSDK.Type.PRIVATE}.
			 * @function
			 * @memberOf SpacesSDK.Space.prototype
			 * @return {string} Bare-JID of the owner of this space.
			 */
			var getOwner = function() {
				return members[0].getJID();
			};
			
			funcs.getOwner = getOwner;
		}
		
		function addOrgaSpaceMethods() {
			/**
			 * Returns the list of data models supported by this space.
			 * Only available for spaces of type {@link SpacesSDK.Type.ORGA}.
			 * @function
			 * @memberOf SpacesSDK.Space.prototype
			 * @return {SpacesSDK.DataModel[]} An array of data model objects.
			 */
			var getSupportedDataModels = function() {
				var result = [];
				for (var i=0; i<dataModels.length; i++) {
					result[i] = dataModels[i];
				}
				return result;
			};
			funcs.getSupportedDataModels = getSupportedDataModels;
		}
		
		function addTeamSpaceMethods() {
			/**
			 * Returns the multi-user chat channel of the space.
			 * Only available for spaces of type {@link SpacesSDK.Type.TEAM} or {@link SpacesSDK.Type.ORGA}.
			 * @function
			 * @memberOf SpacesSDK.Space.prototype
			 * @return {SpacesSDK.SpaceChannel} Channel containing the multi-user chat room address.
			 */
			var getMUCChannel = function() {
				for (var i=0; i<channels.length; i++) {
					if (channels[i].getType() === "muc") {
						return channels[i];
					}
				}
				return null;
			};
			funcs.getMUCChannel = getMUCChannel;
		}
		
		var equals = function(obj) {
			if (obj.getId == null || typeof obj.getId !== "function") {
				return false;
			}
			else if (obj.getType == null || typeof obj.getType !== "function") {
				return false;
			}
			else if(type === obj.getType() && 
						spaceId.toLowerCase() === space.getId().toLowerCase()) {
					return true;
			}
			return false;
		};
		
		/**
		 * Generates a space configuration based on the configuration of this space.
		 * Changes applied to the generated configuration are not applied this space. To the change the configuration of a
		 * existing space, perform the following steps:
		 * 1. Generate a space configuration object using this method. 
		 * 2. Modify the space configuration object.
		 * 3. Use {@link SpaceHandler#configureSpace} to apply the configuration.
		 * @function
		 * @memberOf SpacesSDK.Space.prototype
		 * @return {SpacesSDK.SpaceConfiguration} Space configuration with the settings of this space.
		 */
		var generateSpaceConfiguration = function() {
			var config = new SpaceConfiguration();
			config.setName(name);
			config.setType(type);
			config.setPersistenceType(persistenceType);
			config.setPersistenceDuration(persistenceDuration);
			for (var i = 0; i<members.length; i++) {
				var member = new SpaceMember(members[i].getJID(), members[i].getRole());
				config.addMember(member);
			}
			return config;
		};
		
		/**
		 * Returns the list of channels available for this space.
		 * @function
		 * @memberOf SpacesSDK.Space.prototype
		 * @return {SpacesSDK.SpaceChannel[]} An array of space channel objects.
		 */
		var getChannels = function() {
			var result = [];
			for (var i=0; i<channels.length; i++) {
				result[i] = channels[i];
			}
			return result;
		};
		
		/**
		 * Returns the XMPP domain the space is located on.
		 * E.g., if the space is handled by spaces.mirror-demo.eu, the domain is "mirror-demo.eu".
		 * @function
		 * @memberOf SpacesSDK.Space.prototype
		 * @return {string} Domain the spaces service is located, which provides the space.
		 */
		var getDomain = function() {
			return domain;
		};
		
		/**
		 * Returns the id of this space. The space id is unique within a domain.
		 * @function
		 * @memberOf SpacesSDK.Space.prototype
		 * @return {string} Space identifier.
		 */
		var getId = function() {
			return spaceId;
		};
		
		/**
		 * Returns the list of members of the space.
		 * @function
		 * @memberOf SpacesSDK.Space.prototype
		 * @return {SpacesSDK.SpaceMember[]} List of space member models containing user and role information.
		 */
		var getMembers = function() {
			var result = [];
			for (var i=0; i<members.length; i++) {
				result[i] = members[i];
			}
			return result;
		};
		
		/**
		 * Returns the name of the space.
		 * The name is a human-readable string meant to be displayed to the user. 
		 * @function
		 * @memberOf SpacesSDK.Space.prototype
		 * @return {string} Name of the space or <code>null</code> if no name is set.
		 */
		var getName = function() {
			return name;
		};
		
		/**
		 * Returns the publish-subscribe channel of the space.
		 * @function
		 * @memberOf SpacesSDK.Space.prototype
		 * @return {SpacesSDK.SpaceChannel} Publish-Subscribe channel of the space.
		 */
		var getPubSubChannel = function() {
			for (var i=0; i<channels.length; i++) {
				if (channels[i].getType() === "pubsub") {
					return channels[i];
				}
			}
			return null;
		};
		
		/**
		 * Returns the type of this space.
		 * @function
		 * @memberOf SpacesSDK.Space.prototype
		 * @return {SpacesSDK.Type} Space type.
		 */
		var getType = function() {
			return type;
		};
		
		/**
		 * Checks if the given user is member of this space.
		 * @function
		 * @memberOf SpacesSDK.Space.prototype
		 * @param {string} userId Bare-JID of the user to check the membership for.
		 * @return {boolean} <code>true</code> if the user is member of the space, otherwise <code>false</code>.
		 */
		var isMember = function(userId) {
			for (var i=0; i<members.length; i++) {
				if (members[i].getJID() === userId) {
					return true;
				}
			}
			return false;
		};
		
		/**
		 * Checks if the given user is moderator of the space.
		 * @function
		 * @memberOf SpacesSDK.Space.prototype
		 * @param {string} userId Bare-JID of the user to check.
		 * @return {boolean} <code>true</code> if the user is moderator of the space, otherwise <code>false</code>.
		 */
		var isModerator = function(userId) {
			for (var i=0; i<members.length; i++) {
				if (members[i].getJID() === userId) {
					if (members[i].getRole() === SpacesSDK.Role.MODERATOR) {
						return true;
					}
				}
			}
			return false;
		};
		
		/**
		 * Returns the persistence setting of this space.
		 * @function
		 * @memberOf SpacesSDK.Space.prototype
		 * @return {SpacesSDK.PersistenceType} SpacesSDK.PersistenceType.ON, PSpacesSDK.ersistenceType.OFF, or SpacesSDK.PersistenceType.DURATION 
		 */
		var getPersistenceType = function() {
			return persistenceType;
		};
		
		/** Returns the duration for which data objects are persisted after their publishing date.
		 * Only set if the space's persistence setting is {@link SpacesSDK.PersistenceType.DURATION}.
		 * @function
		 * @memberOf SpacesSDK.Space.prototype
		 * @return {string} XSD duration string or <code>null</code> if duration is not {@link SpacesSDK.PersistenceType.DURATION}.
		 */ 
		var getPersistenceDuration = function() {
			if (getPersistenceType() == SpacesSDK.PersistenceType.DURATION) {
				return persistenceDuration;
			} else {
				return null;
			}
		};
		
		/**
		 * Returns the persistence channel of the space.
		 * @function
		 * @memberOf SpacesSDK.Space.prototype
		 * @return Persistence channel if the space is configured to persist data, otherwise <code>null</code>.
		 */
		var getPersistenceChannel = function() {
			for (var i=0; i<channels.length; i++) {
				if (channels[i].getType() === "persistence") {
					return channels[i];
				}
			}
			return null;
		};
		
		funcs.equals = equals;
		funcs.generateSpaceConfiguration = generateSpaceConfiguration;
		funcs.getChannels = getChannels;
		funcs.getDomain = getDomain;
		funcs.getId = getId;
		funcs.getMembers = getMembers;
		funcs.getName = getName;
		funcs.getPubSubChannel = getPubSubChannel;
		funcs.getType = getType;
		funcs.isMember = isMember;
		funcs.isModerator = isModerator;
		funcs.getPersistenceType = getPersistenceType;
		funcs.getPersistenceDuration = getPersistenceDuration;
		funcs.getPersistenceChannel = getPersistenceChannel;
		
		return funcs;
	};
	
	/**
	 * Used internally to instantiate a new space channel.
	 * @class JavaScript implementation of the space channel API.
	 * @name SpaceChannel
	 * @memberOf SpacesSDK
	 * @param {string} type The type of the SpaceChannel.
	 * @param {object} properties The properties of the SpaceChannel.
	 */
	var SpaceChannel = function(type, properties) {
		
		/**
		 * Returns a map of channel properties.
		 * The availability of properties depends on the channel SpacesSDK.Type. 
		 * @function
		 * @memberOf SpacesSDK.SpaceChannel.prototype
		 * @return {object} Map of channel properties, i.e., key-value pairs.
		 */
		var getProperties = function() {
			return properties;
		};
		
		/**
		 * Returns the type of the channel.  
		 * @function
		 * @memberOf SpacesSDK.SpaceChannel.prototype
		 * @return {string} Type of the channel as string, e.g. "pubsub", "muc", or "persistence".
		 */
		var getType = function() {
			return type;
		};
		
		
		var funcs = {
			'getProperties' : getProperties,
			'getType' : getType
		};
		
		return funcs;
	};

	/**
	 * Creates a new configuration with the given properties.
	 * @class The model for a space configuration.
	 * @name SpaceConfiguration
	 * @memberOf SpacesSDK.
	 * @param {SpacesSDK.Type} type The type of the Space.
	 * @param {string} name The name of the Space.
	 * @param {SpacesSDK.SpaceMember[]} [members] Array of all Members of the space.
	 * @param {SpacesSDK.PersistenceType} persistenceType Persistence settings for the space. See {@link SpacesSDK.PersistenceType} for details.
	 * @param {string} [persistenceDuration] XSD duration string or <code>null</code> if duration is not {@link SpacesSDK.PersistenceType.DURATION}.
	 */
	var SpaceConfiguration = function(type, name, members, persistenceType, persistenceDuration) {
		(function(){
			if (members == null){
				members = [];
			} else if (!members.length || members.length < 0) {
				var member = members;
				members = [];
				members.push(member);
			}
		})();
		
		/**
		 * Adds a member to the space.
		 * If a given user is already member of the space, only the role will be updated. 
		 * @function
		 * @memberOf SpacesSDK.SpaceConfiguration.prototype
		 * @param {SpacesSDK.SpaceMember} member Space member model to set.
		 */
		var addMember = function(member) {
			removeMember(member.getJID());
			members.push(member);
		};
		
		/**
		 * Returns the space members.
		 * Member role are assigned in the space member models. 
		 * @function
		 * @memberOf SpacesSDK.SpaceConfiguration.prototype
		 * @return {SpacesSDK.SpaceMember[]} Array of space members.
		 */
		var getMembers = function() {
			return members;
		};
		
		/**
		 * Returns the name set for the space. 
		 * @function
		 * @memberOf SpacesSDK.SpaceConfiguration.prototype
		 * @return {string} Human-readable name for the space.
		 */
		var getName = function() {
			return name;
		};
		
		/**
		 * Returns the of the the space. 
		 * @function
		 * @memberOf SpacesSDK.SpaceConfiguration.prototype
		 * @return {SpacesSDK.Type} Space type.
		 */
		var getType = function() {
			return type;
		};
		
		/**
		 * Returns the persistence setting of this space.
		 * @function
		 * @memberOf SpacesSDK.SpaceConfiguration.prototype
		 * @return {SpacesSDK.PersistenceType} SpacesSDK.PersistenceType.ON, PSpacesSDK.ersistenceType.OFF, or SpacesSDK.PersistenceType.DURATION 
		 */
		var getPersistenceType = function() {
			return persistenceType;
		};

		/** Returns the duration for which data objects are persisted after their publishing date.
		 * Only set if the space's persistence setting is {@link SpacesSDK.PersistenceType.DURATION}.
		 * @function
		 * @memberOf SpacesSDK.SpaceConfiguration.prototype
		 * @return {string} XSD duration string or <code>null</code> if duration is not {@link SpacesSDK.PersistenceType.DURATION}.
		 */ 
		var getPersistenceDuration = function() {
			if (getPersistenceType() == SpacesSDK.PersistenceType.DURATION) {
				return persistenceDuration;
			} else {
				return null;
			}
		};
		
		/**
		 * Revokes membership of a user.
		 * If the user is not stored in the list of members, nothing will happen. 
		 * @function
		 * @memberOf SpacesSDK.SpaceConfiguration.prototype
		 * @param {string} userId Bare-JID of the user to revoke membership. 
		 */
		var removeMember = function(memberJid) {
			for (var i=0; i<members.length; i++) {
				if (members[i].getJID() === memberJid) {
					for(var j=i; j<members.length - 1; j++) {
						members[j] = members[j+1];
					}
					members.length = members.length - 1;
					break;
				}
			}
		};
		
		/**
		 * Sets the members list for the space.
		 * Replaces the actual list of members. Member roles are assigned in the space member models. 
		 * @function
		 * @memberOf SpacesSDK.SpaceConfiguration.prototype
		 * @param {SpacesSDK.SpaceMember[]} members Array of space members to set.
		 */
		var setMembers = function(spaceMembers) {
			members = spaceMembers;
		};
		
		/**
		 * Sets the name of the space. 
		 * @function
		 * @memberOf SpacesSDK.SpaceConfiguration.prototype
		 * @param {string} name Human-readable name for the space.
		 */
		var setName = function(spaceName) {
			name = spaceName;
		};
		
		/**
		 * Sets the persistence type of the space.
		 * A space can be configured to either do not persist published data objects ({@link SpacesSDK.PersistenceType.OFF}),
		 * persist data objects ({@link SpacesSDK.PersistenceType.ON}), or persist data object for a specific duration 
		 * ({@link SpacesSDK.PersistenceType.DURATION}).
		 * @function
		 * @memberOf SpacesSDK.SpaceConfiguration.prototype
		 * @param {SpacesSDK.PersistenceType} persistenceType Persistence type to set.
		 */
		var setPersistenceType = function(spacePersistenceType) {
			persistenceType = spacePersistenceType;
		};
		
		/**
		 * Sets the duration for the data object persistence.
		 * Only required when the persistence type is {@link SpacesSDK.PersistenceType.DURATION}.
		 * @function
		 * @memberOf SpacesSDK.SpaceConfiguration.prototype
		 * @param {string} duration Duration to set as XSD duration string.
		 */
		var setPersistenceDuration = function(duration) {
			persistenceDuration = duration;
		};
		
		/**
		 * Sets the type for the space. 
		 * @function
		 * @memberOf SpacesSDK.SpaceConfiguration.prototype
		 * @param {SpacesSDK.Type} spaceType Space type.
		 */
		var setType = function(spaceType) {
			type = spaceType;
		};
		
		var funcs = {
			'addMember' : addMember,
			'getMembers' : getMembers,
			'getName' : getName,
			'getType' : getType,
			'getPersistenceType': getPersistenceType,
			'getPersistenceDuration': getPersistenceDuration,
			'removeMember' : removeMember,
			'setMembers' : setMembers,
			'setName' : setName,
			'setPersistenceType' : setPersistenceType,
			'setType' : setType,
			'setPersistenceDuration' : setPersistenceDuration
		};
		
		return funcs;
	};
	
	/**
	 * Creates a new space handler.
	 * @class This class provides methods to create, modify, delete, and retrieve spaces.
	 * @name SpaceHandler
	 * @memberOf SpacesSDK
	 * @param {SpacesSDK.ConnectionHandler} connectionHandler A connection handler instance.
	 */
	var SpaceHandler = function(connectionHandler) {
		var connection = null;
		var userInfo = null;
		var timeout = null;
		
		(function(){
			connection = connectionHandler.getXMPPConnection();
			userInfo = connectionHandler.getCurrentUser();
			timeout = connectionHandler.getConfiguration().requestTimeout();
		})();
		
		/**
		 * Checks if the given user is a moderator of the given space.
		 * @private 
		 * @function
		 * @memberOf SpacesSDK.SpaceHandler.prototype
		 * @param {string} user The user to check.
		 * @param {string} spaceId The id of the space to check.
		 * @param {SpacesSDK.SpaceHandler~isModeratorOfSpace$onSuccess} onSuccess The callback if the request succeeds.
		 * @param {SpacesSDK.SpaceHandler~isModeratorOfSpace$onError} onError The callback if an error occurs.
		 */
		var isModeratorOfSpace = function(user, spaceId, onSuccess, onError) {
			if (!onSuccess || !onError){
				throw {
					name: "IllegalArgumentException.",
					message: "You have to specify two Callbacks."
				};
			}
			funcs.getSpace(spaceId, function(res) {
				var members = res.getMembers();
				for (var i = 0; i<members.length; i++) {
					if (members[i].getJID().toLowerCase() === user.split("/")[0].toLowerCase()) {
						if (members[i].getRole() === SpacesSDK.Role.MODERATOR) {
							onSuccess(true);
							return;
						}
					}
				}
				onSuccess(false);
			}, onError);
		};
		
		/**
		 * The callback if the request succeeds.
		 * Callback of {SpacesSDK.SpaceHandler#isModeratorOfSpace|SpaceHandler.isModeratorOfSpace()}.
		 * @callback SpacesSDK.SpaceHandler~isModeratorOfSpace$onSuccess
		 * @param {boolean} <code>true</code> if the user is a moderator of the space, otherwise <code>false</code>.
		 */
		
		/**
		 * The callback if an error occurs.
		 * Callback of {SpacesSDK.SpaceHandler#isModeratorOfSpace|SpaceHandler.isModeratorOfSpace()}.
		 * @callback SpacesSDK.SpaceHandler~isModeratorOfSpace$onError
		 * @param {string} error Error message.
		 */
		
		var checkConfig = function(config) {
			if (!config.getName() || Utils.trimString(config.getName()).length == 0) {
				throw {
					name: "IllegalArgumentException",
					message: "Please specify a name which consists of at least one letter"
				};
			} else if (!config.getType() || !Type[config.getType()]) {
				throw {
					name: "IllegalArgumentException",
					message: "Please specify a type which is in the enum Type."
				};
			} else if (!config.getPersistenceType() || !PersistenceType[config.getPersistenceType()]) {
				throw {
					name: "IllegalArgumentException",
					message: "Invalid persistence type."
				};
			} else {
				for (var i=0; i<config.getMembers().length; i++) {
					if (config.getMembers()[i].getRole() === Role.MODERATOR) {
						return;
					}
				}
				throw {
					name: "IllegalArgumentException",
					message: "Please specify at least one moderator"
				};
			}
		};
		
		/**
		 * Sends an request to the server to get the supported data models of the given space.
		 * @private
		 * @function
		 * @memberOf SpacesSDK.SpaceHandler.prototype
		 * @param {SpacesSDK.Space} space The space to get the data models for.
		 * @param {SpacesSDK.SpaceHandler~retrieveSupportedDataModels$onSuccess} onSuccess The callback if the request succeeds.
		 * @param {SpacesSDK.SpaceHandler~retrieveSupportedDataModels$onError} onError The callback if an error occurs.
		 */
		var retrieveSupportedDataModels = function(space, onSuccess, onError) {
			if (!onSuccess || !onError){
				throw {
					name: "IllegalArgumentException",
					message: "You have to specify two Callbacks."
				};
			}
			if (space.getType().toLowerCase() !== SpacesSDK.Type.ORGA.toLowerCase()) {
				throw {
					name: "IllegalArgumentException",
					message: "The given id doesn't belong to an Orgaspace."
				};
			}
			var serviceEndpoint = 'spaces.' + connection.domain;
			var attrs = {xmlns: Strophe.NS.SPACES};
			var modelsIq = $iq({
				from: userInfo.getFullJID(),
				to: serviceEndpoint,
				type: 'get'
			}).c("spaces", attrs).c("models", {space: space.getId()});
			connection.sendIQ(modelsIq, function(msg) {
				var result = msg;
				var dataModels = new Array();
				models = result.getElementsByTagName('model');
				for (var i=0; i<models.length; i++) {
					var attributes = models[i].attributes;
					if (attributes.getNamedItem('namespace').value) {
						dataModels.push(new DataModel(attributes.getNamedItem('namespace').value, attributes.getNamedItem('schemaLocation').value));
					} else dataModels.push(new DataModel(attributes.getNamedItem('namespace').nodeValue, attributes.getNamedItem('schemaLocation').nodeValue));
				}
				onSuccess(dataModels);
			}, onError, timeout);
		};
		
		/**
		 * The callback if the request succeeds.
		 * Callback of {SpacesSDK.SpaceHandler#retrieveSupportedDataModels|SpaceHandler.retrieveSupportedDataModels()}.
		 * @callback SpacesSDK.SpaceHandler~retrieveSupportedDataModels$onSuccess
		 * @param {SpacesSDK.DataMode[]} List of supported data models.
		 */

		/**
		 * The callback if an error occurs.
		 * Callback of {SpacesSDK.SpaceHandler#retrieveSupportedDataModels|SpaceHandler.retrieveSupportedDataModels()}.
		 * @callback SpacesSDK.SpaceHandler~retrieveSupportedDataModels$onError
		 * @param {string} error Error message.
		 */
		
		var funcs = new Object();
		
		/**
		 * Tries to apply a configuration to a space.
		 * @function
		 * @memberOf SpacesSDK.SpaceHandler.prototype
		 * @param {string} spaceId Identifier of the space to apply configuration on. 
		 * @param {SpacesSDK.SpaceConfiguration} config Space configuration to apply.
		 * @param {SpacesSDK.SpaceHandler~configureSpace$onSuccess} onSuccess The callback if the request succeeds.
		 * @param {SpacesSDK.SpaceHandler~configureSpace$onError} onError The callback if an error occurs.
		 * @throws SpaceManagementException Failed to apply space configuration.
		 */
		var configureSpace = function(spaceId, config, onSuccess, onError) {
			if (!onSuccess || !onError){
				throw {
					name: "IllegalArgumentException.",
					message: "You have to specify two Callbacks."
				};
			}
			checkConfig(config);
			isModeratorOfSpace(userInfo.getBareJID(), spaceId, function(res) {
				if (!res) {
					throw new SpacesSDK.SpaceManagementException("You have to be an moderator of an space to change its configuration.");
				}
				var serviceEndpoint = 'spaces.' + connection.domain;
				var attrs = {xmlns: Strophe.NS.SPACES};
				var spaceConfigIQ = $iq({
					from: userInfo.getFullJID(),
					to: serviceEndpoint,
					type: 'set'
				}).c('spaces', attrs).c("configure",{space: spaceId}).c('x', {xmlns: Strophe.NS.DATA, type: 'submit'});
				spaceConfigIQ.c("field", {'var': 'FORM_TYPE', type:'hidden'})
								.c("value", null, Strophe.NS.SPACES + ":config").up();
				spaceConfigIQ.c("field", {'var': "spaces#type"})
								.c("value", null, Type[config.getType()].toLowerCase()).up();
				var persistent;
				switch (config.getPersistenceType()) {
				case SpacesSDK.PersistenceType.ON:
					persistent = "true";
					break;
				case SpacesSDK.PersistenceType.OFF:
					persistent = "false";
					break;
				default:
					persistent = config.getPersistenceDuration();
				}
				spaceConfigIQ.c("field", {'var': "spaces#persistent"}).c("value", null, persistent).up();
				spaceConfigIQ.c("field", {'var': "spaces#name"}).c("value", null, config.getName()).up();
				spaceConfigIQ.c("field", {'var': "spaces#members"});
				var members = config.getMembers();
				for (var i=0; i<members.length; i++) {
					var member = members[i];
					spaceConfigIQ.c("value", null, member.getJID());
				}
				spaceConfigIQ.up().c("field", {'var': "spaces#moderators"});
				for (var i=0; i<members.length; i++) {
					var member = members[i];
					if (member.getRole() === SpacesSDK.Role.MODERATOR) {
						spaceConfigIQ.c("value", null, member.getJID());
					}
				}
				connection.sendIQ(spaceConfigIQ, function(msg) {
					funcs.getSpace(spaceId, onSuccess, onError);
				}, onError, timeout);
			}, onError);
		};
		
		/**
		 * The callback if the request succeeds.
		 * Callback of {SpacesSDK.SpaceHandler#configureSpace|SpaceHandler.configureSpace()}.
		 * @callback SpacesSDK.SpaceHandler~configureSpace$onSuccess
		 * @param {SpacesSDK.Space} space Reconfigured space.
		 */

		/**
		 * The callback if an error occurs.
		 * Callback of {SpacesSDK.SpaceHandler#configureSpace|SpaceHandler.configureSpace()}.
		 * @callback SpacesSDK.SpaceHandler~configureSpace$onError
		 * @param {string} error Error message.
		 */
		
		/**
		 * Creates a private space for the current user.
		 * @function
		 * @memberOf SpacesSDK.SpaceHandler.prototype
		 * @param {SpacesSDK.SpaceHandler~createDefaultSpace$onSuccess} onSuccess The callback if the request succeeds. This will be called with the created private space of the current user as a parameter.
		 * @param {SpacesSDK.SpaceHandler~createDefaultSpace$onError} onError The callback if an error occurs.
		 */
		var createDefaultSpace = function(onSuccess, onError) {
			if (!onSuccess || !onError){
				throw {
					name: "IllegalArgumentException.",
					message: "You have to specify two Callbacks."
				};
			}			
			var serviceEndpoint = 'spaces.' + connection.domain;
			var attrs = {xmlns: Strophe.NS.SPACES};
			var spaceCreationIQ = $iq({
				from: userInfo.getFullJID(),
				to: serviceEndpoint,
				type: 'set'
			}).c('spaces', attrs).c("create", null);
			connection.sendIQ(spaceCreationIQ, function(msg) {
				funcs.getDefaultSpace(onSuccess, onError);
			}, onError, timeout);
		};
		
		/**
		 * The callback if the request succeeds.
		 * Callback of {SpacesSDK.SpaceHandler#createDefaultSpace|SpaceHandler.createDefaultSpace()}.
		 * @callback SpacesSDK.SpaceHandler~createDefaultSpace$onSuccess
		 * @param {SpacesSDK.Space} space Private space of the current user.
		 */

		/**
		 * The callback if an error occurs.
		 * Callback of {SpacesSDK.SpaceHandler#createDefaultSpace|SpaceHandler.createDefaultSpace()}.
		 * @callback SpacesSDK.SpaceHandler~createDefaultSpace$onError
		 * @param {string} error Error message.
		 */
		
		/**
		 * Creates a space with the given configuration.
		 * @function
		 * @memberOf SpacesSDK.SpaceHandler.prototype
		 * @param {SpacesSDK.SpaceConfiguration} config Space configuration to apply.
		 * @param {SpacesSDK.SpaceHandler~createSpace$onSuccess} onSuccess The callback if the request succeeds.
		 * @param {SpacesSDK.SpaceHandler~createSpace$onError} onError The callback if an error occurs.
		 */
		var createSpace = function(config, onSuccess, onError) {
			if (!onSuccess || !onError){
				throw {
					name: "IllegalArgumentException.",
					message: "You have to specify two Callbacks."
				};
			}		
			checkConfig(config);	
			var serviceEndpoint = 'spaces.' + connection.domain;
			var attrs = {xmlns: Strophe.NS.SPACES};
			var spaceCreationIQ = $iq({
				from: userInfo.getFullJID(),
				to: serviceEndpoint,
				type: 'set'
			}).c('spaces', attrs).c("create", null).up().c("configure",null).c('x', {xmlns: Strophe.NS.DATA, type: 'submit'});
			spaceCreationIQ.c("field", {'var': 'FORM_TYPE', type:'hidden'})
							.c("value", null, Strophe.NS.SPACES + ":config").up();
			spaceCreationIQ.c("field", {'var': "spaces#type"})
							.c("value", null, Type[config.getType()].toLowerCase()).up();
			var persistent;
			switch (config.getPersistenceType()) {
			case SpacesSDK.PersistenceType.ON:
				persistent = "true";
				break;
			case SpacesSDK.PersistenceType.OFF:
				persistent = "false";
				break;
			default:
				persistent = config.getPersistenceDuration();
			}
			spaceCreationIQ.c("field", {'var': "spaces#persistent"}).c("value", null, persistent).up();
			spaceCreationIQ.c("field", {'var': "spaces#name"})
							.c("value", null, config.getName()).up();
			spaceCreationIQ.c("field", {'var': "spaces#members"});
			var members = config.getMembers();
			for (var i=0; i<members.length; i++) {
				var member = members[i];
				spaceCreationIQ.c("value", null, member.getJID());
			}
			spaceCreationIQ.up().c("field", {'var': "spaces#moderators"});
			for (var i=0; i<members.length; i++) {
				var member = members[i];
				if (member.getRole() === SpacesSDK.Role.MODERATOR) {
					spaceCreationIQ.c("value", null, member.getJID());
				}
			}
			connection.sendIQ(spaceCreationIQ, function(msg) {
				var result = msg;
				funcs.getSpace(result.getElementsByTagName('create')[0].attributes.space.nodeValue, onSuccess, onError);
			}, onError, timeout);
		};
		
		/**
		 * The callback if the request succeeds.
		 * Callback of {SpacesSDK.SpaceHandler#createSpace|SpaceHandler.createSpace()}.
		 * @callback SpacesSDK.SpaceHandler~createSpace$onSuccess
		 * @param {SpacesSDK.Space} space Newly created space.
		 */

		/**
		 * The callback if an error occurs.
		 * Callback of {SpacesSDK.SpaceHandler#createSpace|SpaceHandler.createSpace()}.
		 * @callback SpacesSDK.SpaceHandler~createSpace$onError
		 * @param {string} error Error message.
		 */
		
		/**
		 * Deletes the space with the given id.
		 * @function
		 * @memberOf SpacesSDK.SpaceHandler.prototype
		 * @param {string} spaceId Identifier of the space to delete.
		 * @param {SpacesSDK.SpaceHandler~deleteSpace$onSuccess} [onSuccess] The callback if the requests succeeds. If not set, a message will be prompted to the console.
		 * @param {SpacesSDK.SpaceHandler~deleteSpace$onError} [onError] The callback if an error occurs. If not set, the error is prompted to the console.
		 */
		var deleteSpace = function(spaceId, onSuccess, onError) {
			if (!onSuccess) onSuccess = function() {
				console.log('Successfully deleted space.');
			};
			if (!onError) onError = function(error) {
				console.warn('Failed to delete space:', error);
			};
			isModeratorOfSpace(userInfo.getBareJID(), spaceId, function(res) {
				if (!res) {
					onError('You are not allowed to delete this space.');
					return;
				}
				var serviceEndpoint = 'spaces.' + connection.domain;
				var attrs = {xmlns: Strophe.NS.SPACES};
				var spaceDeletionIQ = $iq({
					from: userInfo.getFullJID(),
					to: serviceEndpoint,
					type: 'set'
				}).c('spaces', attrs).c("delete", {space: spaceId});
				connection.sendIQ(spaceDeletionIQ, function(result) {
					onSuccess();
				}, function(error) {
					onError(error);
				});
			}, function(error) {
				onError(error);
			});		
		};
		
		/**
		 * The callback if the request succeeds.
		 * Callback of {SpacesSDK.SpaceHandler#deleteSpace|SpaceHandler.deleteSpace()}.
		 * @callback SpacesSDK.SpaceHandler~deleteSpace$onSuccess
		 */

		/**
		 * The callback if an error occurs.
		 * Callback of {SpacesSDK.SpaceHandler#deleteSpace|SpaceHandler.deleteSpace()}.
		 * @callback SpacesSDK.SpaceHandler~deleteSpace$onError
		 * @param {string} error Error message.
		 */
		
		/**
		 * Requests all information for a space from the server.
		 * @private
		 * @function
		 * @memberOf SpacesSDK.SpaceHandler.prototype
		 * @param {SpacesSDK.Space} space The Space to get Information on.
		 * @param {function} onSuccess The callback if the request succeeds. This will be called with a new space with all information set.
		 * @param {function} onError The callback if an error occurs.
		 */
		var getAllSpaceInformation = function(space, onSuccess, onError) {
			if (!onSuccess || !onError){
				throw {
					name: "IllegalArgumentException.",
					message: "You have to specify two Callbacks."
				};
			}
			connection.spaces.getSpaceDetails(space.id, function(result) {
				var infos = result;
				connection.spaces.getSpaceChannels(space.id, function(result) {
					var channels = result;
					var members = [];
					var spaceChannels = []; 
					var spaceType = SpacesSDK.Type.getTypeForString(infos.type);
					var dataModels = [];
					for (var i = 0; i<infos.members.length; i++) {
						var role = SpacesSDK.Role.MEMBER;
						for (var j=0; j < infos.moderators.length; j++) {
							if (infos.moderators[j] === infos.members[i]) {
								role = SpacesSDK.Role.MODERATOR;
							}
						}
						members.push(new SpaceMember(infos.members[i], role));
					}
					for (var type in channels) {
						var properties = {};
						for (var key in channels[type]) {
							properties[key] = channels[type][key];
						}
						spaceChannels.push(new SpaceChannel(type, properties));
					}
					var persistenceType;
					var persistenceDuration;
					switch (typeof infos.persistent) {
						case "boolean":
							persistenceType = infos.persistent ? SpacesSDK.PersistenceType.ON : SpacesSDK.PersistenceType.OFF;
							persistenceDuration = null;
							break;
						case "string":
							persistenceType = SpacesSDK.PersistenceType.DURATION;
							persistenceDuration = infos.persistent;
							break;
					}
					if (spaceType === SpacesSDK.Type.ORGA) {
						retrieveSupportedDataModels(new Space(space.name, space.id, space.domain, 
								dataModels, spaceType, spaceChannels, members, persistenceType, persistenceDuration), function(res) {
							onSuccess(new Space(space.name, space.id, space.domain, res, 
							spaceType, spaceChannels, members, 
							persistenceType, persistenceDuration));
						}, onError);
					} else {
						onSuccess(new Space(space.name, space.id, space.domain, dataModels, 
								spaceType, spaceChannels, members, 
								persistenceType, persistenceDuration));
					}
					}, onError, timeout);
				}, onError, timeout);
		};
		
		/**
		 * Returns a list of all spaces available to the user.
		 * The information is retrieved from the server.
		 * @function
		 * @memberOf SpacesSDK.SpaceHandler.prototype
		 * @param {SpacesSDK.SpaceHandler~getAllSpaces$onSuccess} onSuccess The callback if the request succeeds.
		 * @param {SpacesSDK.SpaceHandler~getAllSpaces$onError} onError The callback if an error occurs.
		 */
		var getAllSpaces = function(onSuccess, onError) {
			if (!onSuccess || !onError){
				throw {
					name: "IllegalArgumentException.",
					message: "You have to specify two Callbacks."
				};
			}
			connection.spaces.getSpacesList(function(res) {
				var spaces = res;
				var result = [];
				if (spaces.length == 0) {
					onSuccess(result);
					return;
				}
				var checked = new Object();
				var callBack = function(res2) {
					if (!checked[res2.getId()]) {
						checked[res2.getId()] = true;
						result.push(res2);
						
						for (var i=0; i<spaces.length; i++) {
							if (!checked[spaces[i].id]) return;
						}
						onSuccess(result);
					}
				};
				for (var i = 0; i<spaces.length; i++) {
					var number = i;
					var errCallback = function(res) {
						if (!checked[spaces[number].Id]) {
							checked[spaces[number].id] = true;
							for (var j=0; j<spaces.length; j++) {
								if (!checked[spaces[j].id]) return;
							}
							onSuccess(result);
						}
					};
					getAllSpaceInformation(spaces[i], callBack, errCallback);
				}
				}, onError, timeout);
		};
		
		/**
		 * The callback if the request succeeds.
		 * Callback of {SpacesSDK.SpaceHandler#getAllSpaces|SpaceHandler.getAllSpaces()}.
		 * @callback SpacesSDK.SpaceHandler~getAllSpaces$onSuccess
		 * @param {SpacesSDK.Space[]} List containing all spaces available to the user. May be empty.
		 */

		/**
		 * The callback if an error occurs.
		 * Callback of {SpacesSDK.SpaceHandler#getAllSpaces|SpaceHandler.getAllSpaces()}.
		 * @callback SpacesSDK.SpaceHandler~getAllSpaces$onError
		 * @param {string} error Error message.
		 */
		
		/**
		 * Returns the private space of the user. 
		 * The information is retrieved from the server, and the information is returned.
		 * @function
		 * @memberOf SpacesSDK.SpaceHandler.prototype
		 * @param {SpacesSDK.SpaceHandler~getDefaultSpace$onSuccess} onSuccess The callback if the request succeeds.
		 * @param {SpacesSDK.SpaceHandler~getDefaultSpace$onError} onError The callback if an error occurs.
		 */
		var getDefaultSpace = function(onSuccess, onError) {
			funcs.getSpace(userInfo.getUsername(), onSuccess, onError);
		};
		
		/**
		 * The callback if the request succeeds.
		 * Callback of {SpacesSDK.SpaceHandler#getDefaultSpace|SpaceHandler.getDefaultSpace()}.
		 * @callback SpacesSDK.SpaceHandler~getDefaultSpace$onSuccess
		 * @param {SpacesSDK.Space} space Private space of the current user or <code>null</code> if no private space exists.
		 */

		/**
		 * The callback if an error occurs.
		 * Callback of {SpacesSDK.SpaceHandler#getDefaultSpace|SpaceHandler.getDefaultSpace()}.
		 * @callback SpacesSDK.SpaceHandler~getDefaultSpace$onError
		 * @param {string} error Error message.
		 */
		
		/**
		 * Returns a specific space.
		 * The information is retrieved from the server and the information is returned.
		 * @function
		 * @memberOf SpacesSDK.SpaceHandler.prototype
		 * @param {string} spaceId Identifier of the space to retrieve.
		 * @param {SpacesSDK.SpaceHandler~getSpace$onSuccess} onSuccess The callback if the request succeeds.
		 * @param {SpacesSDK.SpaceHandler~getSpace$onError} onError The callback if an error occurs.
		 */
		var getSpace = function(spaceId, onSuccess, onError) {
			if (!onSuccess || !onError){
				throw {
					name: "IllegalArgumentException",
					message: "You have to specify two Callbacks."
				};
			}
			connection.spaces.getSpacesList(function(result) {
				var spaces = result;
				var space = null;
				for (var i = 0; i<spaces.length; i++) {
					if (spaces[i].id !== spaceId) continue;
					space = spaces[i];
				}
				if (space != null) {
					getAllSpaceInformation(space, onSuccess, onError);
				}
				else onSuccess(null);
				}, onError, timeout);
		};
		
		/**
		 * The callback if the request succeeds.
		 * Callback of {SpacesSDK.SpaceHandler#getSpace|SpaceHandler.getSpace()}.
		 * @callback SpacesSDK.SpaceHandler~getSpace$onSuccess
		 * @param {SpacesSDK.Space} space Space wit the given identifer or <code>null</code> if such a space does not exists.
		 */

		/**
		 * The callback if an error occurs.
		 * Callback of {SpacesSDK.SpaceHandler#getSpace|SpaceHandler.getSpace()}.
		 * @callback SpacesSDK.SpaceHandler~getSpace$onError
		 * @param {string} error Error message.
		 */
		
		/**
		 * Returns a map containing the space identifiers and names for all spaces available to the user.
		 * The information is retrieved from the server.
		 * Requesting the list without spaces details is faster and should be preferred to {@link SpacesSDK.SpaceHandler#getAllSpaces} whenever possible.
		 * @function
		 * @memberOf SpacesSDK.SpaceHandler.prototype
		 * @param {SpacesSDK.SpaceHandler~getSpacesList$onSuccess} onSuccess The callback if the request succeeds.
		 * @param {SpacesSDK.SpaceHandler~getSpacesList$onError} onError The callback if an error occurs.
		 */
		var getSpacesList = function(callBack, onError) {
			if (!callBack || !onError){
				throw {
					name: "IllegalArgumentException",
					message: "You have to specify two Callbacks."
				};
			}
			var spacesCallback = function(result) {
				var spaces = result;
				var result = {};
				if (spaces != null) {
					for (var i=0; i<spaces.length; i++) {
						result[spaces[i].id] = spaces[i].name;
					}
				}
				callBack(result);
			};
			connection.spaces.getSpacesList(spacesCallback, onError, timeout);
		};
		
		/**
		 * The callback if the request succeeds.
		 * Callback of {SpacesSDK.SpaceHandler#getSpacesList|SpaceHandler.getSpacesList()}.
		 * @callback SpacesSDK.SpaceHandler~getSpacesList$onSuccess
		 * @param {object} spacesList Map with spaces identifiers as keys and space names as values. 
		 */

		/**
		 * The callback if an error occurs.
		 * Callback of {SpacesSDK.SpaceHandler#getSpacesList|SpaceHandler.getSpacesList()}.
		 * @callback SpacesSDK.SpaceHandler~getSpacesList$onError
		 * @param {string} error Error message.
		 */
		
		/**
		 * Sets the data models supported by an organizational space.
		 * Performing this operation on spaces of types other than {@link SpacesSDK.Type.ORGA} will fail.
		 * Only data objects instantiating one of the supported data models may be published on this space.
		 * @function
		 * @memberOf SpacesSDK.SpaceHandler.prototype
		 * @param {string} spaceId Identifier of the organizational space to set list of supported models for.
		 * @param {SpacesSDK.DataModel[]} dataModels Array of data models to support.
		 * @param {SpacesSDK.SpaceHandler~setModelsSupportedBySpace$onSuccess} onSuccess The callback if the request succeeds.
		 * @param {SpacesSDK.SpaceHandler~setModelsSupportedBySpace$onError} onError The callback if an error occurs.
		 * @throws SpaceManagementException Failed to apply setting, e.g., because the space is not of type Space.SpacesSDK.Type.ORGA.
		 */
		var setModelsSupportedBySpace = function(spaceId, dataModels, onSuccess, onError) {
			if (!onSuccess || !onError){
				throw {
					name: "IllegalArgumentException.",
					message: "You have to specify two Callbacks."
				};
			}
			isModeratorOfSpace(userInfo.getBareJID(), spaceId, function(res) {
				if (!res) {
					throw new SpacesSDK.SpaceManagementException("You have to be an moderator of an space to delete it.");
				}
				funcs.getSpace(spaceId, function(space){
				if (space == null) {
					throw new SpacesSDK.UnknownEntityException("A space with such an id couldn't be found.");
				} else if (space.getType().toLowerCase() !== SpacesSDK.Type.ORGA.toLowerCase()) {
					throw {
						name: "IllegalArgumentException.",
						message: "The given id doesn't belong to an Orgaspace."
					};
				}
				var serviceEndpoint = 'spaces.' + connection.domain;
				var attrs = {xmlns: Strophe.NS.SPACES};
				var setModelsIQ = $iq({
					from: userInfo.getFullJID(),
					to: serviceEndpoint,
					type: 'set'
				}).c('spaces', attrs).c("models", {space: spaceId});
				for (var i=0; i<dataModels.length; i++) {
					setModelsIQ.c("model", {namespace: dataModels[i].getNamespace(), 
						schemaLocation: dataModels[i].getSchemaLocation()}).up();
				}
				// var persistent;
				// switch (space.getPersistenceType()) {
				// case SpacesSDK.PersistenceType.ON:
					// persistent = true;
					// break;
				// case SpacesSDK.PersistenceType.OFF:
					// persistent = false;
					// break;
				// case SpacesSDK.PersistenceType.DURATION:
					// persistent = space.getPersistenceDuration();
					// break;
				// }
				connection.sendIQ(setModelsIQ, function(msg) {
					onSuccess(new Space(space.getName(), spaceId, space.getDomain(), dataModels, 
							space.getType(), space.getChannels(), space.getMembers(), space.getPersistenceType(), space.getPersistenceDuration()));
				}, onError, timeout);
			}, onError);
			}, onError);
		};
		
		/**
		 * The callback if the request succeeds.
		 * Callback of {SpacesSDK.SpaceHandler#setModelsSupportedBySpace|SpaceHandler.setModelsSupportedBySpace()}.
		 * @callback SpacesSDK.SpaceHandler~setModelsSupportedBySpace$onSuccess
		 * @param {SpacesSDK.Space} space Reconfigured space. 
		 */

		/**
		 * The callback if an error occurs.
		 * Callback of {SpacesSDK.SpaceHandler#setModelsSupportedBySpace|SpaceHandler.setModelsSupportedBySpace()}.
		 * @callback SpacesSDK.SpaceHandler~setModelsSupportedBySpace$onError
		 * @param {string} error Error message.
		 */
		
		
		var funcs = {
			'configureSpace' : configureSpace,
			'createDefaultSpace' : createDefaultSpace,
			'createSpace' : createSpace,
			'deleteSpace' : deleteSpace,
			'getAllSpaces' : getAllSpaces,
			'getDefaultSpace' : getDefaultSpace,
			'getSpace' : getSpace,
			'getSpacesList' : getSpacesList,
			'setModelsSupportedBySpace' : setModelsSupportedBySpace
		};
		
		return funcs;
	};
	
	/**
	 * Create a new space member. To add the member to the space, see {@link SpacesSDK.SpaceHandler#configureSpace}.
	 * @class Model for a space member.
	 * @name SpaceMember
	 * @memberOf SpacesSDK
	 * @param {string} jid The bare-JID of the member.
	 * @param {SpacesSDK.Role} role The role the member should take.
	 */
	var SpaceMember = function(jid, role) {
		
		(function(){
			if (!jid.match(/@/)) {
				throw {
					name: "IllegalArgumentException",
					message: "There has to be a domain as part of the jid."
				};
			}
			if (jid.match(/\//)) {
				jid = jid.split("/")[0];
			}
		})();
		
		var equals = function(o) {
			if (o.getJID == null || typeof o.getJID !== "function") {
				return false;
			} else if (o.getRole == null || typeof o.getRole !== "function") {
				return false;
			} else if (o.getJID() === jid && o.getRole === role) {
				return true;
			}
			return false;
		};
		
		/**
		 * Returns the user JID of the member.
		 * @function
		 * @memberOf SpacesSDK.SpaceMember.prototype
		 * @return {string} Bare-JID of the member.
		 */
		var getJID = function() {
			return jid;
		};
		
		/**
		 * Returns the role of the member.
		 * @function
		 * @memberOf SpacesSDK.SpaceMember.prototype
		 * @return {SpacesSDK.Role} <code>SpacesSDK.Role.MODERATOR</code> if the member is also moderator of the space, otherwise <code>SpacesSDK.Role.MEMBER</code>.
		 */
		var getRole = function() {
			return role;
		};
		
		var funcs = {
			'equals' : equals,
			'getJID' : getJID,
			'getRole' : getRole
		};
		
		return funcs;
	};

	/**
	 * Internally used.
	 * @class Model for the XMPP user information.
	 * @name UserInfo
	 * @memberOf SpacesSDK
	 * @param {string} username The currently used username.
	 * @param {string} domain The domain the user is connected to.
	 * @param {string} resource The unique identifier of the application.
	 */
	var UserInfo = function(username, domain, resource) {
		
		/**
		 * Returns the bare-JID of the user.
		 * The bare-JID contains the node-id and domain string, e.g. "alice@mirror-demo.eu".
		 * @function
		 * @memberOf SpacesSDK.UserInfo.prototype
		 * @return {string} Bare-JID as string.
		 */
		var getBareJID = function() {
			var JID = [];
			JID.push(username, '@', domain);
			return JID.join("");
		};

		/**
		 * Returns the XMPP domain the user is registered.
		 * @function
		 * @memberOf SpacesSDK.UserInfo.prototype
		 * @return {string} XMPP domain string.
		 */
		var getDomain = function() {
			return domain;
		};

		/**
		 * Returns the full JID of the user.
		 * Additionally to the bare-JID, the fill-JID also contains the resource identifier,
		 * e.g. "alice@mirror-demo.eu/myApp01".
		 * @function
		 * @memberOf SpacesSDK.UserInfo.prototype
		 * @return {string} Full-JID as string.
		 */
		var getFullJID = function() {
			var fullJID = [];
			fullJID.push(username, '@', domain, '/', resource);
			return fullJID.join("");
		};

		/**
		 * Returns the XMPP resource identifier of the client the user is connected with.
		 * @function
		 * @memberOf SpacesSDK.UserInfo.prototype
		 * @return {string} XMPP resource string.
		 */
		var getResource = function() {
			return resource;
		};

		/**
		 * Returns the XMPP username (aka node-id) of the user.
		 * The XMPP username is unique in an XMPP domain. 
		 * @function
		 * @memberOf SpacesSDK.UserInfo.prototype
		 * @return {string} Username of the user.
		 */
		var getUsername = function() {
			return username;
		};
		
		var funcs = {
			'getBareJID' : getBareJID,
			'getDomain' : getDomain,
			'getFullJID' : getFullJID,
			'getResource' : getResource,
			'getUsername' : getUsername
		};
		
		return funcs;
	};
	
	var modules = {
		'VERSION' : VERSION,
		'Utils' : Utils,
		'cdm' : cdm,
		'ConnectionStatus' : ConnectionStatus,
		'Role' : Role,
		'Type' : Type,
		'PersistenceType' : PersistenceType,
		'SpaceManagementException' : SpaceManagementException,
		'UnknownEntityException' : UnknownEntityException,
		'CDMData' : CDMData,
		'CDMDataBuilder' : CDMDataBuilder,
		'ConnectionConfiguration' : ConnectionConfiguration,
		'ConnectionConfigurationBuilder' : ConnectionConfigurationBuilder,
		'ConnectionHandler' : ConnectionHandler,
		'ConnectionStatusListener' : ConnectionStatusListener,
		'NetworkInformation' : NetworkInformation,
		'DataHandler' : DataHandler,
		'DataModel' : DataModel,
		'DataObject' : DataObject,
		'DataObjectBuilder' : DataObjectBuilder,
		'DataObjectListener' : DataObjectListener,
		'Space' : Space,
		'SpaceChannel' : SpaceChannel,
		'SpaceConfiguration' : SpaceConfiguration,
		'SpaceHandler' : SpaceHandler,
		'SpaceMember' : SpaceMember,
		'UserInfo' : UserInfo,
		'filter' : filter
	};
	
	return modules;
})();
