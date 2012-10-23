

/**
 * Special commands:
 * - "_type" - indicates an object type looked up in the WB namespace
 * - "_desc" - description
 */
WB.Parser = WB.Class.extend('Parser', {
	
	parse: function(js, type) {
		
		if (!js) {
			return js;
		}
		
		var obj;
		
		if (js.constructor == Array) {
			obj = [];
			for (var i = 0; i < js.length; i++) {
				obj.push(this.parse(js[i]));
			}
		} else if (js.constructor == Object) {
			
			var opts = {};
			for (var prop in js) {
				if (prop == '_type' || prop == '_desc') {
					continue;
				}
				opts[prop] = this.parse(js[prop]);
			}
			
			if (!type) {
				type = js._type;
			}
			if (type) {
				var constr = WB[type];
				if (!constr) {
					throw "Uknown type '" + type + "'";
				}
				obj = new constr(opts);
			} else {
				obj = opts;
			}
		} else {
			obj = js;
		}
		
		return obj;
	}
	
});

