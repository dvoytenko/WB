
var WB = {};

(function() {
    var initializing = false;
    
    // The base Class implementation (does nothing)
    WB.Class = function() {
    };
    
    // Create a new Class that inherits from this class
    WB.Class.extend = function(type, prop) {
        var _super = this.prototype;

        // Instantiate a base class (but only create the instance,
        // don't run the init constructor)
        initializing = true;
        var prototype = new this();
        initializing = false;

        // type
        prototype._type = type;

        // Copy the properties over onto the new prototype
        for(var name in prop) {
            // Check if we're overwriting an existing function
            prototype[name] = typeof prop[name] == "function" && typeof _super[name] == "function" ? (function(name, fn) {
                return function() {
                    var tmp = this._super;

                    // Add a new ._super() method that is the same method
                    // but on the super-class
                    this._super = _super[name];

                    // The method only need to be bound temporarily, so we
                    // remove it when we're done executing
                    var ret;
                    if (!WB.interceptor) {
                        ret = fn.apply(this, arguments);
                    } else {
                    	ret = WB.interceptor(this, name, fn, arguments);
                    }
                    this._super = tmp;

                    return ret;
                };
            })(name, prop[name]) : prop[name];
        }

        // The dummy class constructor
        function Class() {
            // All construction is actually done in the init method
            if(!initializing && this.init) {
            	// TODO copy/clone all properties?
                this.init.apply(this, arguments);
            }
        }
        // Populate our constructed prototype object
        Class.prototype = prototype;

        // Enforce the constructor to be what we expect
        Class.prototype.constructor = Class;

        // And make this class extendable
        Class.extend = arguments.callee;

        return Class;
    };
})();
