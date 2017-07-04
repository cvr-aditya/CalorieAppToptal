
angular.module('httpUtil', [])
	.factory('HttpUtil', ['$http', function($http){
		return {
			login: function(payload) {
				return $http.post('/api/auth/login', payload);
			},
			register: function(payload) {
				return $http.post('/api/users/', payload);
			},
			logout: function(headers) {
				return $http.post('/api/auth/logout', {}, { headers: headers });
			},
			updateCalories: function(userId, payload, headers) {
				return $http.put('/api/users/' + userId, payload, { headers: headers });
			},
			getMealsOfUser: function(userId, headers) {
				return $http.get('/api/users/' + userId + "/meals", { headers: headers });
			},
			createMeal: function(payload, headers) {
				return $http.post('/api/meals', payload, { headers: headers });
			},
			updateMeal: function(mealId, payload, headers) {
				return $http.put('/api/meals/' + mealId, payload, { headers: headers });
			},
			deleteMeal: function(mealId, headers) {
				return $http.delete('/api/meals/' + mealId, { headers: headers });
			},
			updateUser: function(userId, payload, headers) {
				return $http.put('/api/users/' + userId, payload, { headers: headers });
			},
			deleteUser: function(userId, headers) {
				return $http.delete('/api/users/' + userId, { headers: headers });
			},
			getUsers: function(headers) {
				return $http.get('/api/users', { headers: headers });
			},
			getMealsByDateFilter: function(userId, fromDate, toDate, headers) {
				return $http.get('/api/users/' + userId + "/meals?filterType=date&from=" + fromDate + "&to=" + toDate,
								{ headers: headers });
			},
			getMealsByTimeFilter: function(userId, fromTime, toTime, headers) {
				return $http.get('/api/users/' + userId + "/meals?filterType=time&from=" + fromTime + "&to=" + toTime,
								{ headers: headers });
			},
			getAllMeals: function(headers) {
				return $http.get('/api/meals', { headers: headers });
			}
		}
	}]);