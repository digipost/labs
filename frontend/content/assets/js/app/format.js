dp.format = {};

/*
 * Create <p> tags from newlines.
 */
dp.format.paragraphs = function(str) {
    if (!str) return '';
    return '<p>' + str.replace(/[\r]?[\n][\s]*[\r]?[\n]/g, '</p><p>').replace(/[\r]?[\n]/g, '<br>') + '</p>';
};

dp.format.escapeAndFormatLinebreaks = function(str) {
    if (!str) return '';
    return dp.format.paragraphs(_.escape(str));
};

dp.format.spaceToHyphen = function(str) {
    return str.replace(/\s/g, '-');
};

dp.format.hyphenToSpace = function(str) {
    return str.replace(/\-/g, ' ');
};
