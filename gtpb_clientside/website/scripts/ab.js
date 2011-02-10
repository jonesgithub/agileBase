
		function  loadFeedControl() {
			var feed  = "http://blog.agilebase.co.uk/?feed=rss2";
				var options = {
				numResults : 6,
						displayTime : 15000,
						title : "<a href='http://blog.agilebase.co.uk'>announcements</a>",
						linkTarget : google.feeds.LINK_TARGET_SELF
				};
			var fg = new GFdynamicFeedControl(feed, "announce", options);
		}

		$(document).ready(function(){
			// frame buster needed as we may be in one of the agileBase application panes after a login timeout
			if(top != self) {
				top.location.replace("AppController.servlet?return=gui/display_application");
			}
			//if (($("#oViewPane").length != 0) || (document.location.href.indexOf("logout") > -1)) {
			//	top.location="http://agilebase.co.uk/start";
			//}
			// focus the 'username' box for logging in
			$("#j_username").focus();
			try {
				// start the RSS headlines from blog
				google.setOnLoadCallback(loadFeedControl);
			} catch(err) {
			}
			// test for iPhone
			if((navigator.userAgent.match(/iPhone/i)) || (navigator.userAgent.match(/iPod/i)) || (navigator.userAgent.match(/iPad/i))) {
				if(document.location.href.indexOf("mobile") == -1) {
					$('#login').html('<big>Mobile or tablet users log in at<br><a href="http://www.agilebase.co.uk/mobile">www.agilebase.co.uk/mobile</a></big>');
				}
			}
			// if enter is pressed in username field with no password, don't submit details
			$("#loginform").submit(function() {
				if($('#j_password').val() == '') {
					return false;
				} else {
					return true;
				} 
			});
			// just in time etc. tooltips
			$("#just_in a").each(function(i) {
				$(this).mouseover(function() {
					$(this).parent().find(".just_tooltip").fadeIn('normal');
				});
			});
			$(".just_tooltip").mouseout(function() {
				$(this).fadeOut();
			});
	        var screenshots = new flickrshow('screenshots', {
	            'autoplay':true,
	            'hide_buttons':true,
	            'interval':5000,
	            'page':1,
	            'per_page':20,
	            'user':'okohll'
	        });
		}); 

		function showSection(sectionName) {
			$("#navigation").find("a").removeClass("current");
			$("#nav_" + sectionName).addClass("current");
			$(".detail").fadeOut("fast");
			$("#" + sectionName).fadeIn("fast");
		}

