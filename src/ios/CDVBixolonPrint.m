#import "UPOSPrinterController.h"
#import "CDVBixolonPrint.h"

@implementation CDVBixolonPrint

const char ESC_CHAR = 0x1B;

NSString* BIXOLON_ALIGN_LEFT = @"[LEFT]";
NSString* BIXOLON_ALIGN_CENTER = @"[CENTER]";
NSString* BIXOLON_ALIGN_RIGHT = @"[RIGHT]";
NSString* BIXOLON_FONT_A = @"[FONTA]";
NSString* BIXOLON_FONT_B = @"[FONTB]";
NSString* BIXOLON_FONT_C = @"[FONTC]";
NSString* BIXOLON_TEXT_BOLD = @"[BOLD]";
NSString* BIXOLON_TEXT_UNDERLINE = @"[UNDERLINE]";
NSString* BIXOLON_TEXT_NORMAL = @"[NORMAL]";
NSString* BIXOLON_TEXT_SMALL = @"[SMALL]";
NSString* BIXOLON_TEXT_MEDIUM = @"[MEDIUM]";
NSString* BIXOLON_CUT_FEED = @"[CUT]";

CDVPluginResult *pluginResult = nil;

- (void) initUPOS
{
    NSLog(@"[CDVBixolonPrint] initUPOS");
    printerController = [[UPOSPrinterController alloc]init];
    printerList = [[UPOSPrinters alloc]init];
    
    [printerController setLogLevel: LOG_SHOW_NEVER ];
    [printerController setTextEncoding:NSASCIIStringEncoding];
    
    // Delegate events
    printerController.delegate = self;
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didBTStart:) name:__NOTIFICATION_NAME_BT_WILL_LOOKUP_ object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didBTDeviceList:) name:__NOTIFICATION_NAME_BT_FOUND_PRINTER_ object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didBTComplete:) name:__NOTIFICATION_NAME_BT_LOOKUP_COMPLETE_ object:nil];
}

- (void) releaseUPOS
{
    NSLog(@"[CDVBixolonPrint] releaseUPOS");
    // TODO
}

// Controller events
- (void) didBTStart:(NSNotification*)notification {}
- (void) didBTComplete:(NSNotification*)notification {}
- (void) didBTDeviceList:(NSNotification*)notification {
    UPOSPrinter* lookupDevice = (UPOSPrinter*)[[notification userInfo] objectForKey:__NOTIFICATION_NAME_BT_FOUND_PRINTER_];
    if( lookupDevice == nil) return; [printerList addDevice:lookupDevice];
}
-(void)StatusUpdateEvent:(NSNumber*)Status {}

- (void) discovery:(CDVInvokedUrlCommand *)command
{
    NSLog(@"[CDVBixolonPrint] discovery");
    NSMutableArray *devices = [[NSMutableArray alloc] init];
    
    if (!isInitialized) {
        [self initUPOS];
    }
    
    [printerController refreshBTLookup];
    
    sleep(1);
    
    if([[printerList getList] count] > 0) {
        NSLog(@"[CDVBixolonPrint] One or more printers were found");
        // Build Disctionary list
        
        for (UPOSPrinter *p in [printerList getList]) {
           [devices addObject:[self printerToDict:p]];
        }
        
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:devices];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    } else {
        NSLog(@"[CDVBixolonPrint] No printers found");
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"No printers were found"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

- (void) connect:(CDVInvokedUrlCommand *)command
{
    NSLog(@"[CDVBixolonPrint] connect");
    if (!isInitialized) {
        [self initUPOS];
    }
    
    NSString* targetModelName = [command.arguments objectAtIndex:0];
    
    if ([targetModelName isEqualToString:@""]) {
        NSLog(@"[CDVBixolonPrint] Empty model name received, choosing first target in list instead.");
        UPOSPrinter* target = (UPOSPrinter*)[printerList getList].lastObject;
        targetModelName = target.modelName;
    }
    
    NSInteger result = [printerController open:targetModelName];
    
    if (result == UPOS_SUCCESS) {
        if ([printerController claim:5000] == UPOS_SUCCESS) {
            [NSThread sleepForTimeInterval:0.1f];
            [printerController setDeviceEnabled:YES];
        }
    }
    
    [self handleResult:result command:command successMessage:@"Connected" errorMessage:@"Failed to connect"];
}

- (void) disconnect:(CDVInvokedUrlCommand *)command
{
    NSLog(@"[CDVBixolonPrint] disconnect");
    printerController.DeviceEnabled = NO;
    NSInteger result = [printerController releaseDevice];
    
    if (result == UPOS_SUCCESS) {
        NSLog(@"[CDVBixolonPrint] Disconnected");
        [NSThread sleepForTimeInterval:0.01f];
        [printerController close];
    }
    
    [self handleResult:result command:command successMessage:@"Disconnected" errorMessage:@"Failed to disconnect"];
}

- (void) printText:(CDVInvokedUrlCommand *)command
{
    NSLog(@"[CDVBixolonPrint] printText");
    NSArray* lines = [command.arguments objectAtIndex:0];
    lines = [lines arrayByAddingObject:@""];
    NSInteger result = [self printLines:lines];
    [self handleResult:result command:command successMessage:@"Text printed" errorMessage:@"Failed to print text"];
}

- (void) cutPaper:(CDVInvokedUrlCommand *)command
{
    NSLog(@"[CDVBixolonPrint] cutPaper");
    NSInteger result = [printerController printNormal:PTR_S_RECEIPT data:@"\x1B|9fP"]; //[printerController cutPaper:90];
    [self handleResult:result command:command successMessage:@"Paper was cut" errorMessage:@"Failed to send cut command"];
}

- (NSInteger) printLines:(NSArray*) lines
{
    NSString* converted = @"";
    NSInteger result = UPOS_SUCCESS;
    for (NSString* line in lines) {
        NSLog(@"%@", line);
        if ([line isEqualToString:@"\n"] || [line isEqualToString:@""]) {
            converted = @"\r\n";
        } else if ([line isEqualToString:BIXOLON_ALIGN_LEFT]) {
            converted = @"\x1B|lA";
        } else if ([line isEqualToString:BIXOLON_ALIGN_CENTER]) {
            converted = @"\x1B|cA";
        } else if ([line isEqualToString:BIXOLON_ALIGN_RIGHT]) {
            converted = @"\x1B|rA";
        } else if ([line isEqualToString:BIXOLON_FONT_A]) {
            converted = @"\x1B|0fT";
        } else if ([line isEqualToString:BIXOLON_FONT_B]) {
            converted = @"\x1B|1fT";
        } else if ([line isEqualToString:BIXOLON_FONT_C]) {
            converted = @"\x1B|2fT";
        } else if ([line isEqualToString:BIXOLON_TEXT_MEDIUM]) {
            converted = @"\x1B|4C";
        } else if ([line isEqualToString:BIXOLON_TEXT_SMALL]) {
            converted = @"\x1B|1C";
        } else if ([line isEqualToString:BIXOLON_TEXT_BOLD]) {
            converted = @"\x1B|bC";
        } else if ([line isEqualToString:BIXOLON_TEXT_UNDERLINE]) {
            converted = @"\x1B|1uC";
        } else if ([line isEqualToString:BIXOLON_TEXT_NORMAL]) {
            converted = @"\x1B|!bC\x1B|!uC";
        } else if ([line isEqualToString:BIXOLON_CUT_FEED]) {
            // Feed some lines before cutting
            result = [printerController printNormal:PTR_S_RECEIPT data:@"\x1B|9fP"];
//            result = [printerController cutPaper:90];
            converted = @"";
        } else {
            converted = [line stringByAppendingString:@"\r\n"];
        }

        NSLog(@"%@", converted);
        if (![converted isEqualToString:@""]) {
            result = [printerController printNormal:PTR_S_RECEIPT data:converted];
        }

        if (result != UPOS_SUCCESS) {
        break;
    }

    }
    return result;
}

- (void) printQRCode:(CDVInvokedUrlCommand *)command
{
    NSLog(@"[CDVBixolonPrint] printQRCode");
    NSString* text = [command.arguments objectAtIndex:0];

    NSInteger result = [printerController printBarcode:PTR_S_RECEIPT data:text symbology:PTR_BCS_QRCODE height:6 barWidth:6 alignment:PTR_BC_CENTER textPostion:PTR_BC_TEXT_NONE];
    
    [self handleResult:result command:command successMessage:@"QR printed" errorMessage:@"Failed to print QR"];
}

- (void) printImage64:(CDVInvokedUrlCommand *)command
{
    NSLog(@"[CDVBixolonPrint] printImage64");
    NSString* image64 = [command.arguments objectAtIndex:0];
    
    UIImage* image = [self base64ToImage:image64];
    NSInteger result = [printerController printBitmap:PTR_S_RECEIPT image:image width:512 alignment:PTR_BC_CENTER];
    
    [self handleResult:result command:command successMessage:@"Image printed" errorMessage:@"Failed to print image"];
}

- (void) printReceipt:(CDVInvokedUrlCommand *)command
{
    NSLog(@"[CDVBixolonPrint] printReceipt");
    NSString* image64 = [command.arguments objectAtIndex:0];
    NSArray* lines = [command.arguments objectAtIndex:1];
    NSInteger result = UPOS_SUCCESS;
    
    // Image - if received.
    if (![image64 isEqualToString:@""]) {
        UIImage* image = [self base64ToImage:image64];
        result = [printerController printBitmap:PTR_S_RECEIPT image:image width:512 alignment:PTR_BC_CENTER];
    }

    // Text
    result += [self printLines:lines];
    
    [self handleResult:result command:command successMessage:@"Receipt was printed" errorMessage:@"Failed to print receipt"];
}

- (void) getStatus:(CDVInvokedUrlCommand *)command
{
    NSLog(@"[CDVBixolonPrint] getStatus");
    
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:nil];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (NSMutableDictionary*) printerToDict:(UPOSPrinter*)printer
{
    NSMutableDictionary* dict = [[NSMutableDictionary alloc] init];
    [dict setObject:[printer bluetoothDeviceName] forKey:@"name"];
    [dict setObject:[printer address] forKey:@"macAddress"];
    [dict setObject:[printer modelName] forKey:@"modelName"];
    [dict setObject:[printer port] forKey:@"portName"];
    [dict setObject:[printer serialNumber] forKey:@"serialNumber"];
    return dict;
}

- (UIImage*) base64ToImage:(NSString*)base64
{
    NSData *data = [[NSData alloc]initWithBase64EncodedString:base64 options:NSDataBase64DecodingIgnoreUnknownCharacters];
    return [UIImage imageWithData:data];
}

- (void) handleResult:(NSInteger)result command:(CDVInvokedUrlCommand *)command successMessage:(nonnull NSString*)successMessage errorMessage:(nonnull NSString*)errorMessage
{
    if (result == UPOS_SUCCESS) {
        NSLog(@"[CDVBixolonPrint] Result %ld, %@", (long)result, successMessage);
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:successMessage];
    } else {
        NSLog(@"[CDVBixolonPrint] Result %ld, %@", (long)result, errorMessage);
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:errorMessage];
    }
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end
