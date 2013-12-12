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
