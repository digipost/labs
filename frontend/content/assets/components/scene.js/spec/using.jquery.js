describe('using.jquery.js', function() {

    before(function() {
        reset();
        expose('$');
    });

    it('scene() should use jQuery for elements', function(done) {
        var el = $('<main>');
        scene({ template: '<p></p>' }, {}, function(view) {
            view.set('p', { template: '<span>1</span>' }, {}, function(subview) {
                assert.equal(view.el.html(), '<p><div><span>1</span></div></p>');
                assert.equal(view.$('span').text(), '1');
                done();
            });
        });
    });

    it('scene() should use jQuery for XHR', function(done) {
        scene({ template: '/base/spec/templates/1.html' }, {}, function(view) {
            assert.equal(view.el.html(), '<span>1</span>\n');
            done();
        });
    });

    it('scene.use() should add middleware', function(done) {
        scene.use(function(view) {
            view.$('.middleware-init').addClass('middleware-done');
        });
        scene({ template: '<i class="middleware-init"></i>' }, {}, function(view) {
            assert.equal(view.el.html(), '<i class="middleware-init middleware-done"></i>');
            done();
        });
    });

    it('scene.fn() should add prototype props', function(done) {
        scene.fn('id', 42);
        scene({ template: '' }, {}, function(view) {
            assert.equal(view.id, 42);
            done();
        });
    });

    it('scene.fn() props should set "this" to view', function(done) {
        scene.fn('shout', function(selector) {
            this.$(selector).each(function() {
                $(this).text($(this).text().toUpperCase());
            });
        });
        scene({ template: '<p>hello</p><p>world</p><div>test</div>' }, {}, function(view) {
            view.shout('p');
            assert.equal(view.el.html(), '<p>HELLO</p><p>WORLD</p><div>test</div>');
            done();
        });
    });

});
