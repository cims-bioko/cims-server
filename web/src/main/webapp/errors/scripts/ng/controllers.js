var errorControllers = angular.module('errorControllers',[]);

errorControllers.controller('RecentErrorsController', ['$scope', '$rootScope', 'RecentErrorsService',
    function($scope, $rootScope, RecentErrorsService) {
        $rootScope.errors = RecentErrorsService.query();}]);

errorControllers.controller('SearchController', ['$scope', '$rootScope', '$location', 'ErrorQueryService', function($scope, $rootScope, $location, ErrorQueryService) {
    $scope.maxDate= new Date();
    $scope.resolutionStatus = "";

    $scope.entityType = [
        {name:"IndividualForm.class", label:"Individual Form"},
        {name:"LocationForm.class", label:"Location Form"},
        {name:"VisitForm.class", label:"Visit Form"},
        {name:"PregnancyObservationForm.class", label:"Pregnancy Observation Form"},
        {name:"", label:""}
    ];

    $scope.endToday = function() {
        $scope.endDate = new Date();
    };

    $scope.setSevenDayWindow = function() {

        var newStartDate = new Date();
        newStartDate.setDate($scope.endDate.getDate() - 7);
        $scope.startDate = newStartDate;

    };

    $scope.endToday();
    $scope.setSevenDayWindow();

    //MM/dd/yyyy
    function cleanDate(date) {
        var year = date.getFullYear();
        var month = (1 + date.getMonth()).toString();
        month = month.length > 1 ? month : '0' + month;
        var day = date.getDate().toString();
        day = day.length > 1 ? day : '0' + day;
        return month + "-"+day+"-"+year;
    }

    $scope.submit = function() {

        var cleanStartDate = cleanDate($scope.startDate);
        var cleanEndDate = cleanDate($scope.endDate);

        if ("" === $scope.resolutionStatus) {
            if ("" === $scope.entitySelection) {
                //base query without resolutionStatus or entityType
                ErrorQueryService.query(
                    {minDate : cleanStartDate,
                        maxDate : cleanEndDate}, function(response) {
                            $rootScope.errors = response;
                            console.log(response);
                            $location.path('/results');
                    });
            } else {
                //base query without resolutionStatus
                ErrorQueryService.query(
                    {entityType : $scope.entitySelection,
                        minDate : cleanStartDate,
                        maxDate : cleanEndDate}, function(response) {
                            $rootScope.errors = response;
                            console.log(response);
                            $location.path('/results');
                    });
            }
        } else if ("" === $scope.entitySelection){
            //base query without entityType
            ErrorQueryService.query(
                {resolutionStatus : $scope.resolutionStatus,
                    minDate : cleanStartDate,
                    maxDate : cleanEndDate}, function(response) {
                        $rootScope.errors = response;
                        console.log(response);
                        $location.path('/results');
                });
        } else {
            //base query
            ErrorQueryService.query(
                {resolutionStatus : $scope.resolutionStatus,
                    entityType : $scope.entitySelection,
                    minDate : cleanStartDate,
                    maxDate : cleanEndDate}, function(response) {
                        $rootScope.errors = response;
                        console.log(response);
                        $location.path('/results');
                });
        }
    };

}]);