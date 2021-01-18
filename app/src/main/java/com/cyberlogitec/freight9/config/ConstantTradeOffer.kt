package com.cyberlogitec.freight9.config

object ConstantTradeOffer {
    // Y = 1, N = 0
    const val DELETE_YN_Y = "Y"
    const val DELETE_YN_N = "N"

    // 01=Ocean, 02=Air
    const val TRADE_MARKET_TYPE_OCEAN = "01"
    const val TRADE_MARKET_TYPE_AIR = "02"

    // S=Sell, B=Buy
    const val OFFER_TYPE_CODE_SELL = "S"
    const val OFFER_TYPE_CODE_BUY = "B"

    // 01=New, 02=Modify, 03=Cancel
    const val OFFER_TRANSACTION_TYPE_CODE_NEW = "01"
    const val OFFER_TRANSACTION_TYPE_CODE_MODIFY = "02"
    const val OFFER_TRANSACTION_TYPE_CODE_CANCEL = "03"

    // 001=Carrier, 002=Forwarder, 003=NVOCC, 004=Cargo Owner, 005= Trade System
    const val TRACE_ROLE_CODE_CARRIER = "001"
    const val TRACE_ROLE_CODE_FORWARDER = "002"
    const val TRACE_ROLE_CODE_NVOCC = "003"
    const val TRACE_ROLE_CODE_CARGOOWNER = "004"
    const val TRACE_ROLE_CODE_TRADESYSTEM = "005"

    // 1=Whole, 0=Partial
    const val ALL_YN_WHOLE = "1"
    const val ALL_YN_PARTIAL = "0"

    // 오퍼상태 코드
    // 01.Opened, 02.RecommendPlaned, 03.Simulating, 04.Recommended, 05.Waiting,
    // 06.RecommendSelected, 07.Cancelled, 08.Closed, 09.Rejected
    const val OFFER_MAIN_STATUS_CODE_OPENED = "01"
    const val OFFER_MAIN_STATUS_CODE_RECOMMEND_PLANED = "02"
    const val OFFER_MAIN_STATUS_CODE_SIMULATING = "03"
    const val OFFER_MAIN_STATUS_CODE_RECOMMENDED = "04"
    const val OFFER_MAIN_STATUS_CODE_WAITING = "05"
    const val OFFER_MAIN_STATUS_CODE_RECOMMEND_SELECTED = "06"
    const val OFFER_MAIN_STATUS_CODE_CANCELLED = "07"
    const val OFFER_MAIN_STATUS_CODE_CLOSED = "08"
    const val OFFER_MAIN_STATUS_CODE_REJECTED = "09"

    // 만료여부 : Y = 1, N = 0
    const val EXPIRE_YN_Y = "Y"
    const val EXPIRE_YN_N = "N"

    // 블록딜 참여여부 : Y = 1, N = 0
    const val VIRTUAL_GROUP_AGREEMENT_YN_Y = "Y"
    const val VIRTUAL_GROUP_AGREEMENT_YN_N = "N"

    // 매치 계산여부 : Y = 1, N = 0
    const val WHAT_IF_AGREEMENT_YN_Y = "Y"
    const val WHAT_IF_AGREEMENT_YN_N = "N"

    // 부모 블록딜 여부 : Y = 1, N = 0
    const val VIRTUAL_GROUP_OFFER_YN_Y = "Y"
    const val VIRTUAL_GROUP_OFFER_YN_N = "N"

    // 01: 중복주문, 02: 무효한 주문, 03: 장 종료
    const val OFFER_REJECT_CODE_DUPLICATE = "01"
    const val OFFER_REJECT_CODE_INVALIDITY = "02"
    const val OFFER_REJECT_CODE_TRADEEND = "03"

    /**
     * 0=장구분동안유효(장전시간외, 정규장, 시간외등하나의장구분동안만유효),
     * 1=취소할때까지유효(GTC),
     * 2=At the Opening (OPG),
     * 3=전부체결안되면전체수량취소(IOC),
     * 4=체결될수있는만큼체결되고잔량취소(FOK),
     * 5=정해진시간까지유효
     */
    const val TIME_IN_FORCE_CODE_MARKET_VALIDITY = "0"
    const val TIME_IN_FORCE_CODE_GTC = "1"
    const val TIME_IN_FORCE_CODE_OPG = "2"
    const val TIME_IN_FORCE_CODE_IOC = "3"
    const val TIME_IN_FORCE_CODE_FOK = "4"
    const val TIME_IN_FORCE_CODE_UNTIL_TIME = "5"

    // 지불조건 : 01=PPD, 02=CCT
    const val OFFER_PAYMENT_TERM_CODE_PPD = "01"
    const val OFFER_PAYMENT_TERM_CODE_CCT = "02"

    // Payment plan code
    const val PAYMENT_PLANCODE_PS = "01PS"
    const val PAYMENT_PLANCODE_PF = "01PF"
    const val PAYMENT_PLANCODE_PL = "01PL"

    // 운송조건 : 01=CY-CY, 02=CY-DOOR, 03=DOOR-CY, 04=DOOR-DOOR
    const val OFFER_RD_TERM_CODE_CYCY = "01"
    const val OFFER_RD_TERM_CODE_CYDOOR = "02"
    const val OFFER_RD_TERM_CODE_DOORCY = "03"
    const val OFFER_RD_TERM_CODE_DOORDOOR = "04"

    // 입력소스 : 01=User Offer, 02=Deal System
    const val INPUT_SOURCE_USEROFFER = "01"
    const val INPUT_SOURCE_DEALSYSTEM = "02"

    // 01=Dry, 02=Reefer, 03=Empty, 04=SOC
    const val CONTAINER_TYPE_CODE_DRY = "01"
    const val CONTAINER_TYPE_CODE_REEFER = "02"
    const val CONTAINER_TYPE_CODE_EMPTY = "03"
    const val CONTAINER_TYPE_CODE_SOC = "04"

    // 01=20ft, 02=40ft, 03=40ftHC, 04=45ftHC
    const val CONTAINER_SIZE_CODE_20FT = "01"
    const val CONTAINER_SIZE_CODE_40FT = "02"
    const val CONTAINER_SIZE_CODE_40FTHC = "03"
    const val CONTAINER_SIZE_CODE_45FTHC = "04"

    // 위치타입코드 : 01=POR, 02=POL, 03=POD, 04=DEL
    const val LOCATION_TYPE_CODE_POR = "01"
    const val LOCATION_TYPE_CODE_POL = "02"
    const val LOCATION_TYPE_CODE_POD = "03"
    const val LOCATION_TYPE_CODE_DEL = "04"

    // 팩터아이디번호 : 001=Week, 002=Route, 003=Price, 004=Volume
    const val FACTOR_ID_WEEK = 1
    const val FACTOR_ID_ROUTE = 2
    const val FACTOR_ID_PRICE = 3
    const val FACTOR_ID_VOLUME = 4
}