package guepardoapps.lucahome.bixby.services

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import guepardoapps.lucahome.bixby.databases.DbHandler
import guepardoapps.lucahome.bixby.enums.ActionType
import guepardoapps.lucahome.bixby.enums.DatabaseAction
import guepardoapps.lucahome.bixby.enums.NetworkType
import guepardoapps.lucahome.bixby.enums.RequirementType
import guepardoapps.lucahome.bixby.enums.StateType
import guepardoapps.lucahome.bixby.models.actions.ApplicationAction
import guepardoapps.lucahome.bixby.models.actions.BixbyAction
import guepardoapps.lucahome.bixby.models.actions.WirelessSwitchAction
import guepardoapps.lucahome.bixby.models.BixbyPair
import guepardoapps.lucahome.bixby.models.requirements.BixbyRequirement
import guepardoapps.lucahome.bixby.models.requirements.LightRequirement
import guepardoapps.lucahome.bixby.models.requirements.PositionRequirement
import guepardoapps.lucahome.bixby.models.shared.NetworkEntity
import guepardoapps.lucahome.bixby.models.shared.WirelessSocketEntity
import guepardoapps.lucahome.common.controller.NetworkController
import guepardoapps.lucahome.common.models.common.RxOptional
import guepardoapps.lucahome.common.services.position.PositionService
import guepardoapps.lucahome.common.services.puckjs.PuckJsService
import guepardoapps.lucahome.common.services.wirelesssocket.WirelessSocketService
import guepardoapps.lucahome.common.services.wirelessswitch.WirelessSwitchService
import guepardoapps.lucahome.common.utils.Logger
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlin.collections.ArrayList

class BixbyPairService {
    private val tag = BixbyPairService::class.java.simpleName

    private lateinit var context: Context
    private lateinit var receiverActivity: Class<*>

    private lateinit var dbHandler: DbHandler
    private lateinit var networkController: NetworkController

    val bixbyPairListPublishSubject = PublishSubject.create<RxOptional<ArrayList<BixbyPair>>>()!!

    private object Holder {
        @SuppressLint("StaticFieldLeak")
        val instance: BixbyPairService = BixbyPairService()
    }

    companion object {
        val instance: BixbyPairService by lazy { Holder.instance }
    }

    fun initialize(context: Context, receiverActivity: Class<*>) {
        this.context = context
        this.networkController = NetworkController(context)
        this.receiverActivity = receiverActivity
        this.bixbyPairListPublishSubject.onNext(RxOptional(createPairList()))
    }

    fun bixbyButtonPressed() {
        Logger.instance.verbose(tag, "bixbyButtonPressed")

        this.createPairList().forEach { x ->
            if (this.validateRequirements(x.requirementList)) {
                try {
                    this.performAction(x.action)
                } catch (exception: Exception) {
                    Logger.instance.error(tag, exception.toString())
                }
            }
        }
    }

    fun addEntry(entry: BixbyPair): Observable<Pair<Boolean, String>> {
        return Observable.create<Pair<Boolean, String>> { emitter ->
            try {
                this.dbHandler.add(entry.action)
                entry.requirementList.forEach { x -> this.dbHandler.add(x) }
                emitter.onNext(Pair(true, ""))
            } catch (exception: Exception) {
                Logger.instance.error(tag, exception)
                emitter.onNext(Pair(false, exception.message!!))
            }
            this.bixbyPairListPublishSubject.onNext(RxOptional(createPairList()))
            emitter.onComplete()
        }
    }

    fun updateEntry(entry: BixbyPair): Observable<Pair<Boolean, String>> {
        return Observable.create<Pair<Boolean, String>> { emitter ->
            try {
                this.dbHandler.update(entry.action)
                entry.requirementList.forEach { x -> this.dbHandler.update(x) }
                emitter.onNext(Pair(true, ""))
            } catch (exception: Exception) {
                Logger.instance.error(tag, exception)
                emitter.onNext(Pair(false, exception.message!!))
            }
            this.bixbyPairListPublishSubject.onNext(RxOptional(createPairList()))
            emitter.onComplete()
        }
    }

    fun deleteEntry(entry: BixbyPair): Observable<Pair<Boolean, String>> {
        return Observable.create<Pair<Boolean, String>> { emitter ->
            try {
                this.dbHandler.delete(entry.action)
                entry.requirementList.forEach { x -> this.dbHandler.delete(x) }
                emitter.onNext(Pair(true, ""))
            } catch (exception: Exception) {
                Logger.instance.error(tag, exception)
                emitter.onNext(Pair(false, exception.message!!))
            }
            this.bixbyPairListPublishSubject.onNext(RxOptional(createPairList()))
            emitter.onComplete()
        }
    }

    private fun createPairList(): ArrayList<BixbyPair> {
        val pairList = ArrayList<BixbyPair>()

        try {
            this.dbHandler.loadActionList().forEach { x ->
                val bixbyPair = BixbyPair()

                bixbyPair.actionId = x.actionId
                bixbyPair.action = x
                bixbyPair.requirementList = this.dbHandler.loadRequirementList(x.actionId)
                bixbyPair.databaseAction = DatabaseAction.Null

                pairList.add(bixbyPair)
            }
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
        }

        return pairList
    }

    private fun validateRequirements(requirementList: List<BixbyRequirement>): Boolean {
        return requirementList.all { x ->
            when (x.requirementType) {
                RequirementType.Light -> {
                    return this.validateLightRequirement(x.lightRequirement)
                }
                RequirementType.Position -> {
                    return this.validatePositionRequirement(x.positionRequirement)
                }
                RequirementType.Network -> {
                    return this.validateNetworkRequirement(x.networkRequirement)
                }
                RequirementType.WirelessSocket -> {
                    return this.validateWirelessSocketRequirement(x.wirelessSocketRequirement)
                }
                RequirementType.Null -> {
                    Logger.instance.error(tag, "Invalid BixbyRequirement!")
                    return false
                }
            }
        }
    }

    private fun validatePositionRequirement(positionRequirement: PositionRequirement): Boolean {
        return PositionService.instance.currentPosition.roomUuid == PuckJsService.instance.get(positionRequirement.puckJsUuid)?.roomUuid
    }

    private fun validateLightRequirement(lightRequirement: LightRequirement): Boolean {
        return lightRequirement.validateActualValue(PositionService.instance.currentPosition.lightValue)
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
                this.performApplicationAction(applicationAction)
            }
            ActionType.Network -> {
                val networkAction = bixbyAction.networkAction
                this.performNetworkAction(networkAction)
            }
            ActionType.WirelessSocket -> {
                val wirelessSocketAction = bixbyAction.wirelessSocketAction
                this.performWirelessSocketAction(wirelessSocketAction)
            }
            ActionType.WirelessSwitch -> {
                val wirelessSwitchAction = bixbyAction.wirelessSwitchAction
                this.performWirelessSwitchAction(wirelessSwitchAction)
            }
            ActionType.Null -> Logger.instance.error(tag, "Invalid ActionType!")
        }
    }

    @Throws(NullPointerException::class)
    private fun performApplicationAction(applicationAction: ApplicationAction) {
        Logger.instance.debug(tag, String.format("performApplicationAction for $applicationAction"))

        val packageName = applicationAction.packageName
        val packageManager = this.context.packageManager

        val startApplicationIntent = packageManager.getLaunchIntentForPackage(packageName)
                ?: throw NullPointerException("Created startApplicationContent for $packageName is null")

        startApplicationIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        this.context.startActivity(startApplicationIntent)
    }

    private fun performNetworkAction(networkAction: NetworkEntity) {
        Logger.instance.debug(tag, String.format("performNetworkAction for $networkAction"))

        val networkType = networkAction.networkType
        when (networkType) {
            NetworkType.Mobile -> when (networkAction.stateType) {
                StateType.On -> this.networkController.setMobileDataState(true)
                StateType.Off -> this.networkController.setMobileDataState(false)
                StateType.Null -> Logger.instance.error(tag, "Invalid StateType!")
            }
            NetworkType.Wifi -> when (networkAction.stateType) {
                StateType.On -> this.networkController.setWifiState(true)
                StateType.Off -> this.networkController.setWifiState(false)
                StateType.Null -> Logger.instance.error(tag, "Invalid StateType!")
            }
            NetworkType.Null -> Logger.instance.error(tag, "Invalid NetworkType!")
        }
    }

    private fun performWirelessSocketAction(wirelessSocketAction: WirelessSocketEntity) {
        Logger.instance.debug(tag, "performWirelessSocketAction for $wirelessSocketAction")
        val wirelessSocket = WirelessSocketService.instance.get(wirelessSocketAction.uuid)
        when (wirelessSocketAction.stateType) {
            StateType.On -> WirelessSocketService.instance.setState(wirelessSocket!!, true)
            StateType.Off -> WirelessSocketService.instance.setState(wirelessSocket!!, false)
            StateType.Null -> Logger.instance.error(tag, "Invalid StateType!")
        }
    }

    private fun performWirelessSwitchAction(wirelessSwitchAction: WirelessSwitchAction) {
        Logger.instance.debug(tag, "performWirelessSwitchAction for $wirelessSwitchAction")
        val wirelessSwitch = WirelessSwitchService.instance.get(wirelessSwitchAction.uuid)
        WirelessSwitchService.instance.toggle(wirelessSwitch!!)
    }
}