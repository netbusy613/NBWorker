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
    private NBpack[] packs;
    private int read_point = 0;
    private int write_point = 0;
    //最大任务数
    private int COUNT_MAXPACKS = 1000;
    //开启线程数
    private int COUNT_THREADS = 10;

    private Map<String, Class> registedOP;

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
        this.COUNT_THREADS = COUNT_THREADS;
    }

    public void start() {
        packs = new NBpack[COUNT_MAXPACKS];
        registPackage("com.netbusy613.nbworker.packop");
        Object statuControl = new Date();
        NBWorkerRunable[] rs = new NBWorkerRunable[getCOUNT_THREADS()];
        System.out.println("开始启动线程");
        for (int i = 0; i < getCOUNT_THREADS(); i++) {
            rs[i] = new NBWorkerRunable(this, statuControl, i);
            Thread tr = new Thread(rs[i]);
            tr.setDaemon(false);
            tr.start();
            System.out.println("启动线程" + i);
        }
        while (true) {
            synchronized (statuControl) {

                boolean ifall = true;
                for (NBWorkerRunable r : rs) {
                    ifall = r.isRunning() && ifall;
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
        Object lock = new Date();
//        synchronized (lock) {
//            try {
//                lock.wait();
//            } catch (InterruptedException ex) {
//                Logger.getLogger(NBPackManager.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
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
            if(write_point+1==read_point){
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
                return write_point-read_point;
            }else{
                return write_point+COUNT_MAXPACKS-read_point;
            }
        }
    }
}
