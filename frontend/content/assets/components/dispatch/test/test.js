/*global dispatch, window, $ */

/* reset */
window.location.hash = '';

var add = function(num, name) {
    $('body').append($('<div/>')
        .html(num + ' &mdash; ' + name)
        .addClass('test-' + num));
};

var pass = function(num) {
    $('.test-' + num).first()
        .removeClass('fail')
        .addClass('pass');
};

var fail = function(num) {
    $('.test-' + num).first()
        .removeClass('pass')
        .addClass('fail');
};

var then = function(fn, num) {
    return function() { fn(num); };
};

var i = 0, nav = function(loc) {
    setTimeout(function() {
        window.location.hash = loc;
    }, ++i * 30);
};

/* ------------------------------------ */

$(function() {

/* ------------------------------------ */

add(0, 'start at root');
add(1, 'fire initial hash value');
add(2, 'handle hash change');
add(3, 'run second hash change');
add(4, 'run complex hashes');
add(5, 'ignore query strings');

add(6, 'turn off routes by pattern');
add(7, 'turn off routes by handler');
add(8, 'turn off routes by name');

add(9, 'get previous hash');
add(10, 'accept ints as paths');
add(11, 'match ints in hash');
add(12, 'run routes manually');

add(20, 'parse first parameter');
add(21, 'parse second parameter');
add(22, 'parse third parameter');
add(23, 'parse adjecent parameters');
add(24, 'parse central parameters');

add(30, 'run routes once');
add(31, 'force route reload');
add(32, 'run before');
add(33, 'run after');
add(34, 'run multiple before');
add(35, 'run multiple after');
add(36, 'run fallback');

add(40, 'optional parameter set');
add(41, 'optional parameter unset');
add(42, 'optionals inside routes');
add(45, 'optional parameters set');
add(46, 'one of two optionals unset');
add(47, 'two of two optionals unset');
add(48, 'one unset without slash');
add(49, 'two unset without slash');
add(50, 'five optional unset');
add(51, 'required set and optional unset');
add(52, 'required unset and optional unset');

add(60, 'trailing 0');
add(61, 'trailing 1');
add(62, 'trailing 2');
add(63, 'trailing 3');
add(64, 'trailing 4');

add(70, 'replace 1');
add(71, 'replace 2');
add(72, 'replace 3');

add(80, 'parse before repeat');
add(81, 'query does not repeat');
add(82, 'get param value');

add(99, 'complex query strings');


/* ------------------------------------ */

dispatch.start('/');
dispatch.on('/', then(pass, 0));
dispatch.on('/1', then(pass, 1));
dispatch.on('/2', then(pass, 2));
dispatch.on('/:a', function(params) {
    if (params.a === '3') pass(3);
});
dispatch.on('4a+sdf/&$!/:a sdf/_4', then(pass, 4));
dispatch.on('/5', then(pass, 5));

nav('#/1');
nav('#/2');
nav('#/3');
nav('#4a+sdf/&$!/:a sdf/_4');
nav('#/5?a=1');

/* ------------------------------------ */

pass(6);
dispatch.on('6', then(fail, 6));
dispatch.off('6');
nav('#6');

pass(7);
dispatch.on('7', then(fail, 7));
dispatch.off(then(fail, 7));
nav('#7');

pass(8);
dispatch.on('route8', '8', then(fail, 8));
dispatch.off('route8');
nav('#8');

/* ------------------------------------ */

dispatch.on('9', function(params) {
    if (!params.prev || params.prev === '8') pass('9');
    else fail('9');
});
nav('#9');

dispatch.on(10, then(pass, 10));
dispatch.on('11', then(pass, 11));
dispatch.on(12, then(pass, 12));
nav('#10');
window.location.hash = 11;
dispatch.run('12');

/* ------------------------------------ */

dispatch.on('0/:foo', function(p) {
    if (p.foo === 'a') pass(20);
    else fail(20);
});
dispatch.on('1/foo/:foo', function(p) {
    if (p.foo === 'b') pass(21);
    else fail(21);
});
dispatch.on('2/foo/foo/:foo', function(p) {
    if (p.foo === 'c') pass(22);
    else fail(22);
});
dispatch.on('3/:foo/:bar/:baz', function(p) {
    if (p.foo === 'a' && p.bar === 'b' && p.baz === 'c') pass(23);
    else fail(23);
});
dispatch.on('4/lal/:lol/lil', function(p) {
    if (p.lol === 'l') pass(24);
    else fail(24);
});

nav('#0/a');
nav('#1/foo/b');
nav('#2/foo/foo/c');
nav('#3/a/b/c');
nav('#4/lal/l/lil');

/* ------------------------------------ */

var i = 0;
dispatch.on('/num/1', function() { i++; });
dispatch.on('/num/2', function() {});
dispatch.on('/num/3', function() {
    if (i === 2) pass(30);
    else fail(30);
});

nav('#/num/1');
nav('#/num/1');
nav('#/num/2');
nav('#/num/1');
nav('#/num/1');
nav('#/num/3');
nav('#/num/1');

var x = 0;
dispatch.on('/reloaded', function() { if(++x === 3) pass(31); else dispatch.run(); });
nav('#/reloaded');

dispatch.before.push(function(fn) { pass(32); fn(); });
dispatch.after.push(function(fn) { pass(33); fn(); });

dispatch.before.push(function(fn) { fn(); });
dispatch.before.push(function(fn) { pass(34); fn(); });
dispatch.after.push(function(fn) { fn(); });
dispatch.after.push(function(fn) { pass(35); fn(); });

dispatch.fallback = function() { pass(36); };
nav('#/lol/no/way');

/* ------------------------------------ */

dispatch.on('/opt/40/*foo', function(params) {
    if(params.foo === 'o1') pass(40);
});
dispatch.on('/opt/41/*bar', function(params) {
    if(params.bar === undefined) pass(41);
});
nav('#/opt/40/o1');
nav('#/opt/41/');

dispatch.on('/opt/42/*inner/last', function(params) {
    if(params.inner === 'foo') pass(42);
});
nav('#/opt/42/foo/last');

dispatch.on('/opt/45/*bar/*baz', function(params) {
    if(params.bar === 'o2' && params.baz === 'o3') pass(45);
});
dispatch.on('/opt/46/*bar/*baz', function(params) {
    if(params.bar === 'o4' && params.baz === undefined) pass(46);
});
dispatch.on('/opt/47/*bar/*baz', function(params) {
    if(params.bar === undefined && params.baz === undefined) pass(47);
});
dispatch.on('/opt/48/*bar/*baz', function(params) {
    if(params.bar === 'one' && params.baz === undefined) pass(48);
});
dispatch.on('/opt/49/*bar/*baz', function(params) {
    if(params.bar === undefined && params.baz === undefined) pass(49);
});
dispatch.on('/opt/50/*bar/*baz/*boo/*biz/*box', function() { pass(50); });
dispatch.on('/opt/51/:foo/:bar/*baz/*boo/*lal', function(params) { if(params.bar === 'two') pass(51); pass(52); });
dispatch.on('/opt/52/:foo/:bar/*baz/*boo/*lal', function() { fail(52); });
nav('#/opt/45/o2/o3');
nav('#/opt/46/o4/');
nav('#/opt/47/');
nav('#/opt/48/one');
nav('#/opt/49');
nav('#/opt/50/');
nav('#/opt/51/one/two');
nav('#/opt/52/noo');

dispatch.on('/trail/60/', then(pass, 60));
dispatch.on('/trail/61/', then(pass, 61));
dispatch.on('/trail/62', then(pass, 62));
dispatch.on('/trail/63/:a', function(p) { if(p.a === 'a') pass(63); });
dispatch.on('/trail/64/:b/', function(p) { if(p.b === 'b') pass(64); });
nav('#/trail/60/');
nav('#/trail/61');
nav('#/trail/62/');
nav('#/trail/63/a/');
nav('#/trail/64/b');

/* ------------------------------------ */

dispatch.on('/rep/70/:a', function(p) {
    if (p.a === '1') pass(70);
    if (p.a === '7') fail(70);
    if (p.a === '0') fail(70);

    dispatch.replace(':a', 7);
    var h = window.location.hash;
    var t = h.replace(/^#/, '');
    if (t !== '/rep/70/7') fail(70);
});
nav('#/rep/70/1');

dispatch.on('/rep/71/:x/:y/71', function(p) {
    if (p.y === '2') pass(71);
    else fail(71);
});
dispatch.replace(':x', 7);
dispatch.replace(':y', 4);
nav('#/rep/71/1/2/71');
dispatch.replace(':y', 5);

dispatch.on('/rep/72/:c/1/72', function(p) {
    if (p.c === '3') pass(72);
    else fail(72);
    dispatch.set(':c', 0);
    var h = window.location.hash;
    var t = h.replace(/^#/, '');
    if (t !== '/rep/72/0/1/72') fail(72);
});
nav('#/rep/72/3/1/72');

/* ------------------------------------ */

var qs0 = 0;
dispatch.on('/80', function() {
    qs0++;
    if (qs0 === 1) pass(80);
    else fail(80);
});
nav('#/80');
nav('#/80');
nav('#/80/');
nav('#/80/');
nav('#/80');

var qs1 = 0;
dispatch.on('/81', function() {
    qs1++;
    if (qs1 === 1) pass(81);
    else fail(81);
});
nav('#/81');
nav('#/81?a=1');
nav('#/81?a=1');
nav('#/81');
nav('#/81?c=1');
nav('#/81');

dispatch.on('/82/:a/:b', function() {
    var fst = dispatch.get(':a') === 'one';
    var snd = dispatch.get(':b') === 'two';
    var thd = dispatch.get(':c') === undefined;
    if (fst && snd && thd) pass(82);
    else fail(82);
});
nav('#/82/one/two/');

/* ------------------------------------ */

dispatch.on('/qs/foo/bar', function() { pass(99); });
nav('#/qs/foo/bar?asdf=1234/123/$312/qasd/reloaded&foobar=42');
nav('#/done');

});
