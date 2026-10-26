package sa.urtv.screensaver;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;

/** Full-screen preview of the screensaver; any remote key closes it. */
public class MainActivity extends Activity {
    private WebView web;
    private final RenderGuard guard = new RenderGuard();
    private boolean alive;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        // brought up by the sleep timer (sleeping): no wake lock, so the TV's own idle timer carries on to sleep from here
        if (!getIntent().getBooleanExtra("sleeping", false)) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        alive = true;
        show();
    }

    private void show() {
        web = ScreenView.create(this, this::gone);
        setContentView(web);
        web.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    /** The page's renderer died: the dead view is thrown away; a new one at most twice in ten minutes, else the preview closes. */
    private void gone(WebView dead) {
        guard.gone(System.currentTimeMillis());
        ScreenView.release(dead);
        if (dead != web) return;
        web = null;
        if (alive && guard.mayCreate(System.currentTimeMillis())) show();
        else finish();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getAction() == KeyEvent.ACTION_UP) finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        alive = false;
        ScreenView.release(web);
        web = null;
        super.onDestroy();
    }
}
