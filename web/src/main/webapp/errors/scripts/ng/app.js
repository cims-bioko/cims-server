'use strict';

var myApp = angular.module('myApp', ['smart-table','ui.bootstrap','ngRoute','errorServices','errorControllers']);

myApp.config(['$routeProvider',
    function($routeProvider) {
        $routeProvider.when('/recent', {templateUrl: 'errors/partials/results.html', controller: 'RecentResultsController'})
            .when('/search', {templateUrl: 'errors/partials/searchForm.html', controller: 'SearchController'})
            .when('/results', {templateUrl: 'errors/partials/results.html', controller: 'SearchResultsController'})
            .when('/noresults', {templateUrl: 'errors/partials/noresults.html'})
            .when('/details', {templateUrl: 'errors/partials/details.html', controller: 'DetailsController'})
            .when('/home', {templateUrl: 'errors/partials/home.html'})
            .otherwise({redirectTo: '/home'});
    }]);

myApp.run(function($rootScope) {
    $rootScope.cleanDate = function(date) {

        var year = date.getFullYear();
        var month = (1 + date.getMonth()).toString();
        month = month.length > 1 ? month : '0' + month;
        var day = date.getDate().toString();
        day = day.length > 1 ? day : '0' + day;
        return month + "-"+day+"-"+year;

    };

    $rootScope.cleanDateFromString = function(string) {
        var date = new Date(string);
        var year = date.getFullYear();
        var month = (1 + date.getMonth()).toString();
        month = month.length > 1 ? month : '0' + month;
        var day = date.getDate().toString();
        day = day.length > 1 ? day : '0' + day;
        return month + "-"+day+"-"+year;
    }

});