/*global require, process */

/*
 * Labs Proxy (localhost:9090)
 * Setup: cd scripts && ./setup.sh
 */

var fs        = require("fs"),
    util      = require("util"),
    url       = require("url"),
    path      = require("path"),
    exec      = require('child_process').exec,
    https     = require("https"),
    http      = require("http");

try {
    var colors    = require("colors"),
        request   = require("request"),
        httpProxy = require("http-proxy");
} catch (e) {
    if(e.code === "MODULE_NOT_FOUND") {
        util.puts("Missing dependencies: run setup.sh to install required node dependencies");
        process.exit(1);
    } else {
        throw e;
    }
}

/* ----------------------------------- */

var policy = "default-src 'self'; img-src https://*; script-src 'self' https://ssl.google-analytics.com; object-src 'none'; style-src 'self' 'unsafe-inline'; frame-src 'self' https://www.youtube.com https://player.vimeo.com ";

var startProxy = function(ip) {
    // Labs
    createProxy(ip, 7000, {
        '/api/': '7002',
		'/ideer/': '7002/legacy/ideer/',
		'/pages/': '7002/legacy/pages/',
        '/\\?_escaped_fragment_=(.*)': '3000',
        '/': '7001'
    }, policy);
    showLinks(ip);
    util.puts("");
};

var createProxy = function (ip, port, routeConfig, csp) {
    httpProxy.createServer({ router: createRoutes(ip, routeConfig), https: https() }, function(req, res, proxy) {
        if (csp && !req.url.match(/\/app\/(dev|miniprofiler)(\.html)?(\?.*)?/)) addHeader(res, "Content-Security-Policy-Report-Only", csp);

        req.headers["x-forwarded-ssl"] = true;
        req.headers['x-forwarded-host'] = 'localhost:7000';
        proxy.proxyRequest(req, res);
    }).listen(port);
};

var createRoutes = function(ip, routeConfig) {
    var routes = {};
    ip.concat(['localhost']).forEach(function(val) {
        for (var from in routeConfig) {
            routes[val + from] = 'localhost:' + routeConfig[from];
        }
    });
    return routes;
};

var https = function() {
    return {
        key:  fs.readFileSync(path.join(__dirname, "cert", "privatekey.pem"),  "utf8"),
        cert: fs.readFileSync(path.join(__dirname, "cert", "certificate.pem"), "utf8")
    };
};

var addHeader = function(res, name, value) {
    res.oldWriteHead = res.writeHead;
    res.writeHead = function(statusCode, headers) {
        res.setHeader(name, value);
        res.oldWriteHead(statusCode, headers);
    };
};

var showLinks = function(ip) {
    util.puts("");
    util.puts(("Labs Proxy listening!").rainbow.bold);
        util.puts(("  https://localhost:7000").blue);
    ip.forEach(function(e) {
        util.puts(("  https://" + e + ":7000").blue);
    })
};

/* ----------------------------------- */

var platform = function() {
    var commands = {
        win32:    { cmd: 'ipconfig', filter: /\bIP(v[46])?-?[^:\r\n]+:\s*([^\s]+)/g },
        darwin:   { cmd: 'ifconfig', filter: /\binet\s+([^\s]+)/g },
        standard: { cmd: 'ifconfig', filter: /\binet\b[^:]+:\s*([^\s]+)/g }
    };
    return commands[process.platform] || commands.standard;
};

var getNetworkIP = (function () {
    var ignore = /^(127\.0\.0\.1|::1|fe80(:1)?::1(%.*)?)$/i;
    var commands = platform();
    return function(callback) {
        exec(commands.cmd, function(error, stdout, sterr) {
            cached = [];
            var ip, matches = stdout.match(commands.filter) || [];
            for (var i = 0; i < matches.length; i++) {
                ip = matches[i].replace(commands.filter, '$1');
                if (!ignore.test(ip) && (/^(10|192\.168)\./).test(ip)) cached.push(ip);
            }
            callback(error, cached);
        });
    };
})();

getNetworkIP(function(err, ips) {
    if(err) util.puts("Unknown network IP:".red, err);
    else startProxy(ips);
});
