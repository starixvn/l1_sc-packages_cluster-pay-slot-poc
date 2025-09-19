class Result {
    constructor() {
        // Fg Keep the info until the Fg end
        this.category = "";
        this.rageCounter = 0;
        this.gridMulti = [];
        this.featureTriggered = [false, false, false, false];

        this.featureQueue = [];

        // Fg Update Every Round
        this.reelSetIndex = 0;
        this.stopIndex = new Array(6).fill(0);
        this.reels = [];
        this.winScore = 0;
        this.tumbles = [];
    }

    // Queue operations
    offerToQueue(value) {
        this.featureQueue.push(value);
    }

    pollFromQueue() {
        return this.featureQueue.shift();
    }

    isQueueEmpty() {
        return this.featureQueue.length === 0;
    }
}

export default Result;
