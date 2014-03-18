/** File: strophe.spaces.js
 *
 * A Strophe plugin for the MIRROR Spaces Service.
 * 
 * Dependencies: strophe.x.js
 */

Strophe.addConnectionPlugin('spaces', {
	_connection: null,
	_serviceEndpoint : null,
	
	/** Function: init
	 * Plugin init
	 *
	 * Parameters:
	 *   (Strophe.Connection) conn - Strophe connection
	 */
	init: function(conn) {
		this._connection = conn;
		Strophe.addNamespace('SPACES', 'urn:xmpp:spaces');
	},
	
	/**
	 * Manually sets the endpoint of the MIRROR Spaces Service. If not set, the spaces service of
	 * the domain the user is loggin in is used.
	 *
	 * Parameters:
	 *  (String) Component address of the MIRROR spaces server, e.g., spaces.mirror-demo.eu.
	 */
	setServiceEndpoint: function(endpoint) {
		this._serviceEndpoint = endpoint;
	},
	
	/** Function: getSpacesList
	 * Returns the list of all spaces available to the user.
	 *
	 * Parameters:
	 *   (Function) onResult - Function to call when the result is available.
	 *   (Function) onError - Function to call when an error occures.
	 *   (Integer) timeout - The time specified in milliseconds for a timeout to occur.
	 */
	getSpacesList: function(onResult, onError, timeout) {
		var serviceEndpoint = (this._serviceEndpoint) ? this._serviceEndpoint : 'spaces.' + this._connection.domain;
		var attrs = {xmlns: Strophe.NS.DISCO_ITEMS};
		var itemsIQ = $iq({
			from: this._connection.jid,
			to: serviceEndpoint,
			type: 'get'
		}).c('query', attrs);
		
		var successHandler = function (iq) {
			if (!onResult) return;
			var xml = $(iq);
			var spaces = [];
			var itemElements = xml.find('item');
			for (var i=0; i < itemElements.length; i++) {
				var itemElement = itemElements[i];
				var space = {'id': itemElement.getAttribute('node'), 'name': itemElement.getAttribute('name'), 'domain': itemElement.getAttribute('jid')};
				spaces.push(space);
			}
			onResult(spaces);
		};
		var errorHandler = function (iq) {
			if (!onError) return;
			var error = {};
			if (iq == null) {
				error.code = 504;
				error.message = 'Request timed out.';
				error.stanza = null;
			} else {
				var xml = $(iq);
				var errorElement = xml.find('error');
				error.code = errorElement[0].getAttribute('code');
				error.message = 'Retrieved error response.';
				error.stanza = errorElement[0];
			}
			onError(error);
		};
		this._connection.sendIQ(itemsIQ, successHandler, errorHandler, timeout);
	},
	
	/** Function: getSpaceDetails
	 * Returns the details (metadata) of a space.
	 *
	 * Parameters:
	 *   (String) spaceId - Identifier of the space to request details for.
	 *   (Function) onResult - Function to call when the result is available.
	 *   (Function) onError - Function to call when an error occures.
	 *   (Integer) timeout - The time specified in milliseconds for a timeout to occur.
	 */
	getSpaceDetails: function(spaceId, onResult, onError, timeout) {
		var serviceEndpoint = (this._serviceEndpoint) ? this._serviceEndpoint : 'spaces.' + this._connection.domain;
		var attrs = {xmlns: Strophe.NS.DISCO_INFO};
		if (spaceId) {
			attrs.node = spaceId;
		} else {
			var error = {};
			error.code = 400;
			error.message = 'Cannot request space details: No space identifier set.';
			callback({'error': error});
			return;
		}

		var infoIQ = $iq({
			from: this._connection.jid,
			to: serviceEndpoint,
			type: 'get'
		}).c('query', attrs);
		
		var conn = this._connection;
		
		var successHandler = function (iq) {
			if (!onResult) return;
			var parsedResult = conn.x.parseFromResult(iq).toJSON();
			var space = {};
			space.id = spaceId;
			for (var i = 0; i < parsedResult.fields.length; i++) {
				var field = parsedResult.fields[i];
				switch (field['var']) {
				case 'spaces#type':
					space.type = field['values'][0];
					break;
				case 'spaces#persistent':
					switch (field['values'][0]) {
					case 'true':
						space.persistent = true;
						break;
					case 'false':
						space.persistent = false;
						break;
					default:
						space.persistent = field['values'][0];
					}
					break;
				case 'spaces#name':
					space.name = field['values'][0];
					break;
				case 'spaces#members':
					space.members = [];
					for (var j = 0; j < field['values'].length; j++) {
						space.members.push(field['values'][j]);
					}
					break;
				case 'spaces#moderators':
					space.moderators = [];
					for (var k = 0; k < field['values'].length; k++) {
						space.moderators.push(field['values'][k]);
					}
					break;
				}
			}
			onResult(space);
		};
		var errorHandler = function (iq) {
			if (!onError) return;
			var error = {};
			if (iq == null) {
				error.code = 504;
				error.message = 'Request timed out.';
				error.stanza = null;
			} else {
				var xml = $(iq);
				var errorElement = xml.find('error');
				error.code = errorElement[0].getAttribute('code');
				error.message = 'Retrieved error response.';
				error.stanza = errorElement[0];
			}
			onError(error);
		};
		this._connection.sendIQ(infoIQ, successHandler, errorHandler, timeout);
	},
	
	/** Function: getSpaceChannels
	 * Returns the channels of a space and their properties.
	 *
	 * Parameters:
	 *   (String) spaceId - Identifier of the space to request channels for.
	 *   (Function) onResult - Function to call when the result is available.
	 *   (Function) onError - Function to call when an error occures.
	 *   (Integer) timeout - The time specified in milliseconds for a timeout to occur.
	 */
	getSpaceChannels: function(spaceId, onResult, onError, timeout) {
		var serviceEndpoint = (this._serviceEndpoint) ? this._serviceEndpoint : 'spaces.' + this._connection.domain;
		if (spaceId == null) {
			var error = {};
			error.code = 400;
			error.message = 'Cannot request space channels: No space identifier set.';
			callback({'error': error});
			return;
		}
		var channelsIQ = $iq({
			from: this._connection.jid,
			to: serviceEndpoint,
			type: 'get'
		}).c('spaces', {'xmlns': Strophe.NS.SPACES}).c('channels', {'space': spaceId});
		
		var successHandler = function (iq) {
			if (!onResult) return;
			var xml = $(iq);
			var channels = {};
			var channelElements = xml.find('channel');
			for (var i = 0; i < channelElements.length; i++) {
				var channelElement = channelElements[i];
				var type = channelElement.getAttribute('type');
				channels[type] = {};
				
				for (var j = 0; j < channelElement.childNodes.length; j++) {
					var propertyElement = channelElement.childNodes[j];
					var key = propertyElement.getAttribute('key');
					var value = propertyElement.firstChild.data;
					channels[type][key] = value;
				}
			}
			onResult(channels);
		};
		var errorHandler = function (iq) {
			if (!onError) return;
			var error = {};
			if (iq == null) {
				error.code = 504;
				error.message = 'Request timed out.';
				error.stanza = null;
			} else {
				var xml = $(iq);
				var errorElement = xml.find('error');
				error.code = errorElement[0].getAttribute('code');
				error.message = 'Retrieved error response.';
				error.stanza = errorElement[0];
			}
			onError(error);
		};
		this._connection.sendIQ(channelsIQ, successHandler, errorHandler, timeout);
	}
});