package com.github.blackjak34.compute.interfaces;

public interface IRedbusCompatible {

    boolean isDevice();

    int getBusAddress();

    void setBusAddress(int newAddress);

    int read(int index);

    void write(int index, int value);

}
