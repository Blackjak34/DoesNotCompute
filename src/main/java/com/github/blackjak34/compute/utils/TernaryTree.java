package com.github.blackjak34.compute.utils;

import com.github.blackjak34.compute.interfaces.IRedbusCompatible;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.*;

public class TernaryTree {

    private static final HashMap<Integer,Set<TernaryTree>> instances = new HashMap<Integer,Set<TernaryTree>>();

    private World world;
    private final TernaryNode baseNode;

    private final HashMap<Integer,BlockPos> devices = new HashMap<Integer,BlockPos>();

    public TernaryTree(World world, BlockPos basePos) {
        this.world = world;
        baseNode = new TernaryNode(basePos);
        evaluateTree();

        addToInstanceList(this);
    }

    public TernaryTree(NBTTagCompound data) {
        baseNode = new TernaryNode();
        readFromNBT(data);

        addToInstanceList(this);
    }

    public void evaluateTree() {
        devices.clear();
        baseNode.northNode = null;
        baseNode.eastNode = null;
        baseNode.southNode = null;
        baseNode.westNode = null;
        baseNode.evaluateNode();
    }

    public int redbusRead(int address, int index) {
        BlockPos devicePos = devices.get(address);
        if(devicePos == null) {return 0xFF;}

        return ((IRedbusCompatible) world.getTileEntity(devicePos)).read(index);
    }

    public void redbusWrite(int address, int index, int value) {
        BlockPos devicePos = devices.get(address);
        if(devicePos == null) {return;}

        ((IRedbusCompatible) world.getTileEntity(devicePos)).write(index, value);
    }

    public void writeToNBT(NBTTagCompound data) {
        NBTTagCompound treeData = new NBTTagCompound();
        writeNode(treeData, baseNode);
        treeData.setInteger("dimensionID", world.provider.getDimensionId());
        data.setTag("treeData", treeData);
    }

    private void writeNode(NBTTagCompound data, TernaryNode node) {
        data.setIntArray("pos", new int[] {node.pos.getX(), node.pos.getY(), node.pos.getZ()});

        if(node.northNode != null) {
            NBTTagCompound northData = new NBTTagCompound();
            writeNode(northData, node.northNode);
            data.setTag("northNode", northData);
        }
        if(node.eastNode != null) {
            NBTTagCompound eastData = new NBTTagCompound();
            writeNode(eastData, node.eastNode);
            data.setTag("eastNode", eastData);
        }
        if(node.southNode != null) {
            NBTTagCompound southData = new NBTTagCompound();
            writeNode(southData, node.southNode);
            data.setTag("southNode", southData);
        }
        if(node.westNode != null) {
            NBTTagCompound westData = new NBTTagCompound();
            writeNode(westData, node.westNode);
            data.setTag("westNode", westData);
        }
    }

    public void readFromNBT(NBTTagCompound data) {
        NBTTagCompound treeData = (NBTTagCompound) data.getTag("treeData");
        readNode(treeData, baseNode);
        world = DimensionManager.getWorld(treeData.getInteger("dimensionID"));
    }

    private void readNode(NBTTagCompound data, TernaryNode node) {
        int[] pos = data.getIntArray("pos");
        node.pos = new BlockPos(pos[0], pos[1], pos[2]);

        NBTTagCompound northData = (NBTTagCompound) data.getTag("northNode");
        if(northData != null) {
            node.northNode = new TernaryNode();
            readNode(northData, node.northNode);
        }
        NBTTagCompound eastData = (NBTTagCompound) data.getTag("eastNode");
        if(eastData != null) {
            node.eastNode = new TernaryNode();
            readNode(eastData, node.eastNode);
        }
        NBTTagCompound southData = (NBTTagCompound) data.getTag("southNode");
        if(southData != null) {
            node.southNode = new TernaryNode();
            readNode(southData, node.southNode);
        }
        NBTTagCompound westData = (NBTTagCompound) data.getTag("westNode");
        if(westData != null) {
            node.westNode = new TernaryNode();
            readNode(westData, node.westNode);
        }
    }

    private static void addToInstanceList(TernaryTree tree) {
        Set<TernaryTree> treesForWorld = instances.get(tree.world.provider.getDimensionId());
        if(treesForWorld == null) {
            treesForWorld = Collections.newSetFromMap(new WeakHashMap<TernaryTree,Boolean>());
            instances.put(tree.world.provider.getDimensionId(), treesForWorld);
        }
        treesForWorld.add(tree);
    }

    public static Set<TernaryTree> getTreesContainingPos(World worldIn, BlockPos pos) {
        Set<TernaryTree> treesForWorld = instances.get(worldIn.provider.getDimensionId());
        Set<TernaryTree> containingTrees = new HashSet<TernaryTree>();
        if(treesForWorld == null) {return containingTrees;}
        containingTrees.addAll(treesForWorld);

        Iterator<TernaryTree> itr = containingTrees.iterator();
        while(itr.hasNext()) {
            TernaryTree tree = itr.next();
            if(!tree.baseNode.containsPos(pos)) {itr.remove();}
        }
        return containingTrees;
    }

    private class TernaryNode {

        private BlockPos pos;

        private TernaryNode northNode;
        private TernaryNode eastNode;
        private TernaryNode southNode;
        private TernaryNode westNode;

        // This should not be used anywhere but in the readNode function and NBT constructor
        public TernaryNode() {}

        public TernaryNode(BlockPos pos) {
            this.pos = pos;
        }

        public boolean containsPos(BlockPos pos) {
            return (this.pos.equals(pos))
                    || (northNode != null && northNode.containsPos(pos))
                    || (eastNode != null && eastNode.containsPos(pos))
                    || (southNode != null && southNode.containsPos(pos))
                    || (westNode != null && westNode.containsPos(pos));
        }

        public void evaluateNode() {
            northNode = evaluatePos(pos.offsetNorth());
            if(northNode != null) {northNode.evaluateNode();}
            eastNode = evaluatePos(pos.offsetEast());
            if(eastNode != null) {eastNode.evaluateNode();}
            southNode = evaluatePos(pos.offsetSouth());
            if(southNode != null) {southNode.evaluateNode();}
            westNode = evaluatePos(pos.offsetWest());
            if(westNode != null) {westNode.evaluateNode();}
        }

    }

    private TernaryNode evaluatePos(BlockPos pos) {
        if(baseNode.containsPos(pos)) {return null;}

        TileEntity tileEntity = world.getTileEntity(pos);
        if(!(tileEntity instanceof IRedbusCompatible)) {return null;}

        IRedbusCompatible compatibleTile = (IRedbusCompatible) tileEntity;
        if(compatibleTile.isDevice()) {
            devices.put(compatibleTile.getBusAddress(), pos);
            return null;
        }

        return new TernaryNode(pos);
    }

}
