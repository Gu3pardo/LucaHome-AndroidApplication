package guepardoapps.lucahome.bixby.services

import android.annotation.SuppressLint
import android.content.Context
import guepardoapps.lucahome.bixby.databases.DbHandler
import guepardoapps.lucahome.bixby.enums.DatabaseAction
import guepardoapps.lucahome.bixby.enums.NetworkType
import guepardoapps.lucahome.bixby.enums.RequirementType
import guepardoapps.lucahome.bixby.enums.StateType
import guepardoapps.lucahome.bixby.models.BixbyPair
import guepardoapps.lucahome.bixby.models.requirements.BixbyRequirement
import guepardoapps.lucahome.bixby.models.requirements.LightRequirement
import guepardoapps.lucahome.bixby.models.shared.NetworkEntity
import guepardoapps.lucahome.bixby.models.shared.WirelessSocketEntity
import guepardoapps.lucahome.common.controller.NetworkController
import guepardoapps.lucahome.common.services.wirelesssocket.WirelessSocketService
import guepardoapps.lucahome.common.utils.Logger
import kotlin.collections.ArrayList
import android.content.Intent
import guepardoapps.lucahome.bixby.models.actions.ApplicationAction
import guepardoapps.lucahome.bixby.models.actions.BixbyAction
import guepardoapps.lucahome.bixby.enums.ActionType
import guepardoapps.lucahome.bixby.models.actions.WirelessSwitchAction
import guepardoapps.lucahome.bixby.models.requirements.PositionRequirement

class BixbyPairService {
    private val tag = BixbyPairService::class.java.simpleName

    private lateinit var context: Context
    private lateinit var receiverActivity: Class<*>
    private var onBixbyServiceListener: OnBixbyServiceListener? = null

    private lateinit var dbHandler: DbHandler
    private lateinit var networkController: NetworkController

    init {
    }

    private object Holder {
        @SuppressLint("StaticFieldLeak")
        val instance: BixbyPairService = BixbyPairService()
    }

    companion object {
        val instance: BixbyPairService by lazy { Holder.instance }
    }

    fun initialize(context: Context,
                   receiverActivity: Class<*>,
                   onBixbyServiceListener: OnBixbyServiceListener?) {
        this.context = context
        this.networkController = NetworkController(context)
        this.receiverActivity = receiverActivity
        this.onBixbyServiceListener = onBixbyServiceListener
    }

    fun dispose() {
        Logger.instance.verbose(tag, "dispose")
    }

    fun bixbyButtonPressed() {
        Logger.instance.verbose(tag, "bixbyButtonPressed")

        for (bixbyPair in createPairList()) {
            val bixbyAction = bixbyPair.action
            val requirementList = bixbyPair.requirementList

            if (validateRequirements(requirementList)) {
                Logger.instance.info(tag, "All requirements true! Running callback!")
                try {
                    performAction(bixbyAction)
                } catch (exception: Exception) {
                    Logger.instance.error(tag, exception.toString())
                }

            }
        }
    }

    fun getActionList(): ArrayList<BixbyPair> {
        return createPairList()
    }

    fun addEntry(entry: BixbyPair) {
        try {
            dbHandler.add(entry.action)
            for (requirement in entry.requirementList) {
                dbHandler.add(requirement)
            }
            onBixbyServiceListener!!.onAddFinished(true)
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
            onBixbyServiceListener!!.onAddFinished(false, exception.message)
        }
    }

    fun updateEntry(entry: BixbyPair) {
        try {
            dbHandler.update(entry.action)
            for (requirement in entry.requirementList) {
                dbHandler.update(requirement)
            }
            onBixbyServiceListener!!.onUpdateFinished(true, entry)
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
            onBixbyServiceListener!!.onUpdateFinished(false, entry, exception.message)
        }
    }

    fun deleteEntry(entry: BixbyPair) {
        try {
            dbHandler.delete(entry.action)
            for (requirement in entry.requirementList) {
                dbHandler.delete(requirement)
            }
            onBixbyServiceListener!!.onDeleteFinished(true, entry)
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
            onBixbyServiceListener!!.onDeleteFinished(false, entry, exception.message)
        }
    }

    private fun createPairList(): ArrayList<BixbyPair> {
        val pairList = ArrayList<BixbyPair>()

        try {
            val actionList = dbHandler.loadActionList()
            val requirementList = dbHandler.loadRequirementList()

            for (actionIndex in actionList.indices) {
                val bixbyAction = actionList[actionIndex]
                val actionId = bixbyAction.actionId
                val pairRequirementList = ArrayList<BixbyRequirement>()

                for (requirementIndex in requirementList.indices) {
                    val bixbyRequirement = requirementList[requirementIndex]
                    if (bixbyRequirement.actionId == actionId) {
                        pairRequirementList.add(bixbyRequirement)
                    }
                }

                val bixbyPair = BixbyPair()
                bixbyPair.actionId = actionId
                bixbyPair.action = bixbyAction
                bixbyPair.requirementList = pairRequirementList
                bixbyPair.databaseAction = DatabaseAction.Null

                pairList.add(bixbyPair)
            }
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
        }

        onBixbyServiceListener!!.onLoad(pairList)

        return pairList
    }

    private fun validateRequirements(requirementList: java.util.ArrayList<BixbyRequirement>): Boolean {
        var allRequirementsTrue = true

        for (requirementIndex in requirementList.indices) {
            val bixbyRequirement = requirementList[requirementIndex]
            when (bixbyRequirement.requirementType) {
                RequirementType.Light -> {
                    allRequirementsTrue = allRequirementsTrue and validateLightRequirement(bixbyRequirement.lightRequirement)
                }
                RequirementType.Position -> {
                    allRequirementsTrue = allRequirementsTrue and validatePositionRequirement(bixbyRequirement.positionRequirement)
                }
                RequirementType.Network -> {
                    allRequirementsTrue = allRequirementsTrue and validateNetworkRequirement(bixbyRequirement.networkRequirement)
                }
                RequirementType.WirelessSocket -> {
                    allRequirementsTrue = allRequirementsTrue and validateWirelessSocketRequirement(bixbyRequirement.wirelessSocketRequirement)
                }
                RequirementType.Null -> {
                    Logger.instance.error(tag, "Invalid BixbyRequirement!")
                    allRequirementsTrue = false
                }
            }
        }

        return allRequirementsTrue
    }

    private fun validatePositionRequirement(positionRequirement: PositionRequirement): Boolean {
        return false
        // TODO
        // val room = RoomService.getInstance().GetByUuid(_lastReceivedPosition.GetPuckJs().GetRoomUuid())
        // return _lastReceivedPosition != null && puckJsPosition.contains(room.GetName())
    }

    private fun validateLightRequirement(lightRequirement: LightRequirement): Boolean {
        return false
        // TODO
        // return _lastReceivedPosition != null && lightRequirement.ValidateActualValue(_lastReceivedPosition.GetLightValue())
    }

    private fun validateNetworkRequirement(networkRequirement: NetworkEntity): Boolean {
        when (networkRequirement.networkType) {
            NetworkType.Mobile -> {
                val isMobileDataEnabled = networkController.isMobileConnected().second
                return networkRequirement.stateType === StateType.On == isMobileDataEnabled
            }

            NetworkType.Wifi -> {
                val isWifiEnabled = networkController.isWifiConnected().second
                val stateType = networkRequirement.stateType

                return when (stateType) {
                    StateType.On -> {
                        val wifiSsid = networkController.getWifiSsid()
                        isWifiEnabled && wifiSsid.contains(networkRequirement.wifiSsid)
                    }
                    StateType.Off -> !isWifiEnabled
                    StateType.Null -> {
                        Logger.instance.error(tag, "Invalid StateType for WIFI!")
                        false
                    }
                }
            }

            NetworkType.Null -> {
                Logger.instance.error(tag, "Invalid NetworkType!")
                return false
            }
        }
    }

    private fun validateWirelessSocketRequirement(wirelessSocketRequirement: WirelessSocketEntity): Boolean {
        val wirelessSocket = WirelessSocketService.instance.get(wirelessSocketRequirement.uuid)
        return wirelessSocketRequirement.stateType === StateType.On == wirelessSocket?.state
    }

    private fun performAction(bixbyAction: BixbyAction) {
        when (bixbyAction.actionType) {
            ActionType.Application -> {
                val applicationAction = bixbyAction.applicationAction
                performApplicationAction(applicationAction)
            }
            ActionType.Network -> {
                val networkAction = bixbyAction.networkAction
                performNetworkAction(networkAction)
            }
            ActionType.WirelessSocket -> {
                val wirelessSocketAction = bixbyAction.wirelessSocketAction
                performWirelessSocketAction(wirelessSocketAction)
            }
            ActionType.WirelessSwitch -> {
                val wirelessSwitchAction = bixbyAction.wirelessSwitchAction
                performWirelessSwitchAction(wirelessSwitchAction)
            }
            ActionType.Null -> Logger.instance.error(tag, "Invalid ActionType!")
        }
    }

    @Throws(NullPointerException::class)
    private fun performApplicationAction(applicationAction: ApplicationAction) {
        Logger.instance.debug(tag, String.format("performApplicationAction for $applicationAction"))

        val packageName = applicationAction.packageName
        val packageManager = context.packageManager

        val startApplicationIntent = packageManager.getLaunchIntentForPackage(packageName)
                ?: throw NullPointerException("Created startApplicationContent for $packageName is null")

        startApplicationIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        context.startActivity(startApplicationIntent)
    }

    private fun performNetworkAction(networkAction: NetworkEntity) {
        Logger.instance.debug(tag, String.format("performNetworkAction for $networkAction"))

        val networkType = networkAction.networkType
        when (networkType) {
            NetworkType.Mobile -> when (networkAction.stateType) {
                StateType.On -> networkController.setMobileDataState(true)
                StateType.Off -> networkController.setMobileDataState(false)
                StateType.Null -> Logger.instance.error(tag, "Invalid StateType!")
            }
            NetworkType.Wifi -> when (networkAction.stateType) {
                StateType.On -> networkController.setWifiState(true)
                StateType.Off -> networkController.setWifiState(false)
                StateType.Null -> Logger.instance.error(tag, "Invalid StateType!")
            }
            NetworkType.Null -> Logger.instance.error(tag, "Invalid NetworkType!")
        }
    }

    private fun performWirelessSocketAction(wirelessSocketAction: WirelessSocketEntity) {
        Logger.instance.debug(tag, "performWirelessSocketAction for $wirelessSocketAction")
        // TODO
        /* when (wirelessSocketAction.stateType) {
            StateType.On -> WirelessSocketService.instance.setWirelessSocketState(wirelessSocketAction.wirelessSocketName, true)
            StateType.Off -> WirelessSocketService.instance.setWirelessSocketState(wirelessSocketAction.wirelessSocketName, false)
            StateType.Null -> Logger.instance.error(tag, "Invalid StateType!")
        } */
    }

    private fun performWirelessSwitchAction(wirelessSwitchAction: WirelessSwitchAction) {
        Logger.instance.debug(tag, "performWirelessSwitchAction for $wirelessSwitchAction")
        // TODO
        // WirelessSwitchService.instance.toggleWirelessSwitch(wirelessSwitchAction.wirelessSwitchName)
    }
}