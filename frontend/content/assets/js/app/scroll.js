dp.scroll = {};

/*
 * Init saving of scroll position.
 */
dp.scroll.init = function() {
    $(window).scroll(_.throttle(function() {
        dp.scroll.save(window.location.hash, window.pageYOffset);
    }, 100));
    $('body').on('click', 'a', function() {
        dp.scroll.save($(this).attr('href'), 0);
    });
};

/*
 * Load saved scroll position.
 */
dp.scroll.load = function() {
    var key = window.location.hash;
    var pos = dp.scroll.posStore[key];

    if(!pos) {
        var selector = dp.scroll.selectorStore[key];
        if (selector) {
            dp.scroll.to(selector);
        }
    } else {
        $('body').scrollTop(pos || 0);
    }
};

/*
 * Save scroll position.
 */
dp.scroll.save = function(key, pos) {
    dp.scroll.posStore[key] = pos || 0;
};

/*
 * Save scroll selector.
 */
dp.scroll.saveSelector = function(key, selector) {
    dp.scroll.selectorStore[key] = selector || '';
};

/*
 * Scroll to element on page.
 */
dp.scroll.to = function(selector) {
    var offset = $(selector).offset();
    if (offset) $('html, body').animate({
        scrollTop: $(selector).offset().top
    }, 200);
};

/*
 * Saved scroll positions.
 */
dp.scroll.posStore = {};

/*
 * Saved scroll selector
 */
dp.scroll.selectorStore = {};