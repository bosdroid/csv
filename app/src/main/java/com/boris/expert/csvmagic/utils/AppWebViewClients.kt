package com.boris.expert.csvmagic.utils

import android.content.Context
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient


class AppWebViewClients(var context: Context,query:String) : WebViewClient() {
    var loadingFinished = true
    var redirect = false

    var mQuery = query

    override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
        // TODO Auto-generated method stub
//        if (BaseActivity.isNetworkAvailable(context)) {
//            if (!loadingFinished) {
//                redirect = true
//            }
//            loadingFinished = false
//            view.loadUrl(url!!)
//        } else {
//            context.startActivity(Intent(context, ErrorActivity::class.java))
//        }
        return false
    }

//    override fun onPageStarted(view: WebView?, url: String?, facIcon: Bitmap?) {
//        loadingFinished = false
//    }

    override fun onPageFinished(view: WebView?, url: String?) {

        Log.i("web link", String.format(url!!))
        if (url.compareTo(mQuery) != 0) {
//            images.add(url)
        }

//        if(!redirect){
//            loadingFinished = true
//        }
//
//        if(loadingFinished && !redirect){
//            view!!.webChromeClient = object: WebChromeClient(){
//                override fun onProgressChanged(view: WebView?, newProgress: Int) {
//                    if (newProgress == 100)
//                    {
//                        BaseActivity.dismiss()
//                    }
//                }
//            }
//        } else{
//            redirect = false
//        }

    }

//    override fun onReceivedError(
//        view: WebView?,
//        errorCode: Int,
//        description: String?,
//        failingUrl: String?
//    ) {
//        BaseActivity.dismiss()
//        context.startActivity(Intent(context, ErrorActivity::class.java))
//        super.onReceivedError(view, errorCode, description, failingUrl)
//    }

}