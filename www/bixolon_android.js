var exec = require("cordova/exec");

var BTPrinter = {
    connect: function(str, fnSuccess, fnError) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "connect", [str]);
    },
    cutPaper: function(fnSuccess, fnError) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "cutPaper", []);
    },
    disconnect: function(fnSuccess, fnError) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "disconnect", []);
    },
    list: function(fnSuccess, fnError) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "list", []);
    },
    printBase64: function(str, align, fnSuccess, fnError) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "printBase64", [str, align]);
    },
    printImageUrl: function(str, align, fnSuccess, fnError) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "printImageUrl", [str, align]);
    },
    printPOSCommand: function(str, fnSuccess, fnError) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "printPOSCommand", [str]);
    },
    printQRCode: function(str, fnSuccess, fnError) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "printQRCode", [str]);
    },
    printText: function(str, fnSuccess, fnError) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "printText", [str]);
    },
    setBrand: function(str, fnSuccess, fnError) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "setBrand", [str]);
    },
    status: function(fnSuccess, fnError) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "status", []);
    },
};

module.exports = BTPrinter;
