'use strict';

var myApp = angular.module('myApp', ['ngRoute','errorServices','errorControllers']);

myApp.config(['$routeProvider',
    function($routeProvider) {
        $routeProvider.when('/all/unresolved', {templateUrl: 'errors/partials/errors-all-unresolved.html', controller: 'AllUnresolvedErrorsController'})
        .otherwise({redirectTo: 'partials/home.html'});
}]);