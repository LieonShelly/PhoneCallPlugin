# PhoneCallPlugin
PhoneCallPlugin

### Install

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
                                 