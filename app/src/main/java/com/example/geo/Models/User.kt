package com.example.geo.Models

import java.io.Serializable

class User : Serializable {
    var userName: String = ""
    var nickName: String = ""
    var emailID: String = ""
    var nationality: String = ""
    var languages: MutableMap<String, String> = mutableMapOf<String,String>()
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var haslocation:Boolean = false

    constructor() {}

    constructor(userName: String?, nickName: String?, emailID: String?, nationality:String?, languages:MutableMap<String,String>,haslocation:Boolean) {
        this.userName = userName?:""
        this.nickName = nickName?:""
        this.emailID = emailID?:""
        this.nationality = nationality?:""
        this.languages = languages
        this.haslocation = haslocation
    }
}