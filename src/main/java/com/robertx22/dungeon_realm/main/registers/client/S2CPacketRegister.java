package com.robertx22.dungeon_realm.main.registers.client;

import com.robertx22.dungeon_realm.main.DungeonMain;
import com.robertx22.dungeon_realm.packets.DungeonStatsPacket;
import com.robertx22.library_of_exile.main.Packets;

public class S2CPacketRegister {

    public static void register() {
        int i = 1;

        Packets.registerServerToClient(DungeonMain.NETWORK, new DungeonStatsPacket(), i++);
    }
}
