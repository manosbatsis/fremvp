package com.cyberlogitec.freight9.lib.util

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Base64
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.config.Channel
import com.cyberlogitec.freight9.config.Constant
import com.cyberlogitec.freight9.config.ConstantTradeOffer
import com.cyberlogitec.freight9.config.ContainerType
import com.cyberlogitec.freight9.lib.model.OrderTradeOfferDetail
import com.cyberlogitec.freight9.lib.ui.route.RouteData
import com.cyberlogitec.freight9.lib.ui.route.RouteDataList
import com.cyberlogitec.freight9.lib.ui.view.SafeClickListener
import com.cyberlogitec.freight9.ui.splash.SplashActivity
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.popup_order_terms.view.*
import timber.log.Timber
import java.io.File
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ln
import kotlin.math.pow


////////////////////////////////////////////////////////////////////////////////////////////////////
// Rx
// joinBy (join 2 list)
fun <T : Any, U : Any> List<T>.joinBy(collection: List<U>, filter: (Pair<T, U>) -> Boolean): List<Pair<T, List<U>>> = map { t ->
    val filtered = collection.filter { filter(Pair(t, it)) }
    Pair(t, filtered)
}

////////////////////////////////////////////////////////////////////////////////////////////////////
// Windows
fun Int.toDp(): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), App.instance.resources.displayMetrics)

fun Int.toPx(): Int = (this * App.instance.resources.displayMetrics.density).toInt()

////////////////////////////////////////////////////////////////////////////////////////////////////
// String
fun String.toFile(): File? = File(this)

fun String.capitalizeWords(): String = split(",").map { it.capitalize() }.joinToString(",")

fun File.toBase64(): String? = "data:image/${name.split(",").lastOrNull()};base64, ${kotlin.text.String(Base64.encode(readBytes(), Base64.DEFAULT))}"

inline fun <T> Iterable<T>.findWithIndex(predicate: (T) -> Boolean): Pair<T, Int>? {
    for ((count, element) in this.withIndex()) {
        if (predicate(element))
            return element to count
    }
    return null
}

inline fun <T> Iterable<T>.sumByLong(selector: (T) -> Long): Long {
    var sum = 0L
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

fun String.isNumber(): Boolean =
        this.trim().matches(Regex("\\d+"))

////////////////////////////////////////////////////////////////////////////////////////////////////
// Json
inline fun <reified T> String.fromJson(): T? = App.instance.component.enviorment().gson.fromJson(this, T::class.java)
inline fun <reified T> String.fromJsonWithTypeToken(): T = App.instance.component.enviorment().gson.fromJson(this, object: TypeToken<T>() {}.type)
fun Any.toJson(): String = App.instance.component.enviorment().gson.toJson(this)

////////////////////////////////////////////////////////////////////////////////////////////////////
// Keyboard
fun AppCompatActivity.hideKeyboard() {
    val view = this.currentFocus
    if (view != null) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    // else {
    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    // }
}

fun AppCompatActivity.showKeyboard() {
    val view = this.currentFocus
    if (view != null) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        view.requestFocus()
        imm.showSoftInput(view, 0)
    }
}

fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    this.requestFocus()
    imm.showSoftInput(this, 0)
}

fun View.hideKeyboard(): Boolean {
    try {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    } catch (ignored: RuntimeException) { }
    return false
}

// Prevent double click
fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}


////////////////////////////////////////////////////////////////////////////////////////////////////
// Context
//
fun Context.showToast(text: String) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

fun Context.showToast(resId: Int) = Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()

fun Context.isOnline(): Boolean {
    val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo: NetworkInfo? = connMgr.activeNetworkInfo
    Timber.d("f9: isOnline : " + (networkInfo?.isConnected == true))
    return networkInfo?.isConnected == true
}

fun makeRouteDataList(offerRoutes: List<OrderTradeOfferDetail.OfferRoute>): RouteDataList {
    val routeDataList = RouteDataList()
    val offerRoutesMap = offerRoutes
            .sortedBy { offerRoute -> offerRoute.offerRegSeq }
            .groupBy { offerRoute -> offerRoute.offerRegSeq }
    for (data in offerRoutesMap) {
        // data.key : offerRegSeq
        var porCode = Constant.EmptyString; var polCode = Constant.EmptyString; var podCode = Constant.EmptyString; var delCode = Constant.EmptyString
        var porCodeName = Constant.EmptyString; var polCodeName = Constant.EmptyString; var podCodeName = Constant.EmptyString; var delCodeName = Constant.EmptyString
        for (routeDatas in data.value) {
            when (routeDatas.locationTypeCode) {
                ConstantTradeOffer.LOCATION_TYPE_CODE_POR -> {
                    porCode = routeDatas.locationCode
                    porCodeName = routeDatas.locationName
                }
                ConstantTradeOffer.LOCATION_TYPE_CODE_POL -> {
                    polCode = routeDatas.locationCode
                    polCodeName = routeDatas.locationName
                }
                ConstantTradeOffer.LOCATION_TYPE_CODE_POD -> {
                    podCode = routeDatas.locationCode
                    podCodeName = routeDatas.locationName
                }
                ConstantTradeOffer.LOCATION_TYPE_CODE_DEL -> {
                    delCode = routeDatas.locationCode
                    delCodeName = routeDatas.locationName
                }
            }
        }
        routeDataList.add(RouteData(porCode, porCodeName, polCode, polCodeName,
                podCode, podCodeName, delCode, delCodeName))
    }
    return routeDataList
}

fun getEngShortMonth(month: Int) = DateFormatSymbols(Locale.US).shortMonths[month - 1]!!

////////////////////////////////////////////////////////////////////////////////////////////////////
// EditText
//
fun EditText.addTextWatcher(afterTextChanged: (String) -> Unit = { },
                            beforeTextChanged: (CharSequence?, Int, Int, Int) -> Unit = { _, _, _, _ -> },
                            onTextChanged: (CharSequence?, Int, Int, Int) -> Unit = { _, _, _, _ -> }) =
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = afterTextChanged(s.toString())

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = beforeTextChanged(s, start, count, after)

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = onTextChanged(s, start, before, count)
        })


////////////////////////////////////////////////////////////////////////////////////////////////////
// Calendar (1)
//
// get Year Week Number (nth week of year) of Today
fun getTodayWeekNumber(): Int {
    val cal = Calendar.getInstance()
    cal.firstDayOfWeek = Calendar.MONDAY
    cal.minimalDaysInFirstWeek = 4

    return cal.get(Calendar.WEEK_OF_YEAR)
}

// 오늘이 속한 주차 : 2020W02
fun getTodayYearWeekNumber() : String {
    val cal = Calendar.getInstance()
    cal.firstDayOfWeek = Calendar.SUNDAY
    cal.minimalDaysInFirstWeek = 4
    cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

    val year = cal.get(Calendar.YEAR)
    val week = cal.get(Calendar.WEEK_OF_YEAR)
    val yearweek = String.format("%d%02d", year, week)

    Timber.d("f9: getTodayYearWeekNumber = $yearweek")

    return yearweek
}

// get Last Year Week Number of This Year (or Today)
fun getThisYearWeekNumber(): Int {
    val cal = Calendar.getInstance()
    cal.firstDayOfWeek = Calendar.MONDAY
    cal.minimalDaysInFirstWeek = 4

    return cal.weeksInWeekYear
}

fun getThisYearNumber(): Int {
    val cal = Calendar.getInstance()
    cal.firstDayOfWeek = Calendar.MONDAY
    cal.minimalDaysInFirstWeek = 4

    return cal.get(Calendar.YEAR)
}

fun getThisMonthNum(): Int {
    val cal = Calendar.getInstance()
    cal.firstDayOfWeek = Calendar.MONDAY
    cal.minimalDaysInFirstWeek = 4

    return cal.get(Calendar.MONTH)
}

fun getThisDayNum(): Int {
    val cal = Calendar.getInstance()
    cal.firstDayOfWeek = Calendar.MONDAY
    cal.minimalDaysInFirstWeek = 4

    return cal.get(Calendar.DAY_OF_MONTH)
}

fun Calendar.getYearWeeks(year: Int): Int {
    val cal = Calendar.getInstance()
    cal.firstDayOfWeek = Calendar.MONDAY
    cal.minimalDaysInFirstWeek = 4

    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.MONTH, 1)

    val yearWeeks = cal.weeksInWeekYear
    Timber.d("f9: " + " yearWeeks: " + yearWeeks)

    return yearWeeks
}

// getMonthOfYearWeek
fun Calendar.getMonthOfWeek(year: Int, week: Int): Int {
    val cal = Calendar.getInstance()
    cal.firstDayOfWeek = Calendar.MONDAY
    cal.minimalDaysInFirstWeek = 4

    cal.set(Calendar.YEAR, year)
    var month = 11

    for (i in 0..10) {
        cal.set(Calendar.MONTH, i)
        cal.set(Calendar.DATE, 1)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        cal.set(Calendar.DAY_OF_MONTH, daysInMonth)
        cal.minimalDaysInFirstWeek = 4
        cal.firstDayOfWeek = Calendar.MONDAY

        val weeksOfMonth = cal.get(Calendar.WEEK_OF_YEAR)

        if (week < weeksOfMonth) {
            month = i
            break
        }

    }

    return month
}

fun Calendar.isBeginOfMonth(year: Int, week: Int): Boolean {
    var bResult = false

    val cal = Calendar.getInstance()
    cal.minimalDaysInFirstWeek = 4
    cal.firstDayOfWeek = Calendar.MONDAY

    cal.set(Calendar.YEAR, year)

    if (week == 1)
        bResult = true
    else for (i in 0..10) {
        cal.set(Calendar.MONTH, i)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        cal.set(Calendar.DAY_OF_MONTH, daysInMonth)

        val weeksOfMonth = cal.get(Calendar.WEEK_OF_YEAR)

        if (weeksOfMonth + 1 == week)  {
            bResult = true
            break
        }
    }
    return bResult
}

////////////////////////////////////////////////////////////////////////////////////////////////////
// Calendar (2)
//

fun String.getBeginDateOfWeekNumber() : String {
    val year = this.substring(0, 4)
    val numberWeekOfYear = this.substring(4, 6)

    val cal = Calendar.getInstance()
    cal.minimalDaysInFirstWeek = 4
    cal.firstDayOfWeek = Calendar.SUNDAY
    cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    cal.set(Calendar.YEAR, year.toInt())
    cal.set(Calendar.WEEK_OF_YEAR, numberWeekOfYear.toInt())

    val beginDate = SimpleDateFormat("yyyy-MM-dd").format(cal.time)
    return "'$beginDate"
}

// 2019-10-19 > '19-10-14
fun String.getBeginOfWeek(isBeginOfWeek: Boolean = false) : String {
    val year = this.substring(0, 4)
    val month = this.substring(5, 7)
    val date = this.substring(8, 10)

    val cal = Calendar.getInstance()
    cal.minimalDaysInFirstWeek = 4
    cal.firstDayOfWeek = Calendar.MONDAY

    cal.set(Calendar.YEAR, year.toInt())
    cal.set(Calendar.MONTH, month.toInt() - 1)
    cal.set(Calendar.DATE, date.toInt())

    if (isBeginOfWeek) {
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    }

    val beginOfWeek = SimpleDateFormat("yyyy-MM-dd").format(cal.time)
    return "'" + beginOfWeek.substring(2, beginOfWeek.length)
}

// 2019-12-20 00:00:00.0 > 2019-12-20
fun String.getYYYYMMDD(isYYYY: Boolean = false) : String {
    val year = this.substring(0, 4)
    val month = this.substring(5, 7)
    val date = this.substring(8, 10)

    val cal = Calendar.getInstance()
    cal.minimalDaysInFirstWeek = 4
    cal.firstDayOfWeek = Calendar.MONDAY

    cal.set(Calendar.YEAR, year.toInt())
    cal.set(Calendar.MONTH, month.toInt() - 1)
    cal.set(Calendar.DATE, date.toInt())

    val yyyyMMDD = SimpleDateFormat("yyyy-MM-dd").format(cal.time)
    return if (isYYYY) yyyyMMDD else yyyyMMDD.substring(2, yyyyMMDD.length)
}

// 20191220 00:00:00.0 > '2019-12-20
fun String.getYMDWithHypen() : String {
    val year = this.substring(0, 4)
    val month = this.substring(4, 6)
    val date = this.substring(6, 8)

    val cal = Calendar.getInstance()
    cal.minimalDaysInFirstWeek = 4
    cal.firstDayOfWeek = Calendar.MONDAY

    cal.set(Calendar.YEAR, year.toInt())
    cal.set(Calendar.MONTH, month.toInt() - 1)
    cal.set(Calendar.DATE, date.toInt())

    return "'" + SimpleDateFormat("yyyy-MM-dd").format(cal.time)
}

// 20191220000000000000 > 2019-12-20
fun String.getYMDNoSymbol() : String {
    val year = this.substring(0, 4)
    val month = this.substring(4, 6)
    val date = this.substring(6, 8)

    val cal = Calendar.getInstance()
    cal.minimalDaysInFirstWeek = 4
    cal.firstDayOfWeek = Calendar.MONDAY

    cal.set(Calendar.YEAR, year.toInt())
    cal.set(Calendar.MONTH, month.toInt() - 1)
    cal.set(Calendar.DATE, date.toInt())

    return SimpleDateFormat("yyyy-MM-dd").format(cal.time)
}

/**
 * ISO8601
 * String : YYYY-MM-DD
 * Int : 주차 (1 ~ 52 or 53)
 */
fun String.getWeekOfYear() : Int {
    val year = this.substring(0, 4)
    val month = this.substring(5, 7)
    val date = this.substring(8, 10)

    val cal = Calendar.getInstance()
    cal.minimalDaysInFirstWeek = 4
    cal.firstDayOfWeek = Calendar.MONDAY

    cal.set(Calendar.YEAR, year.toInt())
    cal.set(Calendar.MONTH, month.toInt() - 1)
    cal.set(Calendar.DATE, date.toInt())

    return cal.get(Calendar.WEEK_OF_YEAR)
}

/**
 * String : YYYY-MM-DD
 * Int : YYYY의 총 주차 수
 * 마지막날의 주차수가 1이면 1주일 전 날짜의 주차수를 총주차수로 판단한다
 * http://www.staff.science.uu.nl/~gent0113/calendar/isocalendar.htm
 * http://myweb.ecu.edu/mccartyr/isowdcal.html
 * https://www.epochconverter.com/weeknumbers
 * https://www.epochconverter.com/weeks/2019
 */
fun String.getTotalWeekCountCurrentYear() : Int {
    val year = this.substring(0, 4)

    val cal = Calendar.getInstance()
    cal.minimalDaysInFirstWeek = 4
    cal.firstDayOfWeek = Calendar.MONDAY

    cal.set(Calendar.YEAR, year.toInt())
    cal.set(Calendar.MONTH, 11)
    cal.set(Calendar.DATE, 31)

    var weekOfYear = cal.get(Calendar.WEEK_OF_YEAR)
    if (weekOfYear == 1) {
        cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 7)
        weekOfYear = cal.get(Calendar.WEEK_OF_YEAR)
    }
    return weekOfYear
}

/**
 * String : yyyyMMdd
 */
fun String.getWeekFirstSunday() : Int {
    val month = this.substring(4,6).toInt() -1
    val day = this.substring(6,8).toInt()

    val cal = Calendar.getInstance()
    cal.minimalDaysInFirstWeek = 1
    cal.firstDayOfWeek = Calendar.SUNDAY

    cal.set(Calendar.YEAR, this.substring(0,4).toInt())
    cal.set(Calendar.MONTH, month)
    cal.set(Calendar.DATE, day)
    //var date = this
    val wdf = SimpleDateFormat("ww")
    val weekofyear = TextUtils.concat(cal.weekYear.toString(),wdf.format(cal.time)).toString().toInt()
    //Timber.d("calendar = $date /${SimpleDateFormat("E").format(cal.time)}= ${cal.weekYear} / ${wdf.format(cal.time)}/ ${cal.weeksInWeekYear}  = ${weekofyear}")
    return weekofyear

}

fun String.getDateBeginOfWeek(format:String) : String {

    val cal = Calendar.getInstance()
    cal.time = Date()
    cal.set(Calendar.YEAR,this.substring(0,4).toInt())
    cal.set(Calendar.WEEK_OF_YEAR, this.substring(4,6).toInt())
    cal[Calendar.DAY_OF_WEEK] = 1

    val sdf = SimpleDateFormat(format)
    return sdf.format(cal.time)
}

fun String.getDateEndOfWeek(format:String) : String {

    val cal = Calendar.getInstance()
    cal.time = Date()
    cal.set(Calendar.YEAR,this.substring(0,4).toInt())
    cal.set(Calendar.WEEK_OF_YEAR, this.substring(4,6).toInt())
    cal[Calendar.DAY_OF_WEEK] = 7

    val sdf = SimpleDateFormat(format)
    return sdf.format(cal.time)
}


fun Int.getMonthName() : String {
    val nameOfMonths = arrayListOf<String> (
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    return nameOfMonths.get(this)
}

@SuppressLint("SimpleDateFormat")
fun String.toDate(format: String = "yyy-MM-dd HH:mm:ss"): Date? =
        try {
            SimpleDateFormat(format).parse(this)
        } catch (e: Exception) {
            null
        }

val NowInMillis get() = System.currentTimeMillis()

fun Long.toDateTimeText(): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = this
    return "'" + SimpleDateFormat("'yyyy-MM-dd hh:mm").format(cal.time).substring(2)
}

fun String.getYyMmDdHhMmDateTime(isYYYY: Boolean = false): String {
    val year = this.substring(0, 4)
    val month = this.substring(4, 6)
    val date = this.substring(6, 8)
    val hour = this.substring(8, 10)
    val min = this.substring(10, 12)

    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, year.toInt())
    cal.set(Calendar.MONTH, month.toInt() - 1)
    cal.set(Calendar.DATE, date.toInt())
    cal.set(Calendar.HOUR_OF_DAY, hour.toInt())
    cal.set(Calendar.MINUTE, min.toInt())

    var returnValue = if (isYYYY) {
        SimpleDateFormat("yyyy-MM-dd HH:mm").format(cal.time)
    } else {
        "'" + SimpleDateFormat("yyyy-MM-dd HH:mm").format(cal.time).substring(2)
    }

    return returnValue
}

fun String.getHhMmSsDateTime(): String {
    val year = this.substring(0, 4)
    val month = this.substring(4, 6)
    val date = this.substring(6, 8)
    val hour = this.substring(8, 10)
    val min = this.substring(10, 12)
    val sec = this.substring(12, 14)

    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, year.toInt())
    cal.set(Calendar.MONTH, month.toInt() - 1)
    cal.set(Calendar.DATE, date.toInt())
    cal.set(Calendar.HOUR_OF_DAY, hour.toInt())
    cal.set(Calendar.MINUTE, min.toInt())
    cal.set(Calendar.SECOND, sec.toInt())

    return SimpleDateFormat("HH:mm:ss").format(cal.time)
}

fun String.getMessageDeliveryDateTime(): String {
    val year = this.substring(0, 4)
    val month = this.substring(4, 6)
    val date = this.substring(6, 8)
    val hour = this.substring(8, 10)
    val min = this.substring(10, 12)
    val sec = this.substring(12, 14)

    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, year.toInt())
    cal.set(Calendar.MONTH, month.toInt() - 1)
    cal.set(Calendar.DATE, date.toInt())
    cal.set(Calendar.HOUR_OF_DAY, hour.toInt())
    cal.set(Calendar.MINUTE, min.toInt())
    cal.set(Calendar.SECOND, sec.toInt())

    return "'" + SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.time).substring(2)
}

@SuppressLint("SimpleDateFormat")
fun Long.toTimeText(): String =
        ((NowInMillis - this) / 1000).let { diff ->
            if (diff < 60)
                "방금 전"
            else if (diff < 60 * 60)
                "${diff / 60}분 전"
            else if (diff < 60 * 60 * 24)
                "${diff / 60 / 60}시간 전"
            else if (diff < 60 * 60 * 24 * 7)
                "${diff / 60 / 60 / 24}일 전"
            else
                SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분").format(Date(this))
        }

fun Date.toTimeText(): String = time.toTimeText()

// yearWeek : yyyyww
fun Context.getWeek(yearWeek: String?) : String {
    return this.getString(R.string.week_seq, yearWeek!!.substring(4,6))
}

// 201903 > Week 03
fun Context.getWeekFull(yearWeek: String?) : String {
    return this.getString(R.string.week_full_seq, yearWeek!!.substring(4,6))
}

// 2019-12-20 20:59:01.0 > 19-12-20
fun Context.getYYMMDD(yyyyMmDdHhMmSsMmm: String?) : String {
    return if (yyyyMmDdHhMmSsMmm!!.length >= 10) yyyyMmDdHhMmSsMmm.substring(2, 10) else Constant.EmptyString
}

////////////////////////////////////////////////////////////////////////////////////////////////////
// TermsMorePopup
fun Context.showTermsMorePopup() {
    val view = LayoutInflater.from(this).inflate(R.layout.popup_order_terms, null)
    val popupWindow = PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
    popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0 )
    view.iv_order_terms_close.setOnClickListener{ popupWindow.dismiss() }
}

// Get carrier Resource Id with its name
fun String?.getCarrierIcon(defaultDark: Boolean = true) : Int {
    val carrierIcon = if (defaultDark) {
        R.drawable.carrier_logo_default_d
    } else {
        R.drawable.carrier_logo_default_l
    }

    this?.let { code ->
        when {
            code.startsWith("cma", true) -> return R.drawable.ic_carrier_cma
            code.startsWith("cos", true) -> return R.drawable.ic_carrier_cos
            code.startsWith("emc", true) -> return R.drawable.ic_carrier_emc
            code.startsWith("hlc", true) -> return R.drawable.ic_carrier_hlc
            code.startsWith("hmm", true) -> return R.drawable.ic_carrier_hmm
            code.startsWith("isl", true) -> return R.drawable.ic_carrier_isl
            code.startsWith("kmd", true) -> return R.drawable.ic_carrier_kmd
            code.startsWith("msc", true) -> return R.drawable.ic_carrier_msc
            code.startsWith("msk", true) -> return R.drawable.ic_carrier_msk
            code.startsWith("one", true) -> return R.drawable.ic_carrier_one
            code.startsWith("pil", true) -> return R.drawable.ic_carrier_pil
            code.startsWith("sit", true) -> return R.drawable.ic_carrier_sit
            code.startsWith("slc", true) -> return R.drawable.ic_carrier_slc
            code.startsWith("tsl", true) -> return R.drawable.ic_carrier_tsl
            code.startsWith("whl", true) -> return R.drawable.ic_carrier_whl
            code.startsWith("xpr", true) -> return R.drawable.ic_carrier_xpr
            code.startsWith("yml", true) -> return R.drawable.ic_carrier_yml
            code.startsWith("zim", true) -> return R.drawable.ic_carrier_zim
            else -> {
                return carrierIcon
            }
        }
    }

    return carrierIcon
}

fun Context.getCarrierCode(code: String?): String {
    var carrierCode = this.getString(R.string.all_carriers)
    code?.let {
        carrierCode = code.trim()
    }
    return carrierCode
}

// Get carrier Resource Id with its name
fun Context.getCarrierCodeToF9(code: String?) : String {

    code?.let { code ->
        when {
            code.startsWith("cma", true) -> return "CMA"
            code.startsWith("cos", true) -> return "COS"
            code.startsWith("emc", true) -> return "EMC"
            code.startsWith("hlc", true) -> return "HLC"
            code.startsWith("hmm", true) -> return "HMM"
            code.startsWith("isl", true) -> return "ISL"
            code.startsWith("kmd", true) -> return "KMD"
            code.startsWith("MEDU", true) -> return "MSC"
            code.startsWith("msk", true) -> return "MSK"
            code.startsWith("oney", true) -> return "ONE"
            code.startsWith("pil", true) -> return "PIL"
            code.startsWith("sit", true) -> return "SIT"
            code.startsWith("slc", true) -> return "SLC"
            code.startsWith("tsl", true) -> return "TSL"
            code.startsWith("whl", true) -> return "WHL"
            code.startsWith("xpr", true) -> return "XPR"
            code.startsWith("yml", true) -> return "YML"
            code.startsWith("zim", true) -> return "ZIM"
            else -> {
                return code
            }
        }
    }
    return getString(R.string.all_carriers)
}


fun Int?.getCodeCount(isNotMinusCount: Boolean = true): String {
    var countValue = Constant.EmptyString
    this?.let { count ->
        countValue = if (count > (if (isNotMinusCount) 0 else 1)) {
            String.format("+%d", (if (isNotMinusCount) count else count -1))
        } else {
            Constant.EmptyString
        }
    }
    return countValue
}

////////////////////////////////////////////////////////////////////////////////////////////////////
// Etc
fun Context.getContainerType(info: List<String>?): String {
    info?.let {
        when {
            it.any { it == ContainerType.F_TYPE } -> { return ContainerType.F_TYPE }
            it.any { it == ContainerType.R_TYPE } -> { return ContainerType.R_TYPE }
            it.any { it == ContainerType.E_TYPE } -> { return ContainerType.E_TYPE }
            it.any { it == ContainerType.S_TYPE } -> { return ContainerType.S_TYPE }
            else -> { return "X" }
        }
    }

    return "X"
}

/**
 * Teu unit convert
 * isT : true - "T", false - "F" (T, F type)
 * isFullUnit : true - "kT", "mT", false - "k", "m"
 */
fun Context.getConvertedTeuValue(value: Int,
                                 isTType: Boolean = true,
                                 isDisplayFullUnit: Boolean = true): String {
    //Timber.d("f9: TeuValue : $value, isT : $isTType, isFullUnit : $isDisplayFullUnit")

    var convertedValue = value.toString() + (
            if (isDisplayFullUnit) {
                if (isTType) this.getString(R.string.teu_unit_t)
                else this.getString(R.string.teu_unit_f)
            } else {
                Constant.EmptyString
            }
            )

    val kilo = value / 1_000
    val mega = value / 1_000_000

    if (mega > 0) {
        convertedValue = mega.toString() + (
                if (isDisplayFullUnit) {
                    if (isTType) this.getString(R.string.teu_unit_mega_t)
                    else this.getString(R.string.teu_unit_mega_f)
                } else {
                    this.getString(R.string.teu_unit_mega)
                }
                )
    } else if (kilo > 0) {
        convertedValue = kilo.toString() + (
                if (isDisplayFullUnit) {
                    if (isTType) this.getString(R.string.teu_unit_kilo_t)
                    else this.getString(R.string.teu_unit_kilo_f)
                } else {
                    this.getString(R.string.teu_unit_kilo)
                }
                )
    }

    //Timber.d("f9: TeuValue(Converted) : $convertedValue")
    return convertedValue
}

/**
 * Check service running
 *
 * getRunningServices(int maxNum)
 * This method was deprecated in API level 26.
 * As of Build.VERSION_CODES.O, this method is no longer available to third party applications.
 * For backwards compatibility, it will still return the caller's own services.
 */
@Suppress("DEPRECATION")
fun Context.isRunningService(serviceClass: Class<*>): Boolean {
    val activityManager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service in activityManager.getRunningServices(Integer.MAX_VALUE)) {
        Timber.d("f9: service.className : ${service.service.className}")
        if (serviceClass.name == service.service.className) {
            Timber.d("f9: service.className : ${service.service.className} is Running...")
            return true
        }
    }
    return false
}

/***************************************************************************************************
 * TODO : Channles, Push notification, Badge 기획/시나리오/디자인 Fix 시 수정 필요
 * - Badge count
 * - Push noti style : Heads up, BigTextStyle
 *   https://aroundck.tistory.com/4943
 *   https://codechacha.com/ko/notifications-in-android/
 *   .setCustomBigContentView(notificationLayoutExpanded)
 * - EventService : createNotification() 관련됨
 *   동일한 channelId 에서 Noti property 변경 시 반영 안됨(앱 재설치 또는 ChannelId변경(?) 필요)
 */
fun NotificationManager.sendNotification(messageTitle: String, messageBody: String, context: Context) {

    val contentIntent = Intent(context, SplashActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(Intents.GOTO_MESSAGE_BOX, true)
    }

    val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    val bigStyle = NotificationCompat.BigTextStyle().bigText(messageBody)

    val notificationLayout = RemoteViews(context.packageName, R.layout.notification_normal)
    notificationLayout.setTextViewText(R.id.notification_title, messageTitle)
    notificationLayout.setTextViewText(R.id.notification_info, messageBody)

    val notificationLayoutExpanded = RemoteViews(context.packageName, R.layout.notification_expanded)
    notificationLayoutExpanded.setTextViewText(R.id.notification_expanded_title, messageTitle)
    notificationLayoutExpanded.setTextViewText(R.id.notification_expanded_info, messageBody)

//    val stackIntent = TaskStackBuilder.create(context)
//            .addNextIntent(contentIntent)
//            .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

    // FLAG_CANCEL_CURRENT : 이전에 생성한 PendingIntent 는 취소하고 새롭게 만든다.
    // FLAG_UPDATE_CURRENT : 이미 생성된 PendingIntent 가 존재하면 해당 Intent 의 Extra Data 만 변경한다.
    val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
    )

    val builder = createNougatNotification(context, messageTitle, messageBody)
            .setStyle(bigStyle)
            .setCustomHeadsUpContentView(notificationLayout)
            .setContentIntent(contentPendingIntent)
            .setSound(notificationSound)
    // Push를 쌓을려면 : getNotifyId()
    notify(getNotifyId(), builder.build())

    val summary = createSummaryForNougat(context)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setDefaults(0)
    notify(Channel.NOTIFICATION_PUSH_GROUP_ID, summary.build())
}

fun createNougatNotification(context: Context, messageTitle: String, messageBody: String):
        NotificationCompat.Builder {
    return NotificationCompat.Builder(context, Channel.CHANNEL_PUSH_ID)
            .setContentTitle(messageTitle)
            .setContentText(messageBody)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setGroup(Channel.NOTIFICATION_PUSH_GROUP)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
}

fun createSummaryForNougat(context: Context): NotificationCompat.Builder {
    return NotificationCompat.Builder(context, Channel.CHANNEL_PUSH_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setGroup(Channel.NOTIFICATION_PUSH_GROUP)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setGroupSummary(true)
}

fun NotificationManager.cancelNotifications() {
    cancelAll()
}

fun Context.createNotificationTradeChannel() {
    // STOMP Service channel 생성 (need for O, P, Q)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // TODO : Channel Id 변경됨 : 다른 Channel Id 로 create 된 경우 기존 created된 Channel Id 삭제
        // 일단, 사용자가 어떤 Channel id 로 설정된 앱을 사용하고 있는지 모르므로...
        // 정식 배포 시 deleteNotificationChannel 부분 코드 제거 필요.
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var createdChannel = notificationManager.getNotificationChannel("Freight9")
        if (createdChannel != null) {
            notificationManager.deleteNotificationChannel(createdChannel.id)
        }
        createdChannel = notificationManager.getNotificationChannel("STOMP SERVICE CHANNEL")
        if (createdChannel != null) {
            notificationManager.deleteNotificationChannel(createdChannel.id)
        }
        createdChannel = notificationManager.getNotificationChannel("freight9_channel")
        if (createdChannel != null) {
            notificationManager.deleteNotificationChannel(createdChannel.id)
        }
        createdChannel = notificationManager.getNotificationChannel("freight9_channel_trade")
        if (createdChannel != null) {
            notificationManager.deleteNotificationChannel(createdChannel.id)
        }

        // CHANNEL_ID 가 create 되지 않은 경우에만 create
        createdChannel = notificationManager.getNotificationChannel(Channel.CHANNEL_SERVICE_ID)
        if (createdChannel == null) {
            val channel = NotificationChannel(
                    Channel.CHANNEL_SERVICE_ID, Channel.CHANNEL_SERVICE_NAME, NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = Channel.CHANNEL_SERVICE_DESC
                importance = NotificationManager.IMPORTANCE_LOW
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
        createdChannel = notificationManager.getNotificationChannel(Channel.CHANNEL_SERVICE_ID)
        Timber.d("f9: createdChannel : $createdChannel")
    }
}

fun Context.createNotificationPushChannel() {
    // PUSH channel 생성 (need for O, P, Q)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // CHANNEL_ID 가 create 되지 않은 경우에만 create
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var createdChannel = notificationManager.getNotificationChannel(Channel.CHANNEL_PUSH_ID)
        if (createdChannel == null) {
            val channel = NotificationChannel(
                    Channel.CHANNEL_PUSH_ID, Channel.CHANNEL_PUSH_NAME, NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = Channel.CHANNEL_PUSH_DESC
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
        createdChannel = notificationManager.getNotificationChannel(Channel.CHANNEL_PUSH_ID)
        Timber.d("f9: createdChannel : $createdChannel")
    }
}

// Push를 쌓기 위해서 Id 생성
fun getNotifyId(): Int {
    val cal = Calendar.getInstance()
    return Integer.parseInt(SimpleDateFormat("ddHHmmss", Locale.US).format(cal.time))
}

// Digit to k,m convert
fun Long.getFormatedNumber(): String {
    if (this < 1000) return String.format("%.1f", this.toFloat())
    val exp = (ln(this.toDouble()) / ln(1000.0)).toInt()

    //return String.format("%.1f %c", count / 1000.0.pow(exp.toDouble()), "kMGTPE"[exp - 1])
    return String.format("%.1f%c", (this / 1000.0.pow(exp.toDouble())).toFloat(), "kMGTPE"[exp - 1])
}

// catesian products
fun <T, U> cartesianProduct(c1: Collection<T>, c2: Collection<U>): List<Pair<T, U>> {
    return c1.flatMap { lhsElem -> c2.map { rhsElem -> lhsElem to rhsElem } }
}
/**************************************************************************************************/
