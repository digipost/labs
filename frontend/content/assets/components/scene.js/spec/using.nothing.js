describe('using.nothing.js', function() {

    before(function() {
        reset();
    });

    it('scene() should render simple views', function(done) {
        scene({}, { a: 1 }, function(view) {
            assert.equal(view.a, 1);
            assert.ok(view.el);
            assert.ok(view.$);
            assert.equal(view.$('div').length, 0);
            done();
        });
    });

    it('scene() should return the view', function(done) {
        var view = scene({}, { a: 1 });
        assert.equal(view.a, 1);
        assert.ok(view.el);
        done();
    });

    it('scene() should use natives for XHR', function(done) {
        scene({ template: '/base/spec/templates/2.html' }, {}, function(view) {
            assert.equal(view.el.innerHTML, '<p>1<span>2</span></p>\n');
            done();
        });
    });

    it('scene() should render HTML strings', function(done) {
        scene({ template: '<p>1</p>' }, {}, function(view) {
            assert.equal(view.el.innerHTML, '<p>1</p>');
            done();
        });
    });

    it('scene() should accept existing elements', function(done) {
        var el = document.createDocumentFragment()
            .appendChild(document.createElement('p'));
        var tp = '<span>foo</span>';
        scene({ template: tp }, { el: el }, function(view) {
            assert.equal(el.innerHTML, tp);
            done();
        });
    });

    it('scene() should trigger before function', function(done) {
        var vm = { before: function() { this.bar = 2; } };
        scene(vm, { bar: 1 }, function(view) {
            assert.equal(view.bar, 2);
            done();
        });
    });

    it('scene() should trigger init function', function(done) {
        var vm = { init: function() { this.foo = 1; } };
        scene(vm, {}, function(view) {
            assert.equal(view.foo, 1);
            done();
        });
    });

    it('scene() should set up after function', function(done) {
        var count = 1;
        scene.after = function() { count = count + 1; };
        scene({}, {}, function(view) {
            assert.equal(count, 2);
            view.set('.a', {}, {}, function(sub) {
                assert.equal(count, 3);
                done();
            });
        });
    });

    it('scene() should add subviews', function(done) {
        scene({ template: '<p></p>' }, {}, function(view) {
            view.set('p', { template: '<span>1</span>' }, {}, function(subview) {
                assert.equal(view.el.innerHTML, '<p><div><span>1</span></div></p>');
                done();
            });
        });
    });

    it('view.set should return the view', function(done) {
        scene({ template: '<p></p>' }, {}, function(view) {
            var subview = view.set('p', { template: '<span>1</span>' });
            assert.ok(subview.el);
            done();
        });
    });

});
