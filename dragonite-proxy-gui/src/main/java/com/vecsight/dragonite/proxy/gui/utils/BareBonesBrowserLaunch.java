package com.vecsight.dragonite.proxy.gui.utils;

import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/*******************************************************************************
 * Copyright (c) 2005-2017 Mritd, Inc.
 * dragonite
 * com.vecsight.dragonite.proxy.gui.utils
 * Created by mritd on 17/12/24 下午6:55.
 * Description: BareBonesBrowserLaunch
 *******************************************************************************/
public class BareBonesBrowserLaunch {
    public static void openURL(String url) {
        try {
            browse(url);
        } catch (Exception e) {
            Logger.error(e, "Error attempting to launch web browser:");
        }
    }

    private static void browse(String url) throws ClassNotFoundException,
            IllegalAccessException, IllegalArgumentException,
            InterruptedException, InvocationTargetException,
            IOException, NoSuchMethodException {
        String osName = System.getProperty("os.name", "");
        if (osName.startsWith("Mac OS")) {
            Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
            Method openURL = fileMgr.getDeclaredMethod("openURL", String.class);
            openURL.invoke(null, url);
        } else if (osName.startsWith("Windows")) {
            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
        } else { // assume Unix or Linux
            String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
            String browser = null;
            for (int count = 0; count < browsers.length && browser == null; count++)
                if (Runtime.getRuntime()
                        .exec(new String[]{"which", browsers[count]})
                        .waitFor() == 0)
                    browser = browsers[count];
            if (browser == null) {
                Logger.error("Could not find web browser");
            } else {
                Runtime.getRuntime().exec(new String[]{browser, url});
            }
        }
    }

}
