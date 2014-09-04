var errorControllers = angular.module('errorControllers',[]);


errorControllers.controller('AllUnresolvedErrorsController', ['$scope', 'AllUnresolvedErrorsService',
    function($scope, AllUnresolvedErrorsService) {
            var jsonResponse = AllUnresolvedErrorsService.query();

            //convert insertDate from epoch time string to JS date string
            if (jsonResponse.data.errors.length > 0) {
                        for (var i = 0; i < jsonResponse.data.errors.length; i++) {
                            var insertDateEpochTime = jsonResponse.data.errors[i].insertDate;
                            var insertDate = new Date(insertDateEpochTime);
                            var insertDateString = insertDate.toString();
                            jsonResponse.data.errors[i].insertDate = insertDateString;
                        }
            }

            $scope.allUnresolvedErrors = jsonResponse;}]);