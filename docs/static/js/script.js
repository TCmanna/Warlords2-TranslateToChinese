//*******************BLOG SECTION CAROUSAL***************
$('#blog-carousal').owlCarousel({
    items:3,
    loop:true,
    center:true,
    // margin:20,
    nav:false,
    dots:true,
    responsive:{
        0:{
            items:1
        },
        600:{
            items:3
        },
        1000:{
            items:3
        }
    }
});


//******************GALLERY POERFLIO************************//
$("#flat-gallery").cubeportfolio({
    layoutMode: 'grid',
    filters: '#flat-filters',
    defaultFilter: '.top',
    animationType: "quicksand",
    gapHorizontal: 35,
    gapVertical: 25,
    gridAdjustment: "responsive",
    mediaQueries: [{
        width: 1500,
        cols: 4
    }, {
        width: 1100,
        cols: 4
    }, {
        width: 768,
        cols: 3
    }, {
        width: 480,
        cols: 2
    },{
        width: 320,
        cols: 2
    }],
});



//************************* SCROLL FUNCTIONS***********************//

// Navbar Scroll Function
var $window = $(window);
$window.scroll(function () {
    var $scroll = $window.scrollTop();
    var $navbar = $(".navbar");
    if (!$navbar.hasClass("sticky-bottom")) {
        if ($scroll > 250) {
            $navbar.addClass("fixed-menu");
        } else {
            $navbar.removeClass("fixed-menu");
        }
    }
});
// bottom nav
if ($('.bottom-nav').length) {
    var $navheight = $('.bottom-nav').offset().top;
    $window.scroll(function () {
        if ($(this).scrollTop() > $navheight) {
            $('.bottom-nav').addClass("fixed-menu");
        } else {
            $('.bottom-nav').removeClass("fixed-menu");
        }

    });
}
//     //scroll to appear
$(window).on('scroll', function () {
    if ($(this).scrollTop() > 500)
        $('.scroll-top-arrow').fadeIn('slow');
    else
        $('.scroll-top-arrow').fadeOut('slow');
});

//     //Click event to scroll to top
$(document).on('click', '.scroll-top-arrow', function () {
    $('html, body').animate({scrollTop: 0}, 800);
    return false;
});



//***************** MENU AND CLOSE BUTTON OF NAVBAR WINDOW*********************//
$('.my-tog-btn').on("click", function () {
    $('.outer-window').removeClass('end-anm');
    $('.outer-window').addClass('inner-window');
    $('.outer-window').addClass('start-anm');

});
$('.close-outerwindow').on("click", function () {
    $('.outer-window').removeClass('start-anm');
    $('.outer-window').addClass('end-anm');
    setTimeout(function () {
        $('.outer-window').removeClass('inner-window');
    }, 800);
});
$('.outer-window ul li a').click(function () {
    $('.outer-window').removeClass('inner-window');
});




