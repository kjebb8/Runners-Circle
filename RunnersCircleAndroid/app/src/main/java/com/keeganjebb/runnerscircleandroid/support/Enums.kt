package com.keeganjebb.runnerscircleandroid.support

enum class FirebaseString(val value: String) {
    USERS("users"),
    NAME("name"),
    EMAIL("email"),
    MEMBERS("members"),
    ACTIVE("active"),
    RUN_GROUPS("run_groups"),
    OWNER("owner"),
    MEMBER_CODE("member_code"),
    GROUPS("groups"),
    LATITUDE("latitude"),
    LONGITUDE("longitude"),
    DISTANCE("distance"),
    AVG_PACE("avg_pace")
}


enum class GroupType {
    JOINED, SEARCHED
}


enum class StatType {
    TIME, DISTANCE, PACE, TOTAL_DISTANCE, RUNNING, CONNECTED
}

//Global constant strings

//val LOBBY = "Lobby"
//val MAP = "Map"
//val STATS = "Stats"