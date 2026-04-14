package me.lekkernakkie.lekkeranimal.data;

import java.util.Collections;
import java.util.List;

public class HarvestLevelProfile {

    private final int level;
    private final List<HarvestDrop> drops;

    public HarvestLevelProfile(int level, List<HarvestDrop> drops) {
        this.level = level;
        this.drops = drops;
    }

    public int getLevel() {
        return level;
    }

    public List<HarvestDrop> getDrops() {
        return Collections.unmodifiableList(drops);
    }
}