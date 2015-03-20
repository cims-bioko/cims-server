var errorControllers = angular.module('errorControllers',[]);

errorControllers.controller('ErrorResultsController', ['$scope', '$rootScope', '$location', 'ErrorQueryService',
    function($scope, $rootScope, $location, ErrorQueryService) {

        ErrorQueryService.query({resolutionStatus : 'Unresolvedd'}), function(returnData) {
            $rootScope.errors = returnData;
            if ($rootScope.errors.data.length == 0) {
                $location.path('/noresults');
            }
        };

        $scope.details = function(selected) {
            ErrorQueryService.query({uuid: selected}, function(result) {
                console.log(result);
                $rootScope.errorDetail = result.data[0];
                $location.path('/details');
            });
        };

    }]);

errorControllers.controller('SearchResultsController', ['$scope', '$rootScope', '$location', 'ErrorQueryService',
    function($scope, $rootScope, $location, ErrorQueryService) {

        $scope.details = function(selected) {
            ErrorQueryService.query({uuid: selected}, function(result) {
                console.log(result);
                $rootScope.errorDetail = result.data[0];
                $location.path('/details');
            });
        };
    }]);

errorControllers.controller('SearchController', ['$scope', '$rootScope', '$location', 'ErrorQueryService', function($scope, $rootScope, $location, ErrorQueryService) {
    $scope.fieldWorkerExtId = "";
    $scope.statusSelection = "";
    $scope.entitySelection = "";

    $scope.statusList = [
        {name: "Modified ExtId", label: "Modified ExtId"},
        {name: "", label: "All"}
    ]

    $scope.entityType = [
        {name:"IndividualForm", label:"Individual Form"},
        {name:"LocationForm", label:"Location Form"},
        {name:"VisitForm", label:"Visit Form"},
        {name:"PregnancyObservationForm", label:"Pregnancy Observation Form"},
        {name:"", label:"All"}
    ];


    $scope.maxDate= new Date();
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


    $scope.submit = function() {

        var cleanStartDate = cleanDate($scope.startDate);
        var cleanEndDate = cleanDate($scope.endDate);

        ErrorQueryService.query(
            {resolutionStatus : $scope.statusSelection,
                entityType : $scope.entitySelection,
                fieldWorkerExtId : $scope.fieldWorkerExtId,
                minDate : cleanStartDate,
                maxDate : cleanEndDate}, function(response) {
                    $rootScope.errors = response;
                    console.log(response);
                    $location.path('/results');
            });

    };

    //MM/dd/yyyy
    function cleanDate(date) {
        var year = date.getFullYear();
        var month = (1 + date.getMonth()).toString();
        month = month.length > 1 ? month : '0' + month;
        var day = date.getDate().toString();
        day = day.length > 1 ? day : '0' + day;
        return month + "-"+day+"-"+year;
    }

}]);