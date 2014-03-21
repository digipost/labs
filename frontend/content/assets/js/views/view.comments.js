dp.views.comments = {

    template: 'items/comments.tmpl',

    init: function(view) {
        view.bindSave(view);
        view.expandCommentArea(view);
        view.showComments(view);
    },

    expandCommentArea: function(view) {
        view.$('.expand').focus(function () {
            $(this).animate({ height: '12em' }, 200);
        });
        view.$('.expand').blur(function () {
            var textArea = $(this);
            if (!textArea.val()) {
                $(this).animate({ height: '4em' }, 200);
            }
        });
    },

    bindSave: function(view) {
        view.$('.save').on('click', function(event) {
            event.preventDefault();
            var text = view.$('textarea').val();

            var err = view.validateInput(text);
            if(!err) {
                dp.api.comment({
                    item: view.item,
                    data: { body: text }
                }).then(function(item) {
                    view.$('textarea').val('').blur();
                    view.addComment(view, _.last(item.comments));
                    $('.comments-count').increment();
                    dp.track.event(item.type, 'comment');
                    dp.render.notification('Takk for din kommentar!');
                });
            }
        });
    },

    showComments: function(view) {
        _.each(view.comments, _.partial(view.addCommentWithoutAnimations, view));
    },

    addComment: function(view, comment) {
        scene(dp.views.comment, { item: view.item, comment: comment }, function(commentview) {
            view.animate(commentview.el);
            view.$('.comments-list').append(commentview.el);
            dp.scroll.to(commentview.el);
        });
    },

    addCommentWithoutAnimations: function(view, comment) {
        scene(dp.views.comment, { item: view.item, comment: comment }, function(commentview) {
            view.$('.comments-list').append(commentview.el);
        });
    },

    animate: function(el) {
        $(el).css('opacity', 0);
        setTimeout(function() {
            $(el).addClass('animated fadeInLeft');
        }, _.random(10, 200));
    },

    validateInput: function(comment) {
        var commentField = this.$('.inputfield');
        if(_.trim(comment) === '') {
            commentField.addClass('error');
            commentField.select();
            return 'Kommentarfeltet må fylles ut.';
        } else {
            commentField.removeClass('error');
        }
        return false;
    }

};
