package com.github.blackjak34.compute.interfaces;

import com.github.blackjak34.compute.redbus.RedbusDataPacket;

/**
 * @author Blackjak34
 * @since 1.0.0
 */
public interface IRedbusCompatible {

    void onPacketReceived(RedbusDataPacket dataPacket);

}
