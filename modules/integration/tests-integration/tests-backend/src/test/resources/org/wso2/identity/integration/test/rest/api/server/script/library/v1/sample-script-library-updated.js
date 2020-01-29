function getAge(birthDate) {
    var today = new Date();
    var age = today.getFullYear() - birthDate.getFullYear();
    var m = today.getMonth() - birthDate.getMonth();

    if (m < 0 || (m === 0 && today.getDate() <    birthDate.getDate())) {
        age--;
    }

    return age;
}

var validateDOB = function (dob) {
    console.log("Updated");
};

module.exports.getAge = getAge;
module.exports.validateDOB = validateDOB;
