//
// ----------------------------------------
//
// File : UPOSCDController.h
// Date : 12/07/2019
//
// ========================================


#import "UPOSDeviceController.h"
#import "UPOSDefinesCashDrawer.h"

NS_ASSUME_NONNULL_BEGIN

@interface UPOSCDController : UPOSDeviceController

@property (readonly)    BOOL    DrawerOpened;

-(NSInteger) OpenDrawer;


@end

@interface UPOSCDController(BXLDeprecated)
-(NSInteger) waitForDrawerClose:(NSInteger)beepTimeout
                  beepFrequency:(NSInteger)beepFrequency
                   beepDuration:(NSInteger)beepDuration
                      beepDelay:(NSInteger)beepDelay
__deprecated_msg("It is unnecessary to call this API");
@end

NS_ASSUME_NONNULL_END
