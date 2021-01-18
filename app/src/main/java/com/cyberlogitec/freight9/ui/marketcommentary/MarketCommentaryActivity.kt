package com.cyberlogitec.freight9.ui.marketcommentary

import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebViewClient
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import kotlinx.android.synthetic.main.act_market_commentary.*

@RequiresActivityViewModel(value = MarketCommentaryViewModel::class)
class MarketCommentaryActivity : BaseActivity<MarketCommentaryViewModel>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_market_commentary)

        webview.webViewClient = WebViewClient()
        var webSettings = webview.settings
        webSettings.javaScriptEnabled = true
        webSettings.setSupportMultipleWindows(false)
        webSettings.javaScriptCanOpenWindowsAutomatically = false
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true
        webSettings.setSupportZoom(false)
        webSettings.builtInZoomControls = false
        webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        webSettings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        webSettings.domStorageEnabled = true

        webview.loadUrl("http://www.freight9.com")
    }
}