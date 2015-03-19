var errorServices = angular.module('errorServices', ['ngResource']);

errorServices.factory('RecentErrorsService', ['$resource',
    function($resource){
        return $resource('/openhds/api/rest/errors.json', {}, {
            query: {method:'GET', params:{resolution:'Unresolved'}}
        });
    }]);

errorServices.factory('ErrorQueryService', ['$resource', function($resource) {

    console.log("Error query service entered");
    return $resource("/openhds/api/rest/errors.json", {}, {
        query: {method: 'GET', isArray: false}
    });

}]);