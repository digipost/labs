dp.support = {};

dp.support.isMobile = function() {
    return dp.support.is('Android', 'webOS', 'iPhone', 'iPod', 'BlackBerry', 'IEMobile');
};

dp.support.isTablet = function() {
    return dp.support.is('iPad');
};

dp.support.isHandheld = function() {
    return dp.support.isMobile() || dp.support.isTablet();
};

dp.support.isOldIE = function() {
    return dp.support.is('MSIE 7.0') || dp.support.is('MSIE 8.0') || dp.support.is('MSIE 9.0');
};

dp.support.cssAnimations = function() {
    return !dp.support.isOldIE();
};

dp.support.editor = function() {
    return !dp.support.isHandheld() && !dp.support.isOldIE();
};

dp.support.is = function() {
    return _.any(_.toArray(arguments), function(s) {
        return !!(navigator.userAgent.toLowerCase().match(s.toLowerCase()));
    });
};
