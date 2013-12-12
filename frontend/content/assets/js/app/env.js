dp.env = dp.env || {};

/*
 * Init app environment.
 */
dp.env.init = function(app, dir, debug) {
    this.debug = _.isSet(debug) ? debug : dp.env.getDebug();
};

/*
 * Debug mode is decided based on the current domain.
 */
dp.env.getDebug = function(host) {
    host = (host || window.location.hostname || '');
    return !(/labs\.digipost\.no/).test(host);
};
