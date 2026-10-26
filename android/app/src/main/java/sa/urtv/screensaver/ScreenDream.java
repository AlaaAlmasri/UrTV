package sa.urtv.screensaver;

import android.service.dreams.DreamService;
import android.webkit.WebView;

/** The system screensaver: starts on the TV's own idle timer, any key wakes it. */
public class ScreenDream extends DreamService {
    private WebView web;
    private final RenderGuard guard = new RenderGuard();
    private boolean attached;

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setInteractive(false);
        setFullscreen(true);
        setScreenBright(true);
        attached = true;
        show();
    }

    private void show() {
        web = ScreenView.create(this, this::gone);
        setContentView(web);
    }

    /** The page's renderer died: the dead view is thrown away; a new one at most twice in ten minutes, else the dream ends (the system starts it again later). */
    private void gone(WebView dead) {
        guard.gone(System.currentTimeMillis());
        ScreenView.release(dead);
        if (dead != web) return;
        web = null;
        if (attached && guard.mayCreate(System.currentTimeMillis())) show();
        else finish();
    }

    @Override
    public void onDetachedFromWindow() {
        attached = false;
        ScreenView.release(web);
        web = null;
        super.onDetachedFromWindow();
    }
}
