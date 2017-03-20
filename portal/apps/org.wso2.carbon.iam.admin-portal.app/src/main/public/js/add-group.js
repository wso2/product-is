$(document).ready(function () {
    $('#domainSelector option[value=' + $('#domainSelector').attr('data-primary') + ']').prop('selected', 'selected');
    var primaryDomain = $('#domainSelector').val();
    $("#domain").val(primaryDomain);
    //load domain drop down when page loads
    displayVals();
    $('#domainSelector').change(function () {
        var domain = $(this).val();
        $("#domain").val(domain);
    });
    $('#verificationSelector').change(displayVals);
});
document.getElementById("input-groupname").onblur = function () {
    groupNameExists()
};
function groupNameExists() {
    var groupName = document.getElementById('input-groupname').value;
    if (!groupName) {
        return;
    }
    var groupNameClaimUri = "http://wso2.org/claims/groupname";
    var domain = null;
    if (document.getElementById('domainSelector')) {
        domain = document.getElementById('domainSelector').value;
    }
    $.ajax({
        type: "GET",
        url: "/admin-portal/root/apis/identityStore-micro-service/groupExists",
        data: {groupName: groupName, groupNameClaimUri: groupNameClaimUri, domain: domain},
        success: function (data) {
            if (data === "true") {
                var fillingObject = {
                    "groupExists": true
                };
                var callbacks = {
                    onSuccess: function () {
                        $("#addGroup").prop('disabled', true);
                    },
                    onFailure: function (e) {
                    }
                };
                UUFClient.renderFragment("group-existence", fillingObject,
                    "groupExistsError-area", "OVERWRITE", callbacks);
            } else {
                var fillingObject = {};
                var callbacks = {
                    onSuccess: function () {
                        $("#addGroup").prop('disabled', false);
                    },
                    onFailure: function (e) {
                    }
                };
                UUFClient.renderFragment("group-existence", fillingObject,
                    "groupExistsError-area", "OVERWRITE", callbacks);
            }
        }
    });
}
$("#addGroupForm").validate({
    rules: {
        inputGroupName: {
            required: true
        }
    },
    messages: {
        inputUsername: {
            required: "Required to provide a group name"
        }

    }
});


