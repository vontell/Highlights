/* global $,Handlebars */


let update = function(selector) {
    return function(content) {
        $(selector).html(content);
    };
};

let template = function(selector) {
    return Handlebars.compile($(selector).html());
};

$(function(){
    $.ajax({url:"/api/subscribed_videos"})
        .then(template("#videos-template"))
        .then(update("#main"));
});