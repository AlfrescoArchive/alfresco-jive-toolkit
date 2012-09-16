/**
 * Todo:
 * 
 * Internet explorer
 * - Internet explorer doesn't support background-size css property, so 
 * "loading images" don't view correctly when opening a directory
 * - If directory/file name is too long, it wraps over text beneath it 
 */

(function($j) {
	var FileChooser = function(originalDomElement, url) {
		var thisFileChooser = this;
		
		// Executed when file is selected
		var callBackFunction = null;
		
		var directorySelectable = false;
		var fetchDirectories = false;
		
		var fileSelectable = false;
		var fetchFiles = false;
		
		/////////
		// DOM //
		/////////
		var $originalDomElement = $j(originalDomElement);
		var labelSearch = "Filter       "; // Spaces are important - user could be searching for "Filter"
		var labelSearchButton = "Search";
		var labelLoading = "Loading...";
		var labelClearSearch = "Clear search criteria";
		var labelLoadMore = "Load more...";
		var $inputSearch;
		var $btnSearch;
		
		///////////////
		// CONSTANTS //
		///////////////
		
		var DATA_KEY = "fileChooser_key"; // string
		var DATA_EXPANDED = "fileChooser_expanded"; // boolean
		
		var CLASS_LOADING = "fileChooser_loading";
		var CLASS_CHILDREN = "fileChooser_children";
		var CLASS_DIRECTORY = "fileChooser_directory";
		var CLASS_DIRECTORY_NAME = "fileChooser_directoryName";
		var CLASS_EXPAND_BUTTON = "fileChooser_expandButton";
		var CLASS_EXPANDED_FALSE = "fileChooser_expandedFalse";
		var CLASS_EXPANDED_TRUE = "fileChooser_expandedTrue";
		var CLASS_FILE = "fileChooser_file";
		var CLASS_SELECTABLE = "fileChooser_selectable";
		var CLASS_HOVER = "_mouseOver";
		var CLASS_SEARCH_INPUT = "fileChooser_searchInput";
		var CLASS_NO_CONTENT = "_noContent";
		var CLASS_SEARCH_BUTTON = "fileChooser_searchButton";
		var CLASS_LOAD_MORE = "fileChooser_loadMore";
		var CLASS_SEARCH_CLEAR = "fileChooser_clearSearch";
		
		/**
		 * Constructor.  Code execution starts here.  Must be explicitly
		 * called after instantiation.  First set any properties via
		 * getters/setters before constructing.
		 */
		this.construct = function() {
			thisFileChooser._constructDOM();
			
			// Pull first set of data from url
			thisFileChooser.getData(null);
			
			thisFileChooser._registerEvents();
			
			return thisFileChooser; // Return this object for JavaScript chaining
		};
		
		/**
		 * Constructs necessary DOM for this widget.
		 * Includes:
		 *   - Search input box
		 *   - Search button
		 */
		this._constructDOM = function() {
			$inputSearch = $j(document.createElement('input'));
			$inputSearch.addClass(CLASS_SEARCH_INPUT);
			$inputSearch.attr("type", "text");
			$inputSearch.val(labelSearch);
			$inputSearch.addClass(CLASS_SEARCH_INPUT + CLASS_NO_CONTENT);
			
			$btnSearch = $j(document.createElement('input'));
			$btnSearch.addClass(CLASS_SEARCH_BUTTON);
			$btnSearch.attr("type", "button");
			$btnSearch.val(labelSearchButton);
			
			$originalDomElement.append($inputSearch);
			$originalDomElement.append($btnSearch);
		};
		
		/**
		 * Registers events for static content for widget.
		 * Includes:
		 *  - Focus/Blur of search input box
		 *  - Click of search button 
		 */
		this._registerEvents = function() {
			$inputSearch.focus(function() {
				if ($inputSearch.val() == labelSearch) {
					$inputSearch.val("");
					$inputSearch.removeClass(CLASS_SEARCH_INPUT + CLASS_NO_CONTENT);
				}
			});
			
			$inputSearch.blur(function() {
				if ($inputSearch.val() == "") {
					$inputSearch.val(labelSearch);
					$inputSearch.addClass(CLASS_SEARCH_INPUT + CLASS_NO_CONTENT);
				}
			});
			
			$inputSearch.keypress(function(e) {
				var code = (e.keyCode ? e.keyCode : e.which);
				if(code == 13) { // ENTER
					thisFileChooser.search($inputSearch.val());
				}
			});
			
			$btnSearch.click(function() {
				thisFileChooser.search($inputSearch.val());
			});
		};
		
		/**
		 * Makes an ajax call to the server to the previously given url.
		 * Parameter "key" is used if a parent is provided.
		 */
		this.getData = function($parent, firstRecord, searchString) {
			// Parameters used an ajax request
			var key = null;
			if ($parent != null) {
				key = $parent.data(DATA_KEY);
			}
			else {
				$parent = $originalDomElement;
			}
			
			var params = {
					firstRecord: firstRecord,
					searchString: searchString,
					remoteContainerId: key,
					fetchDirectories: fetchDirectories,
					fetchFiles: fetchFiles
			};
			
			// Loading bar
			$divLoading = $j(document.createElement('div'));
			$divLoading.append(labelLoading);
			$parent.append($divLoading);
			
			// Ajax
			jQuery.ajax({
				url: url,
				data: params,
				dataType: "json",
				success: function(data, textStatus, jqXHR) {
					$parent.children("." + CLASS_EXPAND_BUTTON).removeClass(CLASS_LOADING);
					$parent.children("." + CLASS_EXPAND_BUTTON).addClass(CLASS_EXPANDED_TRUE);
					
					$divLoading.remove();
					
					thisFileChooser.addContent(
						$parent,
						data.count,
						data.directories,
						data.files,
						searchString);
				}
			});
		};
		
		/**
		 * Remove all the dynamic DOM elements
		 */
		this.removeAllData = function() {
			$originalDomElement.children("." + CLASS_CHILDREN).remove();
			$originalDomElement.children("." + CLASS_SEARCH_CLEAR).remove();
		};
		
		/**
		 * Search for files/folders for the given searchString if a search string is specified.
		 */
		this.search = function(searchString) {
			if ($inputSearch.val() != "" && $inputSearch.val() != labelSearch) {
				var params = {
					searchString: searchString,
					fetchDirectories: fetchDirectories,
					fetchFiles: fetchFiles
				};
				
				thisFileChooser.removeAllData();
				
				jQuery.ajax({
					url: url,
					data: params,
					dataType: "json",
					success: function(data, textStatus, jqXHR) {
						var $divClearSearch = $j(document.createElement('div'));
						$divClearSearch.append(labelClearSearch);
						$divClearSearch.addClass(CLASS_SEARCH_CLEAR);
						$originalDomElement.append($divClearSearch);
						$divClearSearch.click(function() {
							$divClearSearch.remove();
							thisFileChooser.removeAllData();
							$inputSearch.val(labelSearch);
							$inputSearch.addClass(CLASS_SEARCH_INPUT + CLASS_NO_CONTENT);
							thisFileChooser.getData();
						});
						
						thisFileChooser.addContent(
							$originalDomElement,
							data.count,
							data.directories,
							data.files,
							searchString
						);
					}
				});
			}
		};
		
		/**
		 * Adds content to the DOM and registers events for that new content
		 * 
		 * @param {Object} $parent
		 * @param {Object} listDirectoryKeys
		 * @param {Object} listDirectoryValues
		 * @param {Object} listFileKeys
		 * @param {Object} listFileValues
		 */
		this.addContent = function($parent, count, listDirectories, listFiles, searchString) {
			var $divChildren = $parent.children("." + CLASS_CHILDREN);
			if ($divChildren.length == 0) {
				$divChildren = $j(document.createElement('div'));
				$divChildren.addClass(CLASS_CHILDREN);
			}
			
			// First, add directories
			for (var c=0; c<listDirectories.length; c++) {
				var directoryKey = listDirectories[c].id;
				var directoryValue = listDirectories[c].name;

				var $divDirectory = $j(document.createElement('div'));
				$divDirectory.addClass(CLASS_DIRECTORY);
				$divDirectory.data(DATA_KEY, directoryKey);
				
				var $divDirectoryExpandButton = $j(document.createElement('div'));
				$divDirectoryExpandButton.addClass(CLASS_EXPAND_BUTTON);
				$divDirectoryExpandButton.addClass(CLASS_EXPANDED_FALSE);
				$divDirectory.append($divDirectoryExpandButton);
				thisFileChooser._registerExpandButtonEvents($divDirectoryExpandButton);
				
				var $divDirectoryName = $j(document.createElement('div'));
				$divDirectoryName.addClass(CLASS_DIRECTORY_NAME);
				$divDirectoryName.html(directoryValue);
				$divDirectoryName.data(DATA_KEY, directoryKey);
				$divDirectory.append($divDirectoryName);
				thisFileChooser._registerDirectoryEvents($divDirectoryName);
				
				$divChildren.append($divDirectory);
			}
			
			// Second, add files
			for (var c=0; c<listFiles.length; c++) {
				var fileKey = listFiles[c].id;
				var fileValue = listFiles[c].name;
				
				var $divFile = $j(document.createElement('div'));
				$divFile.addClass(CLASS_FILE);
				$divFile.data(DATA_KEY, fileKey);
				$divFile.append(fileValue);
				thisFileChooser._registerFileEvents($divFile);
				
				$divChildren.append($divFile);
			}
			
			// Last, add a "more" link if there's more content that can be loaded (pagination)
			var currentFileCount = 
				$divChildren.children("." + CLASS_DIRECTORY).length +
				$divChildren.children("." + CLASS_FILE).length;
			if (currentFileCount < count) {
				$divLoadMoreDirectories = $j(document.createElement('div'));
				$divLoadMoreDirectories.addClass(CLASS_LOAD_MORE);
				$divLoadMoreDirectories.append(labelLoadMore);
				
				$divChildren.append($divLoadMoreDirectories);
				
				$divLoadMoreDirectories.click(function() {
					$divLoadMoreDirectories.remove();
					thisFileChooser.getData($parent, currentFileCount, searchString);
				});
			}
			
			$parent.append($divChildren);
		};
		
		/**
		 * Registers an event listener on an expand button
		 * 
		 * @param {Object} $divExpandButton
		 */
		this._registerExpandButtonEvents = function($divExpandButton) {
			$divExpandButton.click(function(e) {
				if ($divExpandButton.data(DATA_EXPANDED) == null) {
					$divExpandButton.removeClass(CLASS_EXPANDED_FALSE);
					$divExpandButton.addClass(CLASS_LOADING);
			
					thisFileChooser.getData($divExpandButton.parent());
					$divExpandButton.data(DATA_EXPANDED, true);
				}
				else if ($divExpandButton.data(DATA_EXPANDED) == false) {
					$divExpandButton.parent().children("."+CLASS_CHILDREN).show();
					$divExpandButton.data(DATA_EXPANDED, true);
					
					$divExpandButton.removeClass(CLASS_EXPANDED_FALSE);
					$divExpandButton.addClass(CLASS_EXPANDED_TRUE);
				}
				else {
					$divExpandButton.parent().children("."+CLASS_CHILDREN).hide();
					$divExpandButton.data(DATA_EXPANDED, false);
					
					$divExpandButton.removeClass(CLASS_EXPANDED_TRUE);
					$divExpandButton.addClass(CLASS_EXPANDED_FALSE);
				}
			});
			
			$divExpandButton.hover(
				// mouseOver
				function() {
					$divExpandButton.addClass(CLASS_EXPAND_BUTTON + CLASS_HOVER);
				},
				// mouseOut
				function() {
					$divExpandButton.removeClass(CLASS_EXPAND_BUTTON + CLASS_HOVER);
				}
			);
		};
		
		/**
		 * Register (only if files are selectable) mouse events: click 
		 * and rollover
		 * 
		 * @param {Object} $divFile
		 */
		this._registerFileEvents = function($divFile) {
			if (fileSelectable) {
				$divFile.addClass(CLASS_SELECTABLE);
				
				$divFile.click(function() {
					if (callBackFunction != null) {
						callBackFunction($divFile.data(DATA_KEY));
					}
				});
				
				$divFile.hover(
					// mouseOver
					function() {
						$divFile.addClass(CLASS_FILE + CLASS_HOVER);
					},
					// mouseOut
					function() {
						$divFile.removeClass(CLASS_FILE + CLASS_HOVER);
					}
				);
			}
		};
		
		
		/**
		 * Register (only if directories are selectable) 
		 * mouse events: click and rollover
		 * 
		 * @param {Object} $divFile
		 */
		this._registerDirectoryEvents = function($divDirectory) {
			if (directorySelectable) {
				$divDirectory.addClass(CLASS_SELECTABLE);
				
				$divDirectory.click(function() {
					if (callBackFunction != null) {
						callBackFunction($divDirectory.data(DATA_KEY));
					}
				});
				
				$divDirectory.hover(
					// mouseOver
					function() {
						$divDirectory.addClass(CLASS_DIRECTORY + CLASS_HOVER);
					},
					// mouseOut
					function() {
						$divDirectory.removeClass(CLASS_DIRECTORY + CLASS_HOVER);
					}
				);
			}
		};
	
		///////////////////////
		// GETTERS / SETTERS //
		///////////////////////
		
		// Callback function
		this.setCallBackFunction = function(x){
			callBackFunction = x;
			return thisFileChooser;
		};
		
		// File selectable
		this.isFileSelectable = function() {
			return fileSelectable;
		};
		this.setFileSelectable = function(x) {
			fileSelectable = x;
			return thisFileChooser;
		};
		
		// Fetch files
		this.isFetchFiles = function() {
			return fetchFiles;
		}
		this.setFetchFiles = function(x) {
			fetchFiles = x;
			return thisFileChooser;
		}
		
		// Directory selectable
		this.isDirectorySelectable = function() {
			return directorySelectable;
		};
		this.setDirectorySelectable = function(x) {
			directorySelectable = x;
			return thisFileChooser;
		};
		
		// Fetch directories
		this.isFetchDirectories = function() {
			return fetchDirectories;
		}
		this.setFetchDirectories = function(x) {
			fetchDirectories = x;
			return thisFileChooser;
		}
		
		// Label inside the search input box
		this.getLabelSearch = function() {
			return labelSearch;
		};
		this.setLabelSearch = function(x) {
			labelSearch = x;
			return thisFileChooser;
		};
		
		// Label for search button
		this.getLabelSearchButton = function() {
			return labelSearchButton;
		};
		this.setLabelSearchButton = function(x) {
			labelSearchButton = x;
			return thisFileChooser;
		};
		
		// Label for loading
		this.getLabelLoading = function() {
			return labelLoading;
		}
		this.setLabelLoading = function(x) {
			labelLoading = x;
			return thisFileChooser;
		}
		
		// Label - clear search
		this.getLabelClearSearch = function() {
			return labelClearSearch;
		}
		this.setLabelClearSearch = function(x) {
			labelClearSearch = x;
			return thisFileChooser;
		}
		
		// Label - load more
		this.getLabelLoadMore = function() {
			return labelLoadMore;
		}
		this.setLabelLoadMore = function(x) {
			labelLoadMore = x;
			return thisFileChooser;
		}
	}
	
	$j.fn.fileChooser = function(url) {
		var DATA_FILE_CHOOSER = "fileChooser";
		
		var fileChooser;
		
		this.each(function() {
			var $this = $j(this);
			
			fileChooser = $this.data(DATA_FILE_CHOOSER);
			if (fileChooser == null) {
				fileChooser = new FileChooser(this, url);
				$this.data(DATA_FILE_CHOOSER, fileChooser);
			}
		});
		
		return fileChooser;
	};
})(jQuery);