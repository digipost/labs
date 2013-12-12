dp.views.share = {

    template: 'items/share.tmpl',

    init: function(view) {
        view.$('.action').on('click', $.stop);
        view.$('.action-share').on('click.perform', function() {
            dp.share[$(this).attr('rel')](window.location, view.item.title, view.item.type);
        });
    }

};
