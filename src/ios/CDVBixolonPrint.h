#import <Foundation/Foundation.h>
#import <Cordova/CDV.h>

#import "UPOSPrinterController.h"

@interface CDVBixolonPrint : CDVPlugin <UPOSDeviceControlDelegate>
{
    UPOSPrinterController *printerController;
    UPOSPrinters *printerList;
    Boolean isInitialized;
}

// Initialization
- (void) initUPOS;
- (void) releaseUPOS;

// Plugin functions
- (void) discovery:(CDVInvokedUrlCommand *)command;
- (void) connect:(CDVInvokedUrlCommand *)command;
- (void) disconnect:(CDVInvokedUrlCommand *)command;

- (void) printText:(CDVInvokedUrlCommand *)command;
- (void) printImage64:(CDVInvokedUrlCommand *)command;

- (void) printReceipt:(CDVInvokedUrlCommand *)command;

- (void) getStatus:(CDVInvokedUrlCommand *)command;
- (void) cutPaper:(CDVInvokedUrlCommand *)command;

// Helper Functions
- (NSInteger) printLines:(NSArray*)lines;
- (NSMutableDictionary*) printerToDict:(UPOSPrinter*)printer;
- (UIImage*) base64ToImage:(NSString*)base64;
- (void) handleResult:(NSInteger)result command:(CDVInvokedUrlCommand *)command successMessage:(nonnull NSString*)successMessage errorMessage:(nonnull NSString*)errorMessage;

@end
