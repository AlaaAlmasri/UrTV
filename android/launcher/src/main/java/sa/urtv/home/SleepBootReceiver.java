package sa.urtv.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** The TV has started again: the sleep timer's alarm is gone with the old boot, so its saved end is forgotten (SleepTimer). Not exported to apps. */
public class SleepBootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent i) {
        SleepTimer.forgetAfterBoot(c);
    }
}
