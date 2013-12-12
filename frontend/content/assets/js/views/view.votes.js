dp.views.votes = {

    template: 'items/votes.tmpl',

    init: function(view) {
        view.$('.action').on('click', $.stop);
        view.$('.action-vote').on('click.perform', function() {
            dp.api.vote({ item: view.item });
            view.$('.votes-count').increment();
            dp.track.event(view.item.type, 'vote', 'labs');
        });
        view.$('.action-share').on('click.perform', function() {
            dp.share[$(this).attr('rel')](window.location, view.item.title, view.item.type);
        });
        view.$('.action').on('click', function() {
            view.disable(view, this);
        });
        if (view.item.voted) {
            view.disable(view, '.action-vote');
        }
    },

    disable: function(view, selector) {
        view.$(selector).removeClass('button3').addClass('button4').off('.perform');
    }

};
