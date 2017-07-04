var app = angular.module('calorieKing', ['httpUtil', 'ngRoute', 'ngCookies', "ngMessages", "ngMaterial",
    "ngAnimate",
    "ngAria",
    "mdPickers"
]);

app.factory('ProfileManager', ['$window', function($window) {

    var factory = {};

    factory.setProfile = function(data) {
        $window.localStorage.setItem("profile", JSON.stringify(data));
        $window.localStorage.setItem("calories", data.targetCalories);
        $window.localStorage.setItem("username", data.name);
        return;
    }

    factory.setTargetCalories = function(calories) {
        $window.localStorage.setItem("calories", calories);
    }

    factory.setUserName = function(username) {
        $window.localStorage.setItem("username", username);
    }

    factory.getEmail = function() {
        var profile = JSON.parse($window.localStorage.getItem("profile"));
        if (!profile) {
            return "";
        }
        return profile.email;
    }

    factory.getUserName = function() {
        return $window.localStorage.getItem("username");
    }

    factory.getRole = function() {
        var profile = JSON.parse($window.localStorage.getItem("profile"));
        if (!profile) {
            return "user";
        }
        return profile.role;
    }

    factory.removeCache = function() {
        $window.localStorage.clear();
        return;
    }

    factory.getTargetCalories = function() {
        return $window.localStorage.getItem("calories");
    }

    factory.getProfile = function() {
        return JSON.parse($window.localStorage.getItem("profile"));
    }

    factory.getUserId = function() {
        return JSON.parse($window.localStorage.getItem("profile")).userId;
    }

    return factory;
}]);

app.config(function($routeProvider) {
    $routeProvider
        .when("/", {
            templateUrl: "../login.html",
            controller: 'LoginController'
        })
        .when("/signUp", {
            templateUrl: "../signup.html",
            controller: 'RegistrationController'
        })
        .otherwise({
            redirectTo: '/'
        });
});


app.controller('LoginController', ['$scope', '$http', '$window', '$location', 'HttpUtil', '$cookies', 'ProfileManager',
    function($scope, $http, $window, $location, HttpUtil, $cookies, ProfileManager) {
		var config = {
			apiKey: "",
			authDomain: "",
			databaseURL: "",
			projectId: "",
			storageBucket: "",
			messagingSenderId: ""
	 	};
		try {
			firebase.initializeApp(config);
		} catch(err) {
			
		}
        $scope.login = function(user) {
            HttpUtil.login(user)
                .success(function(data) {
                    console.log("Login success :: ");
                    ProfileManager.setProfile(data.profile);
                    $cookies.put("X-Auth-Token", data.token);
                    window.location.href = "home.html";
                })
                .error(function(err, status) {
                    swal({
                        title: "Login Failed",
                        text: err.message,
                        timer: 5000,
                        type: "error"
                    });
                });
        }

		$scope.loginWithGoogle = function() {
			var provider = new firebase.auth.GoogleAuthProvider();
			provider.setCustomParameters({
			  'login_hint': 'user@example.com'
			});
			firebase.auth().signInWithPopup(provider).then(function(result) {
				var user = result.user;
				//login success. Register in db if user not exist
				var body = {};
				body.name = result.user.displayName;
				body.email = result.user.email;
				body.role = "user";
				body.password = "";
				body.provider = "google";
				HttpUtil.register(body) // register user in our database 
                    .success(function(data) {
						var u = {};
                    	u.email = result.user.email;
						u.provider = "google";
						HttpUtil.login(u)
							.success(function(data) {
								console.log("Login success :: " + JSON.stringify(data));
								ProfileManager.setProfile(data.profile);
								$cookies.put("X-Auth-Token", data.token);
								window.location.href = "home.html";
							})
							.error(function(err, status) {
								swal({
									title: "Login Failed",
									text: err.message,
									timer: 5000,
									type: "error"
								});
							});
                    })
                    .error(function(err, status) {
                        swal({
                        	title: "Login Failed",
							text: err.message,
							timer: 5000,
							type: "error"
						});
                    });
			}).catch(function(error) {
			  	swal({
					title: "Login Failed",
					text: error.message,
					timer: 5000,
					type: "error"
				});
			});
		}
    }
]);

app.controller('RegistrationController', ['$scope', '$http', '$window', '$location', 'HttpUtil',
    function($scope, $http, $window, $location, HttpUtil) {
        $("#formValidate").validate({
            rules: {
                name: {
                    required: true,
                    minlength: 5
                },
                email: {
                    required: true,
                    email: true
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
                name: {
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
            errorElement: 'div',
            errorPlacement: function(error, element) {
                var placement = $(element).data('error');
                if (placement) {
                    $(placement).append(error)
                } else {
                    error.insertAfter(element);
                }
            }
        });

        $scope.register = function(user) {
            if ($("#formValidate").valid()) {
                user.role = "user";
                HttpUtil.register(user)
                    .success(function(data) {
                        console.log("in success");
                        swal("Registration successful", "", "success");
                        $location.path('/');
                    })
                    .error(function(err, status) {
                        swal({
                            title: "Registration Failed",
                            text: err.message,
                            timer: 8000,
                            type: "error"
                        });
                    });
            }
        }

    }
]);

app.controller('HomeController', ['$scope', '$http', '$window', '$location', 'HttpUtil', '$cookies', 'ProfileManager', '$mdpDatePicker', '$mdpTimePicker', function($scope, $http, $window, $location, HttpUtil, $cookies, ProfileManager, $mdpDatePicker, $mdpTimePicker) {

    $("#addUser").validate({
        rules: {
            name: {
                required: true,
                minlength: 5
            },
            email: {
                required: true,
                email: true
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
            name: {
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
        errorElement: 'div',
        errorPlacement: function(error, element) {
            var placement = $(element).data('error');
            if (placement) {
                $(placement).append(error)
            } else {
                error.insertAfter(element);
            }
        }
    });

    $("#adminAddMealForm").validate({
        rules: {
            adminItemName: {
                required: true,
                minlength: 5
            },
            adminEmail: {
                required: true,
                email: true
            }
        },
        messages: {
            adminItemName: {
                required: "Item Name is mandatory",
                minlength: "Enter at least 5 characters"
            },
            adminEmail: {
                required: "E-Mail is mandatory"
            }
        },
        errorElement: 'div',
        errorPlacement: function(error, element) {
            var placement = $(element).data('error');
            if (placement) {
                $(placement).append(error)
            } else {
                error.insertAfter(element);
            }
        }
    });

    $("#addMealForm").validate({
        rules: {
            itemName: {
                required: true,
                minlength: 5
            }
        },
        messages: {
            itemName: {
                required: "Item Name is mandatory",
                minlength: "Enter at least 5 characters"
            }
        },
        errorElement: 'div',
        errorPlacement: function(error, element) {
            var placement = $(element).data('error');
            if (placement) {
                $(placement).append(error)
            } else {
                error.insertAfter(element);
            }
        }
    });

    $("#adminEditMealForm").validate({
        rules: {
            adminEditItemName: {
                required: true,
                minlength: 5
            },
            adminEditEmail: {
                required: true,
                email: true
            }
        },
        messages: {
            adminEditItemName: {
                required: "Item Name is mandatory",
                minlength: "Enter at least 5 characters"
            },
            adminEditEmail: {
                required: "E-Mail is mandatory"
            }
        },
        errorElement: 'div',
        errorPlacement: function(error, element) {
            var placement = $(element).data('error');
            if (placement) {
                $(placement).append(error)
            } else {
                error.insertAfter(element);
            }
        }
    });

    $("#editMealForm").validate({
        rules: {
            editItemName: {
                required: true,
                minlength: 5
            }
        },
        messages: {
            editItemName: {
                required: "Item Name is mandatory",
                minlength: "Enter at least 5 characters"
            }
        },
        errorElement: 'div',
        errorPlacement: function(error, element) {
            var placement = $(element).data('error');
            if (placement) {
                $(placement).append(error)
            } else {
                error.insertAfter(element);
            }
        }
    });

    $scope.currentDate = new Date();
    $scope.showDatePicker = function(ev) {
        $mdpDatePicker($scope.mealTime, {
            targetEvent: ev
        }).then(function(selectedDate) {
            $scope.mealTime = selectedDate;
        });
    };
    this.filterDate = function(date) {
        return moment(date).date() % 2 == 0;
    };

    this.showTimePicker = function(ev) {
        $mdpTimePicker($scope.mealTime, {
            targetEvent: ev
        }).then(function(selectedDate) {
            $scope.mealTime = selectedDate;
        });
    }

    function isEmptyObject(object) {
        for (key in object) {
            if (object[key] && object[key] != "") {
                return false;
            }
        }
        return true;
    }

    var role = ProfileManager.getRole();
    if ((role.localeCompare("admin") == 0) || (role.localeCompare("user_manager") == 0)) {
        if (role.localeCompare("user_manager") == 0) {
            $("#adminMealsList").hide();
        } else {
            $("#adminMealsList").show();
        }
        $("#usersTab").show();
    } else {
        $("#usersTab").hide();
        $("#adminMealsList").hide();
    }
    $scope.username = ProfileManager.getUserName();
    $scope.logout = function() {
        headers = {};
        headers['Content-Type'] = 'application/json';
        headers['X-Auth-Token'] = $cookies.get('X-Auth-Token');
        HttpUtil.logout(headers)
            .success(function(data) {
                $cookies.remove('X-Auth-Token');
                ProfileManager.removeCache();
                window.location.href = "index.html";
            })
            .error(function(err, status) {
                $cookies.remove('X-Auth-Token');
                ProfileManager.removeCache();
                window.location.href = "index.html";
            });
    }

    $scope.updateCalories = function(calories) {
        swal({
                title: "Be a fitness freak",
                text: "Current Target : " + ProfileManager.getTargetCalories(),
                inputPlaceholder: "Set your target calories per day",
                type: "input",
                showCancelButton: true,
                closeOnConfirm: false,
                showLoaderOnConfirm: true
            },
            function(calories) {
                if (calories === false) return false;
                if (calories === "") {
                    swal.showInputError("Please enter target calories");
                    return false
                }

                var userId = ProfileManager.getUserId();
                var body = {};
                body.targetCalories = parseFloat(calories);
                headers = {};
                headers['Content-Type'] = 'application/json';
                headers['X-Auth-Token'] = $cookies.get('X-Auth-Token');
                HttpUtil.updateCalories(userId, body, headers)
                    .success(function(data) {
                        console.log("updated calories success" + JSON.stringify(data));
                        swal("Yayy!", "You are moving a step forward", "success");
                        ProfileManager.setTargetCalories(data.targetCalories);
                        $("#mealsTable").jsGrid("render");
                        $('#usersTable').jsGrid("render");
                    })
                    .error(function(err, status) {
                        swal({
                                title: "Oops! Failed to udpate calories",
                                text: err.message,
                                timer: 8000,
                                type: "error",
                                showCancelButton: true
                            },
                            function() {
                                if (status == 401) {
                                    window.location.href = "index.html";
                                }
                            });
                    });
            }
        )
    };

    var MyCustomDirectLoadStrategy = function(grid) {
        jsGrid.loadStrategies.DirectLoadingStrategy.call(this, grid);
    };

    MyCustomDirectLoadStrategy.prototype = new jsGrid.loadStrategies.DirectLoadingStrategy();
    MyCustomDirectLoadStrategy.prototype.finishInsert = function(insertedItem) {
        var grid = this._grid;
        grid.option("data").unshift(insertedItem);
        grid.refresh();
    }

    MyCustomDirectLoadStrategy.prototype.finishUpdate = function(udpatedItem) {
        console.log("in finishUpdate callback");
        var grid = this._grid;
        grid.render();
    }

    MyCustomDirectLoadStrategy.prototype.finishDelete = function(deletedItem) {
        var grid = this._grid;
        grid.render();
    }

    //Handler for meals
    $scope.mealsList = [];
    $scope.mealController = {
        loadData: function(filter) {
            console.log("filter meal :" + JSON.stringify(filter));
            if (isEmptyObject(filter) || (filter instanceof Array)) {
                var d = $.Deferred();
                var areDateFiltersApplied = (filter instanceof Array)
                console.log("date filters applied ? " + areDateFiltersApplied);
                var userId = ProfileManager.getUserId();
                headers = {};
                headers['X-Auth-Token'] = $cookies.get('X-Auth-Token');
                HttpUtil.getMealsOfUser(userId, headers)
                    .success(function(data) {
                        $scope.mealsList = data.meals;
                        var targetCalories = parseFloat(ProfileManager.getTargetCalories());
                        var dates = {};
                        for (var i = 0; i < data.meals.length; i++) {
                            var temp = data.meals[i];
                            if (!dates[temp.date]) {
                                dates[temp.date] = temp.calories;
                            } else {
                                dates[temp.date] += temp.calories;
                            }
                        }
                        for (var i = 0; i < data.meals.length; i++) {
                            var tempDate = data.meals[i].date;
                            if (dates[tempDate] <= targetCalories) {
                                data.meals[i].color = "green";
                            } else {
                                data.meals[i].color = "red";
                            }
                        }
                        if (areDateFiltersApplied) {
                            for (var i = 0; i < filter.length; i++) {
                                var tempDate = filter[i].date;
                                if (dates[tempDate] <= targetCalories) {
                                    filter[i].color = "green";
                                } else {
                                    filter[i].color = "red";
                                }
                            }
                            console.log("meals after filter color" + JSON.stringify(filter));
                            d.resolve(filter);
                        } else {
                            console.log("meals after color" + JSON.stringify(data.meals));
                            d.resolve(data.meals);
                        }
                    })
                    .error(function(err, status) {
                        swal({
                                title: "Oops! Failed to fetch meals",
                                text: err.message,
                                timer: 8000,
                                type: "error",
                                showCancelButton: true
                            },
                            function() {
                                if (status == 401) {
                                    window.location.href = "index.html";
                                }
                            });
                    });
                return d.promise();
            } else {
                // Filter from data fetched already
                return $.grep($scope.mealsList, function(client) {
                    return (!filter.date || client.date.indexOf(filter.date) > -1) &&
                        (!filter.itemName || client.itemName.indexOf(filter.itemName) > -1) &&
                        (!filter.time || client.time.indexOf(filter.time) > -1) &&
                        (!filter.calories || client.calories === filter.calories);
                });
            }
        },
        insertItem: function(item, currentDate) {
            console.log("inserting item" + JSON.stringify(item));
            var date = moment(currentDate).format('YYYY-MM-DD HH:mm:ss');
            item.mealTime = date + ".0";
            item.email = ProfileManager.getEmail();
            headers = {};
            headers['X-Auth-Token'] = $cookies.get('X-Auth-Token');
            console.log(JSON.stringify(item));
            HttpUtil.createMeal(item, headers)
                .success(function(data) {
                    $scope.mealsList.push(data);
                    $scope.populateMeals();
                })
                .error(function(err, status) {
                    swal({
                            title: "Oops! Failed to create meal",
                            text: err.message,
                            timer: 8000,
                            type: "error",
                            showCancelButton: true
                        },
                        function() {
                            if (status == 401) {
                                window.location.href = "index.html";
                            }
                        });
                });
        },
        updateItem: function(item, editMealTime) {
            item.userId = ProfileManager.getUserId();
            item.mealTime = moment(editMealTime).format('YYYY-MM-DD HH:mm:ss') + ".0";
            delete item.color;
            console.log("Updating item :" + JSON.stringify(item));
            headers = {};
            headers['X-Auth-Token'] = $cookies.get('X-Auth-Token');
            HttpUtil.updateMeal($scope.editMeal.mealId, item, headers)
                .success(function(data) {
                    $scope.populateMeals();
                })
                .error(function(err, status) {
                    swal({
                            title: "Oops! Failed to update meal",
                            text: err.message,
                            timer: 8000,
                            type: "error",
                            showCancelButton: true
                        },
                        function() {
                            if (status == 401) {
                                window.location.href = "index.html";
                            }
                        });
                });
        },
        deleteItem: function(item) {
            swal({
                    title: "Are you sure?",
                    text: "",
                    type: "warning",
                    showCancelButton: true,
                    confirmButtonColor: "#DD6B55",
                    confirmButtonText: "Yes, delete it!",
                    cancelButtonText: "No, cancel!",
                    closeOnConfirm: false,
                    closeOnCancel: true
                },
                function(isConfirm) {
                    if (isConfirm) {
                        console.log("Deleting item :" + JSON.stringify(item));
                        headers = {};
                        headers['X-Auth-Token'] = $cookies.get('X-Auth-Token');
                        HttpUtil.deleteMeal(item.mealId, headers)
                            .success(function(data) {
                                swal("Deleted!", "", "success");
                                var clientIndex = $.inArray(item, $scope.mealsList);
                                $scope.mealsList.splice(clientIndex, 1);
                                console.log("mealslist after delete" + JSON.stringify($scope.mealsList));
                                $scope.populateMeals();
                            })
                            .error(function(err, status) {
                                swal({
                                        title: "Oops! Failed to delete meal",
                                        text: err.message,
                                        timer: 8000,
                                        type: "error"
                                    },
                                    function() {
                                        if (status == 401) {
                                            window.location.href = "index.html";
                                        }
                                    });
                            });
                    }
                });
        }
    }

    $scope.populateMeals = function() {
        $('#homeList').addClass('active');
        $('#usersList').removeClass('active');
        $('#adminMealsList').removeClass('active');
        $('#usersGrid').hide();
        $('#mealsGrid').show();
        $('#addMealModal').hide();
        $('#editMealModal').hide();
        $('#profileModal').hide();
        $('#adminMealsGrid').hide();
        $('#filterBtn').show();
        $('#showFilterCollapsible').hide();
        $('#mealsTable').show();
        grid = $("#mealsTable").jsGrid({
            width: "100%",
            height: "auto",
            filtering: true,
            inserting: false,
            autoload: true,
            editing: false,
			noDataContent: "No Meals found for User. Add a meal",
            rowClick: function(args) {
                var item = args.item;
                $scope.editMeal = item;
                $('#editMealModal').show();
                $('#filterBtn').hide();
                $('#showFilterCollapsible').hide();
                $('#editItemName').val(item.itemName);
                $('#editCalories').val(item.calories);
                $scope.mealTime = new Date(item.date + " " + item.time + ":00");
                $scope.$apply();
                $('#mealsTable').hide();
                $('#addMealModal').hide();
            },
            sorting: true,
            paging: true,
            pageSize: 7,
            confirmDeleting: false,
            loadStrategy: function() {
                return new MyCustomDirectLoadStrategy(this);
            },
            controller: $scope.mealController,
            rowRenderer: function(item) {
                var user = item;
                $control = $("<td>").addClass("jsgrid-cell jsgrid-control-field jsgrid-align-center " + item.color).attr("style", "width: 50px;");
                var $result = $("<tr>").attr("bgColor", item.color)
                    .append($("<td>").addClass("jsgrid-cell jsgrid-align-center " + item.color).attr("style", "width: 150px;").text(item.itemName))
                    .append($("<td>").addClass("jsgrid-cell jsgrid-align-center " + item.color).attr("style", "width: 100px;").text(item.calories))
                    .append($("<td>").addClass("jsgrid-cell jsgrid-align-center " + item.color).attr("style", "width: 200px;").text(item.time))
                    .append($("<td>").addClass("jsgrid-cell jsgrid-align-center " + item.color).attr("style", "width: 200px;").text(item.date))
                    .append($control);
                return $result;
            },
            fields: [{
                    name: "itemName",
                    type: "text",
                    width: 150,
                    validate: "required",
                    title: "Item Name",
                    align: "center"
                },
                {
                    name: "calories",
                    type: "number",
                    step: "any",
                    width: 100,
                    title: "Calories",
                    align: "center"
                },
                {
                    name: "time",
                    type: "text",
                    width: 200,
                    title: "Time",
                    align: "center"
                },
                {
                    name: "date",
                    type: "text",
                    width: 200,
                    title: "Date",
                    align: "center"
                },
                {
                    type: "control",
                    editButton: false,
                    deleteButton: false
                }
            ]
        });
        document.getElementsByClassName("jsgrid-button jsgrid-mode-button jsgrid-search-mode-button jsgrid-mode-on-button")[0].click();
    }

    $scope.userController = {
        loadData: function(filter) {
            if (isEmptyObject(filter)) {
                console.log(" in load data function" + JSON.stringify(filter));
                var d = $.Deferred();
                headers = {};
                headers['X-Auth-Token'] = $cookies.get('X-Auth-Token');
                HttpUtil.getUsers(headers)
                    .success(function(data) {
                        console.log("users : " + JSON.stringify(data));
                        $scope.usersList = data.users;
                        d.resolve(data.users);
                    })
                    .error(function(err, status) {
                        swal({
                                title: "Oops! Failed to fetch users",
                                text: err.message,
                                timer: 8000,
                                type: "error",
                                showCancelButton: true
                            },
                            function() {
                                if (status == 401) {
                                    window.location.href = "index.html";
                                } else {
                                    d.resolve(item);
                                }
                            });
                    });
                return d.promise();
            } else {
                return $.grep($scope.usersList, function(client) {
                    return (!filter.email || client.email.indexOf(filter.email) > -1) &&
                        (!filter.name || client.name.indexOf(filter.name) > -1) &&
                        (!filter.role || client.role.indexOf(filter.role) > -1) &&
                        (!filter.targetCalories || client.targetCalories === filter.targetCalories);
                });
            }
        },
        insertItem: function(item) {
            console.log("inserting user");
            headers = {};
            headers['X-Auth-Token'] = $cookies.get('X-Auth-Token');
            item.role = "user";
            delete item.cpassword;
            console.log(JSON.stringify(item));
            HttpUtil.register(item, headers)
                .success(function(data) {
                    $("#usersTable").jsGrid("render");
                    $('#addUserModal').closeModal();
                    $scope.usersList.push(data);
                    $scope.user = undefined;
                })
                .error(function(err, status) {
                    swal({
                            title: "Oops! Failed to create user",
                            text: err.message,
                            timer: 8000,
                            type: "error",
                            showCancelButton: true
                        },
                        function() {
                            if (status == 401) {
                                window.location.href = "index.html";
                            }
                        });
                });
        },
        updateItem: function(item) {
            var d = $.Deferred();
            console.log("Updating user :" + JSON.stringify(item));
            headers = {};
            headers['X-Auth-Token'] = $cookies.get('X-Auth-Token');
            HttpUtil.updateUser(item.userId, item, headers)
                .success(function(data) {
                    if (item.email === ProfileManager.getEmail()) {
                        $scope.username = item.name;
                        ProfileManager.setUserName(item.name);
                        ProfileManager.setTargetCalories(item.targetCalories);
                        ProfileManager.setProfile(data);
                    }
                    d.resolve(data);
                    $scope.populateUsers();
                })
                .error(function(err, status) {
                    swal({
                            title: "Oops! Failed to update user",
                            text: err.message,
                            timer: 8000,
                            type: "error",
                            showCancelButton: true
                        },
                        function() {
                            if (status == 401) {
                                window.location.href = "index.html";
                            } else {
                                d.resolve(item);
                            }
                        });
                });
            return d.promise();
        },
        deleteItem: function(item) {
            var d = $.Deferred();
            swal({
                    title: "Are you sure?",
                    text: "",
                    type: "warning",
                    showCancelButton: true,
                    confirmButtonColor: "#DD6B55",
                    confirmButtonText: "Yes, delete!",
                    cancelButtonText: "No, cancel!",
                    closeOnConfirm: false,
                    closeOnCancel: true
                },
                function(isConfirm) {
                    if (isConfirm) {
                        console.log("Deleting user :" + JSON.stringify(item));
                        headers = {};
                        headers['X-Auth-Token'] = $cookies.get('X-Auth-Token');
                        HttpUtil.deleteUser(item.userId, headers)
                            .success(function(data) {
                                swal("Deleted!", "", "success");
                                var clientIndex = $.inArray(item, $scope.usersList);
                                $scope.usersList.splice(clientIndex, 1);
                                d.resolve(data);
                            })
                            .error(function(err, status) {
                                swal({
                                        title: "Oops! Failed to delete user",
                                        text: err.message,
                                        timer: 8000,
                                        type: "error",
                                        showCancelButton: true
                                    },
                                    function() {
                                        if (status == 401) {
                                            window.location.href = "index.html";
                                        } else {
                                            d.resolve(item);
                                        }
                                    });
                            });
                    } else {
                        d.resolve(item);
                    }
                });
            return d.promise();
        }
    };

    $scope.updateProfile = function(item) {
        console.log("Updating profile :" + JSON.stringify(item));
        headers = {};
        headers['X-Auth-Token'] = $cookies.get('X-Auth-Token');
        HttpUtil.updateUser(item.userId, item, headers)
            .success(function(data) {
                if (item.email === ProfileManager.getEmail()) {
                    $scope.username = item.name;
                    ProfileManager.setUserName(item.name);
                    ProfileManager.setTargetCalories(item.targetCalories);
                    ProfileManager.setProfile(data);
                }
                $scope.populateMeals();
            })
            .error(function(err, status) {
                swal({
                        title: "Oops! Failed to update user",
                        text: err.message,
                        timer: 8000,
                        type: "error",
                        showCancelButton: true
                    },
                    function() {
                        if (status == 401) {
                            window.location.href = "index.html";
                        }
                    });
            });
    }

    $scope.showProfileModal = function() {
        $scope.profile = ProfileManager.getProfile();
        profile.name = ProfileManager.getUserName();
        $('#profileModal').show();
        $('#mealsGrid').hide();
        $('#adminMealsGrid').hide();
        $("#usersGrid").hide();
        $('#filterBtn').hide();
        $('#showFilterCollapsible').hide();
    }

    $scope.populateUsers = function() {
        $('#homeList').removeClass('active');
        $('#usersList').addClass('active');
        $('#adminMealsList').removeClass('active');
        $('#usersGrid').show();
        $('#mealsGrid').hide();
        $('#adminMealsGrid').hide();
        $('#addMealModal').hide();
        $('#filterBtn').hide();
        $('#profileModal').hide();
        $("#usersTable").jsGrid({
            width: "100%",
            height: "auto",
            filtering: true,
            inserting: false,
            autoload: true,
            editing: true,
            sorting: true,
            paging: true,
            pageSize: 7,
            confirmDeleting: false,
            modeSwitchButton: false,
            loadStrategy: function() {
                return new MyCustomDirectLoadStrategy(this);
            },
            controller: $scope.userController,
            fields: [{
                    name: "name",
                    type: "text",
                    width: 200,
                    validate: "required",
                    title: "User Name",
                    align: "center"
                },
                {
                    name: "email",
                    type: "text",
                    width: 200,
                    title: "E-Mail",
                    editing: false,
                    align: "center"
                },
                {
                    name: "role",
                    type: "text",
                    width: 100,
                    title: "Role",
                    editing: false,
                    align: "center"
                },
                {
                    name: "targetCalories",
                    type: "number",
                    width: 50,
                    title: "Target Calories",
                    align: "center"
                },
                {
                    type: "control"
                }
            ]
        });
        document.getElementsByClassName("jsgrid-button jsgrid-mode-button jsgrid-search-mode-button jsgrid-mode-on-button")[0].click();
    }
    $scope.populateMeals();

    $scope.displayAddMeal = function() {
        $scope.meal = undefined;
        $scope.currentDate = new Date();
        $('#addMealModal').show();
        $('#mealsTable').hide();
        $('#editMealModal').hide();
        $('#filterBtn').hide();
        $('#showFilterCollapsible').hide();
    }

    $scope.displayAdminAddMeal = function() {
        $scope.meal = undefined;
        $scope.currentDate = new Date();
        $('#adminAddMealModal').show();
        $('#adminMealsTable').hide();
        $('#adminEditMealModal').hide();
        $('#filterBtn').hide();
    }

    $scope.adminMealController = {
        loadData: function(filter) {
            if (isEmptyObject(filter)) {
                var d = $.Deferred();
                headers = {};
                headers['X-Auth-Token'] = $cookies.get('X-Auth-Token');
                HttpUtil.getAllMeals(headers)
                    .success(function(data) {
                        console.log("meals : " + JSON.stringify(data));
                        $scope.adminMealsList = data.meals;
                        d.resolve(data.meals);
                    })
                    .error(function(err, status) {
                        swal({
                                title: "Oops! Failed to fetch meals",
                                text: err.message,
                                timer: 8000,
                                type: "error",
                                showCancelButton: true
                            },
                            function() {
                                if (status == 401) {
                                    window.location.href = "index.html";
                                } else {
                                    d.resolve();
                                }
                            });
                    });
                return d.promise();
            } else {
                return $.grep($scope.adminMealsList, function(client) {
                    return (!filter.email || client.email.indexOf(filter.email) > -1) &&
                        (!filter.itemName || client.itemName.indexOf(filter.itemName) > -1) &&
                        (!filter.date || client.date.indexOf(filter.date) > -1) &&
                        (!filter.time || client.time.indexOf(filter.time) > -1) &&
                        (!filter.calories || client.calories === filter.calories);
                });
            }
        },
        insertItem: function(item, currentDate) {
            var date = moment(currentDate).format('YYYY-MM-DD HH:mm:ss');
            item.mealTime = date + ".0";
            headers = {};
            headers['X-Auth-Token'] = $cookies.get('X-Auth-Token');
            console.log("inserting item" + JSON.stringify(item));
            HttpUtil.createMeal(item, headers)
                .success(function(data) {
                    $scope.mealsList.push(data);
                    $scope.populateAdminMeals();
                    $scope.adminMealsList.push(data);
                })
                .error(function(err, status) {
                    swal({
                            title: "Oops! Failed to create meal",
                            text: err.message,
                            timer: 8000,
                            type: "error",
                            showCancelButton: true
                        },
                        function() {
                            if (status == 401) {
                                window.location.href = "index.html";
                            }
                        });
                });
        },
        updateItem: function(item, adminMealTime) {
            item.mealTime = moment(adminMealTime).format('YYYY-MM-DD HH:mm:ss') + ".0";
            delete item.color;
            console.log("Updating item :" + JSON.stringify(item));
            headers = {};
            headers['X-Auth-Token'] = $cookies.get('X-Auth-Token');
            HttpUtil.updateMeal(item.mealId, item, headers)
                .success(function(data) {
                    $scope.populateAdminMeals();
                })
                .error(function(err, status) {
                    swal({
                            title: "Oops! Failed to update meal",
                            text: err.message,
                            timer: 8000,
                            type: "error",
                            showCancelButton: true
                        },
                        function() {
                            if (status == 401) {
                                window.location.href = "index.html";
                            }
                        });
                });
        },
        deleteItem: function(item) {
            var d = $.Deferred();
            swal({
                    title: "Are you sure?",
                    text: "",
                    type: "warning",
                    showCancelButton: true,
                    confirmButtonColor: "#DD6B55",
                    confirmButtonText: "Yes, delete it!",
                    cancelButtonText: "No, cancel!",
                    closeOnConfirm: false,
                    closeOnCancel: true
                },
                function(isConfirm) {
                    if (isConfirm) {
                        console.log("Deleting item :" + JSON.stringify(item));
                        headers = {};
                        headers['X-Auth-Token'] = $cookies.get('X-Auth-Token');
                        HttpUtil.deleteMeal(item.mealId, headers)
                            .success(function(data) {
                                swal("Deleted!", "", "success");
                                var clientIndex = $.inArray(item, $scope.mealsList);
                                $scope.adminMealsList.splice(clientIndex, 1);
                                d.resolve(data);
                            })
                            .error(function(err, status) {
                                swal({
                                        title: "Oops! Failed to delete meal",
                                        text: err.message,
                                        timer: 8000,
                                        type: "error",
                                        showCancelButton: true
                                    },
                                    function() {
                                        if (status == 401) {
                                            window.location.href = "index.html";
                                        } else {
                                            grid.deleteFailed = true;
                                            d.resolve();
                                        }
                                    });
                            });
                    } else {
                        grid.deleteFailed = true;
                        d.resolve();
                    }
                });
            return d.promise();
        }
    }

    $scope.populateAdminMeals = function() {
        $('#homeList').removeClass('active');
        $('#usersList').removeClass('active');
        $('#adminMealsList').addClass('active');
        $('#adminMealsGrid').show();
        $('#usersGrid').hide();
        $('#mealsGrid').hide();
        $('#profileModal').hide();
        $('#adminAddMealModal').hide();
        $('#adminEditMealModal').hide();
        $('#adminMealsTable').show();
        $('#filterBtn').hide();
        $('#showFilterCollapsible').hide();
        $("#adminMealsTable").jsGrid({
            width: "100%",
            height: "auto",
            filtering: true,
            inserting: false,
            autoload: true,
            editing: false,
            sorting: true,
            paging: true,
            pageSize: 7,
            confirmDeleting: false,
            rowClick: function(args) {
                var item = args.item;
                $('#adminEditMealModal').show();
                $('#filterBtn').hide();
                $scope.adminEditMeal = item;
                $scope.adminMealTime = new Date(item.date + " " + item.time + ":00");
                $scope.$apply();
                $('#adminMealsTable').hide();
                $('#adminAddMealModal').hide();
            },
            loadStrategy: function() {
                return new MyCustomDirectLoadStrategy(this);
            },
            controller: $scope.adminMealController,
            fields: [{
                    name: "email",
                    type: "text",
                    width: 200,
                    validate: "required",
                    title: "E-Mail",
                    align: "center"
                },
                {
                    name: "itemName",
                    type: "text",
                    width: 200,
                    title: "Item Name",
                    editing: false,
                    align: "center"
                },
                {
                    name: "calories",
                    type: "number",
                    width: 50,
                    title: "Calories",
                    editing: false,
                    align: "center"
                },
                {
                    name: "time",
                    type: "text",
                    width: 50,
                    title: "Time",
                    align: "center"
                },
                {
                    name: "date",
                    type: "text",
                    width: 70,
                    title: "Date",
                    align: "center"
                },
                {
                    type: "control",
                    editButton: false
                }
            ]
        });
        document.getElementsByClassName("jsgrid-button jsgrid-mode-button jsgrid-search-mode-button jsgrid-mode-on-button")[0].click();
    }

    $scope.filterByDate = function() {
        var fromDate = moment($scope.filterFromDate).format('YYYY-MM-DD');
        var toDate = moment($scope.filterToDate).format('YYYY-MM-DD');
        headers = {};
        headers['X-Auth-Token'] = $cookies.get('X-Auth-Token');
        console.log("from date and to date are : " + fromDate + " " + toDate);
        var userId = ProfileManager.getUserId();
        HttpUtil.getMealsByDateFilter(userId, fromDate, toDate, headers)
            .success(function(data) {
                console.log("date filter " + JSON.stringify(data.meals));
                $("#mealsTable").jsGrid("loadData", data.meals);
            })
            .error(function(err, status) {
                swal({
                    title: "Failed to apply filters",
                    text: err.message,
                    timer: 8000,
                    type: "error"
                });
                $scope.populateMeals();
            });
    }

    $scope.filterByTime = function() {
        var fromTime = moment($scope.filterFromDate).format('HH:mm');
        var toTime = moment($scope.filterToDate).format('HH:mm');
        headers = {};
        headers['X-Auth-Token'] = $cookies.get('X-Auth-Token');
        console.log("from time and to time are : " + fromTime + " " + toTime);
        var userId = ProfileManager.getUserId();
        HttpUtil.getMealsByTimeFilter(userId, fromTime, toTime, headers)
            .success(function(data) {
                console.log("time filter " + JSON.stringify(data.meals));
                $("#mealsTable").jsGrid("loadData", data.meals);
            })
            .error(function(err, status) {
                swal({
                    title: "Failed to apply filters",
                    text: err.message,
                    timer: 8000,
                    type: "error"
                });
                $scope.populateMeals();
            });
    }
}]);