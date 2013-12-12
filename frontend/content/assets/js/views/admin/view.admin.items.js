dp.views.admin.items = {

    template: 'admin/items.tmpl',

    init: function(view) {
        if (view.items.links.more_items) view.registerPaging(view);
        view.appendItems(view, view.items.items);
    },

    registerPaging: function (view) {
        var currentItems = view.items;
        view.$('.action.action-more').on('click', $.stop).on('click', function () {
            dp.api.type({ url: currentItems.links.more_items }).then(function (items) {
                currentItems = items;
                view.appendItems(view, currentItems.items);
                if (!currentItems.links.more_items) view.$('.more-items').hide();
            });
        });
        view.$('.more-items').show();
    },

    appendItems: function(view, items) {
        _.each(items, function(item) {
            scene({template: 'admin/item.tmpl'}, { item: item }, function(itemview) {
                view.$('.items-list tbody').append(itemview.el.html());
            });
        });
    }

};