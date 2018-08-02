/* Operations that deal with the customer's account */
$(function(){

    // Bind all links with class .view-order-details to toggle the order details
    $('body').on('click', 'a.view-order-details', function() {
        var url = $(this).attr('href');
        var $orderDetails = $(this).parents('.order').find('.order-details');
        
        // if this is the currently opened order details, collapse it
        if ($orderDetails.is(':visible')) {
            $orderDetails.slideToggle();
        } else {
            $orderDetails.load(url, function() {
                $visibleSections = $('.order-details:visible');
                $orderDetails.slideToggle();
                $visibleSections.slideToggle();
            });
        }   
        return false;
    });


    $('body').on('click', 'a.action', function() {
        var url = $(this).attr('href');
        var $serviceStatusTag = $(this).parents('.order-info-row').find('.service-status');
		
		$serviceStatusTag.load(url + ' #' + $serviceStatusTag.attr('id'));
        return false;
    });

});