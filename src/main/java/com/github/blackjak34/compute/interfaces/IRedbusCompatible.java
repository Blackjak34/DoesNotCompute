package com.github.blackjak34.compute.interfaces;

import com.github.blackjak34.compute.redbus.RedbusDataPacket;

public interface IRedbusCompatible {

    void onPacketReceived(RedbusDataPacket dataPacket);

}
