'use strict';

var myApp = angular.module('myApp', ['smart-table','ui.bootstrap','ngRoute','errorServices','errorControllers']);

myApp.config(['$routeProvider',
    function($routeProvider) {
        $routeProvider.when('/recent', {templateUrl: 'errors/partials/results.html', controller: 'ErrorResultsController'})
            .when('/search', {templateUrl: 'errors/partials/searchForm.html', controller: 'SearchController'})
            .when('/results', {templateUrl: 'errors/partials/results.html', controller: 'SearchResultsController'})
            .when('/noresults', {templateUrl: 'errors/partials/noresults.html'})
            .otherwise({redirectTo: 'errors/partials/home.html'});
    }]);