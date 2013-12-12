dp.views.admin.createItem = {

    template: function() {
        return 'admin/' + this.type + '.tmpl';
    },

    init: function(view) {
        view.$('.select-status').val(view.item.status);
        view.bindSave(view);
    },

    after: function() {
        this.$('input:first').focus();
    },

    bindSave: function(view) {
        view.$('.save').on('click', $.stop).on('click', function() {
            var title = $('.title').val();
            var body = $('.editable').html() || $('.body').val();
            var status = $('.select-status').val();
            var imageUrl = $('.imageUrl').val();
            var index = $('.inputfield.index').val();

            //for tweets
            var author = $('.author').val();
            var url = $('.url').val();

            var err = view.validateInput(title, body);
            if(!err) {
                $(this).off();
                view.save({
                    title: title,
                    body: body,
                    imageUrl: imageUrl,
                    index: index,
                    status: status,
                    author: author,
                    url: url});
            }
        });
    },

    save: function(fields) {
        var data = {
            type: this.type,
            body: fields.body
        };
        if (fields.title) data.title = fields.title;
        if (fields.status) data.status = fields.status;
        if (fields.imageUrl) data.imageUrl = fields.imageUrl;
        if (_.isSet(fields.index)) data.index = parseInt(fields.index, 10);
        if (fields.author) data.author = fields.author;
        if (fields.url) data.url = fields.url;
        dp.api.save(data, {
            update: this.update,
            id: this.item.id
        }).then(function(item) {
            if (item.type === 'tweet') {
                window.location = '#!/admin';
            } else {
                window.location = '#!/item/' + item.id;
            }
        });
    },

    validateInput: function(title, body) {
        var titleField = this.$('.inputfield.title');
        if(_.isSet(title) && _.trim(title) === '') {
            titleField.addClass('error');
            titleField.select();
            return 'Tittel må fylles ut.';
        } else {
            titleField.removeClass('error');
        }
        var bodyField = this.$('.inputfield.body');
        if(_.isSet(body) && _.trim(body) === '') {
            bodyField.addClass('error');
            bodyField.select();
            return 'Beskrivelse må fylles ut.';
        } else {
            bodyField.removeClass('error');
        }
        return false;
    }

};
