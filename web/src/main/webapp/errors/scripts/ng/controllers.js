var errorControllers = angular.module('errorControllers',[]);


errorControllers.controller('AllUnresolvedErrorsController', ['$scope', 'AllUnresolvedErrorsService',
    function($scope, AllUnresolvedErrorsService) {
            var errorLogArray = AllUnresolvedErrorsService.query();

            for (var i = 0; i < errorLogArray.length; i++) {
                var errLogJsonObject = errorLogArray[i];
                var insertDate = new Date(errLogJsonObject['insertDate']);
                var insertDateString = insertDate.toString();
                errLogJsonObject['insertDate'] = insertDateString;
            }

            $scope.allUnresolvedErrors = errorLogArray;}]);