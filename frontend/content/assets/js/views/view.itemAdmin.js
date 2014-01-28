dp.views.itemAdmin = {

    template: 'items/admin.tmpl',

    init: function(view) {
        view.$('.action').on('click', $.stop);
        view.$('.action-edit').on('click.perform', function() {
            window.location = '#!/admin/item/' + view.item.id + '/edit';
        });
        view.$('.action-delete').on('click.perform', function() {
            dp.modal.confirm('Er du sikker på at du vil slette innlegget?', 'Slett', function(confirm) {
                if (confirm) dp.api.deleteItem(view.item.id).always(function() {
                    dp.track.event(view.item.type, 'delete');
                    window.location = '#!/';
                });
            });
        });
    }
};
