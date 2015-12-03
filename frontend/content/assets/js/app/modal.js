dp.modal = {};

/*
 * Close the current modal window, if any.
 */
dp.modal.close = function(event) {
    if (_.isSet(event)) event.preventDefault();
    $('#modalwrap').remove();
    $('#page').show();
};

/*
 * Create a confirmation modal, with a callback called with the user's choice.
 */
dp.modal.confirm = function(content, confirm, callback) {
    dp.modal.show({ width: 550, content: content, buttons: [
        { text: 'Avbryt', action: function() { dp.modal.close(); callback(false); } },
        { text: confirm,  action: function() { dp.modal.close(); callback(true); } }
    ]}, callback);
};

/*
 * Show a modal window.
 */
dp.modal.show = function(options, callback) {
    dp.modal.close();
    options = dp.modal.defaults(options, callback);
    if (_.str.include(options.content, '.tmpl')) {
        dp.views.render(options.content, options.data || {}, function(instance) {
            dp.modal.create(options, instance.el, callback);
        });
    } else {
        dp.modal.create(options, options.content, callback);
    }
};

/*
 * Create a modal with content.
 */
dp.modal.create = function(options, content, callback) {
    var wrap = $('<div>').attr('id', 'modalwrap');
    var msg  = $('<div>').attr('id', 'modal').attr('role', 'dialog');
    var closer = dp.modal.closer(callback);

    dp.modal.addContent(msg, content, options);
    dp.modal.addClosers(msg, wrap, closer, options);
    dp.modal.addClasses(wrap, options);
    dp.modal.addCss(msg, options);
    if(options.keyboardClose) {
        dp.modal.bindKeys(closer, options);
    }
    $('body').append($(wrap).html(msg).show());

    dp.modal.addFocus();
    dp.modal.updatePosition();
    if (options.renderCallback) options.renderCallback();
};

/*
 * Parse modal.show options.
 */
dp.modal.defaults = function(options, callback) {
    if (_.notSet(options)) {
        options = {};
    }
    if (_.isString(options) || options.jquery) {
        options = { content: options };
    }
    options = _.defaults(options, {
        width: 500,
        header: false,
        closer: true,
        renderCallback: false,
        content: 'En feil har oppstått!',
        buttons: [ {
            text: (options.defaultButtonText || 'OK'),
            action: dp.modal.closer(callback)
        } ]
    });
    return options;
};

/*
 * Create a callback to be run when a modal is closed.
 */
dp.modal.closer = function(callback) {
    return function(event) {
        if (_.isFunction(callback)) callback();
        if (_.isString(callback)) dp.url.go(callback);
        dp.modal.close(event);
        event.stopPropagation();
    };
};

/*
 * Create modal buttons based on options.
 */
dp.modal.buttons = function(options) {
    if (!options.buttons || options.buttons.length === 0) {
        return $();
    }
    var buttons = $('<div>').addClass('buttons');
    _(options.buttons).each(function(button, i) {
        if (_.isSet(button.test) && !button.test) return;
        $(buttons).append($('<a>')
            .addClass('button button2')
            .text(button.text)
            .on('click', button.action)
            .attr('tabindex', i + 1)
            .attr('id', (button.id || '')));
    });
    return buttons;
};

/*
 * Add appropriate CSS based on current UI dimensions.
 */
dp.modal.addCss = function(msg, options) {
    if(options.fullscreen) {
        return;
    }
    var windowWith = $(window).width();
    var minimumSize = windowWith < options.width;
    if(!minimumSize) {
        msg.css({
            height: 'auto',
            width: options.width,
            marginLeft: - (options.width  / 2)
        });
    } else {
        msg.css({
            height: 'auto',
            width: windowWith,
            marginLeft: 0,
            marginTop: 0,
            left: 0
        });
    }
};

/*
 * Add helper classes to modal
 */
dp.modal.addClasses = function(wrap, options) {
    if (!options.header)  wrap.addClass('nohead');
    if (!options.content) wrap.addClass('nocontent');
    if (!options.buttons) wrap.addClass('nobuttons');
    if (options.fullscreen) wrap.addClass('fullscreen');
    if (options.bg === false) wrap.addClass('nobg');
    wrap.addClass(options.classes);
};

/*
 * Add content to modal
 */
dp.modal.addContent = function(msg, content, options) {
    var buttons = dp.modal.buttons(options);
    var text = $('<div>').addClass('text').html(content);
    var head = $('<div>').addClass('head').html(options.header);
    if (options.header)  msg.append(head);
    if (options.content) msg.append(text);
    if (options.buttons) msg.append(buttons);
};

/*
 * Focus on button or input in modal.
 */
dp.modal.addFocus = function() {
    if ($('#modal input:first').length > 0) $('#modal input:first').focus();
    else $('#modal .button:last').focus();
};

/*
 * Center modal on screen.
 */
dp.modal.updatePosition = function() {

    if (dp.ui.med) $('#modal').css({
        marginTop: 100,
        top: 0
    });

    $('#modal').scrollTop(0);
};

/*
 * Add keyboard shortcuts to modal.
 */
dp.modal.bindKeys = function(closer) {
    $(document).one('keyup', function(event) {
        if (event.keyCode === 27 || event.keyCode === 13) closer(event);
    });
};

/*
 * Add closing methods to modal.
 */
dp.modal.addClosers = function(msg, wrap, closer, options) {
    var cross = $('<button>').addClass('closer')
        .attr('aria-label', 'Lukk').on('click', closer);
    if (options.closer) msg.append(cross);
    if( options.clickAnywhereToClose ) {
        if (options.butNotOn) {
           $(msg).find(options.butNotOn).on('click', function(e){ e.stopPropagation(); });
        }
    } else {
        msg.on('click', function(e) { e.stopPropagation(); });
    }
    wrap.find('.closer').on('click', closer);
    wrap.on('click', closer);
    $(msg).find('.modal-close').on('click', closer);
};
