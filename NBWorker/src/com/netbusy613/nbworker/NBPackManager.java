/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netbusy613.nbworker;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class NBPackManager {

    public NBPackManager() {
        registedOP = new HashMap<>();
    }
    public boolean goon = true;

    public final Object control = new Date();
    public final Object waitControl = new Date();
    public final Object statuControl = new Date();
    //线程处理超时检查控制器
    public final Object timeControl = new Date();
    private NBpack[] packs;
    private int read_point = 0;
    private int write_point = 0;
    //最大任务数
    private int COUNT_MAXPACKS = 1000;
    //开启线程数
    private int COUNT_THREADS = 10;

    private int MAX_THREADS = 100;

    //默认超时检查每分钟运行一次
    private int checkduration = 60000;//1 min

    public int getCheckduration() {
        return checkduration;
    }

    //设置线程超时检查时间间隔
    public void setCheckduration(int checkduration) {
        if (this.checkduration == 0 && checkduration != 0) {
            synchronized (timeControl) {
                this.checkduration = checkduration;
                timeControl.notify();
            }
        } else {
            this.checkduration = checkduration;
        }
    }

    private Map<String, Class> registedOP;

    private NBWorkerRunable[] rs;
    private Thread[] ts;

    public NBWorkerRunable[] getRs() {
        return rs;
    }

    public Thread[] getTs() {
        return ts;
    }

    public Map<String, PackOP> registedOPImpl() {
        Map<String, PackOP> re = new HashMap<>();
        for (Map.Entry<String, Class> entry : registedOP.entrySet()) {
            String key = entry.getKey();
            Class value = entry.getValue();
            try {
                PackOP OPImpl = (PackOP) value.newInstance();
                re.put(key, OPImpl);
            } catch (InstantiationException ex) {
                Logger.getLogger(NBPackManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(NBPackManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return re;
    }

    public int getCOUNT_MAXPACKS() {
        return COUNT_MAXPACKS;
    }

    public void setCOUNT_MAXPACKS(int COUNT_MAXPACKS) {
        this.COUNT_MAXPACKS = COUNT_MAXPACKS;
    }

    public int getCOUNT_THREADS() {
        return COUNT_THREADS;
    }

    public void setCOUNT_THREADS(int COUNT_THREADS) {
        if (COUNT_THREADS >= MAX_THREADS) {
            COUNT_THREADS = MAX_THREADS;
            System.err.println("COUNT_THREADS must small than MAX_THREADS, reset COUNT_THREADS =" + MAX_THREADS);
            return;
        }
        this.COUNT_THREADS = COUNT_THREADS;
    }

    public void reStartThread(int i) {
        ts[i].stop();
        rs[i] = new NBWorkerRunable(this, statuControl, i);
        Thread tr = new Thread(rs[i]);
        tr.setDaemon(false);
        tr.start();
        ts[i] = tr;
        System.out.println("重启动线程" + i);
    }

    public void start() {
        //设置待处理最大包的数量
        packs = new NBpack[COUNT_MAXPACKS];
        //注册处理类
        registPackage("com.netbusy613.nbworker.packop");
        //设置处理线程最大数
        rs = new NBWorkerRunable[MAX_THREADS];
        ts = new Thread[MAX_THREADS];
        System.out.println("开始启动线程");
        //创建处理线程，并启动线程
        for (int i = 0; i < getCOUNT_THREADS(); i++) {
            rs[i] = new NBWorkerRunable(this, statuControl, i);
        }
        for (int i = 0; i < getCOUNT_THREADS(); i++) {
            Thread tr = new Thread(rs[i]);
            tr.setDaemon(false);
            tr.start();
            //将线程保存在数组中便于终止问题线程
            ts[i] = tr;
            System.out.println("启动线程" + i);
        }
        // 判断所有线程是否已经启动完成
        while (true) {
            synchronized (statuControl) {
                boolean ifall = true;
                for (int i = 0; i < COUNT_THREADS; i++) {
                    if (!rs[i].isRunning()) {
                        ifall = false;
                        break;
                    }
                }
                if (ifall) {
                    break;
                }
                try {
                    statuControl.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(NBPackManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        System.out.println("线程启动完成！");
        CheckTimerRunable ctr = new CheckTimerRunable(this, timeControl);
        Thread tt = new Thread(ctr);
        tt.setDaemon(false);
        tt.start();

    }

    //注册flag和处理方法；处理方法为继承了 PackOP的类
    public void regist(Class clazz) {
        registedOP.put(clazz.getName(), clazz);
    }

    public void registPackage(String packageName) {
        List<String> clzzstr = PackageUtil.getClassName(packageName, false);
        for (String cString : clzzstr) {
            try {
                Class clazz = Class.forName(cString);
                registedOP.put(clazz.getName(), clazz);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(NBPackManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private Class registed(String flag) {
        return registedOP.get(flag);
    }

    public NBpack readPack() {
        NBpack rePack = null;
        synchronized (control) {
            if (read_point == write_point) {
                return null;
            }
            rePack = packs[read_point];
            read_point++;
            if (read_point == packs.length) {
                read_point = read_point - packs.length;
            }
        }
        return rePack;
    }

    public void addPack(NBpack pack) throws ListFullException {
        synchronized (control) {
            if (write_point + 1 == read_point) {
                throw new ListFullException();
            }
            packs[write_point] = pack;
            write_point++;
            if (write_point == packs.length) {
                write_point = write_point - packs.length;
            }
            control.notifyAll();
        }
    }

    public int count() {
        synchronized (control) {
            if (write_point >= read_point) {
                return write_point - read_point;
            } else {
                return write_point + COUNT_MAXPACKS - read_point;
            }
        }
    }

    public void updateWaitStatu() {
        int c = 0;
        for (int i = 0; i < COUNT_THREADS; i++) {
            if (rs[i].isWaitting()) {
                c++;
            }
        }
        if (c == COUNT_THREADS) {
            synchronized (waitControl) {
                System.out.println("全部线程休息....");
                waitControl.notify();
            }
        } else {
            System.out.println("还有" + (COUNT_THREADS - c) + "线程运行。");
        }
    }
}
