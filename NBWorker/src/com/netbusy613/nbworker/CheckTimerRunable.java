/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netbusy613.nbworker;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author netbusy613
 */
public class CheckTimerRunable implements Runnable {

    private NBPackManager manager;
    private Object control = null;

    public CheckTimerRunable(NBPackManager manager, Object control) {
        this.manager = manager;
        this.control = control;
    }

    @Override
    public void run() {
        while (true) {
            try {
                System.out.println("处理超时检查线程启动！"+new Date());
                synchronized (control) {
                    if (manager.getCheckduration() == 0) {
                        control.wait();
                    } else {
                        control.wait(manager.getCheckduration());
                    }
                }
                for (int i = 0; i < manager.getCOUNT_THREADS(); i++) {
                    try {

                        manager.getRs()[i].checkDuration();
                        System.err.println("线程" + i + "正常！");
                    } catch (DurationTimeOutException ex) {
                        System.err.println("线程" + i + "处理超时！");
                        manager.reStartThread(i);
                    }
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(CheckTimerRunable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
