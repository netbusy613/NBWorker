/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netbusy613.nbworker;

import java.util.Date;
import java.util.logging.Level;

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
                LogUtil.Log("处理超时检查线程启动！"+new Date());
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
                    } catch (DurationTimeOutException ex) {
                        NBpack pack = manager.getRs()[i].getNowPack();
                        PackOP op = manager.registedOPImpl().get(pack.FLAG);
                        op.errorDoing(pack.json);
                        manager.reStartThread(i);
                    }
                }
            } catch (InterruptedException ex) {
                LogUtil.getLog().log(Level.SEVERE, null, ex);
            }
        }
    }

}
