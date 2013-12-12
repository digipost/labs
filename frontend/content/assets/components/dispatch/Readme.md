# Dispatch.js

Dispatch.js is a micro JS library for routing browser hash change events to handlers, with parameters.


## Example

The following snippet will alert `"Hello"` when the page is loaded, and `"Hello world"` when the location hash changes to `"#!/hello/world"`.


    dispatch.on("/", function() {
      alert("Hello");
    });

    dispatch.on("/hello/:name", function(params) {
      alert("Hello " + params.name);
    });

    dispatch.start("/");

The location hash can be changed in JS:

    window.location.hash = "/hello/world";

or by linking to the appropriate location:

    <a href="#!/hello/world">hello</a>

## API

    dispatch.on(path, handler);

Add a handler to respond to `path`. Any part of path starting with `:` is treated as a required paramenter,
which will be a named property of an object passed as the first argument to the handler.
Any part starting with `*` is treated as an optional parameter. You can have more than one optional parameter,
but optional parameter(s) always have to be at the end of the path.

    dispatch.off([path | handler]);

Remove a handler by its path or by passing the handler itself. Calling this function without any arguments
will remove all handlers.

    dispatch.start([path]);

If there is a current hash value (for example if the page is bookmarked), the appropriate `.on` handler will be used.
If not, Dispatch will run the handler answering to the path given to `dispatch.start`.
In other words, you call start with the path of the home page of your application, which should be shown
if there is not already a current hash path in the users browser.

    dispatch.go(path);

Navigate the browser to a certain `path`. Use this method if the handler should be called even if the
path given is the same as the current browser hash. In other words, using this method ensures
that the handler for the given path will be run, and the location hash will be changed if its not
the same as the given path.

    dispatch.before = [f_1, ..., f_n];
    dispatch.after = [f_1, ..., f_n];

`dispatch.before` and `dispatch.after` are arrays to which you may add methods that should be run
before and after each route. Every method in these arrays must take a callback function as their first
argument, which must be called when the method is finished. This is useful for performing asynchronous
operations before or after routes are run.

## Support

Tested in IE8+ and the latest two versions of other popular browsers.
