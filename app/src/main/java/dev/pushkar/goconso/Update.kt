package dev.pushkar.goconso

/**
 * Created by pushkar on 2/3/18.
 */

class Update {
    lateinit var img: String
    lateinit var dt: String
    var sel: Boolean = false
    lateinit var hl: String
    var pri: Int = 0
    var btn: Boolean = false
    lateinit var btnTxt: String
    lateinit var url: String

    constructor() {

    }

    constructor(pri: Int, hl: String, dt: String, img: String, sel: Boolean, btn: Boolean, btnTxt: String, url: String) {
        this.pri = pri
        this.img = img
        this.hl = hl
        this.dt = dt
        this.sel = sel
        this.btn = btn
        this.btnTxt = btnTxt
        this.url = url
    }
}
