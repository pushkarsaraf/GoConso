package dev.pushkar.goconso

/**
 * Created by pushkar on 1/3/18.
 */

class HighScore {
    var score: Int = 0
    var uid: String = ""

    constructor(score: Int, uid: String) {
        this.score = score
        this.uid = uid
    }

    constructor() {}
}
