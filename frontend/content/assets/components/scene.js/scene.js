;(function(root) {
    'use strict';

    /*
     * Render a view model from a prototype and a data object.
     *
     * @proto: The prototype specifies the properties of the view model.
     * @data: The data object holds specific properties for this instance.
     * @callback: Called with the rendered view model instance.
     */
    var scene = root.scene = function(proto, data, callback) {
        var view = instance(proto || {}, data || {});
        render(view, function() {
            if (scene.after) scene.after(view);
            if (callback) callback(view);
        });
    };

    /*
     * Add middleware filters to view model rendering.
     *
     * @fn: A function that recieves and modifies the view model.
     */
    scene.use = function(fn) {
        if (fn) F.push(fn);
    };

    /*
     * Add methods and properties to the default view model prototype.
     *
     * @name: The property name.
     * @val: The value or method.
     */
    scene.fn = function(name, val) {
        if (name) P[name] = val;
    };

    /*
     * Remove all compiled templates, middleware filters and prototype additions.
     */
    scene.reset = function() {
        C = {}; F = []; P = {};
    };

    /*
     * Hook: Get a template from the server.
     *
     * @view: The view model.
     * @callback: Must be called by this function with the template HTML.
     *
     * The default hook will use jQuery if available, or plain XHR.
     */
    scene.get = function(view, callback) {
        if (root.$ && root.$.get) return root.$.get(view.template, callback, 'text');
        var req = new XMLHttpRequest();
        req.onload = function() { callback(this.responseText); };
        req.open('get', view.template, true);
        req.send();
    };

    /*
     * Hook: Create a compiler from an HTML string.
     *
     * @html: An HTML string for this template.
     * @return: A compiler function or a new HTML string.
     *
     * The default hook will use _.template if available, or skip compilation.
     */
    scene.compile = function(html) {
        if (root._ && root._.template) return root._.template(html);
        return html;
    };

    /*
     * Hook: Create empty elements.
     *
     * View models without elements use this function to create blank elements.
     * Override it to create different blank elements.
     *
     * The default hook will use jQuery if available, or create a document fragment.
     */
    scene.el = function() {
        if (root.$) return root.$('<div>');
        return document.createDocumentFragment()
            .appendChild(document.createElement('div'));
    };

    /*
     * Hook: Find elements within another element.
     *
     * @el: The parent element.
     * @selector: The selector of the child to find.
     *
     * The default hook will use jQuery if available, or use querySelectorAll.
     */
    scene.find = function(el, selector) {
        if (root.$) return root.$(el).find(selector);
        return el.querySelectorAll(selector);
    };

    /*
     * Hook: Insert HTML by selector.
     *
     * @el: The parent element.
     * @selector: Where to insert the HTML.
     * @html: The HTML to insert.
     *
     * The default hook will use jQuery if available, or appendChild.
     */
    scene.insert = function(el, selector, html) {
        if (root.$) return root.$(el).find(selector).html(html);
        var match = el.querySelector(selector);
        if (match) match.appendChild(html);
    };

    /*
     * Hook: Append HTML to an element.
     *
     * @el: The parent element.
     * @html: The HTML to insert.
     */
    scene.append = function(el, html) {
        if (root.$) root.$(el).append(html);
        else el.innerHTML = html;
    };

    /*
     * Internal: Render the HTML of a view.
     */
    var render = function(view, callback) {
        compiler(view, function(comp) {
            if (scene.before) scene.before(view);
            if (view.before) view.before(view);
            el(view, invoke({ c: comp }, 'c', view));
            if (view.init) view.init(view);
            for (var i = 0; i < F.length; i++) F[i](view);
            callback();
        });
    };

    /*
     * Internal: Download and compile a template file.
     */
    var compiler = function(view, callback) {
        if (!view.template)
            callback();
        else if (C[view.template])
            callback(C[view.template]);
        else if (view.template.indexOf('<') === 0)
            callback(scene.compile(view.template));
        else scene.get(view, function(html) {
            C[view.template] = scene.compile(html);
            callback(C[view.template]);
        });
    };

    /*
     * Internal: Create the HTML element for this view from compiled HTML.
     */
    var el = function(view, html) {
        if (!view.el) view.el = scene.el();
        if (html) scene.append(view.el, html);
        extend(view, P);
        view.$ = function(selector) {
            return scene.find(this.el, selector);
        };
        view.set = function(selector, proto, data, callback) {
            var subview = instance(proto || {}, data || {});
            render(subview, function() {
                scene.insert(view.el, selector, subview.el);
                if (scene.after) scene.after(subview);
                if (callback) callback(subview);
            });
        };
    };

    /*
     * Internal: Instantiate a view model from a prototype and properties.
     */
    var instance = function(proto, data) {
        if (typeof proto === 'string') proto = { template: proto };
        var model = create(proto);
        extend(model, data);
        model.template = invoke(model, 'template');
        return model;
    };

    /*
     * Internal: Create a new object from a prototype.
     */
    var create = function(proto) {
        if (Object.create) return Object.create(proto);
        var O = function() {};
        O.prototype = proto;
        return new O();
    };

    /*
     * Internal: Extend object with other properties.
     */
    var extend = function(obj, props) {
        for (var key in props)
            if (props.hasOwnProperty(key))
                obj[key] = props[key];
    };

    /*
     * Internal: Invoke something if it is a function, or return if key.
     */
    var invoke = function(obj, key) {
        return typeof obj[key] === 'function' ?
            obj[key].apply(obj, [].slice.call(arguments, 2)) :
            obj[key] ? obj[key] : null;
    };

    /*
     * Internal: A cache of compiled templates.
     */
    var C = {};

    /*
     * Internal: An array of middleware filters.
     */
    var F = [];

    /*
     * Internal: View model prototype.
     */
    var P = {};

}(this));
