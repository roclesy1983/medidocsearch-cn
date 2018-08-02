/**
 * Reveal the Product's quickview link
 */
var windowsize = $(window).width();
$('body').on('mouseenter', '.product_container .image', function() {
	if (windowsize > 480) {
		HC.toggleQuickview($(this), true);
	}
});

/**
 * Hide the Product's quickview link
 */
$('body').on('mouseleave', '.product_container .image', function() {
	if (windowsize > 480) {
		HC.toggleQuickview($(this), false);
	}
});

/**
 * Handle the Product's quickview link click & show the quickview modal
 */
$('body').on('click', '.js-quickview', function() {
	var link = $(this).closest('.image').find('.imageLink').attr('href');
	BLC.ajax({
		url : link + "?quickview=true"
	}, function(data) {
		data.find('.product-options').removeClass('hidden');
		data.find('.product-option-nonjs').remove();
		if (windowsize == 320) {
			data.find('#product_content').css({
				"margin-right" : "-40px"
			})
			data.find('#product_thumbs li img').css({
				"margin" : "0px",
				"width" : "260px"
			})
			data.find('#customer-reviews-container').css({
				"margin-left" : "10px"
			})
		}
		if (windowsize == 768) {
			data.find('#product_main_image').css({
				"width" : "300px",
				"height" : "300px"
			})
			data.find('#product_thumbs_container').css({
				"width" : "300px"
			})
		}
		$.modal(data.find('#left_column').first().css({
			"width" : "90%",
			"height" : "85%"
		}), HC.quickviewOptions);
		$('#simplemodal-container').find('.jqzoom').jqzoom({
			zoomType : 'innerzoom',
			zoomWidth : 402,
			zoomHeight : 402,
			title : false
		});

		// Reinitialize AddThis for modal that was loaded;
		// attributes addthis:title and addthis:url (on product pages) cannot be set with thymeleaf because of the
		// colon, so we fill a dummy attribute and copy it over using jquery
		$('div[addthistitle]').each(function() {
			$(this).attr('addthis:title', $(this).attr('addthistitle'));
			$(this).attr('addthistitle', null);
		});
		$('div[addthisurl]').each(function() {
			$(this).attr('addthis:url', $(this).attr('addthisurl'));
			$(this).attr('addthisurl', null);
		});
		if (typeof (addthis) !== 'undefined') {
			window.addthis.toolbox('.addthis_toolbox');
		}
	});
	return false;
});
