import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * ConfigExporterJackson - Exports slot machine configuration to JSON using Jackson library
 */
public class ConfigExporter {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static ObjectNode createOddsConfig(int multiplier) {
        ObjectNode oddsConfig = objectMapper.createObjectNode();

        for (int i = 1; i < Config.payout.length; i++) {
            ArrayNode oddsRow = objectMapper.createArrayNode();
            for (int j = 0; j < Config.payout[i].length; j++) {
                if (Config.payout[i][j] != 0) {
                    ObjectNode oddsRowNode = objectMapper.createObjectNode();
                    oddsRowNode.put("from", j);
                    if (j == Config.payout[i].length - 1) {
                        oddsRowNode.put("to", 37);
                    } else {
                        oddsRowNode.put("to", j);
                    }
                    oddsRowNode.put("odds", Config.payout[i][j] * multiplier);
                    oddsRow.add(oddsRowNode);
                }
            }
            oddsConfig.set(MessageFormat.format("{0}", i - 1), oddsRow);
        }

        return oddsConfig;
    }
    
    public static ObjectNode createFgRoundsConfig() {
        ObjectNode fgRoundsConfig = objectMapper.createObjectNode();
        ArrayNode initialOddsRow = objectMapper.createArrayNode();
        for (int i = 0; i < Config.fgRoundsArray.length; i++) {
            if (Config.fgRoundsArray[i] == 0) {
                continue;
            }
            ObjectNode initialOddsRowNode = objectMapper.createObjectNode();
            initialOddsRowNode.put("count", i);
            initialOddsRowNode.put("odds", Config.fgRoundsArray[i]);
            initialOddsRow.add(initialOddsRowNode);
        }
        fgRoundsConfig.set("initialOdds", initialOddsRow);
        return fgRoundsConfig;
    }

    public static ArrayNode createRageMeterConfig() {
        ArrayNode rageMeterRow = objectMapper.createArrayNode();
        for (int i = 0; i < Config.featureRageMeterRequire.length; i++) {
            rageMeterRow.add(Config.featureRageMeterRequire[i]);
        }
        return rageMeterRow;
    }

    public static void exportConfigJson(String filename, int multiplier) {
        try {
            ObjectNode rootNode = objectMapper.createObjectNode();

            ObjectNode oddsConfig = createOddsConfig(multiplier);
            rootNode.set("oddsConfig", oddsConfig);

            ObjectNode fgRoundsConfig = createFgRoundsConfig();
            rootNode.set("fgRoundsConfig", fgRoundsConfig);

            ArrayNode rageMeterConfig = createRageMeterConfig();
            rootNode.set("rageMeterConfig", rageMeterConfig);

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new FileWriter(filename), rootNode);

            System.out.println("Configuration exported successfully to: " + filename);

        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
    
    public static void exportReelsJson(String filename, int[][][] reels) {
        try {
            ObjectNode rootNode = objectMapper.createObjectNode();
            int[][][] newReels = autoReduceItem(reels, 1);
            rootNode.set("reels", objectMapper.valueToTree(newReels));
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new FileWriter(filename), rootNode);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
    
    private static int[][][] autoReduceItem(int[][][] reels, int i) {
        int[][][] reducedReels = new int[reels.length][][];
        for (int j = 0; j < reels.length; j++) {
            reducedReels[j] = new int[reels[j].length][];
            for (int k = 0; k < reels[j].length; k++) {
                reducedReels[j][k] = new int[reels[j][k].length];
                for (int l = 0; l < reels[j][k].length; l++) {
                    reducedReels[j][k][l] = reels[j][k][l] - i;
                }
            }
        }
        return reducedReels;
    }

    public static void main(String[] args) {
        System.out.println("=== SLOT MACHINE CONFIGURATION EXPORTER ===");
        System.out.println();
        
        // Export basic JSON
        int multiplier = 1;
        try {
            multiplier = args.length > 0 ? Integer.parseInt(args[0]) : 1;
        } catch (Exception e) {
            System.out.println(String.format("Invalid multiplier: %s, using default value 1", args[0]));
        }

        exportConfigJson("configs.json", multiplier);
        
        exportReelsJson("ng_reels.json", NgReels.reels);
        exportReelsJson("fg_reels.json", FgReels.reels);
        
        System.out.println();
        System.out.println("Export completed! Check the generated JSON files.");
    }
}

