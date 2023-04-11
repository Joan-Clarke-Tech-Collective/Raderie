package org.clarkecollective.raderie.models

data class RaderieUser(val deck: MutableList<HumanValue> = mutableListOf(), val userChosenName: String = "Unnamed", val friendList: MutableList<Friend> = mutableListOf())
