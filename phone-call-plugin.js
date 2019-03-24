'use strict';

var exec = require('cordova/exec');

var PhoneCallPlugin = {

  call: function(sendMsg, onSuccess, onFail) {
    return exec(onSuccess, onFail, 'PhoneCallPlugin', 'callWithCommand', [sendMsg]);
  }

};

module.exports = PhoneCallPlugin; 
