/*
 * Global namespace.
 */
window.dp = {
    views: {}
};

/*
 * View model namespace.
 */
dp.views = {
    admin: {}
};

/*
 * Init js on page load.
 */
$(function() {
    dp.env.init();
    dp.session.init();
    dp.scroll.init();
    dp.session.refreshUser().always(function () {
        dispatch.start('#!/');
    });
});

/*
 * Before each route.
 */
dispatch.before.push(function(next) {
    dp.render.before();
    next();
});

/*
 * Items list page.
 */
dispatch.on('!/*type', function(params) {
    dp.item.startAtItem = 0;
    dp.api.type({ type: params.type }).then(function(items) {
        dp.render.page({
            view: dp.views.tiles,
            data: { items: items, type: params.type },
            active: params.type || 'index'
        });
    });
});

dispatch.on('!/search/:query', function(params) {
    dp.api.search({query: params.query}).then(function(items){
        dp.render.page({
            view: dp.views.tiles,
            data: { items: items, type: params.type, search: true, query: params.query}
        });
    });
});

/*
 * Item page.
 */
dispatch.on('!/item/:id', function(params) {
    dp.api.item({ id: params.id }).then(function(item) {
        dp.render.page({
            view: dp.views.item,
            title: item.title,
            data: { item: item }
        });
    });
});

/*
 * Create new idea page.
 */
dispatch.on('!/create/idea', function() {
    dp.render.page({
        view: dp.views.createIdea,
        data: { item: {}, update: false }
    });
});

/*
 * User profile page.
 */
dispatch.on('!/profiles/:id', function(params) {
    dp.api.profile({id: params.id}).then(function(profile) {
        dp.render.page({
            view: dp.views.profile,
            data: { profile: profile }
        });
    });
});

/*
 * Admin pages
 */
dispatch.on('!/admin', function() {
    if (dp.session.admin()) {
        dp.render.go('#!/admin/items');
    } else {
        dp.render.go('#!/');
    }
});

/*
 * Create new tweet
 */
dispatch.on('!/admin/create/tweet', function() {
    dp.render.page({
        view: dp.views.admin.main,
        data: { page: 'createOrEditItem', type: 'tweet', item: {}, update: false }
    });
});

/*
 * Create new item page.
 */
dispatch.on('!/admin/create/news', function() {
    dp.render.page({
        view: dp.views.admin.main,
        data: { page: 'createOrEditItem', type: 'news', item: {}, update: false }
    });
});

/*
 * Edit item
*/
dispatch.on('!/admin/item/:id/edit', function(params) {
    dp.api.editableItem({ id: params.id }).then(function(item) {
        dp.render.page({
            view: dp.views.admin.main,
            data: { page: 'createOrEditItem', type: item.type, item: item, update: true }
        });
    });
});

dispatch.on('!/admin/comments', function() {
    if (dp.session.admin()) {
        dp.api.latestComments().then(function(comments) {
            dp.render.page({
                view: dp.views.admin.main,
                data: { page: 'comments', comments: comments}
            });
        });
    } else {
        dp.render.go('#!/');
    }
});

dispatch.on('!/admin/profiles', function() {
    if (dp.session.admin()) {
        dp.api.profiles().then(function(profiles) {
            dp.render.page({
                view: dp.views.admin.main,
                data: { page: 'profiles', profiles: profiles}
            });
        });
    } else {
        dp.render.go('#!/');
    }
});

dispatch.on('!/admin/items', function() {
    if (dp.session.admin()) {
        dp.api.type({}).then(function(items) {
            dp.render.page({
                view: dp.views.admin.main,
                data: { page: 'items', items: items}
            });
        });
    } else {
        dp.render.go('#!/');
    }
});