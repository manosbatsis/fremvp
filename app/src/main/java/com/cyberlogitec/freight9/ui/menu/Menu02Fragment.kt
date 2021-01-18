package com.cyberlogitec.freight9.ui.menu


import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.lib.model.Message
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.util.getMessageDeliveryDateTime
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.ui.menu.swipe.SwipeController
import com.cyberlogitec.freight9.ui.menu.swipe.SwipeControllerActions
import com.trello.rxlifecycle3.components.support.RxFragment
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.item_menu_02.*
import kotlinx.android.synthetic.main.item_msg.view.*
import timber.log.Timber


class Menu02Fragment constructor(val viewModel:MenuViewModel) : RxFragment() {

    /**
     * adapter about received push messages
     */
    private val adapter by lazy {
        RecyclerAdapter()
                .apply {
                    //onClickItem = { viewModel.inPuts.clickTopic(it) }
                    //onLikeTopic = { viewModel.inPuts.likeTopic(it) }
                }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View = inflater.inflate(R.layout.item_menu_02, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timber.v("f9: onViewCreated")

        viewModel.outPuts.onSuccessGetMessages()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: get all messages : ${it.size}")
                    if (it.isNotEmpty()) {
                        adapter.datas.clear()
                        adapter.setItems(it.sortedByDescending { message -> message.timestamp })
                        adapter.notifyDataSetChanged()
                    }
                }

        viewModel.outPuts.onSuccessDeleteAllMessages()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: click clear all")
                    adapter.datas.clear()
                    adapter.notifyDataSetChanged()
                }

        viewModel.outPuts.onSuccessDeleteMessage()
                .bindToLifecycle(this)
                .subscribe { it ->
                    Timber.d("f9: delete message rows : $it")
                    if (it < 1) { loadMessages() }
                }

        viewModel.outPuts.onSuccessInsertMessages()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: inserted messages")
                    viewModel.inPuts.getMessages(Parameter.CLICK)
                }

        // "Clear All" on click event
        tv_clear_all.setSafeOnClickListener{
            Timber.d("f9: tv_clear_all")
            viewModel.inPuts.deleteAllMessages(Parameter.CLICK)
        }

        recyclerViewInit()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Timber.v("f9: onActivityCreated")
    }

    /**
     * onResume 시 db 에 저장된 push message load
     */
    override fun onResume() {
        super.onResume()
        loadMessages()
    }

    /**
     * db 에 저장된 push message load
     */
    private fun loadMessages() {
        viewModel.inPuts.getMessages(Parameter.CLICK)
    }

    /**
     * list item 의 left swipe 시 "Delete" 메뉴 노출
     * "Delete" 선택 시 해당 message 삭제
     */
    lateinit var swipeController: SwipeController
    private fun recyclerViewInit() {
        recycler_view_menu02.apply {
            layoutManager = LinearLayoutManager(this@Menu02Fragment.context)
            adapter = this@Menu02Fragment.adapter
        }

        swipeController = SwipeController(context!!, object: SwipeControllerActions() {
            override fun onRightClicked(position: Int) {
                viewModel.inPuts.deleteMessage(adapter.datas[position].msgSeq!!)
                adapter.removeItem(position)
                adapter.notifyItemRemoved(position)
                adapter.notifyItemRangeChanged(position, adapter.itemCount)
            }
        })

        val itemTouchHelper = ItemTouchHelper(swipeController)
        itemTouchHelper.attachToRecyclerView(recycler_view_menu02)

        recycler_view_menu02.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                swipeController.onDraw(c)
            }
        })
    }

    /**
     * recycler view adapter about received push messages
     * TODO : "readYn" 처리, Item click 시 해당 menu(?) 로 이동
     */
    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
        val datas = mutableListOf<Message>()
        var onClickItem: (Long) -> Unit = { }

        override fun getItemCount(): Int = datas.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.ViewHolder =
                ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_msg, parent, false))

        override fun onBindViewHolder(holder: RecyclerAdapter.ViewHolder, position: Int) {
            with(holder.itemView) {
                val data = datas[position]
                tv_msg.text = data.messageTitle
                tv_msg_desc.text = data.errorMessage
                tv_time.text = data.timestamp!!.getMessageDeliveryDateTime()
                setSafeOnClickListener { onClickItem( datas.indexOf( data ).toLong() ) }
            }
        }

        fun setItems(messages: List<Message>) {
            datas.addAll(messages)
        }

        fun removeItem(position: Int) {
            datas.removeAt(position)
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

    companion object {
        @JvmStatic
        fun newInstance(viewModel:MenuViewModel) : Menu02Fragment {
            val fragment = Menu02Fragment(viewModel)
            return fragment
        }
    }
}