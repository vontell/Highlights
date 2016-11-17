/* global $,Handlebars */


let update = function(selector) {
    return function(content) {
        $(selector).html(content);
    };
};

$(function(){
    let template = Handlebars.compile($("#videos-template").html());
    
    console.log(template);
    
    $.ajax({url:"/api/subscribed_videos"}).then(template).then(update("#main"));
    
});