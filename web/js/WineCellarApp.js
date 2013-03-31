var app = angular.module('WineCellar', ['ngResource']);

app.config(['$routeProvider', function($routeProvider) {
    $routeProvider
        .when('/wine', {templateUrl: 'view_welcome.html'})
        .when('/wine/:id', {templateUrl: 'view_detail.html', controller: ViewCtrl})
        .otherwise({redirectTo:'/wine'});
    }]);

app.factory('WineResource', function($resource) {
    return $resource('grest/h2tab/wine/:id');
});

function GlobalCtrl($scope, WineResource) {
    
    $scope.queryList = function() {
        $scope.winelist = WineResource.query({order: "winename"});
    };
    
    $scope.addWine = function () {
        window.location = "#/wine/new";
    };
    
    $scope.queryList();
}

function ViewCtrl($scope, $routeParams, WineResource) {

    $scope.wine = new WineResource();
    
    var id = $routeParams.id;
    if (id !== "new") {
        var list = $scope.winelist;
        var flag = false;
        for (var i = 0; i < list.length; i++) {
            if (list[i].ID == id) {
                $scope.wine = list[i];
                flag = true;
            }
        }   
        if (!flag) {
            $scope.wine = WineResource.get({id: id});
        }
    }
    
    $scope.saveWine = function () {
        $scope.wine.$save(function () {
            $scope.queryList();
            window.location = "#/wine";
        });
    };

    $scope.deleteWine = function () {
        WineResource.remove({id: id}, function() {
            $scope.queryList();
            window.location = "#/wine";
        });
    };

}