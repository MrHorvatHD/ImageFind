package horvatApps.ImageScan.logic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartML extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Util.scheduleJob(context);
    }
}
