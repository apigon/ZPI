package pl.mf.zpi.matefinder;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Adam on 2015-05-15.
 */
public class MessageService extends Service {
    private MessageAsync msg;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("SERVICES", "Uruchomiono SERVICES");
        msg = (MessageAsync) new MessageAsync(MessageService.this).execute();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        msg.stop();
        Log.d("SERVICES", "Zatrzymano MessageAsync");
    }
}
