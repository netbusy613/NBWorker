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

    public static void Log(String msg) {
        if(log==null){
            init();
        }
       log.info(msg);
    }
    private static Logger log;

    public static void init() {
        log = Logger.getLogger("nbworker");
        FileHandler fileHandle = null;
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
