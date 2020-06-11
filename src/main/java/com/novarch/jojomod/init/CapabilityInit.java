package com.novarch.jojomod.init;

import com.novarch.jojomod.capabilities.stand.StandCapability;
import com.novarch.jojomod.capabilities.timestop.Timestop;
import com.novarch.jojomod.util.handlers.CapabilityHandler;
import net.minecraftforge.common.MinecraftForge;

public class CapabilityInit
{
    public static void register()
    {
        StandCapability.register();
        Timestop.register();

        MinecraftForge.EVENT_BUS.register(new CapabilityHandler());
    }
}
