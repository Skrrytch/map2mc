package eu.jgdi.mc.map2mc.config.csv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import eu.jgdi.mc.map2mc.utils.Logger;

public class BlockStack {

    private static Logger logger = Logger.logger();

    private List<String> blockIdList;

    private List<CompoundDef> compoundDefList;

    private float probability;

    public BlockStack(List<String> blockIdList, float probability) {
        this.blockIdList = blockIdList;
        this.probability = probability;
    }

    public List<CompoundDef> getCompoundDefList() {
        return compoundDefList;
    }

    public void setCompoundDefList(List<CompoundDef> compoundDefList) {
        this.compoundDefList = compoundDefList;
    }

    public List<String> getBlockIdList() {
        return blockIdList;
    }

    public float getProbability() {
        return probability;
    }

    public static BlockStack parseStack(String blockStackAsString) {
        if (blockStackAsString == null || blockStackAsString.isBlank()) {
            return null;
        }
        // First of all lets see if we have a probability value as a postfix after a colon (otherwise use 1 for 100%)
        // Example: '2*oak_log,lantern:25'
        String[] stackAndProbability = blockStackAsString.split(":");
        if (stackAndProbability.length > 2) {
            throw new IllegalArgumentException("Block stack list definition not valid: '" + blockStackAsString + "'");
        }
        float probability = stackAndProbability.length >= 2 ? Integer.parseInt(stackAndProbability[1].trim()) / 100f : 1f;

        // Now lets see if we have a list of blocks separated by comma
        // Example: '2*oak_log,lantern'
        List<String> blockStringList = Arrays.stream(stackAndProbability[0].split(","))
                .map(String::trim)
                .collect(Collectors.toList());
        List<String> blockIdList = new ArrayList<>();
        for (String blockDef : blockStringList) {
            // each block definition may have a preceeding multiplicator
            // Example: '2*oak_log'
            String[] optionalFactorAndBlockId = blockDef.split("\\*");
            if (optionalFactorAndBlockId.length > 2) {
                throw new IllegalArgumentException("Block stack definition not valid: '" + blockDef + "'");
            }
            int factor = optionalFactorAndBlockId.length == 2 ? Integer.parseInt(optionalFactorAndBlockId[0]) : 1;
            String blockID = optionalFactorAndBlockId.length == 2 ? optionalFactorAndBlockId[1] : optionalFactorAndBlockId[0];
            for (int i = 0; i < factor; i++) {
                blockIdList.add(blockID);
            }
        }
        return new BlockStack(blockIdList, probability);
    }

    public static List<BlockStack> parseStacksWithProbability(String blockStackListAsString) {
        if (blockStackListAsString == null || blockStackListAsString.isBlank()) {
            return null;
        }
        List<BlockStack> result = new ArrayList<>();
        List<String> blockStackAsStringList = Arrays.stream(blockStackListAsString.split("\\|"))
                .map(String::trim)
                .collect(Collectors.toList());
        for (String blockStrackAsString : blockStackAsStringList) {
            BlockStack stack = parseStack(blockStrackAsString);
            if (stack != null) {
                result.add(stack);
            } else {
                logger.warn("Parse failure for ''{0}'' in ''{1}''", blockStrackAsString, blockStackAsStringList);
            }
        }
        return result;
    }
}
