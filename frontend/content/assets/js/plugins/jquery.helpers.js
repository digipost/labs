// Stop event propagation
$.stop = function(event, immediate) {
    if (!event) return false;
    if (immediate === true)
        event.stopImmediatePropagation();
    event.stopPropagation();
    event.preventDefault();
    return false;
};

// Increment a number in the DOM
$.fn.extend({
    increment: function() {
        return this.each(function() {
            var num = parseInt($(this).text(), 10) || 0;
            return $(this).text(num + 1);
        });
    }
});
