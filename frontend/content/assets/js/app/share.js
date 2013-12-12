dp.share = {};

dp.share.track = function(type, service) {
    dp.track.event(type, 'share', service);
};

dp.share.facebook = function(link, pageTitle, pageType) {
    var base  = 'http://www.facebook.com/sharer/sharer.php?';
    var params = ['s=100'];
    dp.share.addParam(params, 'p[title]=', pageTitle);
    dp.share.addParam(params, 'p[url]=', link.href);

    dp.share.track(pageType, 'facebook');
    window.open(base + params.join('&'), 'fbsharer', 'toolbar=0,status=0,width=626,height=436');
};

dp.share.twitter = function(link, pageTitle, pageType) {
    var customTitle = (pageType === 'news') ? 'Jeg anbefaler denne saken på Digipost Labs: ' : 'Jeg liker denne idéen på Digipost Labs: ';
    customTitle += pageTitle;

    var base = 'https://twitter.com/intent/tweet?';
    var params = [];
    dp.share.addParam(params, 'text=', customTitle);
    dp.share.addParam(params, 'url=', link.href);

    dp.share.track(pageType, 'twitter');
    window.open(base + params.join('&'), 'twittersharer', 'toolbar=0,status=0,width=550,height=420');
};

dp.share.google = function(link, pageTitle, pageType) {
    var base = 'https://plus.google.com/share?';
    var params = [];
    dp.share.addParam(params, 'url=', link.href);

    dp.share.track(pageType, 'google plus');
    window.open(base + params.join('&'), 'gplussharer', 'toolbar=0,status=0,width=626,height=436');
};

dp.share.linkedin = function (link, pageTitle, pageType) {
    var customTitle = (pageType === 'news') ? 'Jeg anbefaler denne saken på Digipost Labs: ' : 'Jeg liker denne idéen på Digipost Labs: ';
    customTitle += pageTitle;

    var customText = 'Digipost Labs er en lekeplass for utvikling av fremtidens postkasse, levert av Posten Norge. Her kan du delta på idémyldring og debatt.';

    var base = 'http://www.linkedin.com/shareArticle?';
    var params = ['mini=true'];
    dp.share.addParam(params, 'title=', customTitle);
    dp.share.addParam(params, 'summary=', customText);
    dp.share.addParam(params, 'url=', link.href);
    dp.share.addParam(params, 'source=', 'Digipost Labs');

    dp.share.track(pageType, 'linkedin');
    window.open(base + params.join('&'), 'linkedinsharer', 'toolbar=0,status=0,width=520,height=570');
};

dp.share.addParam = function (params, param, value) {
    if (_.isString(value) && !_.isEmpty(value)) {
        params.push(param + encodeURIComponent(value));
    }
};
