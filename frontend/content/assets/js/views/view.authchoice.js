dp.views.authchoice = {

    template: 'authchoice.tmpl',

    show: function(options) {
        dp.views.render(dp.views.authchoice, { options: options }, function(instance) {
            dp.modal.show({
                content: instance.el,
                buttons: false,
                width: 400,
                classes: 'animated fadeIn',
                closer: true
            });
        });
    },

    init: function(view) {
        view.$('.action').on('click', $.stop);
        view.registerLoginButtons(view);
        view.registerOpenIDInputField(view);
    },

    registerLoginButtons: function(view) {
        view.$('.action-login').on('click.perform', function() {
            dp.session['loginWith' + $(this).attr('rel')]();
        });
    },

    registerOpenIDInputField: function(view) {
        view.$('.show-openid-input').on('click.perform', function(event) {
            view.$('.openid').show();
            event.preventDefault();
        });

        view.$('.inputfield.action-openid-login').on('keypress', function(event) {
            if (event.keyCode === 13) {
                event.preventDefault();
                var url =  view.$('.inputfield.action-openid-login').val();
                if(url) {
                    dp.session.loginWithOpenID(url);
                }
            }
        });

        view.$('.button.action-openid-login').on('click.perform', function() {
            var url =  view.$('.inputfield.action-openid-login').val();
            if(url) {
                dp.session.loginWithOpenID(url);
            }
        });
    }

};