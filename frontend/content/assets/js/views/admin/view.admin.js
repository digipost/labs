dp.views.admin.main = {

    template: function() {
        return 'admin/admin.tmpl';
    },

    init: function(view) {
        dp.views.admin.main[view.page](view);
    },

    comments: function(view) {
        scene(dp.views.admin.comments, { comments: view.comments }, function(commentsview) {
            view.$('.box.box-big.box-inside').html(commentsview.el);
        });
    },

    profiles: function(view) {
        scene(dp.views.admin.profiles, { profiles: view.profiles }, function(profilesview) {
            view.$('.box.box-big.box-inside').html(profilesview.el);
        });
    },

    createTweet: function(view) {
        scene(dp.views.admin.createTweet, {item: view.item}, function(tweetview) {
            view.$('.box.box-big.box-inside').html(tweetview.el);
        });
    },

    createOrEditItem: function(view) {
        scene({ template: 'admin/markdownHelp.tmpl' }, {}, function(helpview) {
            view.$('.boxes').append(helpview.el);
        });
        scene(dp.views.admin.createItem, { type: view.type, item: view.item, update: view.update }, function(itemview) {
            view.$('.box.box-big.box-inside').html(itemview.el);
        });
    },

    items: function(view) {
        scene(dp.views.admin.items, { items: view.items }, function(itemsView) {
            view.$('.box.box-big.box-inside').html(itemsView.el);
        });
    }
};
