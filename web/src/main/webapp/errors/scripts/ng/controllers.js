var errorControllers = angular.module('errorControllers',[]);


errorControllers.controller('AllUnresolvedErrorsController', ['$scope', 'AllUnresolvedErrorsService',
    function($scope, AllUnresolvedErrorsService) {

            $scope.jsonResponse = AllUnresolvedErrorsService.query();
            $scope.jsonResponse.$promise.then(function (result) {
                $scope.jsonResponse = result;
            })

            $scope.resultMessage = $scope.jsonResponse.resultMessage;

            var errors = $scope.jsonResponse.data.errors;

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