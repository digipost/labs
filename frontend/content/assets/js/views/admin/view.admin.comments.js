dp.views.admin.comments = {

    template: 'admin/comments.tmpl',

    init: function(view) {
        view.showComments(view);
    },

    showComments: function(view) {
        _.each(view.comments, _.partial(view.addComment, view));
    },

    addComment: function(view, comment) {
        scene({
            template: 'admin/comment.tmpl',

            init: function(view) {
                view.$('.action').on('click', $.stop);
                view.$('.action-goto-item').on('click.perform', function() {
                    var target = '!/item/' + view.comment.itemId;
                    dp.scroll.saveSelector('#' + target, '#' + view.comment.id);
                    dp.render.go(target);
                });
            }}, { comment: comment }, function(commentview) {

            view.animate(commentview.el);
            view.$('.comments-list').append(commentview.el);
        });
    },

    animate: function(el) {
        $(el).css('opacity', 0);
        setTimeout(function() {
            $(el).addClass('animated fadeInLeft');
        }, _.random(10, 200));
    }
};