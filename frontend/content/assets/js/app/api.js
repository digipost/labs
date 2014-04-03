dp.api = {};

/*
 * Perform a request.
 */
dp.api.request = function(options) {
    if (options.data) {
        options.data = JSON.stringify(options.data);
    }
    if (dp.session.user) {
        options.headers = options.headers || {};
        options.headers['X-CSRF-Token'] = dp.session.user.token;
    }
    return $.ajax(options);
};

/*
 * Get blog posts.
 */
dp.api.type = function(options) {
    var url = options.url || (options.type ?
        '/api/items/type/' + options.type :
        '/api/items');
    return dp.api.request({
        type: 'get',
        url: url
    });
};

dp.api.search = function(options) {
    return dp.api.request({
        type: 'get',
        url: '/api/search?query=' + encodeURIComponent(options.query)
    });
};

/*
 * Get user info
 */
dp.api.user = function() {
    return dp.api.request({
       type: 'get',
       url: '/api/sessions/user'
    });
};

/*
 * Get a single item.
 */
dp.api.item = function(options) {
    return dp.api.request({
        type: 'get',
        url: '/api/items/' + options.id
    });
};

/*
 * Get a single item for editing (must be admin).
 */
dp.api.editableItem = function(options) {
    return dp.api.request({
        type: 'get',
        url: '/api/items/' + options.id + '/editable'
    });
};

/*
 * Vote on an item.
 */
dp.api.vote = function(options) {
    options.item.voted = true;
    return dp.api.request({
        type: 'post',
        url: '/api/items/' + options.item.id + '/votes',
        contentType: 'application/json'
    });
};

dp.api.save = function(data, options) {
    var url;
    if (data.type === 'news') {
        url = '/api/news' + (options.update ? '/' + options.id : '');
    } else if(data.type === 'idea') {
        url = '/api/ideas' + (options.update ? '/' + options.id : '');
    } else if(data.type === 'tweet') {
        url = '/api/tweets' + (options.update ? '/' + options.id : '');
    } else {
        throw 'Invalid item type';
    }
    return dp.api.request({
        type: 'post',
        url: url,
        data: data,
        contentType: 'application/json'
    });
};

dp.api.deleteItem = function(itemId) {
    return dp.api.request({
        type: 'delete',
        url: '/api/items/' + itemId
    });
};

/*
 * Create comment
 */
dp.api.comment = function(options) {
    return dp.api.request({
        type: 'post',
        url: '/api/items/' + options.item.id + '/comments',
        data: options.data,
        contentType: 'application/json'
    });
};

/*
 * Delete comment
 */
dp.api.deleteComment = function(options) {
    return dp.api.request({
        type: 'delete',
        url: '/api/items/' + options.itemId + '/comments/' + options.commentId
    });
};

/*
 * Get latest comments
 */
dp.api.latestComments = function() {
  return dp.api.request({
      type: 'get',
      url: '/api/comments'
  });
};

dp.api.profile = function(options) {
    return dp.api.request({
        type: 'get',
        url: '/api/users/' + options.id + '/profile'
    });
};

dp.api.profiles = function() {
    return dp.api.request({
        type: 'get',
        url: '/api/users/profiles'
    });
};
