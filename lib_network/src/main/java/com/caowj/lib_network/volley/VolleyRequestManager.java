package com.caowj.lib_network.volley;


import com.caowj.lib_network.interfaces.IRequestCallback;
import com.caowj.lib_network.interfaces.IRequestManager;

/**
 * 采用Volley请求网络
 * 具体封装使用请参考百度云：【Lib_kedacom_vnet】
 */
public class VolleyRequestManager implements IRequestManager {
    private static VolleyRequestManager VOLLEY_REQUEST_MANAGER;
    public static VolleyRequestManager getInstance() {
        if (VOLLEY_REQUEST_MANAGER == null) {
            VOLLEY_REQUEST_MANAGER = new VolleyRequestManager();
        }
        // TODO: 2020/10/23 具体封装使用请参考百度云：【Lib_kedacom_vnet】 
        return VOLLEY_REQUEST_MANAGER;
    }

    @Override
    public void get(String url, final IRequestCallback requestCallback) {
//        StringRequest request = new StringRequest(Request.Method.GET, url,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String s) {
//                        requestCallback.onSuccess(s);
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError volleyError) {
//                        requestCallback.onFailure(volleyError);
//                    }
//                });
//        HttpRequestExampleApp.mQueue.add(request);
    }

    @Override
    public void post(String url, String requestBodyJson, final IRequestCallback requestCallback) {
//        requestWithBody(url, requestBodyJson, requestCallback, Request.Method.POST);
    }

    @Override
    public void put(String url, String requestBodyJson, final IRequestCallback requestCallback) {
//        requestWithBody(url, requestBodyJson, requestCallback, Request.Method.PUT);
    }

    @Override
    public void delete(String url, String requestBodyJson, final IRequestCallback requestCallback) {
//        requestWithBody(url, requestBodyJson, requestCallback, Request.Method.DELETE);
    }

    /**
     * 封装带请求体的请求方法
     *
     * @param url             url
     * @param requestBodyJson Json string请求体
     * @param requestCallback 回调接口
     * @param method          请求方法
     */
    private void requestWithBody(String url, String requestBodyJson, final IRequestCallback requestCallback, int method) {
//        JSONObject jsonObject = null;
//        try {
//            jsonObject = new JSONObject(requestBodyJson);
//        } catch (JSONException e) {
//            e.printStackTrace();
//            //// TODO: 2016/12/11 异常处理
//        }
//        JsonRequest<JSONObject> request = new JsonObjectRequest(method, url, jsonObject,
//                new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        requestCallback.onSuccess(response != null ? response.toString() : null);
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        requestCallback.onFailure(error);
//                    }
//                });
//        HttpRequestExampleApp.mQueue.add(request);
    }

}
