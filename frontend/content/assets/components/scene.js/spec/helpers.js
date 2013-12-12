/*
 * Expose test helpers.
 */
var assert = chai.assert, expect = chai.expect, should = chai.should();

/*
 * Vendor dependencies, removed from global scope.
 */
var dependencies = {
    _: _.noConflict(),
    $: $.noConflict()
};

/*
 * Expose a dependency.
 */
var expose = function(name) {
    window[name] = dependencies[name];
};

/*
 * Remove dependencies from the global scope.
 */
var reset = function() {
    Object.keys(dependencies).forEach(function(key) {
        window[key] = undefined;
    });
    scene.reset();
};
