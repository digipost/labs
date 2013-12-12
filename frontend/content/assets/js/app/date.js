/*
 * Functions for formatting ISO dates.
 */
dp.date = {};

/*
 * Format: 01.12.2020 kl. 12.00
 */
dp.date.datetime = function(date) {
    var m = dp.date.parse(date);
    return !m ? '' : m.day + '.' + m.month + '.' + m.year + ' kl. ' + m.hour + '.' + m.min;
};

/*
 * Format: 01.12.2020 12.00
 */
dp.date.date = function(date) {
    var m = dp.date.parse(date);
    return !m ? '' : m.day + '.' + m.month + ' ' + m.year + ' ' + m.hour + '.' + m.min;
};

/*
 * Format: 01. desember 2020
 */
dp.date.day = function(date) {
    var m = dp.date.parse(date);
    return !m ? '' : m.day + '. ' + dp.date.month(m.month) + ' ' + m.year;
};

/*
 * Format: 2020-12-01, kl. 12.00
 */
dp.date.sortable = function(date) {
    var m = dp.date.parse(date);
    return !m ? '' : m.year + '-' + m.month + '-' + m.day + ', kl.' + m.hour + '.' + m.min;
};

/*
 * Format: 1378551537000
 */
dp.date.timestamp = function(date) {
    var m = dp.date.ints(date);
    var d = new Date(m.year, m.month, m.day, m.hour, m.min, m.sec);
    return d.getTime();
};

/*
 * Index: 1: Januar, 2: Februar, ....
 */
dp.date.month = function(n) {
    var months = ['', 'januar', 'februar', 'mars', 'april', 'mai', 'juni',
        'juli', 'august', 'september', 'oktober', 'november', 'desember'];
    var num = _.isNumber(n) ? n : parseInt(n, 10);
    return num > 0 && num <= 12 ? months[num] : '';
};

/*
 * Parse ISO dates.
 */
dp.date.parse = function(date) {
    var m = /(\d\d\d\d)-(\d\d)-(\d\d).(\d\d):(\d\d):(\d\d)/.exec(date);
    if (!m) m = /(\d\d\d\d)-(\d\d)-(\d\d)/.exec(date);
    if (!m) return '';
    return { year: m[1], month: m[2], day: m[3], hour: m[4], min: m[5], sec: m[6] };
};

/*
 * Get ints for each part of an ISO date (zero-indexed month etc).
 */
dp.date.ints = function(date) {
    var m = dp.date.parse(date);
    if (!m) return '';
    _.each(m, function(val, key) { m[key] = parseInt(val, 10); });
    m.month = m.month - 1;
    return m;
};
