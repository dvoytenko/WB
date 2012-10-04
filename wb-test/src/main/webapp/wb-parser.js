

/**
 * Special commands:
 * - "_type" - indicates an object type looked up in the WB namespace
 * - "_desc" - description
 */
WB.Parser = WB.Class.extend({
	
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

/*

Segment:

	MoveToSegment:
		{
			_type: 'MoveToSegment',
			point: {x, y}
		}
	
	LineToSegment:
		{
			_type: 'LineTo',
			point: {x, y}
		}

	LineToSegment:
		{
			_type: 'LineTo',
			distance: 100,
			angle: PI/2
		}

Shape:

	Circle(Arc or Ellipse):
		{
			_type: 'Circle',
			arcSegment: {
				_type: 'ArcAngleSegment',
				center: {x, y}
				radius: 100,
				startAngle: 0,
				endAngle: PI2
			}
		}
		
	Group:
		{
			_type: 'Group',
			transform: {
				_type: 'Transform',
				_desc: 'translate(100, 100) rotate(20)',
				m: [a,b,c,d,e,f]
			},
			shapes: []
		}
		
	Path:
		{
			_type: 'Path',
			pathSegment: {
				_type: 'PathSegment',
				startPoint: {x,y},
				segments: []
			}
		}
 
*/
