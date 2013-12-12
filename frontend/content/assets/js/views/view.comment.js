dp.views.comment = {

    template: 'items/comment.tmpl',

    init: function(view) {
        view.$('.action').on('click', $.stop);
        view.$('.action-delete').on('click.perform', function() {
            dp.modal.confirm('Er du sikker på at du vil slette kommentaren?', 'Slett', function(confirm) {
                if (confirm) dp.api.deleteComment({
                    itemId: view.item.id,
                    commentId: view.comment.id
                }).always(function() {
                    view.$('#' + view.comment.id).fadeOut((300), function() {
                        view.el.remove();
                    });
                });
            });
        });
    }
};
