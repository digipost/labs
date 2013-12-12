dp.views.tiles = {

    template: 'tiles.tmpl',


    init: function(view) {
        view.registerSearch(view);
        if(!view.search && view.items.links.more_items) view.registerPaging(view);
        if(!view.search) $('.search .inputfield.action-search').val('');
        view.appendItems(view, view.items.items);
        view.$('.type-' + (view.type || 'index')).show();
        if(dp.support.cssAnimations()) view.animate(view.$('.tile'));
        dp.item.startAtItem += view.items.items.length;
    },

    registerPaging: function (view) {
        var currentItems = view.items;
        view.$('.action.action-more').on('click', $.stop).on('click', function () {
            dp.api.type({ type: view.type, url: currentItems.links.more_items }).then(function (items) {
                currentItems = items;
                view.appendItems(view, currentItems.items);
                if(dp.support.cssAnimations()) view.animate(view.$('.tile'));
                if (!currentItems.links.more_items) view.$('.menu.bottom').hide();
            });
        });
        view.$('.menu.bottom').show();
    },

    registerSearch: function(view) {
        if (view.query) {
            $('.search .inputfield.action-search').val(view.query);
            $('.action-clear-search').show();
        }
        $('.search form').on('submit', $.stop).on('submit', function() {
            var input = $('.search .inputfield.action-search');
            var query = input.val();
            if (query) {
                input.blur();
                dp.render.go('!/search/' + query);
            }
        });
        $('.action-clear-search').on('click', $.stop).on('click', function() {
            $('.search .inputfield.action-search').val('');
            $(this).hide();
            dp.render.go('!/');
        });
        $('.search .inputfield.action-search').on('keyup', function() {
            if ($(this).val()) {
                $('.action-clear-search').show();
            }
        });
    },

    appendItems: function(view, items) {
        _.each(items, function(item) {
            scene(dp.views.tile, { item: item }, function(itemview) {
                view.$('.tiles-inner').append(itemview.el);
            });
        });
    },

    animate: function(el) {
        $(el).css('opacity', 0).each(function(i, el) {
            setTimeout(function() {
                $(el).addClass('animated fadeInDown');
            }, _.random(10, 200));
        });
    }

};
