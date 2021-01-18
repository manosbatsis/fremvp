package com.cyberlogitec.freight9.lib.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.CurrentUser
import com.cyberlogitec.freight9.common.Environment
import com.cyberlogitec.freight9.config.Constant.CONNECT_TIMEOUT
import com.cyberlogitec.freight9.config.Constant.DB_NAME
import com.cyberlogitec.freight9.config.Constant.READ_TIMEOUT
import com.cyberlogitec.freight9.config.Constant.WRITE_TIMEOUT
import com.cyberlogitec.freight9.lib.apibooking.APIBookingClient
import com.cyberlogitec.freight9.lib.apibooking.APIBookingClientType
import com.cyberlogitec.freight9.lib.apibooking.APIBookingRequestInterceptor
import com.cyberlogitec.freight9.lib.apibooking.APIBookingService
import com.cyberlogitec.freight9.lib.apistat.APIStatClient
import com.cyberlogitec.freight9.lib.apistat.APIStatClientType
import com.cyberlogitec.freight9.lib.apistat.APIStatRequestInterceptor
import com.cyberlogitec.freight9.lib.apistat.APIStatService
import com.cyberlogitec.freight9.lib.data.*
import com.cyberlogitec.freight9.lib.db.*
import com.cyberlogitec.freight9.lib.apitrade.APITradeClient
import com.cyberlogitec.freight9.lib.apitrade.APITradeClientType
import com.cyberlogitec.freight9.lib.apitrade.APITradeRequestInterceptor
import com.cyberlogitec.freight9.lib.apitrade.APITradeService
import com.cyberlogitec.freight9.lib.util.SharedPreferenceManager
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class AppModule(val app: App) {

    // App
    @Provides
    @Singleton
    fun provideApplicationContext(): Context = app.applicationContext

    // F9Db
    @Singleton
    @Provides
    fun provideF9Db(application: Context): F9Db =
            Room.databaseBuilder(application, F9Db::class.java, DB_NAME)
                    .addCallback(object : RoomDatabase.Callback(){
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                        }
                    }).allowMainThreadQueries()
                .build()

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // User Dao
    @Singleton
    @Provides
    fun provideUserDao(db: F9Db): UserDao = db.userDao()

    // User Repository
    @Provides
    @Singleton
    fun userRepository(userDao: UserDao): UserRepositoryType = UserRepository(userDao)

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Port Dao
    @Singleton
    @Provides
    fun providePortDao(db: F9Db): PortDao = db.portDao()

    // Port Repository
    @Provides
    @Singleton
    fun portRepository(portDao: PortDao): PortRepositoryType = PortRepository(portDao)

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ScheduleDao
    @Singleton
    @Provides
    fun provideScheduleDao(db: F9Db): ScheduleDao = db.ScheduleDao()

    // Schedule Repository
    @Singleton
    @Provides
    fun scheduleRepository(scheduleDao: ScheduleDao): ScheduleRepositoryType = ScheduleRepository(scheduleDao)

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Inventory Dao
    @Singleton
    @Provides
    fun provideInventoryDao(db: F9Db): InventoryDao = db.inventoryDao()

    // inventoryRepository Repository
    @Provides
    @Singleton
    fun inventoryRepository(inventoryDao: InventoryDao): InventoryRepositoryType = InventoryRepository(inventoryDao)

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // FeaturedRoute Dao
    @Singleton
    @Provides
    fun provideFeaturedRouteDao(db: F9Db): FeaturedRouteDao = db.featuredRouteDao()

    // FeaturedRoute Repository
    @Provides
    @Singleton
    fun featuredRouteRepository(featuredRouteDao: FeaturedRouteDao): FeaturedRouteRepositoryType = FeaturedRouteRepository(featuredRouteDao)

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // carrier Dao
    @Singleton
    @Provides
    fun provideCarrierDao(db:F9Db): CarrierDao = db.carrierDao()

    // carrier Repository
    @Provides
    @Singleton
    fun carrierRepository(carrierDao: CarrierDao): CarrierRepositoryType = CarrierRepository(carrierDao)

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // container Dao
    @Singleton
    @Provides
    fun provideContainerDao(db:F9Db): ContainerDao = db.containerDao()

    // container Repository
    @Provides
    @Singleton
    fun containerRepository(containerDao: ContainerDao): ContainerRepositoryType = ContainerRepository(containerDao)

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // payment Dao
    @Singleton
    @Provides
    fun providePaymentDao(db:F9Db): PaymentDao = db.paymentDao()

    // payment Repository
    @Provides
    @Singleton
    fun paymentRepository(paymentDao: PaymentDao): PaymentRepositoryType = PaymentRepository(paymentDao)

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // message Dao
    @Singleton
    @Provides
    fun provideMessageDao(db:F9Db): MessageDao = db.messageDao()

    // message Repository
    @Provides
    @Singleton
    fun messageRepository(messageDao: MessageDao): MessageRepositoryType = MessageRepository(messageDao)

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // marketRouteFilter Dao
    @Singleton
    @Provides
    fun provideMarketRouteDao(db:F9Db): MarketRouteDao = db.marketRouteDao()

    // marketRouteFilter Repository
    @Provides
    @Singleton
    fun marketRouteFilterRepository(marketRouteFilterDao: MarketRouteDao): MarketRouteFilterRepositoryType = MarketRouteFilterRepository(marketRouteFilterDao)

    // MarketChartSettingDao Dao
    @Singleton
    @Provides
    fun provideMarketChartSettingDao(db:F9Db): MarketChartSettingDao = db.marketChartSettingDao()

    // marketChartSetting Repository
    @Provides
    @Singleton
    fun marketChartSettingRepository(marketChartSettingDao: MarketChartSettingDao): MarketChartSettingRepositoryType = MarketChartSettingRepository(marketChartSettingDao)

    // MarketIndexListDao Dao
    @Singleton
    @Provides
    fun provideMarketIndexListDao(db:F9Db): MarketIndexListDao = db.marketIndexListDao()

    // marketIndex Repository
    // marketChartSetting Repository
    @Provides
    @Singleton
    fun marketIndexListRepository(marketIndexListDao: MarketIndexListDao): MarketIndexListRepositoryType = MarketIndexListRepository(marketIndexListDao)
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Shared preference (Repository)
    @Provides
    @Singleton
    fun provideSharedPreferenceManager(context: Context) = SharedPreferenceManager(context)

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // CurrentUser
    @Provides
    @Singleton
    fun provideAPITradeRequestInterceptor(currentUser: CurrentUser) = APITradeRequestInterceptor(currentUser)

    @Provides
    @Singleton
    fun provideApiStatReuqestInterceptor(currentUser: CurrentUser) = APIStatRequestInterceptor(currentUser)

    @Provides
    @Singleton
    fun provideApiBookingReuqestInterceptor(currentUser: CurrentUser) = APIBookingRequestInterceptor(currentUser)

    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Provides
    @Singleton
    fun provideCurrentUser(sharedPreferenceManager: SharedPreferenceManager) = CurrentUser(sharedPreferenceManager = sharedPreferenceManager)

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Glide
    @Provides
    @Singleton
    fun requestManager(context: Context): RequestManager = Glide.with(context)

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Api (retrofit, okthttp3, gson)
    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Provides
    @Singleton
    fun provideAPITradeClient(apiTradeService: APITradeService): APITradeClientType = APITradeClient(apiTradeService)

    @Provides
    @Singleton
    fun provideAPIStatClient(apiStatService: APIStatService): APIStatClientType = APIStatClient(apiStatService)

    @Provides
    @Singleton
    fun provideAPIBookingClient(apiBookingService: APIBookingService): APIBookingClientType = APIBookingClient(apiBookingService)

    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Provides
    @Singleton
    fun provideOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor, apiTradeRequestInterceptor: APITradeRequestInterceptor): OkHttpClient =
            OkHttpClient.Builder()
                    .addInterceptor(httpLoggingInterceptor)
                    .addInterceptor(apiTradeRequestInterceptor)
                    .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                    .build()

    @Provides
    @Singleton
    fun provideAPITradeService(gson: Gson, okhttpClient: OkHttpClient): APITradeService =
            Retrofit.Builder()
                    .client(okhttpClient)
                    .baseUrl(APITradeService.EndPoint.baseUrl)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                    .create(APITradeService::class.java)

    @Provides
    @Singleton
    fun provideAPIStatService(gson: Gson,
                              httpLoggingInterceptor: HttpLoggingInterceptor,
                              apiStatRequestInterceptor: APIStatRequestInterceptor)
            : APIStatService =
            Retrofit.Builder()
                    .client(OkHttpClient.Builder()
                            .addInterceptor(httpLoggingInterceptor)
                            .addInterceptor(apiStatRequestInterceptor)
                            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                            .build())
                    .baseUrl(APIStatService.EndPoint.baseLocalUrl)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                    .create(APIStatService::class.java)

    @Provides
    @Singleton
    fun provideAPIBookingService(gson: Gson,
                              httpLoggingInterceptor: HttpLoggingInterceptor,
                              apiBookingRequestInterceptor: APIBookingRequestInterceptor)
            : APIBookingService =
            Retrofit.Builder()
                    .client(OkHttpClient.Builder()
                            .addInterceptor(httpLoggingInterceptor)
                            .addInterceptor(apiBookingRequestInterceptor)
                            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                            .build())
                    .baseUrl(APIBookingService.EndPoint.baseUrl)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                    .create(APIBookingService::class.java)

    // Environment
    @Provides
    @Singleton
    fun provideEnvironment(
            apiTradeClient: APITradeClientType,
            apiStatClient: APIStatClientType,
            apiBookingClient: APIBookingClientType,
            currentUser: CurrentUser,
            userRepository: UserRepositoryType,
            portRepository: PortRepositoryType,
            scheduleRepository: ScheduleRepositoryType,
            inventoryRepository: InventoryRepositoryType,
            featuredRouteRepository: FeaturedRouteRepositoryType,
            carrierRepository: CarrierRepositoryType,
            containerRepository: ContainerRepositoryType,
            paymentRepository: PaymentRepositoryType,
            messageRepository: MessageRepositoryType,
            marketRouteFilterRepository: MarketRouteFilterRepositoryType,
            marketChartSettingRepository: MarketChartSettingRepositoryType,
            marketIndexListRepository: MarketIndexListRepositoryType,
            requestManager: RequestManager,
            gson: Gson
    ): Environment = Environment(
            apiTradeClient,
            apiStatClient,
            apiBookingClient,
            currentUser,
            userRepository,
            portRepository,
            scheduleRepository,
            inventoryRepository,
            featuredRouteRepository,
            carrierRepository,
            containerRepository,
            paymentRepository,
            messageRepository,
            marketRouteFilterRepository,
            marketChartSettingRepository,
            marketIndexListRepository,
            requestManager,
            gson
    )
}