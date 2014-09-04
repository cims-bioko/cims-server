var errorControllers = angular.module('errorControllers',[]);


errorControllers.controller('AllUnresolvedErrorsController', ['$scope', 'AllUnresolvedErrorsService',
    function($scope, AllUnresolvedErrorsService) {
            var data = AllUnresolvedErrorsService.query();
            var errorLogArray = data['errors'];

            for (var i = 0; i < errorLogArray.length; i++) {
                var errLogJsonObject = errorLogArray[i];
                var insertDate = new Date(errLogJsonObject['insertDate']);
                var insertDateString = insertDate.toString();
                errLogJsonObject['insertDate'] = insertDateString;
            }

            $scope.allUnresolvedErrors = errorLogArray;}]);