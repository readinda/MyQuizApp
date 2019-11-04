package com.adindaef.myquizapp

class Category {
    companion object{
        val PROGRAMMING = 1
        val GEOGRAPHY = 2
        val MATH = 3
    }

    var id: Int = 0
    var name: String = ""

    constructor(){}

    constructor(name: String) {
        this.id = id
        this.name = name
    }

    override fun toString(): String {
        return name
    }
}