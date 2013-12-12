dp.tile = {};

dp.tile.animate = function(el) {
    $(el).css('opacity', 0);
    setTimeout(function() {
        $(el).addClass('animated fadeInDown');
    }, _.random(10, 200));
};
