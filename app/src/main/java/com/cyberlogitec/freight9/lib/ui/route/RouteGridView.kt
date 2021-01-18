package com.cyberlogitec.freight9.lib.ui.route

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.lib.util.toPx
import kotlinx.android.synthetic.main.item_route_perview.view.*


class RouteGridView : LinearLayout {


    private var mPortDataList: RouteDataList? = RouteDataList()
    lateinit var paint: Paint
    lateinit var paintPorHighlight: Paint
    lateinit var paintPolHighlight: Paint
    lateinit var paintPodHighlight: Paint
    lateinit var paintDelHighlight: Paint

    val mPorPathHighlight = Path()
    val mPolPathHighlight = Path()
    val mPodPathHighlight = Path()
    val mDelPathHighlight = Path()
    var mContext: Context
    private var svPor: View? = null
    private var svPol: View? = null
    private var svPod: View? = null
    private var svDel: View? = null
    private var ivCenterPort: ImageView? = null

    var mPor = ""
    var mPol = ""
    var mPod = ""
    var mDel = ""

    var mLastSelected:String? = ""

    var mLastSelectedType: PortType = PortType.NOT

    var mViewType:GridViewType = GridViewType.MARKET_POPUP

    enum class GridViewType {MARKET_POPUP, SELL_OFFER}

    enum class PortType {NOT, POR, POL, POD, DEL}

    lateinit var mPorAnimator:ObjectAnimator
    lateinit var mPolAnimator:ObjectAnimator
    lateinit var mPodAnimator:ObjectAnimator
    lateinit var mDelAnimator:ObjectAnimator

    var porLength:Float = 0.0f
    var polLength:Float = 0.0f
    var podLength:Float = 0.0f
    var delLength:Float = 0.0f

    constructor(context: Context) : super(context) {
        mContext = context
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mContext = context
        initView()

    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        mContext = context
        initView()
    }

    fun getPaint(isHighlight: Boolean): Paint {
        var paint = Paint()
        paint.style = Paint.Style.STROKE
        if(isHighlight){
            paint.strokeWidth = 2.toPx().toFloat() // 선 굵기
        } else {
            paint.strokeWidth = 1.toPx().toFloat() // 선 굵기
        }
        paint.color = getLineColor(isHighlight)

        return paint
    }

    private fun initView() {
        initPaint()

        val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val v = li.inflate(R.layout.route_preview_layout, this, false)
        addView(v)
        this.setWillNotDraw(false)

        mPorAnimator = ObjectAnimator.ofFloat(this@RouteGridView, "phasePor", 1.0f, 0.0f)
        mPorAnimator.duration = 600
        mPorAnimator.addListener(object: Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {showNextAni(PortType.POL)}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}
        })

        mPolAnimator = ObjectAnimator.ofFloat(this@RouteGridView, "phasePol", 1.0f, 0.0f)
        mPolAnimator.duration = 600
        mPolAnimator.addListener(object: Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {showNextAni(PortType.POD)}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}
        })
        mPodAnimator = ObjectAnimator.ofFloat(this@RouteGridView, "phasePod", 1.0f, 0.0f)
        mPodAnimator.duration = 600
        mPodAnimator.addListener(object: Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {showNextAni(PortType.DEL)}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}
        })
        mDelAnimator = ObjectAnimator.ofFloat(this@RouteGridView, "phaseDel", 1.0f, 0.0f)
        mDelAnimator.duration = 600
        mDelAnimator.addListener(object: Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {changeStateShip(true)}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}
        })
    }

    private fun initPaint() {
        paint = getPaint(false)

        paintPorHighlight = getPaint(true)
        paintPolHighlight = getPaint(true)
        paintPodHighlight = getPaint(true)
        paintDelHighlight = getPaint(true)
    }

    private fun cancelAni() {
        if(mPorAnimator.isRunning)
            mPorAnimator.cancel()
        if(mPolAnimator.isRunning)
            mPolAnimator.cancel()
        if(mPodAnimator.isRunning)
            mPodAnimator.cancel()
        if(mDelAnimator.isRunning)
            mDelAnimator.cancel()
    }

    private fun showNextAni(portType: PortType) {

        when(portType) {
            PortType.POR -> {
                changeStateShip(false)
                if(mPor.isNotEmpty() && mPol.isNotEmpty())
                    mPorAnimator.start()
                else
                    showNextAni(PortType.POL)
            }
            PortType.POL -> {
                var polList = mPortDataList!!.dataList
                        .filter { it.polCd.equals(mPol) && it.podCd.equals(mPod) }
                        .distinctBy { it.podCd }
                if(mPol.isNotEmpty() && mPod.isNotEmpty() && polList.isNotEmpty())
                    mPolAnimator.start()
                else
                    showNextAni(PortType.POD)
            }
            PortType.POD -> {
                var polList = mPortDataList!!.dataList
                        .filter { it.polCd.equals(mPol) && it.podCd.equals(mPod) }
                        .distinctBy { it.podCd }
                if(mPol.isNotEmpty() && mPod.isNotEmpty() && polList.isNotEmpty()) {
                    changeStateShip(true)
                    mPodAnimator.start()
                }
                else
                    showNextAni(PortType.DEL)
            }
            PortType.DEL -> {
                if(mPod.isNotEmpty() && mDel.isNotEmpty())
                    mDelAnimator.start()
                /*else
                    changeStateShip(true)*/
            }
        }
    }
    private fun changeStateShip(isEnabled: Boolean) {
        when(isEnabled){
            false -> {
                ivCenterPort!!.setImageResource(R.drawable.ic_grid_oceanfreight_disabled)}
            true -> {
                //2. pol -> pod
                if(mPol.isNotEmpty() && mPod.isNotEmpty()) {

                    var polList = mPortDataList!!.dataList
                            .filter { it.polCd.equals(mPol) && it.podCd.equals(mPod) }
                            .distinctBy { it.podCd }
                    if(polList.isNotEmpty()) {
                        ivCenterPort!!.setImageResource(R.drawable.ic_grid_oceanfreight)
                    }else {
                        ivCenterPort!!.setImageResource(R.drawable.ic_grid_oceanfreight_disabled)
                    }
                }
            }
        }

    }

    private fun getLineColor(isSelect: Boolean): Int {
        when(mViewType) {
            GridViewType.MARKET_POPUP -> {
                if(isSelect)
                    return context.getColor(R.color.blue_violet)
                else
                    return context.getColor(R.color.greyish_brown)

            }
            GridViewType.SELL_OFFER -> {
                if(isSelect)
                    return context.getColor(R.color.blue_violet)
                else
                    return context.getColor(R.color.very_light_pink)
            }
        }
    }

    private fun setPortSelectDrawable(view: View, isSelect: Boolean) {

        when(mViewType) {
            GridViewType.MARKET_POPUP -> {
                when(isSelect){
                    true -> {view.background = context.getDrawable(R.drawable.bg_round_corner_8_purpley_blue)
                        view.tv_code_name.setTextColor(context.getColor(R.color.white))
                        view.tv_name.setTextColor(context.getColor(R.color.white))
                        view.tv_name.mFadeColor = context.getColor(R.color.purpley_blue)
                    }
                    false -> {view.background = context.getDrawable(R.drawable.bg_round_corner_8_greyish_brown_border)
                        view.tv_code_name.setTextColor(context.getColor(R.color.white))
                        view.tv_name.setTextColor(context.getColor(R.color.very_light_pink))
                        view.tv_name.mFadeColor = context.getColor(R.color.greyish_brown)
                    }
                }
            }
            GridViewType.SELL_OFFER -> {
                when(isSelect){
                    true -> {
                        view.background = context.getDrawable(R.drawable.bg_round_corner_8_purpley_blue)
                        view.tv_code_name.setTextColor(context.getColor(R.color.white))
                        view.tv_name.setTextColor(context.getColor(R.color.white))
                        view.tv_name.mFadeColor = context.getColor(R.color.purpley_blue)
                    }
                    false -> {
                        view.background = context.getDrawable(R.drawable.bg_round_corner_8_white)
                        view.tv_code_name.setTextColor(context.getColor(R.color.greyish_brown))
                        view.tv_name.setTextColor(context.getColor(R.color.very_light_pink))
                        view.tv_name.mFadeColor = context.getColor(R.color.white)
                    }
                }
            }
        }
    }

    fun setData(dataList: RouteDataList) {
        mPortDataList = dataList.clone()
        mLastSelectedType=PortType.NOT
        mLastSelected=""
        initPaint()
        makeView()
    }
    fun resetView(){
        mLastSelectedType=PortType.NOT
        mLastSelected=""
        mPor=""
        mPol=""
        mPod=""
        mDel=""
        mPortDataList?.clear()
    }

    private fun makeView() {
        val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        var porList: List<RouteDataList.RouteItemData> = ArrayList()
        //1. por을 만든다
        if(mPol.isNotEmpty()){
            porList = mPortDataList!!.getPors(mPol)
        }else if (mPor.isNotEmpty()){
            porList = mPortDataList!!.getPor(mPor)

        }
        val llPor = findViewById<LinearLayout>(R.id.ll_category_por)
        llPor.removeAllViewsInLayout()
        for ((index, data) in porList.withIndex()) {
            val v = li.inflate(R.layout.item_route_perview, this, false)
            v.tv_code_name.text = data.code
            v.tv_name.text = data.name

            v.setOnClickListener {
                kotlin.run {
                    var viewData: RouteDataList.RouteItemData = it.tag as RouteDataList.RouteItemData
                    selectedPort(viewData.code!!,PortType.POR)
                    this.makeView() }
            }
            v.tag = data
            if(data.code.equals(mPor))
                setPortSelectDrawable(v.ll_port,true)
            else
                setPortSelectDrawable(v.ll_port,false)
            llPor.addView(v)
            data.view = v

            if(index == 0)
                v.v_left_view.visibility = View.VISIBLE
            else
                v.v_left_view.visibility = View.GONE
            if(index == porList.lastIndex)
                v.v_right_view.visibility = View.VISIBLE
            else
                v.v_right_view.visibility = View.GONE

        }


        //2. pol를 만든다
        val llPol = findViewById<LinearLayout>(R.id.ll_category_pol)
        llPol.removeAllViewsInLayout()
        for ((index, data) in mPortDataList!!.polList.withIndex()) {
            val v = li.inflate(R.layout.item_route_perview, this, false)
            v.tv_code_name.text = data.code
            v.tv_name.text = data.name
            v.setOnClickListener {
                kotlin.run {
                    var viewData: RouteDataList.RouteItemData = it.tag as RouteDataList.RouteItemData
                    selectedPort(viewData.code!!,PortType.POL)
                    this.makeView() }
            }
            v.tag = data
            if(data.code.equals(mPol))
                setPortSelectDrawable(v.ll_port,true)
            else
                setPortSelectDrawable(v.ll_port,false)
            llPol.addView(v)
            data.view = v

            if(index == 0)
                v.v_left_view.visibility = View.VISIBLE
            else
                v.v_left_view.visibility = View.GONE
            if(index == mPortDataList!!.polList.lastIndex)
                v.v_right_view.visibility = View.VISIBLE
            else
                v.v_right_view.visibility = View.GONE

        }
        //3. pod를 만든다
        val llPod = findViewById<LinearLayout>(R.id.ll_category_pod)
        llPod.removeAllViewsInLayout()
        for ((index, data) in mPortDataList!!.podList.withIndex()) {
            val v = li.inflate(R.layout.item_route_perview, this, false)
            v.tv_code_name.text = data.code
            v.tv_name.text = data.name
            v.setOnClickListener {
                kotlin.run {
                    var viewData: RouteDataList.RouteItemData = it.tag as RouteDataList.RouteItemData
                    selectedPort(viewData.code!!,PortType.POD)
                    this.makeView() }
            }
            v.tag = data
            if(data.code.equals(mPod))
                setPortSelectDrawable(v.ll_port,true)
            else
                setPortSelectDrawable(v.ll_port,false)
            llPod.addView(v)
            data.view = v

            if(index == 0)
                v.v_left_view.visibility = View.VISIBLE
            else
                v.v_left_view.visibility = View.GONE
            if(index == mPortDataList!!.podList.lastIndex)
                v.v_right_view.visibility = View.VISIBLE
            else
                v.v_right_view.visibility = View.GONE

        }
        //4. del을 만든다

        var delList: List<RouteDataList.RouteItemData> = ArrayList()
        if (mPod.isNotEmpty()){
            delList = mPortDataList!!.getDels(mPod)
        }else if(mDel.isNotEmpty()) {
            delList = mPortDataList!!.getDel(mDel)
        }
        val llDel = findViewById<LinearLayout>(R.id.ll_category_del)
        llDel.removeAllViewsInLayout()
        for ((index, data) in delList.withIndex()) {
            val v = li.inflate(R.layout.item_route_perview, this, false)
            v.tv_code_name.text = data.code
            v.tv_name.text = data.name
            v.setOnClickListener {
                kotlin.run {
                    var viewData: RouteDataList.RouteItemData = it.tag as RouteDataList.RouteItemData
                    selectedPort(viewData.code!!,PortType.DEL)
                    this.makeView() }
            }
            v.tag = data
            if(data.code.equals(mDel))
                setPortSelectDrawable(v.ll_port,true)
            else
                setPortSelectDrawable(v.ll_port,false)
            llDel.addView(v)

            data.view = v
            if(index == 0)
                v.v_left_view.visibility = View.VISIBLE
            else
                v.v_left_view.visibility = View.GONE
            if(index == delList.lastIndex)
                v.v_right_view.visibility = View.VISIBLE
            else
                v.v_right_view.visibility = View.GONE
        }
        svPor = findViewById(R.id.hlv_por)
        svPol = findViewById(R.id.hlv_pol)
        svPod = findViewById(R.id.hlv_pod)
        svDel = findViewById(R.id.hlv_del)
        svPor!!.setOnScrollChangeListener { view, i, i1, i2, i3 -> cancelAni()
            invalidate() }
        svPol!!.setOnScrollChangeListener { view, i, i1, i2, i3 -> cancelAni()
            invalidate() }
        svPod!!.setOnScrollChangeListener { view, i, i1, i2, i3 -> cancelAni()
            invalidate() }
        svDel!!.setOnScrollChangeListener { view, i, i1, i2, i3 -> cancelAni()
            invalidate() }

        ivCenterPort = findViewById(R.id.iv_center_port)
        cancelAni()
        showNextAni(PortType.POR)
        invalidate()
    }

    private fun selectedPort(port:String, portType:PortType){
        mLastSelected = port
        mLastSelectedType = portType
        when(portType) {
            PortType.POR -> {
                if(mPor.equals(port)) {
                    mPor = ""
                    mLastSelected = ""
                    mLastSelectedType = PortType.NOT
                }else
                    mPor = port
            }
            PortType.POL -> {
                if(mPol.equals(port)) {
                    mPol = ""
                    mLastSelected = ""
                    mLastSelectedType = PortType.NOT
                }else {
                    mPol = port
                    if (mPortDataList!!.dataList.filter { it.polCd.equals(mPol) && it.porCd.equals(mPor) }.isEmpty())
                        mPor = ""
                }
            }
            PortType.POD -> {
                if(mPod.equals(port)) {
                    mPod = ""
                    mLastSelected = ""
                    mLastSelectedType = PortType.NOT
                }else {

                    mPod = port
                    if(mPortDataList!!.dataList.filter { it.podCd.equals(mPod) && it.delCd.equals(mDel) }.isEmpty())
                        mDel = ""
                }
            }
            PortType.DEL -> {
                if(mDel.equals(port)){
                    mDel = ""
                    mLastSelected = ""
                    mLastSelectedType = PortType.NOT
                }else
                    mDel = port
            }
        }
    }
    private fun isSelectedPort(port:String, portType:PortType):Boolean {
        return (port.equals(mLastSelected) && portType == mLastSelectedType)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        renderPreviewMode(canvas)
    }

    private fun renderPreviewMode(canvas: Canvas) {
        if (null != mPortDataList) {
            //1. mPor -> mPol line을 그린다
            if((mPor.isNotEmpty() && mPol.isEmpty()) || mLastSelectedType.equals(PortType.POR)){
                // por만 선택된 상태로 가능한 경로만 그린다
                var data = mPortDataList!!.getPor(mPor).firstOrNull()

                var polList = mPortDataList!!.dataList.filter { it.porCd.equals(mPor) }
                polList = polList.distinctBy { it.polCd }

                for(dataPol in mPortDataList!!.polList) {
                    if (null != data?.view && null != dataPol.view && polList.any { it.polCd == dataPol.code }) {
                        drawPorPolLine(canvas, paint, data, dataPol,false)
                    }
                }

            }else {
                //1.1 pol이 선택된 경우 por-> pol
                var dataPol = mPortDataList!!.getPol(mPol).firstOrNull()

                for(data in mPortDataList!!.getPors(mPol)) {
                    dataPol?.let { drawPorPolLine(canvas, paint, data, it, false) }
                }
            }

            //2. mPol -> ship 라인을 그린다
            if(mLastSelectedType == PortType.POD){
                //pod의 pol item을 가져온다 중복제거

                var polList = mPortDataList!!.dataList.filter { it.podCd.equals(mPod) }.distinctBy { it.polCd }

                for(dataPol in mPortDataList!!.polList) {
                    if (null != dataPol.view && polList.any { it.polCd == dataPol.code }) {
                        drawPolShipLine(canvas, paint, dataPol,false)
                    }
                }
                var dataPod = mPortDataList!!.getPod(mPod).firstOrNull()
                if (null != dataPod?.view) {
                    drawShipPodLine(canvas, paint, dataPod,false)
                }

            }else if(mLastSelectedType == PortType.POL){
                var podList = mPortDataList!!.dataList.filter { it.polCd.equals(mPol) }.distinctBy { it.podCd }

                for(dataPod in mPortDataList!!.podList) {
                    if (null != dataPod.view && podList.any { it.podCd == dataPod.code }) {
                        drawShipPodLine(canvas, paint, dataPod, false)
                    }
                }
                var dataPol = mPortDataList!!.getPol(mPol).firstOrNull()
                if (null != dataPol?.view) {
                    drawPolShipLine(canvas, paint, dataPol, false)
                }

            }else {
                //경로가 없거나 있거나...
                if(mPol.isNotEmpty() && mPod.isNotEmpty()){
                    //경로가 있는지 체크
                    var polList = mPortDataList!!.dataList
                            .filter { it.polCd.equals(mPol) && it.podCd.equals(mPod) }
                            .distinctBy { it.podCd }

                    var dataPol = mPortDataList!!.getPol(mPol).firstOrNull()

                    if (null != dataPol?.view && polList.any { it.polCd == dataPol.code }) {
                        drawPolShipLine(canvas, paint, dataPol, false)
                        //ship -> pod
                        var dataPod = mPortDataList!!.getPod(mPod).firstOrNull()
                        if (null != dataPod?.view) {
                            drawShipPodLine(canvas, paint, dataPod, false)
                        }
                    }
                }else{
                    //여기는 경로가 없다...
                }

            }

            //3. pod-> del 경로를 그린다
            if((mDel.isNotEmpty() && mPod.isEmpty()) || mLastSelectedType.equals(PortType.DEL)){
                // del만 선택된 상태로 가능한 경로만 그린다
                var data = mPortDataList!!.getDel(mDel).firstOrNull()

                var podList = mPortDataList!!.dataList.filter { it.delCd.equals(mDel) }
                podList = podList.distinctBy { it.podCd }

                for(dataPod in mPortDataList!!.podList) {
                    if (null != data?.view && null != dataPod.view && podList.any { it.podCd == dataPod.code }) {

                        drawPodDelLine(canvas, paint, dataPod, data, false)
                    }
                }

            }else {
                //3.1 pod이 선택된 경우 del-> pod
                var dataPod = mPortDataList!!.getPod(mPod).firstOrNull()

                for(data in mPortDataList!!.getDels(mPod)) {

                    dataPod?.let { drawPodDelLine(canvas, paint, it, data, false) }
                }
            }

        }
        renderRouteHighlight(canvas)
    }

    private fun drawPodDelLine(canvas: Canvas, paint: Paint, dataPod: RouteDataList.RouteItemData, data: RouteDataList.RouteItemData, isHighlight:Boolean) {
        val lt = IntArray(2)
        val lb = IntArray(2)
        var offsetxt = dataPod.view!!.width / 2
        if(dataPod.view!!.v_left_view.visibility == View.VISIBLE && dataPod.view!!.v_right_view.visibility == View.VISIBLE){

        }else if(dataPod.view!!.v_left_view.visibility == View.VISIBLE){
            offsetxt += dataPod.view!!.v_left_view.width /2
        }else if(dataPod.view!!.v_right_view.visibility == View.VISIBLE){
            offsetxt -= dataPod.view!!.v_right_view.width /2
        }
        var offsetxb = data.view!!.width / 2
        if(data.view!!.v_left_view.visibility == View.VISIBLE && data.view!!.v_right_view.visibility == View.VISIBLE) {
            //
        }else if(data.view!!.v_left_view.visibility == View.VISIBLE){
            offsetxb += data.view!!.v_left_view.width /2
        }else if(data.view!!.v_right_view.visibility == View.VISIBLE){
            offsetxb -= data.view!!.v_right_view.width /2
        }

        dataPod.view!!.getLocationOnScreen(lt)
        data.view!!.getLocationOnScreen(lb)

        val path = Path()
        path.moveTo(lt[0].toFloat() + offsetxt , svPod!!.bottom.toFloat() - resources.getDimensionPixelSize(R.dimen.route_grid_item_margine).toFloat())

        if (lt[0].toFloat() + offsetxt != lb[0].toFloat() + offsetxb) {
            path.lineTo(lt[0].toFloat() + offsetxt, (svPod!!.top - (svPod!!.top - svDel!!.bottom) / 2).toFloat())
            path.lineTo(lb[0].toFloat() + offsetxb, (svPod!!.top - (svPod!!.top - svDel!!.bottom) / 2).toFloat())
        }

        path.lineTo(lb[0].toFloat() + offsetxb, svDel!!.top.toFloat() /*+ resources.getDimensionPixelSize(R.dimen.route_grid_item_margine).toFloat()*/)
        if(isHighlight){
            mDelPathHighlight.addPath(path)
        }else {
            canvas.drawPath(path, paint)
        }
    }

    private fun drawShipPodLine(canvas: Canvas, paint: Paint, dataPod: RouteDataList.RouteItemData, isHighlight: Boolean) {

        val lt = IntArray(2)
        val lb = IntArray(2)
        val offsetxt = dataPod.view!!.width / 2
        ivCenterPort!!.getLocationOnScreen(lt)
        lt[0] += ivCenterPort!!.width / 2
        lt[1] = svPol!!.bottom + ivCenterPort!!.bottom// +iv_center_port.getHeight()/2;

        var offsetxb = dataPod.view!!.width / 2
        if(dataPod.view!!.v_left_view.visibility == View.VISIBLE && dataPod.view!!.v_right_view.visibility == View.VISIBLE){

        }else if(dataPod.view!!.v_left_view.visibility == View.VISIBLE){
            offsetxb += dataPod.view!!.v_left_view.width /2
        }else if(dataPod.view!!.v_right_view.visibility == View.VISIBLE){
            offsetxb -= dataPod.view!!.v_right_view.width /2
        }
        dataPod.view!!.getLocationOnScreen(lb)
        lb[0] += offsetxb
        lb[1] = svPod!!.top

        val path = Path()
        path.moveTo(lt[0].toFloat(), lt[1].toFloat())
        if (lt[0].toFloat() != lb[0].toFloat()) {
            path.lineTo(lt[0].toFloat(), (lt[1] + (svPod!!.top - lt[1]) / 2).toFloat())
            path.lineTo(lb[0].toFloat(), (lt[1] + (svPod!!.top - lt[1]) / 2).toFloat())
        }
        path.lineTo(lb[0].toFloat(), lb[1].toFloat() /*+ resources.getDimensionPixelSize(R.dimen.route_grid_item_margine).toFloat()*/)

        if(isHighlight){
            mPodPathHighlight.addPath(path)
        }else {
            canvas.drawPath(path, paint)
        }
    }

    private fun drawPolShipLine(canvas: Canvas, paint: Paint, dataPol: RouteDataList.RouteItemData, isHighlight: Boolean) {

        val lt = IntArray(2)
        var offsetxt = dataPol.view!!.width / 2
        dataPol.view!!.getLocationOnScreen(lt)
        if(dataPol.view!!.v_left_view.visibility == View.VISIBLE && dataPol.view!!.v_right_view.visibility == View.VISIBLE){

        }else if(dataPol.view!!.v_left_view.visibility == View.VISIBLE){
            offsetxt += dataPol.view!!.v_left_view.width /2
        }else if(dataPol.view!!.v_right_view.visibility == View.VISIBLE){
            offsetxt -= dataPol.view!!.v_right_view.width /2
        }

        val lb = IntArray(2)
        ivCenterPort!!.getLocationOnScreen(lb)

        lb[0] += ivCenterPort!!.width / 2
        lb[1] = svPol!!.bottom + ivCenterPort!!.top

        val offsetxb = dataPol.view!!.width / 2

        val path = Path()
        path.moveTo(lt[0].toFloat() + offsetxt, svPol!!.bottom.toFloat() - resources.getDimensionPixelSize(R.dimen.route_grid_item_margine).toFloat())

        if (lt[0].toFloat() + offsetxt != lb[0].toFloat()) {
            path.lineTo(lt[0].toFloat() + offsetxt, (lb[1] - ivCenterPort!!.top / 2).toFloat())
            path.lineTo(lb[0].toFloat(), (lb[1] - ivCenterPort!!.top / 2).toFloat())
        }
        path.lineTo(lb[0].toFloat(), lb[1].toFloat())
        if(isHighlight){
            mPolPathHighlight.addPath(path)
        }else {
            canvas.drawPath(path, paint)
        }
    }

    private fun drawPorPolLine(canvas: Canvas, paint: Paint, data: RouteDataList.RouteItemData, dataPol: RouteDataList.RouteItemData, isHighlight: Boolean) {
        val lt = IntArray(2)
        var offsetxt = data.view!!.width / 2
        if(data.view!!.v_left_view.visibility == View.VISIBLE && data.view!!.v_right_view.visibility == View.VISIBLE){
            //
        }else if(data.view!!.v_left_view.visibility == View.VISIBLE){
            offsetxt += data.view!!.v_left_view.width /2
        }else if(data.view!!.v_right_view.visibility == View.VISIBLE){
            offsetxt -= data.view!!.v_right_view.width /2
        }
        data.view!!.getLocationOnScreen(lt)
        val lb = IntArray(2)
        var offsetxb = dataPol.view!!.width / 2
        if(dataPol.view!!.v_left_view.visibility == View.VISIBLE && dataPol.view!!.v_right_view.visibility == View.VISIBLE){

        }else if(dataPol.view!!.v_left_view.visibility == View.VISIBLE){
            offsetxb += dataPol.view!!.v_left_view.width /2
        }else if(dataPol.view!!.v_right_view.visibility == View.VISIBLE){
            offsetxb -= dataPol.view!!.v_right_view.width /2
        }
        dataPol.view!!.getLocationOnScreen(lb)

        val path = Path()
        path.moveTo(lt[0].toFloat() + offsetxt, svPor!!.bottom.toFloat() - resources.getDimensionPixelSize(R.dimen.route_grid_item_margine).toFloat())

        if (lt[0].toFloat() + offsetxt != lb[0].toFloat() + offsetxb) {
            path.lineTo(lt[0].toFloat() + offsetxt, (svPol!!.top - (svPol!!.top - svPor!!.bottom) / 2).toFloat())
            path.lineTo(lb[0].toFloat() + offsetxb, (svPol!!.top - (svPol!!.top - svPor!!.bottom) / 2).toFloat())
        }
        path.lineTo(lb[0].toFloat() + offsetxb, svPol!!.top.toFloat() /*+ resources.getDimensionPixelSize(R.dimen.route_grid_item_margine).toFloat()*/)
        if(isHighlight){
            mPorPathHighlight.addPath(path)
        } else {
            canvas.drawPath(path, paint)
        }
    }

    private fun resetPathHighlight() {
        mPorPathHighlight.reset()
        mPolPathHighlight.reset()
        mPodPathHighlight.reset()
        mDelPathHighlight.reset()
    }

    private fun renderRouteHighlight(canvas: Canvas) {
        resetPathHighlight()
        //1. por -> pol

        if(mPor.isNotEmpty() && mPol.isNotEmpty()){
            var data = mPortDataList!!.getPor(mPor).first()

            drawPorPolLine(canvas, paintPorHighlight, mPortDataList!!.getPor(mPor).first(),
                    mPortDataList!!.getPol(mPol).first(),true)

        }

        //2. pol -> pod
        if(mPol.isNotEmpty() && mPod.isNotEmpty()) {

            var polList = mPortDataList!!.dataList
                    .filter { it.polCd.equals(mPol) && it.podCd.equals(mPod) }
                    .distinctBy { it.podCd }
            if(polList.isNotEmpty()) {
                drawPolShipLine(canvas, paintPolHighlight, mPortDataList!!.getPol(mPol).first(), true)
                drawShipPodLine(canvas, paintPodHighlight, mPortDataList!!.getPod(mPod).first(), true)
            }
        }

        //3. pod -> del
        if(mPod.isNotEmpty() && mDel.isNotEmpty()){
            drawPodDelLine(canvas, paintDelHighlight, mPortDataList!!.getPod(mPod).first(), mPortDataList!!.getDel(mDel).first(), true)

        }

        // Measure the path
        val measure = PathMeasure(mPorPathHighlight, false)
        porLength = measure.length
        val measure1 = PathMeasure(mPolPathHighlight, false)
        polLength = measure1.length
        val measure2 = PathMeasure(mPodPathHighlight, false)
        podLength = measure2.length
        val measure3 = PathMeasure(mDelPathHighlight, false)
        delLength = measure3.length

        if(mPorAnimator.isRunning){
            canvas.drawPath(mPorPathHighlight,paintPorHighlight)
        }else if(mPolAnimator.isRunning) {
            canvas.drawPath(mPorPathHighlight,paintPorHighlight)
            canvas.drawPath(mPolPathHighlight, paintPolHighlight)
        }else if(mPodAnimator.isRunning) {
            canvas.drawPath(mPorPathHighlight,paintPorHighlight)
            canvas.drawPath(mPolPathHighlight, paintPolHighlight)
            canvas.drawPath(mPodPathHighlight, paintPodHighlight)
        }else if(mDelAnimator.isRunning){
            canvas.drawPath(mPorPathHighlight,paintPorHighlight)
            canvas.drawPath(mPolPathHighlight, paintPolHighlight)
            canvas.drawPath(mPodPathHighlight, paintPodHighlight)
            canvas.drawPath(mDelPathHighlight, paintDelHighlight)
        }else {
            canvas.drawPath(mPorPathHighlight,getPaint(true))
            canvas.drawPath(mPolPathHighlight, getPaint(true))
            canvas.drawPath(mPodPathHighlight, getPaint(true))
            canvas.drawPath(mDelPathHighlight, getPaint(true))
        }
    }

    fun setPhasePor(phase: Float) {
        paintPorHighlight.pathEffect = createPathEffect(porLength, phase, 0.0f)
        invalidate()//will calll onDraw
    }
    fun setPhasePol(phase: Float) {
        paintPolHighlight.pathEffect = createPathEffect(polLength, phase, 0.0f)
        invalidate()//will calll onDraw
    }
    fun setPhasePod(phase: Float) {
        paintPodHighlight.pathEffect = createPathEffect(podLength, phase, 0.0f)
        invalidate()//will calll onDraw
    }
    fun setPhaseDel(phase: Float) {
        paintDelHighlight.pathEffect = createPathEffect(delLength, phase, 0.0f)
        invalidate()//will calll onDraw
    }
    private fun createPathEffect(pathLength: Float, phase: Float, offset: Float): PathEffect {
        return DashPathEffect(floatArrayOf(pathLength, pathLength),
                Math.max(phase * pathLength, offset))
    }

    fun setDel(del: String) {
        mDel = del
    }

    private fun getDel(): String {
        return mDel
    }
}
