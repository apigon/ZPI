package pl.mf.zpi.matefinder;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Adam on 2015-05-15.
 */
public class MessageService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
