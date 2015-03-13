var errorControllers = angular.module('errorControllers',[]);


errorControllers.controller('AllUnresolvedErrorsController', ['$scope', 'AllUnresolvedErrorsService',
    function($scope, AllUnresolvedErrorsService) {
        $scope.allUnresolvedErrors = AllUnresolvedErrorsService.query();}]);

errorControllers.controller('SearchController', ['$scope', '$location', 'ErrorQueryService', function($scope) {
    $scope.maxDate= new Date();

    $scope.endToday = function() {
        $scope.endDate = new Date();
    };

    $scope.setSevenDayWindow = function() {

        var newStartDate = new Date();
        newStartDate.setDate($scope.endDate - 7);
        $scope.startDate = newStartDate;

    };

    $scope.endToday();
    $scope.setSevenDayWindow();

    $scope.submit = function($scope, $location, ErrorQueryService) {

        //validate input

        var result = ErrorQueryService.query(
            {resolutionStatus : $scope.resolutionStatus,
            entityType : $scope.entityType,
            minDate : $scope.startDate,
            maxDate : $scope.endDate}, function($location) {
                $location.path('/all/unresolved');
            });


    }

}]);