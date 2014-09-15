var errorControllers = angular.module('errorControllers',[]);


errorControllers.controller('AllUnresolvedErrorsController', ['$scope', 'AllUnresolvedErrorsService',
    function($scope, AllUnresolvedErrorsService) {
            var jsonResponse = AllUnresolvedErrorsService.query();
                var obj = JSON.parse(jsonResponse);
                $scope.resultMessage = obj.resultMessage;
                var data = obj.data;

            //convert insertDate from epoch time string to JS date string
            if (data.errors.length > 0) {
                        for (var i = 0; i < data.errors.length; i++) {
                            var insertDateEpochTime = data.errors[i].insertDate;
                            var insertDate = new Date(insertDateEpochTime);
                            var insertDateString = insertDate.toString();
                            data.errors[i].insertDate = insertDateString;
                        }
            }

            $scope.allUnresolvedErrors = data;}]);