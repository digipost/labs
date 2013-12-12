dp.storage = {};

/*
 * Set a value, with an optional number of days until expiration.
 */
dp.storage.set = function(key, value, ttl) {
    return $.cookie(key, value, {
        path: '/',
        expires: ttl || 999,
        secure: true
    });
};

/*
 * Get a value, or a default value if not set.
 */
dp.storage.get = function(key, fallback) {
    return $.cookie(key) || fallback;
};

/*
 * Delete a key:value pair.
 */
dp.storage.clear = function(key) {
    return $.removeCookie(key, { path: '/' });
};
