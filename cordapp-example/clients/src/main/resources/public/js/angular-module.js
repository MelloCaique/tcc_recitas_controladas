"use strict";

const app = angular.module('demoAppModule', ['ui.bootstrap']);

// Fix for unhandled rejections bug.
app.config(['$qProvider', function ($qProvider) {
    $qProvider.errorOnUnhandledRejections(false);
}]);

app.controller('DemoAppController', function($http, $location, $uibModal) {
    const demoApp = this;

    const apiBaseURL = "/api/example/";
    let peers = [];

    $http.get(apiBaseURL + "me").then((response) => demoApp.thisNode = response.data.me);

    $http.get(apiBaseURL + "peers").then((response) => peers = response.data.peers);

    demoApp.openModal = () => {
        const modalInstance = $uibModal.open({
            templateUrl: 'demoAppModal.html',
            controller: 'ModalInstanceCtrl',
            controllerAs: 'modalInstance',
            resolve: {
                demoApp: () => demoApp,
                apiBaseURL: () => apiBaseURL,
                peers: () => peers
            }
        });

        modalInstance.result.then(() => {}, () => {});
    };

    demoApp.reloadModal = () => {
        location.reload()
    };


    demoApp.getIOUs = () => $http.get(apiBaseURL + "ious")
        .then((response) => demoApp.ious = Object.keys(response.data)
            .map((key) => response.data[key].state.data)
            .reverse());

    demoApp.getMyIOUs = () => $http.get(apiBaseURL + "my-ious")
        .then((response) => demoApp.myious = Object.keys(response.data)
            .map((key) => response.data[key].state.data)
            .reverse());

    demoApp.getIOUs();
    demoApp.getMyIOUs();

    console.log("Fora while")
    console.log(demoApp.thisNode)

});

app.controller('ModalInstanceCtrl', function ($http, $location, $uibModalInstance, $uibModal, demoApp, apiBaseURL, peers) {
    const modalInstance = this;

    modalInstance.peers = peers;
    modalInstance.form = {};
    modalInstance.formError = false;

        // Validates and sends IOU.
        modalInstance.create = function validateAndSendIOU() {
            if (modalInstance.form.valorProcedimento <= 0) {
                modalInstance.formError = true;
            } else {
                modalInstance.formError = false;
                $uibModalInstance.close();

                let CREATE_IOUS_PATH = apiBaseURL + "create-iou"

                let createIOUData = $.param({
                    procedimento : modalInstance.form.procedimento,
                    valorProcedimento : modalInstance.form.valorProcedimento,
                    dataOcorrencia : modalInstance.form.dataOcorrencia,
                    nome : modalInstance.form.nome,
                    cpf : modalInstance.form.cpf,

                });

                let createIOUHeaders = {
                    headers : {
                        "Content-Type": "application/x-www-form-urlencoded"
                    }
                };

                // Create IOU  and handles success / fail responses.
                $http.post(CREATE_IOUS_PATH, createIOUData, createIOUHeaders).then(
                    modalInstance.displayMessage,
                    modalInstance.displayMessage
                );
            }
        };

    modalInstance.displayMessage = (message) => {
        const modalInstanceTwo = $uibModal.open({
            templateUrl: 'messageContent.html',
            controller: 'messageCtrl',
            controllerAs: 'modalInstanceTwo',
            resolve: { message: () => message }
        });

        // No behaviour on close / dismiss.
        modalInstanceTwo.result.then(() => {}, () => {});
    };

    // Close create IOU modal dialogue.
    modalInstance.cancel = () => $uibModalInstance.dismiss();

    // Validate the IOU.
    function invalidFormInput() {
        return isNaN(modalInstance.form.value) || (modalInstance.form.counterparty === undefined);
    }
});

// Controller for success/fail modal dialogue.
app.controller('messageCtrl', function ($uibModalInstance, message) {
    const modalInstanceTwo = this;
    modalInstanceTwo.message = message.data;
});


angular.element(function () {
    var verificando = setInterval(changeOrganizationColor, 500);
});



function changeOrganizationColor () {
    //console.log('page loading completed');
    var panels = angular.element(document.querySelectorAll('.changeColor'))
    var seguradora = document.getElementsByClassName('navbar-brand')

    //    console.log(seguradora)
    //    console.log(seguradora[0])
    //    console.log(seguradora[0].innerHTML)
    //    console.log(seguradora[0].innerText)
    // TODO clearInterval(verificando);
       for (var i = 0 ; i < panels.length; i++) {
           //var seguradora = angular.element(document.querySelector('.navbar-brand'))
           //console.log(angular.element(document.querySelector('.navbar-brand')))
           switch (seguradora[0].innerText){
               case "O=Seguradora A, L=Sao Paulo, C=BR":
                   panels[i].style.background = "#b31212"
                   break;
               case "O=Seguradora B, L=Rio de Janeiro, C=BR":
                   panels[i].style.background = "#1128bd"
                   break;
               case "O=Seguradora C, L=Minas Gerais, C=BR":
                   panels[i].style.background = "#0c8a15"
                   break;
               default:
                   //panels[i].style.background = "orange"
           }
    }
};