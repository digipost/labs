/*
 * Tracking with Google Analytics and Adform.
 */
dp.track = {};

/*
 * Track a page id with GA
 */
dp.track.analytics = function(id) {
    if (_.notSet(id) || dp.env.debug) return;
    if (_.isArray(id)) id = id.join('/');
    if (!_.isString(id) || id === '') return;
    id = id.toLowerCase();
    dp.track.google(id);
    return id;
};

/*
 * Track a page id with GA.
 */
dp.track.google = function(id) {
    if (_.isSet(window._gaq)) {
        window._gaq.push(['_trackPageview', id]);
    }
};

/*
 * Track an event with GA.
 */
dp.track.event = function(category, action, label) {
    if (window._gaq && !dp.env.debug) {
        window._gaq.push(['_set', 'page', window.location.pathname + window.location.hash]);
        window._gaq.push(['_trackEvent', category, action, label]);
    }
};

/*
 * Add GA tracking code on load.
 */
window._gaq = window._gaq || [];
window._gaq.push(['_setAccount', 'UA-45636912-1']);
window._gaq.push(['_setDomainName', 'auto']);
window._gaq.push (['_gat._anonymizeIp']);

(function() {
    if (window.googleAnalyticsInjected) return;
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = 'https://ssl.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
    window.googleAnalyticsInjected = true;
})();
