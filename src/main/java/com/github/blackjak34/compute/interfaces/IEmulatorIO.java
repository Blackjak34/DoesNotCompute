package com.github.blackjak34.compute.interfaces;

/**
 * @author Blackjak34
 * @since 1.0.0
 */
public interface IEmulatorIO {

    void halt();

    void lightOnFire();

    int readNetwork(int address, int index);

    void writeNetwork(int address, int index, int value);

}
