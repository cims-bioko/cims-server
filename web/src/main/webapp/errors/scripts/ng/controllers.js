var errorControllers = angular.module('errorControllers',[]);

errorControllers.controller('RecentResultsController', ['$scope', '$rootScope', '$location', 'ErrorQueryService',
    function($scope, $rootScope, $location, ErrorQueryService) {

        ErrorQueryService.query(function(returnData) {
            $rootScope.errors = returnData;
            if ($rootScope.errors.data.errors.length == 0) {
                $location.path('/noresults');
            };
        });

        //onClick function for viewing a specific error
        $scope.details = function(selected) {
            ErrorQueryService.query({uuid: selected}, function(result) {
                console.log(result);
                $rootScope.errorDetail = result.data.error;
                $location.path('/details');
            });
        };

    }]);

errorControllers.controller('SearchResultsController', ['$scope', '$rootScope', '$location', 'ErrorQueryService',
    function($scope, $rootScope, $location, ErrorQueryService) {

        $scope.details = function(selected) {
            ErrorQueryService.query({uuid: selected}, function(result) {
                console.log(result);
                $rootScope.errorDetail = result.data.error;
                $location.path('/details');
            });
        };
    }]);

errorControllers.controller('DetailsController', ['$scope', '$rootScope', '$location', 'ErrorQueryService',
    function($scope, $rootScope, $location, ErrorQueryService) {

        //use x2js to convert error data payload to json
        var x2js = new X2JS();
        $scope.payload = x2js.xml_str2json($rootScope.errorDetail.dataPayload);

        //var cleanDate = new Date($rootScope.errorDetail.insertDate);
        $rootScope.errorDetail.insertDate = $rootScope.cleanDateFromString($rootScope.errorDetail.insertDate);

    }]);

errorControllers.controller('SearchController', ['$scope', '$rootScope', '$location', 'ErrorQueryService',
    function($scope, $rootScope, $location, ErrorQueryService) {
    $scope.fieldWorkerExtId = "";
    $scope.statusSelection = "";
    $scope.entitySelection = "";

    $scope.statusList = [
        {name: "", label: "All"},
        {name: "Modified ExtId", label: "Modified ExtId"}
    ];

    $scope.entityType = [
        {name:"", label:"All"},
        {name:"IndividualForm", label:"Individual Form"},
        {name:"LocationForm", label:"Location Form"},
        {name:"VisitForm", label:"Visit Form"},
        {name:"PregnancyObservationForm", label:"Pregnancy Observation Form"}
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

        var cleanStartDate = $rootScope.cleanDate($scope.startDate);
        var cleanEndDate = $rootScope.cleanDate($scope.endDate);

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

}]);