package com.cyberlogitec.freight9.common

import com.bumptech.glide.RequestManager
import com.cyberlogitec.freight9.lib.apibooking.APIBookingClientType
import com.cyberlogitec.freight9.lib.apistat.APIStatClientType
import com.cyberlogitec.freight9.lib.data.*
import com.cyberlogitec.freight9.lib.apitrade.APITradeClientType
import com.google.gson.Gson


data class Environment (
        var apiTradeClient: APITradeClientType,
        var apiStatClient: APIStatClientType,
        var apiBookingClient: APIBookingClientType,
        var currentUser: CurrentUser,
        var userRepository: UserRepositoryType,
        var portRepository: PortRepositoryType,
        var scheduleRepository: ScheduleRepositoryType,
        var inventoryRepository: InventoryRepositoryType,
        var featuredRouteRepository: FeaturedRouteRepositoryType,
        var carrierRepository: CarrierRepositoryType,
        var containerRepository: ContainerRepositoryType,
        var paymentRepository: PaymentRepositoryType,
        var messageRepository: MessageRepositoryType,
        var marketRouteFilterRepository: MarketRouteFilterRepositoryType,
        var marketChartSettingRepository: MarketChartSettingRepositoryType,
        var marketIndexListRepository: MarketIndexListRepositoryType,
        var requestManager: RequestManager,
        var gson: Gson
)
