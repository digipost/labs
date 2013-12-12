/*
 * Render a view model with custom data,
 * using scene.js for view model rendering.
 */
dp.views.render = function(model, data, callback) {
    scene(model, data, function(view) {
        if (callback) callback(view, view.el);
    });
};