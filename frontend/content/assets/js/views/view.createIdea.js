dp.views.createIdea = {

    template: function() {
        return 'create/idea.tmpl';
    },

    init: function(view) {
        view.bindSave(view);
    },

    after: function() {
        this.$('input:first').focus();
    },

    bindSave: function(view) {
        view.$('.save').on('click', $.stop).on('click', function() {
            var title = $('.title').val();
            var body = $('.editable').html() || $('.body').val();

            var err = view.validateInput(title, body);
            if(!err) {
                $(this).off();
                view.save(title, body);
            }
        });
    },

    save: function(title, body) {
        var data = {
            type: 'idea',
            title: title,
            body: body
        };
        dp.api.save(data, {
            update: this.update,
            id: this.item.id
        }).then(function(item) {
            window.location = '#!/item/' + item.id;
        });
    },

    validateInput: function(title, body) {
        var titleField = this.$('.inputfield.title');
        if(_.trim(title) === '') {
            titleField.addClass('error');
            titleField.select();
            return 'Tittel må fylles ut.';
        } else {
            titleField.removeClass('error');
        }
        var bodyField = this.$('.inputfield.body');
        if(_.trim(body) === '') {
            bodyField.addClass('error');
            bodyField.select();
            return 'Beskrivelse må fylles ut.';
        } else {
            bodyField.removeClass('error');
        }
        return false;
    }

};
