# Scene.js

Scene.js is a small JavaScript library for rendering view models. A view consists of a JS object (the model), an HTML file (the template), and an object with properties (the data). Scene.js takes care of loading the template, compiling it with the model, and adds a few helpful methods.

## Example

This example assumes you are using jQuery and underscore.js, but those are completely optional.

```html
<!-- mytemplate.html -->
<div>Hello <span><%= greet() %></span>!</div>
```

```javascript
var mymodel = {

    // The URL to your template file.
    // This could also be a string of HTML.
    template: 'mytemplate.html',

    // This is run before the HTML is compiled.
    // Use it to set or change properties.
    before: function() {
        this.name = this.name.toUpperCase();
    },

    // This is run after the HTML is compiled.
    // Use it to add listeners, change HTML etc.
    init: function() {
        this.$('span').css('color', 'red');
    },

    // All methods and properties are available
    // to the HTML templating language.
    greet: function() {
        return this.name;
    }

};

scene(mymodel, { name: 'world' }, function(view) {
    console.log(view.el.html());
    // => '<div>Hello <span style="color:red">WORLD</span>!</div>'
});
```

## API

By default, Scene generates document fragments. If jQuery is available, jQuery objects are generated in stead.
Also, by default, no templating language is required.
If you are using underscore.js, Scene will use `_.template` to compile templates.
You can set your own templating language by overriding `scene.compiler(html) => fn`.
See the 'hooks' section on how to override any assumptions.

```javascript
/*
 * The scene method renders view models with a model prototype, a set of properties, and a callback.
 */
scene(model, properties, callback(view));

/*
 * The model can have a couple of optional magic methods.
 */

// The URL to your template. This can be a function or a property.
// If the template starts with '<', it is treated as an HTML string.
model.template

// The before method runs before the HTML is rendered.
model.before()

// The init method runs after the HTML is rendered.
model.init()

/*
 * Rendered views also have a few methods.
 */

// The rendered HTML is stored in view.el. This will be a jQuery object or a document fragment.
view.el

// Use view.$ to find something inside the HTML of this view.
view.$(selector)

// Use view.set to add a subview inside an element in this view.
view.set(selector, model, data, callback)
```

## Filters

You can add filters, or middleware, to Scene, which will be applied to each rendered template:

```javascript
// Add a filter which will do something to the view:
scene.use(function(view) {
    view.$('span').addClass('foo');
});

// Render a view model:
scene({ template: '<span>1</span>' }, {}, function(view) {
    console.log(view.el.innerHTML); // => '<span class="foo">1</span>'
});
```

## Hooks

Hooks let you override default behaviour for getting templates, compiling HTML, and for
creating and manipulating elements.

```javascript

/*
 * Hook: Get a template from the server.
 *
 * @template: The template property, or result of the template function.
 * @callback: Must be called by this function with the template HTML.
 *
 * The default hook will use jQuery if available, or plain XHR.
 */
scene.get = function(template, callback) { ... };

/*
 * Hook: Create a compiler from an HTML string.
 *
 * @html: An HTML string for this template.
 * @return: A compiler function or a new HTML string.
 *
 * The default hook will use _.template if available, or skip compilation.
 */
scene.compile = function(html) { ... };

/*
 * Hook: Create empty elements.
 *
 * View models without elements use this function to create blank elements.
 * Override it to create different blank elements.
 *
 * The default hook will use jQuery if available, or create a document fragment.
 */
scene.el = function() { ... };

/*
 * Hook: Find elements within another element.
 *
 * @el: The parent element.
 * @selector: The selector of the child to find.
 *
 * The default hook will use jQuery if available, or use querySelectorAll.
 */
scene.find = function(el, selector) { ... };

/*
 * Hook: Insert HTML by selector.
 *
 * @el: The parent element.
 * @selector: Where to insert the HTML.
 * @html: The HTML to insert.
 *
 * The default hook will use jQuery if available, or appendChild.
 */
scene.insert = function(el, selector, html)  { ... };

/*
 * Hook: Append HTML to an element.
 *
 * @el: The parent element.
 * @html: The HTML to insert.
 */
scene.append = function(el, html) { ... };
```
