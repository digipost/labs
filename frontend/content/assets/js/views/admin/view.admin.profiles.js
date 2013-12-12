dp.views.admin.profiles = {

    template: 'admin/profiles.tmpl',

    init: function(view) {
        view.showProfiles(view);
    },

    showProfiles: function(view) {
        _.each(view.profiles, _.partial(view.addProfile, view));
    },

    addProfile: function(view, profile) {
        scene(dp.views.profile, { profile: profile }, function(profileview) {
            view.animate(profileview.el);
            view.$('.profiles-list').append(profileview.el);
        });
    },

    animate: function(el) {
        $(el).css('opacity', 0);
        setTimeout(function() {
            $(el).addClass('animated fadeInLeft');
        }, _.random(10, 200));
    }
};