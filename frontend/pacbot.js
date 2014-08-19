exports.config = function(pacbot) {

    var config = {
        port: 7001,
        root: '.',
        packed: 'assets/digipost/packed',
        assets: {},
        transform: {},
        ignore_build: ['assets/js/spec', 'assets/components'],
        ignore_processing: ['assets/templates', 'assets/components']
    };

    config.templateSettings = {
        evaluate    : /<%([\s\S]+?)%>/g,
        interpolate : /<%unsafe([\s\S]+?)%>/g,
        escape      : /<%=([\s\S]+?)%>/g
    };

    config.assets.css = {
        common: [
            'assets/components/font-awesome/css/font-awesome.css',
            'assets/css/base.css',
            'assets/css/animate.css',
            'assets/css/images.css',
            'assets/css/tiles.css',
            'assets/css'
        ]
    };

    config.assets.js = {
        common: [
            'assets/components/es5-shim/es5-shim.js',
            'assets/components/es5-shim/es5-sham.js',
            'assets/components/underscore/underscore.js',
            'assets/components/underscore.string/lib/underscore.string.js',
            'assets/components/dispatch/dispatch.js',
            'assets/components/scene.js/scene.js',
            'assets/components/js-md5/js/md5.js',
            'assets/components/jquery/dist/jquery.js',
            'assets/components/jquery.cookie/jquery.cookie.js',
            'assets/js/main.js',
            'assets/js'
        ]
    };

    config.assets.tmpl = {
        common: 'assets/templates'
    };

    // Remove ".html" from most filenames in build mode.
    pacbot.filter.set('target', 'html', function(file) {
        if (file.indexOf('index.html') === -1 && file.indexOf('404.html') === -1) {
            return file.replace(/\.html$/, '');
        }
        return file;
    });

    return config;

};
