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
function generateNewPassword(){
    $.ajax({
        type: "GET",
        url: "/admin-portal/root/apis/identityStore-micro-service/generatePassword",
        success: function (result) {
            $("#newPassword").val(result);
        }
    });
}
function displayVals() {
    if ($("#verificationSelector option:selected").text() === "With password") {
        var fillingObject = {};
        var callbacks = {
            onSuccess: function () {
                initScript();
            },
            onFailure: function (e) {
            }
        };
        UUFClient.renderFragment("password-generation", fillingObject,
            "accountVerificationMethod-area", "OVERWRITE", callbacks);
    }
    else if ($("#verificationSelector option:selected").text() === "With email/phone verification") {
        var fillingObject = {};
        var callbacks = {
            onSuccess: function () {
            },
            onFailure: function (e) {
            }
        };
        UUFClient.renderFragment("email-verification", fillingObject,
            "accountVerificationMethod-area", "OVERWRITE", callbacks);
    }
}
document.getElementById("inputUsername").onblur = function() {userNameExists()};
function userNameExists() {
    var username = document.getElementById('inputUsername').value;
    if (!username) {
        return;
    }
    var usernameClaimUri = "http://wso2.org/claims/username";
    var domain = null;
    if (document.getElementById('domainSelector')) {
        domain = document.getElementById('domainSelector').value;
    }
    $.ajax({
        type: "GET",
        url: "/admin-portal/root/apis/identityStore-micro-service/userExists",
        data: {username: username, usernameClaimUri: usernameClaimUri, domain: domain},
        success: function (data) {
            if (data === "true") {
                var fillingObject = {
                    "userExists": true
                };
                var callbacks = {
                    onSuccess: function () {
                        $("#addUser").prop('disabled', true);
                    },
                    onFailure: function (e) {
                    }
                };
                UUFClient.renderFragment("user-existance", fillingObject,
                    "userExistsError-area", "OVERWRITE", callbacks);
            } else {
                var fillingObject = {};
                var callbacks = {
                    onSuccess: function () {
                        $("#addUser").prop('disabled', false);
                    },
                    onFailure: function (e) {
                    }
                };
                UUFClient.renderFragment("user-existance", fillingObject,
                    "userExistsError-area", "OVERWRITE", callbacks);
            }
        }
    });
}
$("#addUserForm").validate({
    rules: {
        confirmPassword: {
            equalTo: "#newPassword",
            required: {
                depends: function (element) {
                    return $("#newPassword").is(":not(:blank)");
                }
            }
        },
        newPassword: {
            required: true
        },
        inputUsername:{
            required: true
        }
    },
    messages: {
        confirmPassword: {
            equalTo: "These passwords do not match.",
            required: "Please re-enter the new password."
        },
        newPassword: {
            required: "Required to provide a new password."
        },
        inputUsername:{
            required: "Required to provide a username"
        }

    }
});


