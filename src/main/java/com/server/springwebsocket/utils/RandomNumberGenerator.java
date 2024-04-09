package com.server.springwebsocket.utils;
import java.lang.Math;
import java.math.BigInteger;

public class RandomNumberGenerator {
    private int m;
    private int a;
    private int c;
    private double state;

    public RandomNumberGenerator(String seed) {
        this.m = 0x80000000;
        this.a = 1103515245;
        this.c = 12345;
        this.state = 0;

        for (int i = 0; i < seed.length(); i++) { 
            int c = seed.charAt(i);
            //System.out.println(c);
            this.state = this.state + (c * i);
        }
        //System.out.println(this.state);
    }

    public double nextInt() {
        this.state = (this.a * this.state + this.c) % this.m;
        //System.out.println("state: " + (this.a * this.state + this.c)% this.m);
        return this.state;
    }

    public int nextRange(int start, int end) {
        int range = end - start;
        int add = (int)Math.floor(this.nextFloat() * range);
        //System.out.println(start + add);
        return start + add;
    }

    public double nextFloat() {
        double fl = (this.nextInt() / (this.m - 1));
        //System.out.println("fl: " + fl + " " + (long)this.nextInt() + " " + (this.m - 1));
        return fl;
    }


}
