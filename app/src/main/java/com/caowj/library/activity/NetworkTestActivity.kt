package com.caowj.library.activity

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.caowj.lib_network.retrofit_sample.HttpServicesFactory
import com.caowj.lib_network.retrofit_sample.HttpServicesFactory2
import com.caowj.lib_network.RequestFactory
import com.caowj.lib_network.interfaces.IRequestCallback
import com.caowj.library.R
import com.caowj.library.databinding.ActivityNetworkTestBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * 免费开放接口API：https://blog.csdn.net/c__chao/article/details/78573737
 */
class NetworkTestActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var testBinding: ActivityNetworkTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        testBinding = DataBindingUtil.setContentView(this, R.layout.activity_network_test)

        testRetrofit2()
    }


    private fun testRetrofit1() {
        // https://api.apiopen.top/getJoke?page=1&count=2&type=video
        HttpServicesFactory.getHttpServiceApi().getJoke(1, 2, "video").enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                testBinding.response = response.body().toString()
                Log.d("caowj", "response:$response")
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                testBinding.response = t.message
                Log.w("caowj", "response:" + t.message)
            }
        })
    }

    private fun testRetrofit2() {
        // https://api.apiopen.top/getJoke?page=1&count=2&type=video
        HttpServicesFactory2.getInstance().httpServiceApi.getJoke(1, 2, "video").enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                testBinding.response = response.body().toString()
                Log.d("caowj", "response:$response")
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                testBinding.response = t.message
                Log.w("caowj", "response:" + t.message)
            }
        })
    }

    private fun testNet() {
        //测试请求
        val url = "https://api.apiopen.top/getJoke?page=1&count=2&type=video"
        //这里发起请求依赖的是IRequestManager接口
        val requestManager = RequestFactory.getRequestManager()
        requestManager[url, object : IRequestCallback {
            override fun onSuccess(response: String) {
                testBinding.response = response
                Log.d("caowj", "response:$response")
            }

            override fun onFailure(throwable: Throwable) {
                testBinding.response = throwable.message
                Log.w("caowj", "response:" + throwable.message)
                throwable.printStackTrace()
            }
        }]
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.test1 -> testRetrofit1()
            R.id.test2 -> testNet()
        }
    }
}