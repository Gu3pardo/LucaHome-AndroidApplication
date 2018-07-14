package guepardoapps.lucahome.voicerecognition.models

import guepardoapps.lucahome.voicerecognition.enums.Action

internal data class RelationAction(val action: Action, val parameter: ArrayList<String>)