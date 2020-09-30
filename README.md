# cordova-plugin-ia-bixolon
A cordova plugin to utilize bluetooth printers that use the ESC POS system. This plugin opens a serial connection with the printer.

__NOTE: This repository is written and maintained with personal use in mind. As such, I will not be taking on issues, updates, requests and such.__
_If you are looking for a regularly maintained plugin, look elsewhere._ :-|

## ESC POS Commands
Commands can differ among manufacturers. Be sure to check with your manufacturer and adapt the commands accordingly.
Some examples:
- [BIXOLON](https://www.bixolon.com/product.php?key=pos) - Select your printer, check under Manuals
- [EPSON](https://reference.epson-biz.com/modules/ref_escpos/index.php?content_id=72)
- [STAR](https://www.starmicronics.com/support/SDKDocumentation.aspx)
- [SAM4S](http://www.sam4s.co.kr/files/DOWN/201901239303_1.pdf)
- [POSX](https://pos-x.com/download/escpos-programming-manual/)

## Installation
```
ionic cordova plugin add cordova-plugin-ia-bixolon
```

## Usage - ANDROID

The android-side of the plugin gets exposed globally as `BTPrinter`. To appease tha language server and lint, declare the variable at the top of the file as so:
```typescript
declare var BTPrinter: any;
```
I am planning on creating an Ionic Native wrapper to convert to Promise-based usage. For now, check out the [interface](#plugin-interface) at the bottom of the readme if you'd like.

## Supported Functions
- [connect](#connect) - string, successFunction, errorFunction
- [cutPaper](#cutPaper) - successFunction, errorFunction
- [disconnect](#disconnect) - successFunction, errorFunction
- [list](#list) - successFunction, errorFunction
- [status](#status) - successFunction, errorFunction
- [setBrand](#setBrand) - string, successFunction, errorFunction
- [printBase64](#printBase64) - string, align, successFunction, errorFunction
- [printImageUrl](#printImageUrl) - string, align, successFunction, errorFunction
- [printPOSCommand](#printPOSCommand) - string, successFunction, errorFunction
- [printText](#printText) - string, successFunction, errorFunction

## connect
Attempt to open a serial connection with the printer. It is best to use the __list__ command, and then send the name you received to this command.
```typescript
let printerName = 'printer123';

BTPrinter.connect(printerName,
    function(){
        console.log('connected');
    },
    function(errorMessage){
        console.error(errorMessage);
    }
);
```

### disconnect
```typescript
BTPrinter.disconnect(
    function(){
        console.log('disconnected');
    },
    function(errorMessage){
        console.error(errorMessage);
    }
);
```

### list
```typescript
BTPrinter.list(
    function(data){
        console.log(data);
    },
    function(errorMessage){
        console.error(errorMessage);
    }
);
```
```typescript
data = [ 'name1', 'address1', typeNumber1, 'name2', 'address2', typeNumber2, ... ]
```

### printBase64
```typescript
let image64 = 'data:image/png;base64,iVBORw0KGgoAAAAN...   ...xDts=';
let align = '0'; // See Align Options

BTPrinter.printBase64(image64, align,
    function(){
        console.log('image print command sent to printer');
    },
    function(errorMessage){
        console.error(errorMessage);
    }
);
```

### printImageUrl
#### Example
```typescript
let imageUrl = '/storage/emulated/0/Pictures/myfolder/myimage.jpg'; // Maximum Size: 300x300px
let align = '0'; // See Align Options

BTPrinter.printImageUrl(imageUrl, align,
    function(){
        console.log('image print command sent to printer');
    },
    function(errorMessage){
        console.error(errorMessage);
    }
);
```

### printPOSCommand
__Take note: Commands can differ from one brand of POS printer to another.__

#### Example
```typescript
let commandString = '\x1D\x28\x41'; // Command to execute test print on SAM4S printers
BTPrinter.printPOSCommand(commandString,
    function(){
        console.log('image print command sent to printer');
    },
    function(errorMessage){
        console.error(errorMessage);
    }
);
```

### printText
#### Example
```typescript
let text = 'This is a line of text.';
BTPrinter.printText(text,
    function(){
        console.log('text sent to printer');
    },
    function(errorMessage){
        console.error(errorMessage);
    }
);
```

### status
#### Example
```typescript
// TODO Expand code to include all posibile variables
// TODO Return object in place of string array
// TODO Example
```

#### Returns
```typescript
// TODO Example
```

## Option Values
### Size options

```typescript
     0 = CHAR_SIZE_01 // equivalent 0x1B, 0x21, 0x00
     8 = CHAR_SIZE_08 // equivalent 0x1B, 0x21, 0x08
    10 = CHAR_SIZE_10 // equivalent 0x1B, 0x21, 0x10
    11 = CHAR_SIZE_11 // equivalent 0x1B, 0x21, 0x11
    20 = CHAR_SIZE_20 // equivalent 0x1B, 0x21, 0x20
    30 = CHAR_SIZE_30 // equivalent 0x1B, 0x21, 0x30
    31 = CHAR_SIZE_31 // equivalent 0x1B, 0x21, 0x31
    51 = CHAR_SIZE_51 // equivalent 0x1B, 0x21, 0x51
    61 = CHAR_SIZE_61 // equivalent 0x1B, 0x21, 0x61
```

### Align options

```typescript
    0 = ESC_ALIGN_LEFT // equivalent 0x1B, 0x61, 0x00
    1 = ESC_ALIGN_CENTER // equivalent 0x1B, 0x61, 0x01
    2 = ESC_ALIGN_RIGHT // equivalent 0x1B, 0x61, 0x02
```


## plugin-interface

```typescript
declare var BTPrinter: {
  connect(printerName: string, fnSuccess: any, fnError: any): any;
  disconnect(fnSuccess: any, fnError: any): any;
  list(fnSuccess: any, fnError: any): any;
  status(fnSuccess: any, fnError: any): any;
  cutPaper(fnSuccess: any, fnError: any): any;
  setBrand(brandName: string, fnSuccess: any, fnError: any): any;

  printBase64(image64: string, align: any, fnSuccess: any, fnError: any): any;
  printImageUrl(imageUrl: string, align: any, fnSuccess: any, fnError: any): any;
  printPOSCommand(commandString: string, fnSuccess: any, fnError: any): any;
  printText(text: string, fnSuccess: any, fnError: any): any;
};
```
---
## Usage - IOS

The plugin creates the 'global' object `BixolonPrint`. As with all other native plugins, the variable is only accessible once the platform is ready.

Add this to the top of your service outside the component declaration:

```typescript
declare var BixolonPrint: {
    discovery(fnSuccess: any, fnError: any): void;
    connect(portName: string, fnSuccess: any, fnError: any): void;
    disconnect(fnSuccess: any, fnError: any): void;
    cutPaper(fnSuccess: any, fnError: any): void;
    getStatus(printStatus: boolean, fnSuccess: any, fnError: any): void;
    printText(lines: string[], fnSuccess: any, fnError: any): void;
    printImage64(imageBase64: string, fnSuccess: any, fnError: any): void;
    printReceipt(imageBase64: string, lines: string[], fnSuccess: any, fnError: any): void;
};
```

## Functions
### discovery
```typescript
BixolonPrint.discovery(
    (printers: any[]) => {
        // Array of printers to use here
    },
    (error: string) => {
        // Error handling here
    }
);
```

### connect
```typescript
// Best to use the modelName you get from `discovery` here.
const printerModelName = '_____';

BixolonPrint.connect(printerModelName,
    (response: any) => {
        // Yay, connected. You can now do getStatus().
    },
    (error: string) => {
        // Error handling here
    }
);
```

### disconnect
```typescript
BixolonPrint.disconnect(
    (response: any) => {
        // Now disconnected
    },
    (error: string) => {
        // Error handling here
    }
);
```

### cutPaper
```typescript
BixolonPrint.cutPaper(
    (response: any) => {
        // Cut command was successful
    },
    (error: string) => {
        // Error handling here
    }
);
```

### getStatus
```typescript
BixolonPrint.getStatus(false,
    (status: any) => {
        // This part is still TODO, currently status is always null
    },
    (error: string) => {
        // Error handling here
    }
);
```

### printText
```typescript
// See BixolonCommands -> bottom of readme
const lines = ['Line 1', 'Line 2', BixolonCommands.FONT_BOLD, 'Bold Line 3'];
BixolonPrint.printText(lines,
    (response: any) => {
        // yay
    },
    (error: string) => {
        // nay handling here
    }
);
```

### printImage64
```typescript
// On iOS, the data length must be a multiple of 4, so pad with '=' as needed.
const imageBase64 = 'UT/8xQ+AkmQI_________fHl7yvfrsexnr==';

BixolonPrint.printImage64(imageBase64,
    (response: any) => {
        // yay
    },
    (error: string) => {
        // nay handling here
    }
);
```

### printReceipt
Essentialy a combination of printText and printImage. However, when imageBase64 is empty, no image is printed.
```typescript
const imageBase64 = 'UT/8xQ+AkmQI_________fHl7yvfrsexnr==';
const lines = ['Line 1', 'Line 2', BixolonCommands.FONT_BOLD, 'Bold Line 3'];

BixolonPrint.printReceipt(imageBase64, lines,
    (response: any) => {
        // yay
    },
    (error: string) => {
        // nay handling here
    }
);
```

### ESC Commands
```typescript
// Add these as separate lines amongst your own
enum BixolonCommands {
    FONT_A = '[FONTA]',
    FONT_B = '[FONTB]',
    FONT_C = '[FONTC]',
    ALIGN_LEFT = '[LEFT]',
    ALIGN_CENTER = '[CENTER]',
    ALIGN_RIGHT = '[RIGHT]',
    FONT_BOLD = '[BOLD]',
    FONT_NORMAL = '[NORMAL]',
    TEXT_SMALL = '[SMALL]',
    TEXT_MEDIUM = '[MEDIUM]'
}
```
