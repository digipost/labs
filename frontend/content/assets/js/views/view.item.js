dp.views.item = {

    template: function() {
        return 'items/' + _.str.slugify(this.item.type) + '.tmpl';
    },

    init: function(view) {
        view.showComments(view);
        view.showVotes(view);
        view.showShare(view);
        if (dp.session.admin()) {
            view.showAdmin(view);
        }
        if (view.item.status === 'finished') {
            view.$('.finished').show();
            view.$('.finished-inner').show();
        }
    },

    showVotes: function(view) {
        view.set('.view-votes', dp.views.votes, {
            item: view.item
        });
    },

    showComments: function(view) {
        view.set('.view-comments', dp.views.comments, {
            comments: view.item.comments,
            item: view.item
        });
    },

    showShare: function(view) {
        view.set('.view-share', dp.views.share, {
            item: view.item
        });
    },

    showAdmin: function(view) {
        view.set('.view-admin', dp.views.itemAdmin, {
            item: view.item
        });
    }
};
