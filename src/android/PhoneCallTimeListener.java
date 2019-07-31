package phone.call.plugin;

import java.io.Serializable;

/**
 * Created by lieon on 2019/4/5.
 */


interface PhoneCallTimeListener extends Serializable {
    public void dialingTime(String time);
    public  void talkingTime(String time);
}
