'use strict';

var myApp = angular.module('myApp', ['ui.bootstrap','ngRoute','errorServices','errorControllers']);

myApp.config(['$routeProvider',
    function($routeProvider) {
        $routeProvider.when('/all/unresolved', {templateUrl: 'errors/partials/errors-all-unresolved.html', controller: 'AllUnresolvedErrorsController'})
            .when('/search', {templateUrl: 'errors/partials/searchForm.html', controller: 'SearchController'})
            .otherwise({redirectTo: 'partials/home.html'});
    }]);