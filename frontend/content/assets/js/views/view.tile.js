dp.views.tile = {

    template: function() {
        return 'tiles/' + _.str.slugify(this.item.type) + '.tmpl';
    },

    init: function(view) {
        view.$('.action').on('click', $.stop);
        view.$('.action-vote').on('click.perform', function() {
            dp.api.vote({ item: view.item });
            view.$('.votes-count').increment();
        });
        view.$('.action-vote').on('click', function() {
            view.disableVote(view);
        });
        if (view.item.voted) {
            view.disableVote(view);
        }
        if (view.item.status === 'finished') {
            view.$('.finished').show();
            view.$('.finished-inner').show();
        }
        if (view.item.type !== 'tweet' && !_.isEmpty(view.item.url)) {
            view.$('.tile-inner').addClass('tile-bg')
                .attr('style', 'background-image: url("'+ _.escape(view.item.url) +'"); background-size: cover;');
            view.$('.tile-bg-lower').removeClass('hide');
        }
    },

    disableVote: function(view) {
        view.$('.action-vote').removeClass('button3').addClass('button4').off('.perform');
    }

};
