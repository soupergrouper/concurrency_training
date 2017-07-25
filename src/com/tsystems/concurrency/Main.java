package com.tsystems.concurrency;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by sgorev on 28.03.2017.
 */
public class Main {

    public static void main (String[] args) {

        class DefaultLoader implements LoadableCache.Loader<Integer> {
            Map<String, Integer> values = new HashMap<>();

            DefaultLoader() {
                values.put("first", 1);
                values.put("second", 2);
                values.put("third", 3);
            }

            @Override
            public Integer load(String key) throws InterruptedException, NoSuchElementException {
                System.out.println("Loading value " + key + "...");
                //Time-consuming loading emulation
                Thread.sleep(1000);
                if (values.get(key) == null)
                    throw new NoSuchElementException();
                return values.get(key);
            }
        }

        LoadableCache<Integer> lc = new LoadableCacheImpl<Integer>(10000L, new DefaultLoader());

        class getTask implements Runnable {
            String key;

            public getTask(String key){
                this.key = key;
            };

            @Override
            public void run() {
                try {
                    System.out.println(lc.get(key));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        Scanner sc = new Scanner(System.in);
        while (true) {
            int value = sc.nextInt();
            if (value == 9) {
                sc.close();
                break;
            }
            switch (value) {
                case 1:
                    Thread t1 = new Thread(new getTask("first"));
                    t1.start();
                    break;
                case 2:
                    Thread t2 = new Thread(new getTask("second"));
                    t2.start();
                    break;
                case 3:
                    Thread t3 = new Thread(new getTask("third"));
                    t3.start();
                    break;
                case 4:
                    Thread t4 = new Thread(new getTask("fourth"));
                    t4.start();
                    break;
                default:
                    System.out.println("No such element");
                    break;
            }
        }
    }


}
