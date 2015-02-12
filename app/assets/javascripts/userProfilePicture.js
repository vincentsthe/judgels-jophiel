require(["jquery"], function( __tes__ ) {
    $("#avatar-input").unbind("change");
    $("#avatar-input").change(function() {
        $("#avatar-form").submit();
    });
});
