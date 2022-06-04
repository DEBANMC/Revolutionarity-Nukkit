package de.mariocst.revolutionarity.tasks;

import cn.nukkit.scheduler.PluginTask;
import de.mariocst.revolutionarity.Revolutionarity;

import java.util.HashMap;

public class ClearChecks extends PluginTask<Revolutionarity> {

    public ClearChecks(Revolutionarity owner) {
        super(owner);
    }

    @Override
    public void onRun(int i) {
        for(HashMap<String, Integer> map : Revolutionarity.getChecks()){
            map.clear();
        }
    }
}
