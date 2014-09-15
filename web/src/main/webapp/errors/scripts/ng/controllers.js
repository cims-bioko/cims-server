var errorControllers = angular.module('errorControllers',[]);


errorControllers.controller('AllUnresolvedErrorsController', ['$scope', 'AllUnresolvedErrorsService',
    function($scope, AllUnresolvedErrorsService) {
            var jsonResponse = AllUnresolvedErrorsService.query();
            $scope.resultMessage = jsonResponse.resultMessage;
            var errors = jsonResponse.data.errors;

            //convert insertDate from epoch time string to JS date string
            if (errors.length > 0) {
                        for (var i = 0; i < errors.length; i++) {
                            var insertDateEpochTime = errors[i].insertDate;
                            var insertDate = new Date(insertDateEpochTime);
                            var insertDateString = insertDate.toString();
                            errors[i].insertDate = insertDateString;
                        }
            }

            $scope.allUnresolvedErrors = errors;}]);