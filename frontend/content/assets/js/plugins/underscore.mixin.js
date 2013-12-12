/*
 * Mixin underscore.string.js string utilities.
 */
_.mixin(_.string.exports());

/*
 * Mixin custom underscore utility functions.
 */
_.mixin({

    /*
     * No Operation.
     */
    noop: function() {},

    /*
     * Check if a variable is set (i.e. not undefined or null).
     */
    isSet: function(variable) {
        return (typeof variable !== 'undefined') && (variable !== null);
    },

    /*
     * Check if a varaible is not set.
     */
    notSet: function(variable) {
        return !_(variable).isSet();
    },

    /*
     * Returns the identity function with an argument.
     */
    identityFn: function(arg) {
        return function() {
            return _.identity.call(_, arg);
        };
    },

    /*
     * Get nested property from an object: foo.get('a.b.c')
     */
    get: function(obj, props) {
        if (!obj || !props) return undefined;
        if (props.indexOf('.') === -1) return obj[props];
        var first = props.substring(0, props.indexOf('.'));
        var rest  = props.substring(props.indexOf('.') + 1);
        return _.get(obj[first], rest);
    },

    /*
     * Set nested property on an object: foo.get('a.b.c')
     */
    set: function(obj, props, val, original) {
        if (!original) original = obj;
        if (!obj || !props) return undefined;
        if (props.indexOf('.') === -1) {
            obj[props] = val;
            return original;
        }
        var first = props.substring(0, props.indexOf('.'));
        var rest  = props.substring(props.indexOf('.') + 1);
        if (!obj[first]) obj[first] = {};
        return _.set(obj[first], rest, val, original);
    },

    /*
     * Check if a variable is blank (undefined, null, [], {}, '', 0).
     */
    isBlank: function(obj) {
        return (
            _.notSet(obj) ||
                (_.isArray(obj) && obj.length === 0) ||
                (_.isObject(obj) && _.keys(obj).length === 0) ||
                obj === '' ||
                obj === 0
            );
    },

    /*
     * Check if an object lacks a key.
     */
    missing: function(obj, key) {
        return !_.has(obj, key);
    },

    /*
     * Return a copy of the object without the blacklisted properties.
     */
    exclude: function(obj) {
        var blacklist = _.flatten(Array.prototype.slice.call(arguments, 1));
        return _.pick(obj, _.difference(_.keys(obj), blacklist));
    },

    /*
     * Generate a timestamp.
     *
     * @extra: Anything to add to the timestamp.
     */
    timestamp: function(extra) {
        return '' + (new Date()).getTime() + (extra || '');
    },

    /*
     * Generate a random hash of length @n.
     */
    hash: function(n) {
        var text = '', possible = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
        for (var i=0; i < n; i++) text += possible.charAt(Math.floor(Math.random() * possible.length));
        return text;
    },

    /*
     * Wrap anything in an array, unless its already an array.
     */
    wrapInArray: function(obj) {
        if (_(obj).notSet()) return [];
        if (!_(obj).isArray()) return [obj];
        return obj;
    },

    /*
     * Ensure that the variable is an array, and then map over its values.
     */
    wrapMap: function(obj, fn) {
        return _.map(_.wrapInArray(obj), fn);
    },

    /*
     * Unwrap one-element arrays or objects with one-array elements as values.
     */
    unwrap: function(obj) {
        if (_.isArray(obj) && obj.length === 1) return obj[0];
        if (_.isObject(obj)) _.each(obj, function(v, k) {
            obj[k] = _.unwrap(v);
        });
        return obj;
    },

    /*
     * Split an array in groups of N items.
     */
    inGroupsOf: function(a, n) {
        return _.reduce(a, function(g, el, i) {
            if (i % n === 0) {
                g.push([el]);
            } else {
                _.last(g).push(el);
            }
            return g;
        }, []);
    },

    /*
     * Remove an item from a collection, based on a key and target value.
     *
     * @collection: An array of instances.
     * @key: The key to use for evaluation equality.
     * @value: The value for @key to search for.
     */
    remove: function(collection, key, value) {
        collection.splice(collection.indexOf(_(collection).findBy(key, value)), 1);
        return collection;
    },

    /*
     * Get the value of a property, or invoke it to get the value if its a function.
     */
    resultOf: function(item, key) {
        if (typeof item[key] === 'function') {
            return item[key]();
        } else {
            return item[key];
        }
    },

    /*
     * Given an object with keys and functions, this will invoke each function,
     * and assign its result to its respective key.
     */
    invokeAll: function(obj) {
        _.each(obj, function(value, key) {
            obj[key] = _.resultOf(obj, key);
        });
        return obj;
    },

    /*
     * Given an object where the keys are names, and the values are functions that take callbacks,
     * run each function and finally run one callback with an object of names/results.
     */
    callbacks: function(obj, callback) {
        var res = {}, i = _.keys(obj).length;
        _.each(obj, function(fn, key) {
            fn(function() {
                res[key] = _.toArray(arguments);
                if (--i === 0) callback(res);
            });
        });
    },

    /*
     * Find a model instance in an array of instances.
     * This will only return the first matching object.
     *
     * @collection: An array of instances.
     * @key: The key to use for equality.
     * @value: The value for @key to search for.
     */
    findBy: function(collection, key, value) {
        var matches = _(collection).filter(function(item) {
            return _(item).resultOf(key) === value;
        });
        return matches.length > 0 ? matches[0] : null;
    },

    /*
     * Case-insensitive search for a query string in multiple target strings.
     */
    findByString: function(q) {
        return _.any(_.rest(arguments), function(t) {
            return q && t && _.string.include(t.toLowerCase(), q.toLowerCase());
        });
    },

    /*
     * Returns an array of IDs from the passed model instances.
     *
     * @collection: An array of instances, or one instance.
     */
    ids: function(collection) {
        var matches = _.map(_.wrapInArray(collection), function(e) {
            if (_.isNumber(e) || _.isString(e)) return '' + e;
            if (!_.isObject(e)) return null;
            return _.resultOf(e, 'id');
        });
        return _(matches).filter(function(e) {
            return _.isSet(e);
        });
    },

    /*
     * Convert a string field into a variable of the correct type.
     */
    field: function(instance, key) {
        return _.parse(instance.data[key]);
    },

    /*
     * Parse a string to the correct type.
     */
    parse: function(str) {
        if (str === 'true')  return true;
        if (str === 'false') return false;
        return str;
    },

    /*
     * Run a function with an object as the argument, or, if the object is an array,
     * run the function with each of the objects in the array.
     *
     * Any extra arguments are passed to the function after each object.
     */
    applyAll: function(obj, fn) {
        var args = _(_(arguments).toArray()).rest(2);
        if (_(obj).isArray()) {
            return _(obj).map(function(e) {
                return fn.apply(this, [e].concat(args));
            });
        } else {
            return fn.apply(this, [obj].concat(args));
        }
    },

    /*
     * Remove an item from an array in place.
     */
    arrayRemove: function(array, from, to) {
        var rest = array.slice((to || from) + 1 || array.length);
        array.length = from < 0 ? array.length + from : from;
        return array.push.apply(array, rest);
    },

    /*
     * Remove a list of items from an array in place.
     */
    arrayRemoveAll: function(array, toRemoveArray) {
        return _.reject(array, function(e) {
            return _.contains(toRemoveArray, e);
        });
    },

    /*
     * Return the sum of each n-cell in a 2D array.
     */
    arraySum: function(arr, n) {
        if (!arr) {
            return 0;
        }
        if (!_.isArray(arr[0])) {
            return _.reduce(arr, function(memo, num) {
                return memo + num;
            }, 0);
        }
        return _.reduce(arr, function(memo, row) {
            return memo + (row[n] || 0);
        }, 0);
    },

    /*
     * Clean an array of strings (trim + lowercase).
     */
    cleanStrings: function(arr) {
        return _.chain(arr).toArray().map(function(s) {
            return _.trim(s).toLowerCase();
        }).compact().value();
    }

});
