dp.render = {};

/*
 * Activate menu items.
 */
dp.render.active = function(name) {
    $('.menu .active').removeClass('active');
    if (name) $('.' + name).addClass('active');
};

/*
 * Runs before each page.
 */
dp.render.before = function() {
    $('#page').empty();
    document.title = 'Digipost Labs';
    $('footer').hide();
};

/*
 * Render a page from a view model and a data object.
 */
dp.render.page = function(options) {
    $('#page').empty();
    if(options.title) document.title = options.title + ' - Digipost Labs';
    dp.render.active(options.active);
    scene(options.view, options.data, function(view) {
        $('#page').html(view.el);
        if (view.after) view.after(view);
        dp.session.toggle();
        dp.scroll.load();
        dp.track.analytics(options.track || window.location.pathname + window.location.hash);
        setTimeout(function() {
            $('footer').fadeIn();
        }, 300);
    });
};

/*
 * Show a notification.
 */
dp.render.notification = function(text) {
    clearTimeout(dp.render.notification.timer);
    $('#notification').remove();
    if (!text) return;
    var notification = $('<div>').attr('id', 'notification').text(text).hide();
    $('body').append(notification.fadeIn());
    dp.render.notification.timer = setTimeout(function() {
        $('#notification').fadeOut();
    }, 2000);
};

/*
 * Go to a page or reload the current page.
 */
dp.render.go = function(loc) {
    var current = window.location.hash.split('?')[0];
    var target  = loc || current;
    var timestamp = '?_=' + (new Date()).getTime();
    if (target === current) target += timestamp;
    window.location.hash = target;
};

/*
 * Hook: Use precompiled template files.
 */
scene.get = function(view, callback) {
    callback(window.templates['assets/templates/' + view.template]);
};

/*
 * Hook: Templates are already precompiled.
 */
scene.compile = function(precompiled) {
    return precompiled;
};
