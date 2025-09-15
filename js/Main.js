import Config from "./Config.js";
import Coordinate from "./Coordinate.js";
import FgReels from "./FgReels.js";
import FgResult from "./FgResult.js";
import Grid from "./Grid.js";
import NgReels from "./NgReels.js";
import NgResult from "./NgResult.js";
import Prize from "./Prize.js";
import Statistician from "./Statistician.js";

class Main {
    static totalRounds = 100_000_000;
    static theoreticalRTP = 0.97;
    static gridSize = 6;
    static singleGridMaxMulti = 20;
    static maxPayout = 10000;
    static wallet = 100;
    static cashOutFreq = 0;
    static stat;
    static random = Math.random;
    static currScNumber;

    static main() {
        Main.stat = new Statistician();
        for (let r = 0; r < Main.totalRounds; r++) {
            Main.wallet--;
            const ngResult = new NgResult();
            ngResult.category = "Ng";
            ngResult.reelSetIndex = Math.floor(
                Main.genSymbolByWeights(
                    NgReels.reelsIndex,
                    NgReels.weightSum,
                    NgReels.weight
                )
            );
            Main.genGrid(NgReels.reels[ngResult.reelSetIndex], ngResult);
            ngResult.gridMulti = Main.iniGridMulti();

            // printGrid(ngResult.reels);

            let prizeList = [];
            Main.calcPrize(ngResult, prizeList);
            ngResult.winScore += Main.calcPayout(prizeList);
            ngResult.rageCounter += Main.calcRageMeter(prizeList);
            Main.updateFeatureTrigger(ngResult);
            while (prizeList.length > 0 && ngResult.winScore < Main.maxPayout) {
                Main.stat.ngCascadeFreq++;
                Main.markDetonated(ngResult, prizeList);
                Main.detonateAndCascadeGrid(ngResult);
                Main.renewGrid(ngResult.reels);
                prizeList = [];
                Main.calcPrize(ngResult, prizeList);
                while (prizeList.length === 0 && !ngResult.isQueueEmpty()) {
                    // Process Rage Meter
                    Main.processRageMeter(ngResult);
                    Main.renewGrid(ngResult.reels);
                    Main.calcPrize(ngResult, prizeList);
                }
                ngResult.winScore += Main.calcPayout(prizeList);
                ngResult.rageCounter += Main.calcRageMeter(prizeList);
                Main.updateFeatureTrigger(ngResult);
            }
            Main.currScNumber = Main.countSC(ngResult.reels);
            if (Main.currScNumber > 6) {
                Main.currScNumber = 6;
            }
            if (ngResult.winScore > 0) {
                Main.stat.ngHitFreq++;
            }

            // FG
            let fgWin = 0;
            const fgResult = new FgResult();
            fgResult.category = "Fg";
            let fgRounds = Config.fgRoundsArray[Main.currScNumber];
            if (fgRounds > 0) {
                Main.stat.fgTriggerFreq++;
                fgResult.gridMulti = Main.iniGridMulti();
                while (
                    fgRounds > 0 &&
                    fgWin + ngResult.winScore < Main.maxPayout
                ) {
                    fgResult.reels = [];
                    fgResult.reelSetIndex = Math.floor(
                        Main.genSymbolByWeights(
                            FgReels.reelsIndex,
                            FgReels.weightSum,
                            FgReels.weight
                        )
                    );
                    Main.genGrid(
                        FgReels.reels[fgResult.reelSetIndex],
                        fgResult
                    );
                    prizeList = [];
                    Main.calcPrize(fgResult, prizeList);
                    fgResult.winScore += Main.calcPayout(prizeList);
                    fgResult.rageCounter += Main.calcRageMeter(prizeList);
                    Main.updateFeatureTrigger(fgResult);
                    while (prizeList.length > 0) {
                        Main.stat.fgCascadeFreq++;
                        Main.markDetonated(fgResult, prizeList);
                        Main.detonateAndCascadeGrid(fgResult);
                        Main.renewGrid(fgResult.reels);
                        prizeList = [];
                        Main.calcPrize(fgResult, prizeList);
                        while (
                            prizeList.length === 0 &&
                            !fgResult.isQueueEmpty()
                        ) {
                            // Process Rage Meter
                            Main.processRageMeter(fgResult);
                            Main.renewGrid(fgResult.reels);
                            Main.calcPrize(fgResult, prizeList);
                        }
                        fgResult.winScore += Main.calcPayout(prizeList);
                        fgResult.rageCounter += Main.calcRageMeter(prizeList);
                        Main.updateFeatureTrigger(fgResult);
                    }

                    // Renew Rage meter
                    if (fgResult.rageCounter >= 80) {
                        fgResult.rageCounter = 0;
                        fgResult.featureTriggered = [
                            false,
                            false,
                            false,
                            false,
                        ];
                    }

                    if (fgResult.winScore > 0) {
                        Main.stat.fgHitFreq++;
                        fgWin += fgResult.winScore;
                        fgResult.winScore = 0;
                    }

                    Main.currScNumber = Main.countSC(fgResult.reels);
                    if (Main.currScNumber > 6) {
                        Main.currScNumber = 6;
                    }

                    // retrigger FG
                    fgRounds += Config.fgRoundsArray[Main.currScNumber];
                    fgRounds--;
                }
                if (fgWin + ngResult.winScore > Main.maxPayout) {
                    fgWin = Main.maxPayout - ngResult.winScore;
                }

                Main.logPrize(fgWin, Main.stat.fgPrizeDist);
            }

            if (ngResult.winScore > Main.maxPayout) {
                ngResult.winScore = Main.maxPayout;
            }

            // Round end
            if (ngResult.winScore + fgWin > 0) {
                Main.stat.hitFreq++;
            }

            // Log Ng Win
            Main.logPrize(ngResult.winScore + fgWin, Main.stat.ngPrizeDist);

            Main.stat.ngTotalWin += ngResult.winScore;
            Main.stat.fgTotalWin += fgWin;
            Main.stat.totalWin += ngResult.winScore + fgWin;
            Main.wallet += ngResult.winScore + fgWin;
            if (Main.wallet <= 0 || Main.wallet > 250) {
                Main.wallet = 100;
                Main.cashOutFreq++;
            }
            Main.recordMaxWinAndMSE(ngResult.winScore + fgWin);
        }

        // Simulation end
        Main.printResult();
    }

    static countSC(reels) {
        let result = 0;
        for (let i = 0; i < Main.gridSize; i++) {
            for (let j = 0; j < Main.gridSize; j++) {
                if (reels[i][j].symbol === Config.SC) {
                    result++;
                }
            }
        }
        return result;
    } // Checked

    static updateFeatureTrigger(result) {
        for (let i = 0; i < Config.featureRageMeterRequire.length; i++) {
            if (
                result.rageCounter >= Config.featureRageMeterRequire[i] &&
                !result.featureTriggered[i]
            ) {
                result.offerToQueue(i);
                result.featureTriggered[i] = true;
            }
        }
    } // Checked

    static processRageMeter(result) {
        if (!result.isQueueEmpty()) {
            const feature = result.pollFromQueue();
            if (feature === 0) {
                Main.processStage0(result);
            } else if (feature === 1) {
                Main.processStage1(result);
            } else if (feature === 2) {
                Main.processStage2(result);
            } else if (feature === 3) {
                Main.processStage3(result);
            }
        }
    } // Checked

    static processStage3(result) {
        if (result.category === "Ng") {
            Main.stat.ngRageMeterFreq[3]++;
        } else if (result.category === "Fg") {
            Main.stat.fgRageMeterFreq[3]++;
        }
        // Remove small symbols
        for (let i = 0; i < Main.gridSize; i++) {
            for (let j = 0; j < Main.gridSize; j++) {
                if (
                    result.reels[i][j].symbol >= 5 &&
                    result.reels[i][j].symbol <= 8
                ) {
                    result.reels[i][j].isDetonated = true;
                    result.gridMulti[i][j]++;
                }
            }
        }
        Main.detonateAndCascadeGrid(result);
    } // Checked

    static processStage2(result) {
        if (result.category === "Ng") {
            Main.stat.ngRageMeterFreq[2]++;
        } else if (result.category === "Fg") {
            Main.stat.fgRageMeterFreq[2]++;
        }

        // Rand select 2 col to remove
        const colIndex = [0, 1, 2, 3, 4, 5];
        Main.shuffleArr(colIndex);
        const col1 = colIndex[0];
        const col2 = colIndex[1];

        for (let j = 0; j < Main.gridSize; j++) {
            result.gridMulti[col1][j]++;
            if (result.reels[col1][j].symbol !== Config.SC) {
                result.reels[col1][j].isDetonated = true;
            }
            result.gridMulti[col2][j]++;
            if (result.reels[col2][j].symbol !== Config.SC) {
                result.reels[col2][j].isDetonated = true;
            }
        }
        Main.detonateAndCascadeGrid(result);
    } // Checked

    static processStage1(result) {
        if (result.category === "Ng") {
            Main.stat.ngRageMeterFreq[1]++;
        } else if (result.category === "Fg") {
            Main.stat.fgRageMeterFreq[1]++;
        }
        // remove 3 x 3 (excl. SC)
        const x = Math.floor(Math.random() * (Main.gridSize - 2));
        const y = Math.floor(Math.random() * (Main.gridSize - 2));
        for (let i = x; i < x + 3; i++) {
            for (let j = y; j < y + 3; j++) {
                result.gridMulti[i][j]++;
                if (result.reels[i][j].symbol !== Config.SC) {
                    result.reels[i][j].isDetonated = true;
                }
            }
        }
        Main.detonateAndCascadeGrid(result);
    } // Checked

    static processStage0(result) {
        if (result.category === "Ng") {
            Main.stat.ngRageMeterFreq[0]++;
        } else if (result.category === "Fg") {
            Main.stat.fgRageMeterFreq[0]++;
        }
        // randomly remove 6 symbols (excl. SC)
        for (let i = 0; i < 6; i++) {
            let x = Math.floor(Math.random() * Main.gridSize);
            let y = Math.floor(Math.random() * Main.gridSize);
            while (result.reels[x][y].symbol === Config.SC) {
                x = Math.floor(Math.random() * Main.gridSize);
                y = Math.floor(Math.random() * Main.gridSize);
            }
            result.reels[x][y].isDetonated = true;
            result.gridMulti[x][y]++;
        }
        Main.detonateAndCascadeGrid(result);
    } // Checked

    static calcRageMeter(prizeList) {
        let result = 0;
        for (let i = 0; i < prizeList.length; i++) {
            result += prizeList[i].link;
        }
        return result;
    } // Checked

    static renewGrid(reels) {
        for (let i = 0; i < Main.gridSize; i++) {
            for (let j = 0; j < Main.gridSize; j++) {
                reels[i][j].used = false;
            }
        }
    } // Checked

    static markDetonated(result, prizeList) {
        // 0. Tag whether this grid should be detonated or not?
        for (let i = 0; i < prizeList.length; i++) {
            for (let j = 0; j < prizeList[i].position.length; j++) {
                const x = prizeList[i].position[j].x;
                const y = prizeList[i].position[j].y;
                result.reels[x][y].isDetonated = true;
            }
        }
    } // Checked

    static detonateAndCascadeGrid(result) {
        // 1. Detonate
        for (let i = 0; i < Main.gridSize; i++) {
            for (let j = Main.gridSize - 1; j >= 0; j--) {
                if (result.reels[i][j].isDetonated) {
                    result.reels[i].splice(j, 1);
                }
            }
        }

        // 2. Cascade
        for (let i = 0; i < Main.gridSize; i++) {
            while (result.reels[i].length < Main.gridSize) {
                result.stopIndex[i]--;
                let symbol;
                if (result.category === "Ng") {
                    if (result.stopIndex[i] < 0) {
                        result.stopIndex[i] =
                            NgReels.reels[result.reelSetIndex][i].length - 1;
                    }
                    symbol =
                        NgReels.reels[result.reelSetIndex][i][
                            result.stopIndex[i]
                        ];
                    result.reels[i].unshift(new Grid(symbol));
                } else if (result.category === "Fg") {
                    if (result.stopIndex[i] < 0) {
                        result.stopIndex[i] =
                            FgReels.reels[result.reelSetIndex][i].length - 1;
                    }
                    symbol =
                        FgReels.reels[result.reelSetIndex][i][
                            result.stopIndex[i]
                        ];
                    result.reels[i].unshift(new Grid(symbol));
                }
            }
        }
    } // Checked

    static iniGridMulti() {
        return [
            [-1, -1, -1, -1, -1, -1],
            [-1, -1, -1, -1, -1, -1],
            [-1, -1, -1, -1, -1, -1],
            [-1, -1, -1, -1, -1, -1],
            [-1, -1, -1, -1, -1, -1],
            [-1, -1, -1, -1, -1, -1],
        ];
    } // Checked

    static calcPrize(result, prizeList) {
        for (let i = 0; i < Main.gridSize; i++) {
            for (let j = 0; j < Main.gridSize; j++) {
                const symbol = result.reels[i][j].symbol;
                if (
                    !result.reels[i][j].used &&
                    symbol !== 0 &&
                    symbol !== Config.space &&
                    symbol !== Config.SC
                ) {
                    const prize = new Prize();
                    prize.symbol = symbol;
                    prize.position.push(new Coordinate(i, j));
                    prize.link = Main.walk(i, j, prize, result.reels);
                    if (prize.link >= Config.minLinkRequirement) {
                        prizeList.push(prize);
                    }
                }
            }
        }
        Main.calcMulti(prizeList, result.gridMulti);
    } // Checked

    static calcMulti(prizeList, gridMulti) {
        for (let i = 0; i < prizeList.length; i++) {
            prizeList[i].multi = Main.calcPrizeMulti(
                prizeList[i].position,
                gridMulti
            );
        }
    } // Checked

    static calcPrizeMulti(position, gridMulti) {
        let result = 0;
        for (let i = 0; i < position.length; i++) {
            const x = position[i].x;
            const y = position[i].y;
            if (gridMulti[x][y] > 0) {
                if (gridMulti[x][y] * 2 >= Main.singleGridMaxMulti) {
                    result += Main.singleGridMaxMulti;
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
    } // Checked

    static calcPayout(prizeList) {
        let result = 0.0;
        for (let i = 0; i < prizeList.length; i++) {
            if (prizeList[i].link >= 15) {
                prizeList[i].winScore =
                    Config.payout[prizeList[i].symbol][15] * prizeList[i].multi;
            } else {
                prizeList[i].winScore =
                    Config.payout[prizeList[i].symbol][prizeList[i].link] *
                    prizeList[i].multi;
            }
            result += prizeList[i].winScore;
        }
        return result;
    } // Checked

    static recordMaxWinAndMSE(roundWin) {
        if (roundWin > Main.stat.maxWin) {
            Main.stat.maxWin = roundWin;
        }
        Main.stat.MSE += Math.pow(roundWin - Main.theoreticalRTP, 2);
    } // Checked

    static printResult() {
        console.log("Total RTP: " + Main.stat.totalWin / Main.totalRounds);
        console.log("NG RTP: " + Main.stat.ngTotalWin / Main.totalRounds);
        console.log("FG RTP: " + Main.stat.fgTotalWin / Main.totalRounds);
        console.log("NG hit rate: " + Main.stat.ngHitFreq / Main.totalRounds);
        console.log("hit rate: " + Main.stat.hitFreq / Main.totalRounds);
        console.log(
            "FG trigger rate: " + Main.stat.fgTriggerFreq / Main.totalRounds
        );
        console.log("FG Freq: " + Main.totalRounds / Main.stat.fgTriggerFreq);
        console.log(
            "FG Multi: " + Main.stat.fgTotalWin / Main.stat.fgTriggerFreq
        );
        console.log(
            "NG Cascade ratio: " + Main.stat.ngCascadeFreq / Main.stat.ngHitFreq
        );
        console.log(
            "FG Cascade ratio: " + Main.stat.fgCascadeFreq / Main.stat.fgHitFreq
        );
        console.log("Max win: " + Main.stat.maxWin);
        console.log("cash out freq: " + Main.totalRounds / Main.cashOutFreq);
        console.log(
            "Standard dev.: " + Math.pow(Main.stat.MSE / Main.totalRounds, 0.5)
        );
        console.log("---Ng Rage Meter Dist.---");
        for (let i = 0; i < Main.stat.ngRageMeterFreq.length; i++) {
            console.log(
                "Feature " +
                    i +
                    ": " +
                    Main.stat.ngRageMeterFreq[i] / Main.totalRounds
            );
        }
        console.log("---Fg Rage Meter Dist.---");
        for (let i = 0; i < Main.stat.fgRageMeterFreq.length; i++) {
            console.log(
                "Feature " +
                    i +
                    ": " +
                    Main.stat.fgRageMeterFreq[i] / Main.stat.fgTriggerFreq
            );
        }
        console.log("---Base Game Dist.---");
        for (let i = 0; i < Main.stat.ngPrizeDist.length; i++) {
            console.log(Main.stat.ngPrizeDist[i]);
        }
        console.log("---Free Game Dist.---");
        for (let i = 0; i < Main.stat.fgPrizeDist.length; i++) {
            console.log(Main.stat.fgPrizeDist[i]);
        }
    } // Checked

    static walk(i, j, prize, grid) {
        grid[i][j].used = true;
        let result = 1;
        result += Main.move(i - 1, j, prize, grid); // go top
        result += Main.move(i, j + 1, prize, grid); // go right
        result += Main.move(i, j - 1, prize, grid); // go left
        result += Main.move(i + 1, j, prize, grid); // go down
        return result;
    } // Checked

    static move(i, j, prize, grid) {
        // check legit
        if (!Main.checkPositionLegit(i, j)) {
            return 0;
        }

        // check used
        if (grid[i][j].used) {
            return 0;
        }

        // if same symbol or wild, keep walking.
        if (grid[i][j].symbol === prize.symbol) {
            prize.position.push(new Coordinate(i, j));
            return Main.walk(i, j, prize, grid);
        } else return 0;
    } // Checked

    static checkPositionLegit(i, j) {
        return i >= 0 && j >= 0 && i < Main.gridSize && j < Main.gridSize;
    } // Checked

    static logPrize(winMulti, prizeDist) {
        if (winMulti === 0) {
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
        } else if (winMulti >= 1000) {
            prizeDist[9]++;
        }
    } // Checked

    static genGrid(reelSet, result) {
        for (let i = 0; i < Main.gridSize; i++) {
            result.stopIndex[i] = Math.floor(Math.random() * reelSet[i].length);
            result.reels.push([]);
            for (let j = 0; j < Main.gridSize; j++) {
                const symbol =
                    reelSet[i][(result.stopIndex[i] + j) % reelSet[i].length];
                result.reels[i].push(new Grid(symbol));
            }
        }
    } // Checked

    static genSymbolByWeights(arr, sumWeight, weights) {
        const rand = Math.random();
        let accWeight = weights[0];
        for (let i = 0; i < weights.length; i++) {
            if (rand < accWeight / sumWeight) {
                return arr[i];
            }
            if (i + 1 < weights.length) {
                accWeight += weights[i + 1];
            }
        }
        return arr[arr.length - 1];
    } // Checked

    static shuffleArr(deck) {
        // read array from the highest index to lowest
        for (let i = deck.length - 1; i >= 1; i--) {
            // generate a random number j such that 0 <= j <= i
            const j = Math.floor(Math.random() * (i + 1));

            // swap the current element with the randomly generated index
            const temp = deck[i];
            deck[i] = deck[j];
            deck[j] = temp;
        }
    }

    static printGrid(grid) {
        for (let i = 0; i < Main.gridSize; i++) {
            let row = "";
            for (let j = 0; j < Main.gridSize; j++) {
                row += grid[i][j].symbol + " ";
            }
            console.log(row);
        }
        console.log();
    }
}

// Run the main function
if (import.meta.url === `file://${process.argv[1]}`) {
    Main.main();
}

export default Main;
