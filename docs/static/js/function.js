jQuery(window).on("load", function () {
    "use strict";

    /*  ===================================
        Loading Timeout
     ====================================== */
    $(".loader-area").fadeOut(800);

});




//************************* SCROLL FUNCTIONS***********************//

// Navbar Scroll Function
var $window = $(window);



$(window).on('scroll', function () {
    if ($(this).scrollTop() > 260) { // Set position from top to add class
        $('header').addClass('header-appear');
    }
    else {
        $('header').removeClass('header-appear');
    }
});

$(".scroll").on("click", function (event) {
    event.preventDefault();
    $("html,body").animate({
        scrollTop: $(this.hash).offset().top - 60}, 1200);
});


/* ===================================
     Counter
====================================== */


$('.count').each(function () {
    $(this).appear(function () {
        $(this).prop('Counter', 0).animate({
            Counter: $(this).text()
        }, {
            duration: 3000,
            easing: 'swing',
            step: function (now) {
                $(this).text(Math.ceil(now));
            }
        });
    });
});



/* ===================================
  WOW Animation
====================================== */

if ($(window).width() > 991) {
    var wow = new WOW({
        boxClass: 'wow',
        animateClass: 'animated',
        offset: 0,
        mobile: false,
        live: true
    });
    new WOW().init();
}



    $('[data-fancybox]').fancybox({
        'transitionIn': 'elastic',
        'transitionOut': 'elastic',
        'speedIn': 600,
        'speedOut': 200,
        buttons: [
            'slideShow',
            'fullScreen',
            'thumbs',
            'share',
            // 'download',
            'zoom',
            'close'
        ],
    });






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



/* ===================================
              search popup
   =================================== */

$('.open_search').on('click', function(event){

    event.stopPropagation();
    $('.dropdown-menu').addClass('hidden');
    $('.search_block').toggleClass('visible');
    $('.search_block .search_input').focus();
    $('.search-overlay').addClass('overlayer');
    $('#sidemenu_toggle').fadeOut(2);
});

$('body').on('click', function(){
    $('.dropdown-menu').removeClass('hidden');
    $('.search_block').removeClass('visible');
    $('.search-overlay').removeClass('overlayer');
    $('#sidemenu_toggle').fadeIn(2);
});

$('.search_box').on('click', function(event){
    event.stopPropagation();
});

$('.search_input').on('keyup', function(event){
    if($(this).val() !== ''){
        $(this).addClass('typing');
    } else {
        $(this).removeClass('typing');
    }
});

$(document).on('keyup',function(e){
    if(e.keyCode===27){
        $('.search_block').removeClass('visible');
        $('.search-overlay').removeClass('overlayer');
        $('#sidemenu_toggle').fadeIn(2);
    }
});


