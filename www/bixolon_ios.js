
var exec = require('cordova/exec');

var BixolonPrint = {
    discovery: function(fnSuccess, fnError) {
        exec(fnSuccess, fnError, "BixolonPrint", "discovery", []);
    },
    connect: function(portName, fnSuccess, fnError) {
        exec(fnSuccess, fnError, "BixolonPrint", "connect", [portName]);
    },
    disconnect: function(fnSuccess, fnError) {
        exec(fnSuccess, fnError, "BixolonPrint", "disconnect", []);
    },
    cutPaper: function(fnSuccess, fnError) {
        exec(fnSuccess, fnError, "BixolonPrint", "cutPaper", []);
    },
    getStatus: function(printStatus, fnSuccess, fnError) {
        exec(fnSuccess, fnError, "BixolonPrint", "getStatus", [printStatus]);
    },
    printText: function(lines, fnSuccess, fnError) {
        exec(fnSuccess, fnError, "BixolonPrint", "printText", [lines]);
    },
    printImage64: function(imageBase64, fnSuccess, fnError) {
        exec(fnSuccess, fnError, "BixolonPrint", "printImage64", [imageBase64]);
    },
    printReceipt: function(imageBase64, lines, fnSuccess, fnError) {
        exec(fnSuccess, fnError, "BixolonPrint", "printReceipt", [imageBase64, lines]);
    }
}

module.exports = BixolonPrint;