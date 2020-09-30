//
// ----------------------------------------
//
// File : UPOSDefinesScr.h
// Date : 20/09/2019
//
// ========================================
        

#ifndef UPOSDefinesScr_h
#define UPOSDefinesScr_h


#include "UPOSDefines.h"

typedef NS_OPTIONS(NSUInteger, __UPOS_SCR_STATUS_UPDATE_EVENT) {
    SC_SUE_NO_CARD       DEPRECATED_MSG_ATTRIBUTE("not USE")    = 1,
    SC_SUE_CARD_PRESENT  DEPRECATED_MSG_ATTRIBUTE("not USE")    = 2
};

typedef NS_OPTIONS(NSUInteger, __UPOS_SCR_READ_DATA_ACTION) {
    SC_READ_DATA                                                    =  11,
    SC_READ_PROGRAM             DEPRECATED_MSG_ATTRIBUTE("not USE") =  12,
    SC_EXECUTE_AND_READ_DATA    DEPRECATED_MSG_ATTRIBUTE("not USE") =  13,
    SC_XML_READ_BLOCK_DATA      DEPRECATED_MSG_ATTRIBUTE("not USE") =  14
};



typedef NS_OPTIONS(NSUInteger, __UPOS_SCR_CAP_INTERFACE_MODE) {
    SC_CMODE_TRANS       =   1,
    SC_CMODE_BLOCK       =   2,
    SC_CMODE_APDU        =   4,
    SC_CMODE_XML         =   8
}__deprecated_msg("It is unnecessary to call this NS_OPTIONS");


typedef NS_OPTIONS(NSUInteger, __UPOS_SCR_CAP_ISO_EMV_MODE) {
    SC_CMODE_ISO         =   1,
    SC_CMODE_EMV         =   2
}__deprecated_msg("It is unnecessary to call this NS_OPTIONS");


typedef NS_OPTIONS(NSUInteger, __UPOS_SCR_CAP_TRANSMISSION_PROTOCOL) {
    SC_CTRANS_PROTOCOL_T0=   1,
    SC_CTRANS_PROTOCOL_T1=   2
}__deprecated_msg("It is unnecessary to call this NS_OPTIONS");


typedef NS_OPTIONS(NSUInteger, __UPOS_SCR_INTERFACE_MODE) {
    SC_MODE_TRANS        =   1,
    SC_MODE_BLOCK        =   2,
    SC_MODE_APDU         =   4,
    SC_MODE_XML          =   8
}__deprecated_msg("It is unnecessary to call this NS_OPTIONS");


typedef NS_OPTIONS(NSUInteger, __UPOS_SCR_ISO_EMV_MODE) {
    SC_MODE_ISO          =   1,
    SC_MODE_EMV          =   2
}__deprecated_msg("It is unnecessary to call this NS_OPTIONS");


typedef NS_OPTIONS(NSUInteger, __UPOS_SCR_TRANSMISSION_PROTOCOL) {
    SC_TRANS_PROTOCOL_T0 =   1,
    SC_TRANS_PROTOCOL_T1 =   2
}__deprecated_msg("It is unnecessary to call this NS_OPTIONS");


typedef NS_OPTIONS(NSUInteger, __UPOS_SCR_WRITE_DATA_ACTION) {
    
    SC_STORE_DATA        =  21,
    SC_STORE_PROGRAM     =  22,
    SC_EXECUTE_DATA      =  23,
    SC_XML_BLOCK_DATA    =  24,
    SC_SECURITY_FUSE     =  25,
    SC_RESET             =  26
}__deprecated_msg("It is unnecessary to call this NS_OPTIONS");




typedef NS_OPTIONS(NSUInteger, __UPOS_SCR_ERROR_EVENT) {
    
    UPOS_ESC_READ        = 201,
    UPOS_ESC_WRITE       = 202,
    UPOS_ESC_TORN        = 203,
    UPOS_ESC_NO_CARD     = 204
}__deprecated_msg("It is unnecessary to call this NS_OPTIONS");



typedef NS_OPTIONS(NSUInteger, __UPOS_SCR_RESPONSE) {
    _UPOS_SCR_RESPONSE_SUCCESSFUL                       = 0x00,
    _UPOS_SCR_RESPONSE_WRONG_COMMAND_LENGTH             = 0x01,
    _UPOS_SCR_RESPONSE_EXCESSIVE_CURRENT_POWEROFF       = 0x02,
    _UPOS_SCR_RESPONSE_DEFECTIVE_VOLTAGE_POWEROFF       = 0x03,
    _UPOS_SCR_RESPONSE_INVALID_07                       = 0x07,
    _UPOS_SCR_RESPONSE_INVALID_08                       = 0x08,
    _UPOS_SCR_RESPONSE_INVALID_09                       = 0x09,
    _UPOS_SCR_RESPONSE_INVALID_0A                       = 0x0A,
    _UPOS_SCR_RESPONSE_INVALID_15                       = 0x15,
    _UPOS_SCR_RESPONSE_SHORT_CIRCUITING                 = 0xA2,
    _UPOS_SCR_RESPONSE_ATR_TOO_LONG                     = 0xA3,
    _UPOS_SCR_RESPONSE_CARD_IS_TOO_LONG                 = 0xB0,
    _UPOS_SCR_RESPONSE_PROTOCOL_ERROR_IN_EMV            = 0xBB,
    _UPOS_SCR_RESPONSE_PROTOCOL_ERROR_DURING_T1EXCHANGE = 0xBD,
    _UPOS_SCR_RESPONSE_APDU_LENGTH_WRONG                = 0xBE,
    _UPOS_SCR_RESPONSE_INVALID_TCK_OF_ATR               = 0xF7,
    _UPOS_SCR_RESPONSE_INVALID_TS_OF_ATR                = 0xF8,
    _UPOS_SCR_RESPONSE_PARITY_ERROR                     = 0xFD,
    _UPOS_SCR_RESPONSE_CARD_IS_NOT_PRESENT_OR_MUTE      = 0xFE,
    _UPOS_SCR_RESPONSE_NO_USED                          = 0xFF,
    
}__deprecated_msg("It is unnecessary to call this NS_OPTIONS");


#endif /* UPOSDefinesScr_h */
