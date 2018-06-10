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
import guepardoapps.lucahome.common.services.WirelessSocketService
import guepardoapps.lucahome.common.utils.Logger
import kotlin.collections.ArrayList

class BixbyPairService {
    private val tag = BixbyPairService::class.java.simpleName

    lateinit var context: Context
    lateinit var receiverActivity: Class<*>
    var onBixbyServiceListener: OnBixbyServiceListener? = null
    var reloadEnabled: Boolean = true
    var reloadTimeout: Int = 5 * 60 * 1000
    var displayNotification: Boolean = true

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
                   onBixbyServiceListener: OnBixbyServiceListener?,
                   reloadEnabled: Boolean = true,
                   reloadTimeout: Int = 5 * 60 * 1000,
                   displayNotification: Boolean = true) {
        this.context = context
        this.receiverActivity = receiverActivity
        this.onBixbyServiceListener = onBixbyServiceListener
        this.reloadEnabled = reloadEnabled
        this.reloadTimeout = reloadTimeout
        this.displayNotification = displayNotification

        this.networkController = NetworkController(context)
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

                pairList.add(BixbyPair(actionId, bixbyAction, pairRequirementList, DatabaseAction.Null))
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
                    allRequirementsTrue = allRequirementsTrue and validatePositionRequirement(bixbyRequirement.puckJsPosition)
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

    private fun validatePositionRequirement(puckJsPosition: String): Boolean {
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
                val isMobileDataEnabled = networkController.IsMobileDataEnabled()
                return networkRequirement.stateType === StateType.On == isMobileDataEnabled
            }

            NetworkType.Wifi -> {
                val isWifiEnabled = networkController.IsWifiConnected()
                val stateType = networkRequirement.stateType

                return when (stateType) {
                    StateType.On -> {
                        val wifiSsid = networkController.GetWifiSsid()
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
        val wirelessSocket = WirelessSocketService.getInstance().GetByName(wirelessSocketRequirement.wirelessSocketName)
        return wirelessSocketRequirement.stateType === StateType.On == wirelessSocket.GetState()
    }
}