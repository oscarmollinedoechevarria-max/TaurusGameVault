package com.example.taurusgamevault.enums

enum class Priority(val text: String, val number: Int) {
    BACK_LOG("Back log",1),
    LOW("Low",2),
    MID("Moderate",3),
    HIGH("High",4),
    CRITICAL("Critical",5);

    companion object {
        fun numberToPriority(numberEntered: Int): Priority? {
            return entries.find { it.number == numberEntered }
        }

        fun stringToPriority(stringEntered: String): Priority? {
            return entries.find { it.text == stringEntered }
        }
    }
}