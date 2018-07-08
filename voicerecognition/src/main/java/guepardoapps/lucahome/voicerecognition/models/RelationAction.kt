package guepardoapps.lucahome.voicerecognition.models

import guepardoapps.lucahome.voicerecognition.enums.Action

data class RelationAction(
        val action: Action,
        val parameter: ArrayList<String>)