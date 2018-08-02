/* The Heat Clinic global module. This contains variables and functions
 * used by various other areas of the site */
var HC = (function($) {
    

		var quickviewOptions = {
		maxWidth : 845,
		maxHeight : 823,
		fitToView : false,
		width : '100%',
		height : '100%',
		autoSize : true,
		closeClick : false,
		topRatio : 0
	};

	function showFacetMultiselect(abbr) {
		$.modal($('#facet-' + abbr).css({
			"height" : "90%"
		}), {
			maxWidth : 200,
			minWidth : 200,
			fitToView : false,
			width : '100%',
			height : '100%',
			autoSize : true,
			closeClick : false,
			topRatio : 0
		});
	}
    
    function updateCurrentImage() {
        // grab the active product option values
        var activeOptions = $('.product-options .active');
        var optionValues = [];
        $.each(activeOptions, function() {
            optionValues.push($.parseJSON($(this).attr('data-product-option-value')));
        });
        var mediaItems = $('#product_thumbs a');
        var finalMedia;
        var finalMediaMatches = 0;
        $.each(mediaItems, function() {
            var candidateMedia = this;
            var candidateMediaMatches = 0;
            $.each(optionValues, function() {
                if ($(candidateMedia).attr('data-tags') != undefined && 
                    $(candidateMedia).attr('data-tags').toLowerCase().indexOf(this.valueName.toLowerCase()) !== -1) {
                    candidateMediaMatches++;
                }
            });
            if (candidateMediaMatches > finalMediaMatches) {
                finalMedia = candidateMedia;
                finalMediaMatches = candidateMediaMatches;
            }
        });
        
        //at this point I should have the best-matched media item; select it
        if (finalMedia != null) {
            finalMedia.click();
        }
    }
    
    function updatePriceDisplay() {
        var productOptions = getProductOptionData();

        var selectedProductOptions = [];

        for (var i = 0; i < productOptions.length; i++) {
            selectedProductOptions.push(productOptions[i].selectedValue); // add selected value to array
        }
        
        var productOptionPricing = getPricingData();
        
        var price;
        
        for (var i = 0; i < productOptionPricing.length; i++) {
            var pricing = productOptionPricing[i];
            if ($(pricing.selectedOptions).not(selectedProductOptions).length == 0 && $(selectedProductOptions).not(pricing.selectedOptions).length == 0) {
                price = pricing.price;
                break;
            }
        }
        
        if (price) {
            $price = $('#price div');
            if ($price.length != 0) {
                $price.text(price);
            } else {
                $('.product-options.modal:visible ul').first().append('<div id="price"><div>' + price + '</div></div>');
            }
            $('#price div').text(price);
        }
        
    }
    
    function showNotification(notification, delay) {
        if (!delay) {
            delay = '3500';
        }
        $('#notification_bar').html(notification).slideToggle('slow').delay(delay).slideToggle('slow');
    }
    
    function changeProductOption($option) {
        if (!$option.is('.active')) {
            $option.siblings('.active').removeClass('active');
            $option.toggleClass('active');
            var selectedOption = $option.data('product-option-value');
            var $optionText = $option.parents('.product-option-group').find('span.option-value');
            if (selectedOption !== undefined) {
                $optionText.text(selectedOption.valueName);
                var productOptionData = getProductOptionData();

                for (var i = 0; i < productOptionData.length; i++) {
                    var option = productOptionData[i];
                    if (option.id === selectedOption.optionId) {
                        option.selectedValue = selectedOption.valueId;
                        break;
                    }
                }
                
                updateCurrentImage();
                updatePriceDisplay();
            }
        }
    }
    
    function getProductOptionData() {
        return $('#product-option-data').data('product-options');
    }
    
    function getPricingData() {
        return $('#product-option-data').data('product-option-pricing');
    }
    
    /**
     * Hides/shows a product's quickview link
     * @param {element} $container - the container of the quickview link
     * @param {boolean} show - whether or not the quickview link should be shown
     */
    function toggleQuickview($container, show) {
		var $qv = $container.find('.js-quickview');
		$qv.toggle(show);
	}
    
    function updateLocaleSelection(){
        var locale = $('span#selectedLocale').text();
        $("#" + locale).addClass('selected');
    }
    
    return {
    	quickviewOptions : quickviewOptions,
    	showNotification : showNotification,
        changeProductOption : changeProductOption,
        showFacetMultiselect : showFacetMultiselect,
        toggleQuickview : toggleQuickview,
        getProductOptionData : getProductOptionData,
        updateLocaleSelection: updateLocaleSelection
    }
})($);
