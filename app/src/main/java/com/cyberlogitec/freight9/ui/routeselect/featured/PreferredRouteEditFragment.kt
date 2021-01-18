package com.cyberlogitec.freight9.ui.routeselect.featured

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.lib.model.FeaturedRoute
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.ui.routeselect.featured.FeaturedRouteDeleteDialog.DialogEventListener
import com.cyberlogitec.freight9.ui.routeselect.search.RouteSearchActivity
import com.trello.rxlifecycle3.components.support.RxFragment
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.frag_preferred_route_edit.*
import kotlinx.android.synthetic.main.item_preferred_route_edit.view.*
import timber.log.Timber
import java.util.*


class PreferredRouteEditFragment(val viewModel: PreferredRouteEditViewModel) : RxFragment(), ItemDragListener {
    private lateinit var mAdapter: PreferredRouteAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var params: ViewGroup.MarginLayoutParams
    private var density: Float = 0f
    private var mLastClickTime: Long = 0

    private final val RESULT_ROUTE_SUCESS = 100
    private final val MAX_PREFERRED_ROUTE = 10
    private final val PADDING_RECYCLER_BOTTOM = 100

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.frag_preferred_route_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        density = resources.displayMetrics.density
        params = container_preferred_route_add.layoutParams as ViewGroup.MarginLayoutParams

        val mAdapter = PreferredRouteAdapter(this)
        recycler_preferred_route_edit.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(this.context)
        }
        itemTouchHelper = ItemTouchHelper(ItemTouchHelperCallback(mAdapter))
        itemTouchHelper.attachToRecyclerView(recycler_preferred_route_edit)

        viewModel.outPuts.getFeaturedRoutes()
                .bindToLifecycle(this)
                .subscribe{
                    Timber.v("route:/route load  on fragment, size=${it.size}")
                    mAdapter.routeList = it as MutableList<FeaturedRoute>
                    mAdapter.notifyDataSetChanged()
                }
        viewModel.inPuts.callFeaturedRoute(Parameter.NULL)

        container_add_preferred_route.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime >= 1000){
                mLastClickTime = SystemClock.elapsedRealtime();

                if (mAdapter.routeList.size >= MAX_PREFERRED_ROUTE) {
                    Toast.makeText(context, "Can't Add Anymore. Max count is "+MAX_PREFERRED_ROUTE, Toast.LENGTH_SHORT).show()
                }
                else {
                    val intent = Intent(context, RouteSearchActivity::class.java)
                    val pageList = arrayListOf<String>(
                            resources.getString(R.string.route_tab_porpol),
                            resources.getString(R.string.route_tab_poddel)
                    )
                    intent.putExtra("tabPosition", 0)
                    intent.putExtra("fromCode", "FROM")
                    intent.putExtra("fromDetail", "Port or City")
                    intent.putExtra("toCode", "TO")
                    intent.putExtra("toDetail", "Port or City")
                    intent.putExtra("pageList", pageList)
                    startActivityForResult(intent, RESULT_ROUTE_SUCESS)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK){
            val addRoute = FeaturedRoute(
                    id = null,
                    fromCode = data!!.getStringExtra("fromCode"),
                    fromDetail = data.getStringExtra("fromDetail"),
                    toCode = data.getStringExtra("toCode"),
                    toDetail = data.getStringExtra("toDetail"),
                    priority = 0
            )

            if (addRoute.fromCode != "FROM" || addRoute.toCode != "TO"){
                viewModel.inPuts.addFeatureRoute(addRoute)
                viewModel.inPuts.callFeaturedRoute(Parameter.CLICK)
            }else {
                Toast.makeText(context, "Invalid Route", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class PreferredRouteAdapter(val mItemDragListener: ItemDragListener) : RecyclerView.Adapter<PreferredRouteAdapter.PreferredRouteViewHolder>(), itemActionListener {
        var routeList = mutableListOf<FeaturedRoute>()

        override fun onItemMoved(fromPosition: Int, toPosition: Int) {
            Collections.swap(routeList, fromPosition, toPosition)
            viewModel.inPuts.updateFeaturedRtoues(routeList)
            notifyItemMoved(fromPosition, toPosition)
        }

        override fun onSwiped(position: Int) {
            //Do Nothing Yet
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreferredRouteViewHolder {
            return PreferredRouteViewHolder(LayoutInflater.from(context).inflate(R.layout.item_preferred_route_edit, parent, false), mItemDragListener)
        }

        override fun getItemCount(): Int = routeList.size

        override fun onBindViewHolder(holder: PreferredRouteViewHolder, position: Int) {
            val data = routeList[position]
            holder.itemView.tv_featured_route_from_code.setText(data.fromCode)
            holder.itemView.tv_featured_route_from_detail.setText(data.fromDetail)
            holder.itemView.tv_featured_route_to_code.setText(data.toCode)
            holder.itemView.tv_featured_route_to_detail.setText(data.toDetail)
            holder.itemView.iv_btn_delete_preferred_route.setOnClickListener {
                val dialog = FeaturedRouteDeleteDialog(holder.itemView.context, object : DialogEventListener {
                    override fun onDeleteEvent() {
                        viewModel.inPuts.deleteFeaturedRoute(routeList[position].id!!.toLong())
                        routeList.removeAt(position)
                        notifyDataSetChanged()
                    }
                })
                dialog.setCancelable(false)
                dialog.show()
            }
        }

        inner class PreferredRouteViewHolder(view: View, mItemDragListener: ItemDragListener) : RecyclerView.ViewHolder(view) {
            init {
                view.iv_btn_drag.setOnTouchListener { v, event ->
                    if(event.action == MotionEvent.ACTION_DOWN){
                        mItemDragListener.onStartDrag(this)
                    }
                    false
                }
            }
        }
    }
    inner class ItemTouchHelperCallback(val mitemActionListener: itemActionListener) : ItemTouchHelper.Callback(){
        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
//            return makeMovementFlags(dragFlags, swipeFlags)
            return makeMovementFlags(dragFlags, 0)
        }

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            if (isCurrentlyActive && actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                viewHolder.itemView.card_preferred_route_edit.cardElevation = 10 * density
                scrollview_preferred_route_edit.smoothScrollBy(0, dY.toInt()/10)
            }
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            mitemActionListener.onItemMoved(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            mitemActionListener.onSwiped(viewHolder.adapterPosition)
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            viewHolder.itemView.card_preferred_route_edit.cardElevation = 3 * density
            super.clearView(recyclerView, viewHolder)
        }
    }
}

//Drag and Drop
interface ItemDragListener {
    fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
}

interface itemActionListener {
    fun onItemMoved(fromPosition: Int, toPosition: Int)
    fun onSwiped(position: Int)
}

