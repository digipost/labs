dp.session = {
    user: false
};

/*
 * Bind login buttons.
 */
dp.session.init = function() {
    $('body').on('click', 'a.session-login', function(event) {
        event.preventDefault();
        dp.session.loginWithDigipost();
    });
    $('body').on('click', 'a.session-logout', function(event) {
        dp.session.logout();
        event.preventDefault();
    });
};

/*
 * Toggle session links.
 */
dp.session.toggle = function() {
    var user = dp.session.user;
    var admin = dp.session.admin();
    $('.session').removeClass('hide');
    if (user) {
        $('.session-in').removeClass('hide');
        $('.session-out').addClass('hide');
        $('.session-name').text(user.name);
    } else {
        $('.session-in').addClass('hide');
        $('.session-out').removeClass('hide');
    }
    if (admin) {
        $('.session-admin').removeClass('hide');
    } else {
        $('.session-admin').addClass('hide');
    }
};

dp.session.refreshUser = function() {
    return dp.api.user().then(function(user) {
        dp.session.user = user || false;
    });
};

/*
 * Get the current admin, or false if not logged in.
 */
dp.session.admin = function() {
    return dp.session.user && dp.session.user.admin;
};

/*
 * Log in the current user via Digipost OAuth.
 */
dp.session.loginWithDigipost = function() {
    var returnUrl = window.location;
    window.location = '/api/sessions?returnUrl=' + encodeURIComponent(returnUrl);
};

/*
 * Log in the current user via Google OpenID.
 */
dp.session.loginWithGoogle = function() {
    var returnUrl = window.location;
    window.location = '/api/sessions/google?returnUrl=' + encodeURIComponent(returnUrl);
};

/*
 * Log in the current user via Yahoo OpenID.
 */
dp.session.loginWithYahoo = function() {
    var returnUrl = window.location;
    window.location = '/api/sessions/yahoo?returnUrl=' + encodeURIComponent(returnUrl);
};

/*
 * Log in the current user via generic OpenID.
 */
dp.session.loginWithOpenID = function(endpoint) {
    var returnUrl = window.location;
    window.location = '/api/sessions/openid?endpoint=' + encodeURIComponent(endpoint) +
        '&returnUrl=' + encodeURIComponent(returnUrl);
};

/*
 * Log out the current user.
 */
dp.session.logout = function() {
    dp.api.request({ url: '/api/sessions', method: 'delete' }).then(function() {
        window.location.reload();
    });
};
