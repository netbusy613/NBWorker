/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netbusy613.nbworker;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 *
 * @author netbusy613
 */
public class LogUtil {

    private static long dur = 86400000;//1天
    private static Object control = new Date();
    private static boolean iflog = false;

    private static Logger log;
    private static FileHandler fileHandle = null;
    private static long oldtime = new Date().getTime();

    public static void setIflog(boolean iflog) {
        LogUtil.iflog = iflog;
    }

    public static void setDur(int sec) {
        dur = sec * 1000;
    }

    public static void Log(String msg) {
        if (iflog) {
            synchronized (control) {
                long now = new Date().getTime();
                if (now - oldtime > dur) {
                    fileHandle.close();
                    log = null;
                }
                if (log == null) {
                    init();
                }
                log.info(msg);
            }
        }
    }

    public static void init() {
        log = Logger.getLogger("nbworker");
        try {
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            fileHandle = new FileHandler(sdf.format(now) + ".log");
            log.addHandler(fileHandle);
        } catch (IOException | SecurityException ex) {
        }
    }

    public static Logger getLog() {
        return log;
    }
}
