describe('using.underscore.js', function() {

    before(function() {
        reset();
        expose('_');
    });

    it('scene() should use _.template', function(done) {
        scene({ template: '<<%= a %>></<%= a %>>' }, { a: 'p' }, function(view) {
            view.set('p', { template: '<span><%= b %></span>' }, { b:1 }, function(subview) {
                assert.equal(view.el.innerHTML, '<p><div><span>1</span></div></p>');
                done();
            });
        });
    });

});
