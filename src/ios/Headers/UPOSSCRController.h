//
// ----------------------------------------
//
// File : UPOSSCRController.h
// Date : 12/07/2019
//
// ========================================
        
#import "UPOSDeviceController.h"

NS_ASSUME_NONNULL_BEGIN

@interface UPOSSCRController : UPOSDeviceController {
    
    // Delete member Value
    //NSString*                _commandClassName;
    
    // Delete member Value
    //NSInteger                _resultSCR;
}


-(NSInteger)beginInsertion:(NSInteger)timeout;
-(NSInteger)beginRemoval:(NSInteger)timeout;


-(NSInteger)readData:(NSInteger)action
                data:(NSData*_Nullable*_Nullable)data;


@end



@interface UPOSSCRController(BXLDeprecated)

-(NSInteger)endInsertion
__deprecated_msg("It is unnecessary to call this API");

-(NSInteger)endRemoval
__deprecated_msg("It is unnecessary to call this API");

-(NSInteger)writeData:(NSInteger)action
                 data:(NSData*)data
__deprecated_msg("It is unnecessary to call this API");

@property (readonly)  BOOL      CapCardErrorDetection
__deprecated_msg("It is unnecessary to call this property");

@property (readonly)  NSInteger CapInterfaceMode
__deprecated_msg("It is unnecessary to call this property");

@property (readonly)  NSInteger CapIsoEmvMode
__deprecated_msg("It is unnecessary to call this property");

@property (readonly)  NSInteger CapSCPresentSensor
__deprecated_msg("It is unnecessary to call this property");

@property (readonly)  NSInteger CapSCSlots
__deprecated_msg("It is unnecessary to call this property");

@property (readonly)  NSInteger CapTransmissionProtocol
__deprecated_msg("It is unnecessary to call this property");

@property (readwrite) NSInteger InterfaceMode
__deprecated_msg("It is unnecessary to call this property");

@property (readwrite) NSInteger IsoEmvMode
__deprecated_msg("It is unnecessary to call this property");

@property (readonly)  NSInteger SCPresentSensor
__deprecated_msg("It is unnecessary to call this property");

@property (readwrite) NSInteger SCSlot
__deprecated_msg("It is unnecessary to call this property");

@property (readonly)  BOOL      TransactionInProgress
__deprecated_msg("It is unnecessary to call this property");

@property (readonly)  NSInteger TransactionProtocol
__deprecated_msg("It is unnecessary to call this property");

@end



NS_ASSUME_NONNULL_END
