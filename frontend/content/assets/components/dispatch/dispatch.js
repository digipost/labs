;(function(window, undefined) {
    'use strict';

    var dispatch = window.dispatch = {}, internal = {},
        id, routes, names, paths, handlers;

    /*
     * Regex matchers.
     */
    var escapeString = /[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g,
        queryMatch   = /\?([^#]*)?$/,
        prefixMatch  = /^[^#]*#/,
        spaceMatch   = /(^\s+)|(\s+$)/,
        fragMatch    = /:([^\/]+)/g,
        fragReplace  = '([^\\/]+)',
        starMatch    = /\\\*([^\/]+)/g,
        starReplace  = '?([^\\/]*)',
        endMatch     = /\/$/;

    /*
     * Reset all routes and callbacks.
     */
    dispatch.reset = function() {
        dispatch.fallback = function() {};
        dispatch.before = [];
        dispatch.after = [];
        routes = {};
        handlers = {};
        names = {};
        paths = {};
        id = 0;
    };

    /*
     * Add a new route.
     *
     * @name: An optional name for the route.
     * @path: The path the route should answer to, with parameters.
     * @handler: The handler function to call when this route is run.
     */
    dispatch.on = function(name, path, handler) {
        if (arguments.length === 2) { handler = path; path = name; }
        if (names[name]) { return; }

        // Create path matcher
        var str = '' + (path || '');
        var esc = str
            .replace(escapeString, '\\$&')
            .replace(fragMatch, fragReplace)
            .replace(starMatch, starReplace)
            .replace(endMatch, '');
        var matcher = new RegExp('^' + esc + '/?$');

        // Store route info
        names[name] = paths[path] = handlers[handler] = ++id;
        routes[id] = {
            name: name,
            path: str,
            matcher: matcher,
            handler: handler,
            id: id
        };
    };

    /*
     * Remove existing route(s).
     * Omit the argument to remove all routes.
     *
     * @x: The name, path or handler of the route to remove.
     */
    dispatch.off = function(x) {
        if (typeof x === 'undefined') { return dispatch.reset(); }
        return !!(delete routes[names[x] || paths[x] || handlers[x] || x]);
    };

    /*
     * Go to a route by changing the current hash,
     * ensuring that if that route is the current route,
     * the callback is still run.
     *
     * @path: The route to run.
     */
    dispatch.go = function(path) {
        var current = internal.path(window.location.hash, {}).path;
        var target  = internal.path(path, {}).path;
        if (current === target) { dispatch.run(target); }
        else { window.location.hash = target; }
    };

    /*
     * Get the current value of a named parameter.
     */
    dispatch.get = function(param) {
        param = '' + param;
        if (!param) return;

        // Find matching route
        var hash = window.location.hash;
        var prev = internal.path(hash, {});
        var next = dispatch.route(hash);
        if (!next) return;
        prev = prev.path.split('/');
        next = next.path.split('/');

        // Find value of named param
        for (var i = 0; i < next.length; i++) {
            if (next[i] === param) return prev[i];
        }
    };

    /*
     * Set (replace) a named parameter in the current path,
     * without running any of the matching handlers.
     *
     * @param: The parameter to replace (e.g. ":a").
     * @value: The new path value to insert.
     */
    dispatch.set = dispatch.replace = function(param, value) {
        param = '' + param;
        value = '' + value;
        if (!param || !value) return;

        // Find matching route
        var hash = window.location.hash;
        var prev = internal.path(hash, {});
        var next = dispatch.route(hash);
        if (!next) return;
        prev = prev.path.split('/');
        next = next.path.split('/');

        // Replace first matching param
        for (var i = 0; i < next.length; i++) {
            internal.skipNextChange = false;
            if (next[i] !== param) continue;
            if (prev[i] === value) return;
            prev[i] = value;
            internal.skipNextChange = true;
            window.location.replace('#' + prev.join('/'));
            return;
        }
    };

    /*
     * Start at a route by changing the hash.
     *
     * @origin: Where to start, defaults to '/'.
     */
    dispatch.start = function(origin) {
        origin = origin || '/';
        if (!window.location.hash) { window.location.hash = origin; }
        else { dispatch.run(window.location.hash); }
    };

    /*
     * Run a route manually, without changing the location.
     * You should not have to use this method, try using
     * dispatch.go or dispatch.start instead.
     *
     * @path: The path which should trigger a route.
     * @params: Optional parameters to pass to the handler.
     */
    dispatch.run = function(path, params) {
        if (!path) { path = window.location.hash; }
        if (!params) { params = {}; }

        // Parse previous and next hash
        var prev = internal.path(params.prev, {});
        var next = internal.path(path, { prev: prev.path });
        var same = prev.path === next.path;
        if (prev.path && next.path && same) { return; }

        // Find matching route
        var route = dispatch.route(next.path);
        if (!route) { return dispatch.fallback(); }

        // Resolve parameters
        var vals = next.path.split('/');
        var keys = route.path.split('/');
        for (var i = 0; i < keys.length; i++) {
            if (keys[i].charAt(0).match(/:|\*/)) {
                next[keys[i].substring(1)] = vals[i] || undefined;
            }
        }

        // Run callbacks
        internal.callbacks(dispatch.before, function() {
            route.handler(next);
            internal.callbacks(dispatch.after);
        });
    };

    /*
     * Find a route by its name, path, handler or matcher.
     */
    dispatch.route = function(x) {
        var route = routes[names[x] || paths[x] || handlers[x] || x];
        if (route) { return route; }
        var parsed = internal.path(x, {}).path;
        for (var p in routes) {
            if (routes.hasOwnProperty(p) && routes[p] && routes[p].matcher.test(parsed)) {
                return routes[p];
            }
        }
    };

    /*
     * @internal Parse an input path.
     */
    internal.path = function(input, params) {
        params.path = (input || '')
            .replace(queryMatch, '')
            .replace(prefixMatch, '')
            .replace(endMatch, '')
            .replace(spaceMatch, '');
        params.path = decodeURIComponent(params.path);
        return params;
    };

    /*
     * @internal Run an array of methods with a final callback.
     */
    internal.callbacks = function(callbacks, after) {
        after = after || function() {};
        if (callbacks.length === 0) {
            after(function() {});
        } else {
            callbacks[0](function() {
                internal.callbacks(Array.prototype.slice.call(callbacks, 1), after);
            });
        }
    };

    /*
     * Set to skip the next change event.
     */
    internal.skipNextChange = false;

    /*
     * Listen on the hash change event to trigger routes,
     * with setInterval fallback for older browsers.
     */
    var prev, next, change = function(event) {
        if (internal.skipNextChange) internal.skipNextChange = false;
        else dispatch.run(event.newURL, { prev: event.oldURL });
    };
    if (!('onhashchange' in window)) {
        prev = window.location.href;
        setInterval(function() {
            next = window.location.href;
            if (prev === next) return;
            change.call(window, {
                type: 'hashchange',
                newURL: next,
                oldURL: prev
            });
            prev = next;
        }, 100);
    } else if (window.addEventListener) {
        window.addEventListener('hashchange', change, false);
    } else if (window.attachEvent) {
        window.attachEvent('onhashchange', change);
    }

    /*
     * Initialize internal state.
     */
    dispatch.reset();

}(window));
