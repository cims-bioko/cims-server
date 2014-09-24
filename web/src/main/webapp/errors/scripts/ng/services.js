var errorServices = angular.module('errorServices', ['ngResource']);

errorServices.factory('AllUnresolvedErrorsService', ['$resource',
    function($resource){
        return $resource('/openhds/api/rest/errors.json', {}, {
            query: {method:'GET', params:{resolution:'Unresolved'}},
        });
    }]);