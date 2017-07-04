var app = angular.module('calorieKing', ['httpUtil']);

app.controller('RegistrationController',['$scope', '$http', '$window', 'HttpUtil', function ($scope, $http, $window, HttpUtil) {
	$("#formValidate").validate({
        rules: {
            name: {
                required: true,
                minlength: 5
            },
            email: {
                required: true,
                email:true
            },
            password: {
				required: true,
				minlength: 5
			},
			cpassword: {
				required: true,
				minlength: 5,
				equalTo: "#password"
			}
        },
        messages: {
            name:{
                required: "Username is mandatory",
                minlength: "Enter at least 5 characters"
            },
			email: {
				required: "E-Mail is mandatory"	
			},
			password: {
				required: "Password is mandatory"	
			},
            cpassword: {
				equalTo: "Passwords do not match"
			}
        },
        errorElement : 'div'
    });
		
	$scope.register = function(user) {
		if ($("#formValidate").valid()) {
			user.role = "user";
			HttpUtil.register(user)
				.success(function(data) {
					console.log("in success");
					console.log(JSON.stringify(data));
				})
				.error(function(err, status) {
					swal({   
						title: "Registration Failed",   
						text: err.message,   
						timer: 5000,
						type: "warning"
					});
				});
		}
	}
	
}]);
