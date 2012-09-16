var JiveChooseContainer = $Class.extend({
   
/*
* Initialize the JiveChooseContainer object.
* @elementID The id of the choose container list - jive-choose-containers-result-block
* @contentType The type of content -  1 for thread, 102 for document
* @chooseContainerURL The url to reload the choose container list - action 'choose-container-list'
* @currentViewClass The css class for the currently selected view - jive-choose-container-currentview
* @queryElementID The id of the query input element - jive-choose-container-query
* @resultListElementID The id of the results list element - jive-choose-container-resultlist
* @writeDocumentText The text for no search query results - i18n key 'content.picker.crtNwCnt.button'
* @uploadDocumentText The text for no search query results - i18n key 'content.picker.upldNwCnt.button'
* @queryResultsEmptyText The text for no search query results - i18n key 'ctr.choose.search.emty'
*/
    init: function(options) {
        // Transfer values from the given options object to instance variables.
        var that = this;
        [
            'suggestionsContainer',
            'contentType',
            'chooseContainerURL',
            'currentViewClass',
            'queryElement',
            'resultListElement',
            'i18n'
        ].forEach(function(param) {
            that[param] = options[param];
        });
        $j(this.queryElement).keyup(this._observeQuery.bind(this)).focus();
    },

    _reload: function(params) {
        var finalParams = $j.param(this.addParams(params));
        $j(this.suggestionsContainer).load(this.chooseContainerURL, finalParams);
    },

    setView: function(elem, view) {
        if (!$j(elem).hasClass(this.currentViewClass)) {
            if (view == 'search') {
                $j('#jive-choose-container-searchbox').show();
                $j(this.queryElement).val('').focus();
            } else {
                $j('#jive-choose-container-searchbox').hide();
            }
            $j('.' + this.currentViewClass).removeClass(this.currentViewClass);
            $j(elem).addClass(this.currentViewClass);
            this._reload({
                'contentType': this.contentType,
                'view': view
            });
        }
    },

    _setQuery: function(wildcard) {
        var query = this._queryElementValue();
        if (query.length >= 3) {
            if (wildcard) {
                query = query + '*';
            }
            this._reload({
                'contentType': this.contentType,
                'view': 'search',
                'query': query
            });
        } else {
            $j(this.resultListElement).html($j('</p>').text(this.i18n.queryResultsEmptyText));
        }
    },

    submitForm: function(containerType, containerID) {
        $j('#jive-container-type').val(containerType);
        $j('#jive-container-id').val(containerID);
        $j('#choosejivecontainerform').submit();
        return false;
    },

    addParams: function(params) {
        return params;
    },

    _observeQuery: function(event) {
        switch(event.keyCode) {
            case jive.Event.KEY_RETURN:
                this._clearQueryEvent();
                this._setQuery(false);
                event.preventDefault();
                event.stopPropagation();
                return;
            case jive.Event.KEY_LEFT:
            case jive.Event.KEY_RIGHT:
            case jive.Event.KEY_TAB:
            case jive.Event.KEY_ESC:
            case jive.Event.KEY_UP:
            case jive.Event.KEY_DOWN:
            case jive.Event.KEY_HOME:
            case jive.Event.KEY_END:
            case jive.Event.KEY_PAGEUP:
            case jive.Event.KEY_PAGEDOWN:
                return;
        }

        this._clearQueryEvent();
        this.queryEvent = setTimeout(this._setWildcardQuery.bind(this), 300);
    },

    _setWildcardQuery: function() {
        this._setQuery(true);
    },

    _clearQueryEvent: function() {
        if (this.queryEvent) {
            clearTimeout(this.queryEvent);
        }
    },

    _queryElementValue: function() {
        return $j(this.queryElement).val();
    },

    selectWriteContent: function() {
        $j('#jive-content-collab').attr('checked', 'checked');
        $j('#jive-content-type-write').addClass('jive-choose-content-type-selected');
        $j('#jive-content-type-upload').removeClass('jive-choose-content-type-selected');
        $j('#jive-selected-doctype').text(this.i18n.writeDocumentText);
    },

    selectUpload: function() {
        $j('#jive-content-upload').attr('checked', 'checked');
        $j('#jive-content-type-upload').addClass('jive-choose-content-type-selected');
        $j('#jive-content-type-write').removeClass('jive-choose-content-type-selected');
        $j('#jive-selected-doctype').text(this.i18n.uploadDocumentText);
    }
});
