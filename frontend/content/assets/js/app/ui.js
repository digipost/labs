dp.ui = {};

dp.ui.init = function() {
    dp.ui.activateMobileMenu();
};

dp.ui.activateMobileMenu = function() {
    var openMobileMenu = function() {
        $('#nav-icon4').addClass('open');
        $('.mobilemenu').addClass('open');
        var background = $('<div />');
        $('body').prepend(background.addClass('mobilemenu-background'));
        $(background).click(closeMobileMenu);
        setTimeout(function() {
            background.addClass('active');
        }, 10);
    };
    var closeMobileMenu = function() {
        $('#nav-icon4').removeClass('open');
        $('.mobilemenu').removeClass('open');
        $('.mobilemenu-background').removeClass('active');
        setTimeout(function() {
            $('.mobilemenu-background').remove();
        }, 250); 
    };

    $('.mobilemenu-opener').click(function(){
        if($('.mobilemenu').hasClass('open')) {
            closeMobileMenu();
        } else {
            openMobileMenu();
        }
    });
    $('.mobilemenu a').click(closeMobileMenu);
};