package com.cyberlogitec.freight9.lib.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cyberlogitec.freight9.config.ContainerType
import com.cyberlogitec.freight9.lib.model.*

@Database(entities = arrayOf(
        User::class,
        Inventory::class,
        Port::class,
        Schedule::class,
        FeaturedRoute::class,
        Carrier::class,
        Container::class,
        Payment::class,
        MarketRoute::class,
        Message::class,
        Chart::class,
        MarketIndexList::class),
        version = 1,
        exportSchema = false)
@TypeConverters(DateConverter::class, MovingAverageDataTypeConverter::class, ArrayListStringDataTypeConverter::class, ArrayListIntervalDataTypeConverter::class)
abstract class F9Db : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun portDao(): PortDao
    abstract fun ScheduleDao(): ScheduleDao
    abstract fun featuredRouteDao(): FeaturedRouteDao
    abstract fun carrierDao(): CarrierDao
    abstract fun containerDao(): ContainerDao
    abstract fun paymentDao(): PaymentDao
    abstract fun messageDao(): MessageDao
    abstract fun marketRouteDao(): MarketRouteDao
    abstract fun marketChartSettingDao(): MarketChartSettingDao
    abstract fun marketIndexListDao(): MarketIndexListDao

    companion object {
        val PRECARRIER_DATA = arrayListOf(Carrier("MSK","MSK", true),
                Carrier("MCS","MCS", true),
                Carrier("ONE","ONE", true),
                Carrier("SNT","SNT", true),
                Carrier("YML","YML", true),
                Carrier("OOCL","OOCL", true),
                Carrier("PIL","PIL", true),
                Carrier("HIC","HIC", true),
                Carrier("CMA","CMA", true),
                Carrier("COS","COS", true),
                Carrier("YANG","YANG", true),
                Carrier("SIN","SIN", true),
                Carrier("HAPAG","HAPAG", true),
                Carrier("HLC","HLC", true),
                Carrier("EMC","EMC", true),
                Carrier("EVER","EVER", true))

        val PRE_CONTAINER_TYPE_DATA = arrayListOf(
                Container("type",ContainerType.F_TYPE,"",true),
                Container("type",ContainerType.R_TYPE,"",false),
                Container("type",ContainerType.E_TYPE,"",false),
                Container("type",ContainerType.S_TYPE,"",false),
                Container("size","","20ft",true),
                Container("size","","40ft",false),
                Container("size","","45ft",false),
                Container("size","","45HC",false)
        )
        val PRE_PAYMENT_DATA = arrayListOf(
                Payment( "type","P","","",
                        "","",true),
                Payment( "type","C","","",
                        "","",false),
                Payment( "plan","","1","10",
                        "80","10",true),
                Payment( "plan","","2","30",
                        "60","10",false),
                Payment( "plan","","3","50",
                        "40","10",false)
        )
    }
}
