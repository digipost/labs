dp.item = {};

/*
 * Avatar img tag for an item.
 */
dp.item.avatar = function(author, size) {
    size = size || 40;
    var sizeAttribute = (author.avatar.indexOf('?') !== -1) ? '&s=' : '?s=';
    var url = author.avatar + sizeAttribute + size;
    return '<img class="avatar" alt="' + _.escape(author.name) + '" src="' + url + '">';
};

dp.item.avatarWithLink = function(author, size) {
    var img = dp.item.avatar(author, size);
    if (!author.admin) return img;
    var id = _.escape(author.userId);
    return '<a class="author" href="#!/profiles/' + id + '">' + img + '</a>';
};

/*
 * Link to author page for an item, if applicable.
 */
dp.item.author = function(author) {
    if (!author.admin) return author.name;
    var id = author.userId;
    return '<a class="author" href="#!/profiles/' + id + '">' + _.escape(author.name) + '</a>';
};