
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import static java.lang.Math.pow;


public class Main {
    public static int totalRounds = 20_000_000;
    public static double theoreticalRTP = 0.97;
    public static double wallet = 100;
    public static double cashOutFreq = 0;
    public static Statistician stat;
    public static Random random = new Random();
    public static int currScNumber;
    public static double nearlyWinFreq = 0;
    public static double ngNoWinFreq = 0;
    public static double mode = 2;

    public static void main(String[] args) {
        stat = new Statistician();
        for (int r = 0; r < totalRounds; r++) {
            wallet--;
            NgResult ngResult = new NgResult();
            ngResult.category = "Ng";

            if (mode > 0) {// Buy Feature
                ngResult.reelSetIndex = 0;
                genGrid(NgReels.reels[ngResult.reelSetIndex], ngResult);
                insertSC(ngResult);
            } else {
                ngResult.reelSetIndex = (int) genSymbolByWeights(NgReels.reelsIndex, NgReels.weight, NgReels.weightSum);
                genGrid(NgReels.reels[ngResult.reelSetIndex], ngResult);
            }

            ngResult.gridMulti = iniGridMulti();
            //            printGrid(ngResult.reels);
            ArrayList<Prize> prizeList = new ArrayList<>();
            calcPrize(ngResult, prizeList);
            ngResult.winScore += calcPayout(prizeList);
            ngResult.rageCounter += calcRageMeter(prizeList);
            updateNgFeatureTrigger(ngResult);

            if (prizeList.isEmpty()) {
                ngNoWinFreq++;
            }
            if (ngResult.nearlyWin && prizeList.isEmpty()) {
                nearlyWinFreq++;
            }
            while (!prizeList.isEmpty() && ngResult.winScore < Config.maxPayout) {
                stat.ngCascadeFreq++;
                markDetonated(ngResult, prizeList);
                detonateAndCascadeGrid(ngResult);
                renewGrid(ngResult.reels);
                prizeList.clear();
                calcPrize(ngResult, prizeList);
                while (prizeList.isEmpty() && !ngResult.featureQueue.isEmpty()) {
                    // Process Rage Meter
                    processRageMeter(ngResult);//
                    renewGrid(ngResult.reels);
                    calcPrize(ngResult, prizeList);
                }
                ngResult.winScore += calcPayout(prizeList);
                ngResult.rageCounter += calcRageMeter(prizeList);
                updateNgFeatureTrigger(ngResult);
            }

            currScNumber = countSC(ngResult.reels);
            if (currScNumber > 6) {
                currScNumber = 6;
            }
            if (ngResult.winScore > 0) {
                stat.ngHitFreq++;
            }
            if (ngResult.winScore > Config.maxPayout) {
                ngResult.winScore = Config.maxPayout;
            }

            // FG
            double fgWin = 0;
            FgResult fgResult = new FgResult();
            fgResult.category = "Fg";
            int fgRounds = Config.fgRoundsArray[currScNumber];
            if (fgRounds > 0 && ngResult.winScore < Config.maxPayout) {
                stat.fgTriggerFreq++;
                if(mode == 2){ // buy super
                    fgResult.gridMulti = iniSuperGridMulti();
                } else {
                    fgResult.gridMulti = iniGridMulti();
                }

                while (fgRounds > 0 && fgWin + ngResult.winScore < Config.maxPayout) {
                    stat.fgTotalRounds++;
                    fgResult.reels.clear();
                    fgResult.reelSetIndex = (int) genSymbolByWeights(FgReels.reelsIndex, FgReels.weight, FgReels.weightSum);
                    genGrid(FgReels.reels[fgResult.reelSetIndex], fgResult);
                    prizeList.clear();
                    calcPrize(fgResult, prizeList);
                    fgResult.winScore += calcPayout(prizeList);
                    fgResult.rageCounter += calcRageMeter(prizeList);
                    updateFgFeatureTrigger(fgResult);
                    while (!prizeList.isEmpty() && fgWin + fgResult.winScore + ngResult.winScore < Config.maxPayout) {
                        stat.fgCascadeFreq++;
                        markDetonated(fgResult, prizeList);
                        detonateAndCascadeGrid(fgResult);
                        renewGrid(fgResult.reels);
                        prizeList.clear();
                        calcPrize(fgResult, prizeList);
                        while (prizeList.isEmpty() && !fgResult.featureQueue.isEmpty()) {
                            // Process Rage Meter
                            processRageMeter(fgResult);//
                            renewGrid(fgResult.reels);
                            calcPrize(fgResult, prizeList);
                        }
                        fgResult.winScore += calcPayout(prizeList);
                        fgResult.rageCounter += calcRageMeter(prizeList);
                        updateFgFeatureTrigger(fgResult);
                    }

                    if (fgResult.winScore > 0) {
                        stat.fgHitFreq++;
                        fgWin += fgResult.winScore;
                        fgResult.winScore = 0;
                    }

                    currScNumber = countSC(fgResult.reels);
                    if (currScNumber > 6) {
                        currScNumber = 6;
                    }
                    // retrigger FG
                    fgRounds += Config.fgRoundsArray[currScNumber];
                    fgRounds--;
                }
                // adjust win if over max
                if (ngResult.winScore + fgWin > Config.maxPayout) {
                    fgWin = Config.maxPayout - ngResult.winScore;
                }
                logPrize(fgWin, stat.fgPrizeDist);
            }
            // Round end

            if (ngResult.winScore + fgWin > 0) {
                stat.hitFreq++;
            }

            // Log Win
            logPrize(ngResult.winScore + fgWin, stat.ngPrizeDist);
            stat.ngTotalWin += ngResult.winScore;
            stat.fgTotalWin += fgWin;
            stat.totalWin += (ngResult.winScore + fgWin);
            wallet += (ngResult.winScore + fgWin);
            if (wallet <= 0 || wallet > 250) {
                wallet = 100;
                cashOutFreq++;
            }
            recordMaxWinAndMSE(ngResult.winScore + fgWin);
        }

        // Simulation end
        printResult();
    }

    private static void insertSC(NgResult ngResult) {
        Random random = new Random();
        int currSC = countSC(ngResult.reels);
        int targetSC = (int) genSymbolByWeights(Config.buyFeatureSCNum, Config.buyFeatureSCNumWeight, Config.buyFeatureSCNumWeightSum);
        while (currSC < targetSC) {
            int randColIndex = random.nextInt(6);
            int randRowIndex = random.nextInt(5);
            while (ngResult.reels.get(randColIndex).get(randRowIndex).symbol == Config.SC) {
                randColIndex = random.nextInt(6);
                randRowIndex = random.nextInt(5);
            }
            ngResult.reels.get(randColIndex).get(randRowIndex).symbol = Config.SC;
            currSC++;
        }
    }

    private static int countSC(ArrayList<ArrayList<Grid>> reels) {
        int result = 0;
        for (int i = 0; i < Config.gridSize; i++) {
            for (int j = 0; j < Config.gridSize; j++) {
                if (reels.get(i).get(j).symbol == Config.SC) {
                    result++;
                }
            }
        }
        return result;
    } // Checked

    private static void updateNgFeatureTrigger(Result result) {
        while (result.rageMeterLevel < 4 && result.rageCounter >= Config.featureRageMeterRequire[result.rageMeterLevel]) {
            result.featureQueue.offer(result.rageMeterLevel);
            result.rageMeterLevel++;
            if (result.rageMeterLevel == 4) {
                break;
            }
        }
        // won't reset
    } // Checked

    private static void updateFgFeatureTrigger(Result result) {
        while (result.rageCounter >= Config.featureRageMeterRequire[result.rageMeterLevel]) {
            result.featureQueue.offer(result.rageMeterLevel);
            result.rageMeterLevel++;
            if (result.rageMeterLevel == 4) {
                break;
            }
        }

        // Reset
        if (result.rageMeterLevel == 4) {
            result.rageCounter -= Config.featureRageMeterRequire[3];
            result.rageMeterLevel = 0;
            while (result.rageCounter >= Config.featureRageMeterRequire[result.rageMeterLevel]) {
                result.featureQueue.offer(result.rageMeterLevel);
                result.rageMeterLevel++;
                // rageMeter increase max 36 per spin, impossible to reach 60/60 twice.
            }
        }
    } // Checked

    private static void processRageMeter(Result result) {
        if (!result.featureQueue.isEmpty()) {
            int feature = result.featureQueue.poll();
            if (feature == 0) {
                processStage0(result);
            } else if (feature == 1) {
                processStage1(result);
            } else if (feature == 2) {
                processStage2(result);
            } else if (feature == 3) {
                processStage3(result);
            }
        }
    } // Checked

    private static void processStage3(Result result) {
        if (Objects.equals(result.category, "Ng")) {
            stat.ngRageMeterFreq[3]++;
        } else if (Objects.equals(result.category, "Fg")) {
            stat.fgRageMeterFreq[3]++;
        }
        // replace small symbols
        for (int i = 0; i < Config.gridSize; i++) {
            for (int j = 0; j < Config.gridSize; j++) {
                if (result.reels.get(i).get(j).symbol >= 5 && result.reels.get(i).get(j).symbol <= 8) {
                    result.reels.get(i).get(j).symbol = (int) genSymbolByWeights(Config.phase4replaceSymbol, Config.phase4replaceSymbolWeight, Config.phase4replaceSymbolWeightSum);
//                    result.gridMulti[i][j]++;
                }
            }
        }
//        detonateAndCascadeGrid(result);
    } // Checked

    private static void processStage2(Result result) {
        if (Objects.equals(result.category, "Ng")) {
            stat.ngRageMeterFreq[2]++;
        } else if (Objects.equals(result.category, "Fg")) {
            stat.fgRageMeterFreq[2]++;
        }

        // Rand select 2 col to remove
        int[] colIndex = new int[]{0, 1, 2, 3, 4, 5};
        shuffleArr(colIndex);
        int col1 = colIndex[0];
        int col2 = colIndex[1];

        for (int j = 0; j < Config.gridSize; j++) {
            result.gridMulti[col1][j]++;
            if (result.reels.get(col1).get(j).symbol != Config.SC) {
                result.reels.get(col1).get(j).isDetonated = true;
            }
            result.gridMulti[col2][j]++;
            if (result.reels.get(col2).get(j).symbol != Config.SC) {
                result.reels.get(col2).get(j).isDetonated = true;
            }
        }
        detonateAndCascadeGrid(result);
    } // Checked

    private static void processStage1(Result result) {
        if (Objects.equals(result.category, "Ng")) {
            stat.ngRageMeterFreq[1]++;
        } else if (Objects.equals(result.category, "Fg")) {
            stat.fgRageMeterFreq[1]++;
        }
        // remove 3 x 3 (excl. SC)
        int x = random.nextInt(Config.gridSize - 2);
        int y = random.nextInt(Config.gridSize - 2);
        for (int i = x; i < x + 3; i++) {
            for (int j = y; j < y + 3; j++) {
                result.gridMulti[x][y]++;
                if (result.reels.get(i).get(j).symbol != Config.SC) {
                    result.reels.get(i).get(j).isDetonated = true;
                }
            }
        }
        detonateAndCascadeGrid(result);
    } // Checked

    private static void processStage0(Result result) {
        if (Objects.equals(result.category, "Ng")) {
            stat.ngRageMeterFreq[0]++;
        } else if (Objects.equals(result.category, "Fg")) {
            stat.fgRageMeterFreq[0]++;
        }
        // randomly remove 6 symbols (excl. SC)
        for (int i = 0; i < 6; i++) {
            int x = random.nextInt(Config.gridSize);
            int y = random.nextInt(Config.gridSize);
            while (result.reels.get(x).get(y).symbol == Config.SC || result.reels.get(x).get(y).isDetonated) {
                x = random.nextInt(Config.gridSize);
                y = random.nextInt(Config.gridSize);
            }
            result.reels.get(x).get(y).isDetonated = true;
            result.gridMulti[x][y]++;
        }
        detonateAndCascadeGrid(result);
    } // Checked

    private static int calcRageMeter(ArrayList<Prize> prizeList) {
        int result = 0;
        for (int i = 0; i < prizeList.size(); i++) {
            result += prizeList.get(i).link;
        }
        return result;
    } // Checked

    private static void renewGrid(ArrayList<ArrayList<Grid>> reels) {
        for (int i = 0; i < Config.gridSize; i++) {
            for (int j = 0; j < Config.gridSize; j++) {
                reels.get(i).get(j).used = false;
            }
        }
    }// Checked

    private static void markDetonated(Result result, ArrayList<Prize> prizeList) {
        // 0. Tag whether this grid should be detonated or not?
        for (int i = 0; i < prizeList.size(); i++) {
            for (int j = 0; j < prizeList.get(i).position.size(); j++) {
                int x = prizeList.get(i).position.get(j).x;
                int y = prizeList.get(i).position.get(j).y;
                result.reels.get(x).get(y).isDetonated = true;
            }
        }
    }// Checked

    private static void detonateAndCascadeGrid(Result result) {
        // 1. Detonate
        for (int i = 0; i < Config.gridSize; i++) {
            for (int j = Config.gridSize - 1; j >= 0; j--) {
                if (result.reels.get(i).get(j).isDetonated) {
                    result.reels.get(i).remove(j);
                }
            }
        }

        // 2. Cascade
        for (int i = 0; i < Config.gridSize; i++) {
            while (result.reels.get(i).size() < Config.gridSize) {
                result.stopIndex[i]--;
                int symbol;
                if (Objects.equals(result.category, "Ng")) {
                    if (result.stopIndex[i] < 0) {
                        result.stopIndex[i] = NgReels.reels[result.reelSetIndex][i].length - 1;
                    }
                    symbol = NgReels.reels[result.reelSetIndex][i][result.stopIndex[i]];
                    result.reels.get(i).add(0, new Grid(symbol));
                } else if (Objects.equals(result.category, "Fg")) {
                    if (result.stopIndex[i] < 0) {
                        result.stopIndex[i] = FgReels.reels[result.reelSetIndex][i].length - 1;
                    }
                    symbol = FgReels.reels[result.reelSetIndex][i][result.stopIndex[i]];
                    result.reels.get(i).add(0, new Grid(symbol));
                }
            }
        }
    } // Checked

    private static int[][] iniGridMulti() {
        return new int[][]{
                {-1, -1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1, -1},
        };
    } // Checked

    private static int[][] iniSuperGridMulti() {
        return new int[][]{
                {1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1},
        };
    } // Checked

    private static void calcPrize(Result result, ArrayList<Prize> prizeList) {
        for (int i = 0; i < Config.gridSize; i++) {
            for (int j = 0; j < Config.gridSize; j++) {
                int symbol = result.reels.get(i).get(j).symbol;
                if (!result.reels.get(i).get(j).used && symbol != 0 && symbol != Config.space && symbol != Config.SC) {
                    Prize prize = new Prize();
                    prize.symbol = symbol;
                    prize.position.add(new Coordinate(i, j));
                    prize.link = walk(i, j, prize, result.reels);
                    if (prize.link == 4) {
                        result.nearlyWin = true;
                    }
                    if (prize.link >= Config.minLinkRequirement) {
                        prizeList.add(prize);
                    }
                }
            }
        }
        calcMulti(prizeList, result.gridMulti);
    } // Checked

    private static void calcMulti(ArrayList<Prize> prizeList, int[][] gridMulti) {
        for (int i = 0; i < prizeList.size(); i++) {
            prizeList.get(i).multi = calcPrizeMulti(prizeList.get(i).position, gridMulti);
        }
    }// Checked

    private static double calcPrizeMulti(ArrayList<Coordinate> position, int[][] gridMulti) {
        int result = 0;
        for (int i = 0; i < position.size(); i++) {
            int x = position.get(i).x;
            int y = position.get(i).y;
            if (gridMulti[x][y] > 0) {
                if (gridMulti[x][y] * 2 > Config.singleGridMaxMulti) {
                    result += Config.singleGridMaxMulti;
                } else {
                    result += gridMulti[x][y] * 2;
                }
            }
            // calc payout first, then update grid multi
            gridMulti[x][y]++;
        }
        if (result > 0) {
            return result;
        } else return 1;
    }// Checked

    private static double calcPayout(ArrayList<Prize> prizeList) {
        double result = 0.0;
        for (int i = 0; i < prizeList.size(); i++) {
            if (prizeList.get(i).link >= 15) {
                prizeList.get(i).winScore = Config.payout[prizeList.get(i).symbol][15] * prizeList.get(i).multi;
            } else {
                prizeList.get(i).winScore = Config.payout[prizeList.get(i).symbol][prizeList.get(i).link] * prizeList.get(i).multi;
            }
            result += prizeList.get(i).winScore;
        }
        return result;
    } // Checked

    private static void recordMaxWinAndMSE(double roundWin) {
        if (roundWin > stat.maxWin) {
            stat.maxWin = roundWin;
        }
        stat.MSE += pow(roundWin - theoreticalRTP, 2);
    } // Checked

    private static void printResult() {
        System.out.println("Total RTP: " + stat.totalWin / totalRounds);
        System.out.println("NG RTP: " + stat.ngTotalWin / totalRounds);
        System.out.println("FG RTP: " + stat.fgTotalWin / totalRounds);
        System.out.println("NG hit rate: " + stat.ngHitFreq / totalRounds);
        System.out.println("hit rate: " + stat.hitFreq / totalRounds);
        System.out.println("FG trigger rate: " + stat.fgTriggerFreq / totalRounds);
        System.out.println("FG Freq: " + totalRounds / stat.fgTriggerFreq);
        System.out.println("FG Multi: " + stat.fgTotalWin / stat.fgTriggerFreq);
        System.out.println("FG Avg Rounds: " + stat.fgTotalRounds / stat.fgTriggerFreq);
        System.out.println("NG Cascade ratio: " + stat.ngCascadeFreq / stat.ngHitFreq);
        System.out.println("FG Cascade ratio: " + stat.fgCascadeFreq / stat.fgHitFreq);
        System.out.println("Max win: " + stat.maxWin);
        System.out.println("cash out freq: " + totalRounds / cashOutFreq);
        System.out.println("Standard dev.: " + pow(stat.MSE / totalRounds, 0.5));
        System.out.println("Nearly Win Rate: " + nearlyWinFreq / ngNoWinFreq);
        System.out.println("---Ng Rage Meter Dist.---");
        for (int i = 0; i < stat.ngRageMeterFreq.length; i++) {
            System.out.print("Feature " + i + ": " + '\t');
            System.out.println(stat.ngRageMeterFreq[i] / totalRounds);
        }
        System.out.println("---Fg Rage Meter Dist.---");
        for (int i = 0; i < stat.fgRageMeterFreq.length; i++) {
            System.out.print("Feature " + i + ": " + '\t');
            System.out.println(stat.fgRageMeterFreq[i] / stat.fgTriggerFreq);
        }
        System.out.println("---Overall Dist.---");
        for (int i = 0; i < stat.ngPrizeDist.length; i++) {
            System.out.println(stat.ngPrizeDist[i] / totalRounds);
        }
        System.out.println("---Free Game Dist.---");
        for (int i = 0; i < stat.fgPrizeDist.length; i++) {
            System.out.println(stat.fgPrizeDist[i] / stat.fgTriggerFreq);
        }
    } // Checked

    public static int walk(int i, int j, Prize prize, ArrayList<ArrayList<Grid>> grid) {
        grid.get(i).get(j).used = true;
        int result = 1;
        result += move(i - 1, j, prize, grid); // go top
        result += move(i, j + 1, prize, grid); // go right
        result += move(i, j - 1, prize, grid); // go left
        result += move(i + 1, j, prize, grid); // go down
        return result;
    } // Checked

    public static int move(int i, int j, Prize prize, ArrayList<ArrayList<Grid>> grid) {
        // check legit
        if (!checkPositionLegit(i, j)) {
            return 0;
        }

        // check used
        if (grid.get(i).get(j).used) {
            return 0;
        }

        // if same symbol or wild, keep walking.
        if (grid.get(i).get(j).symbol == prize.symbol) {
            prize.position.add(new Coordinate(i, j));
            return walk(i, j, prize, grid);
        } else return 0;
    } // Checked

    public static boolean checkPositionLegit(int i, int j) {
        return i >= 0 && j >= 0 && i < Config.gridSize && j < Config.gridSize;
    } // Checked

    private static void logPrize(double winMulti, double[] prizeDist) {
        if (winMulti == 0) {
            prizeDist[0]++;
        } else if (winMulti > 0 && winMulti < 5) {
            prizeDist[1]++;
        } else if (winMulti >= 5 && winMulti < 10) {
            prizeDist[2]++;
        } else if (winMulti >= 10 && winMulti < 20) {
            prizeDist[3]++;
        } else if (winMulti >= 20 && winMulti < 35) {
            prizeDist[4]++;
        } else if (winMulti >= 35 && winMulti < 50) {
            prizeDist[5]++;
        } else if (winMulti >= 50 && winMulti < 100) {
            prizeDist[6]++;
        } else if (winMulti >= 100 && winMulti < 500) {
            prizeDist[7]++;
        } else if (winMulti >= 500 && winMulti < 1000) {
            prizeDist[8]++;
        } else if (winMulti >= 1000 && winMulti < 5000) {
            prizeDist[9]++;
        } else if (winMulti >= 5000 && winMulti < 10000) {
            prizeDist[10]++;
        } else if (winMulti >= 10000 && winMulti < 20000) {
            prizeDist[11]++;
        } else if (winMulti >= 20000 && winMulti < 50000) {
            prizeDist[12]++;
        } else if (winMulti >= 50000) {
            prizeDist[13]++;
        }
    } // Checked

    public static void genGrid(int[][] reelSet, Result result) {
        for (int i = 0; i < Config.gridSize; i++) {
            result.stopIndex[i] = (int) (Math.random() * reelSet[i].length);
            result.reels.add(new ArrayList<>());
            for (int j = 0; j < Config.gridSize; j++) {
                int symbol = reelSet[i][(result.stopIndex[i] + j) % reelSet[i].length];
                result.reels.get(i).add(new Grid(symbol));
            }
        }
    } // Checked

    private static double genSymbolByWeights(int[] arr, double[] weights, double sumWeight) {
        double rand = Math.random();
        double accWeight = weights[0];
        for (int i = 0; i < weights.length; i++) {
            if (rand < accWeight / sumWeight) {
                return arr[i];
            }
            accWeight += weights[i + 1];
        }
        return arr[arr.length - 1];
    } // // Checked


    public static void shuffleArr(int[] deck) {
        // read array from the highest index to lowest
        for (int i = deck.length - 1; i >= 1; i--) {
            // generate a random number j such that 0 <= j <= i
            int j = random.nextInt(i + 1);

            // swap the current element with the randomly generated index
            int temp = deck[i];
            deck[i] = deck[j];
            deck[j] = temp;
        }
    }

    private static void printGrid(ArrayList<ArrayList<Grid>> grid) {
        for (int i = 0; i < Config.gridSize; i++) {
            for (int j = 0; j < Config.gridSize; j++) {
                System.out.print(grid.get(i).get(j).symbol + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
}