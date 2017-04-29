$(function () {
    var list = $("#origin-url-list");

    function addOrigin(url, urlDigest) {
        var li = $("<li>");
        var a = $("<a>");
        a.attr("href", "/res/" + urlDigest);
        a.attr("target", "_blank");
        a.text(url);
        li.append(a);
        list.append(li);
    }

    $("#clone-button").click(function () {
        var originUrl = $("#origin-url").val();
        $.ajax({
            url: "/clone",
            method: "POST",
            data: {
                url: originUrl
            },
            dataType: "text",
            success: function (data) {
                if (data) {
                    addOrigin(originUrl, data);
                }
            }
        });
    });

    $.ajax({
        url: "/origins",
        method: "GET",
        dataType: "json",
        success: function (data) {
            if (data && data.length) {
                data.forEach(i => addOrigin(i.url, i.urlDigest));
            } else {
                console.log("No origin.")
            }
        }
    });
});