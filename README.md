# PhoneCallPlugin
PhoneCallPlugin

### Install
  add swift support

```
  cordova plugin add cordova-plugin-add-swift-support
```

add plugin

```
   cordova plugin add phone-call-plugin
```

### Example
In JS File:

```
var number = "10010"
Cordova.exec(successFunction, failFunction, "PhoneCallPlugin", "call", [number]);
	
function successFunction(status){
		console.log(status)
	}
 
function failFunction(status){
    console.log(status)
}
                                 