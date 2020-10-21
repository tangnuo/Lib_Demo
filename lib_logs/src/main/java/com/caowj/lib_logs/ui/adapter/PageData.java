package com.caowj.lib_logs.ui.adapter;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.SparseArray;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PageData {

    public static int PAGE_SIZE = 30;
    String mPath;
    OnPageDataListener mOnPageDataListener;
    // 存储每页开头的文件位置，为来回滚动翻页重新加载文件内容使用
    SparseArray<Long> mPagePosition = new SparseArray<>();
    // 存储缓存的页码和内容
    SparseArray<List<String>> cachePageDatas = new SparseArray<>();
    // 存储缓存的页码，为了找到最小的和最大的页码
    List<Integer> mCachedPageNums = new ArrayList<>();
    RandomAccessFile mRandomAccessFile = null;
    List<Task> mRunningTask = new ArrayList<>();
//    public RecyclerView recyclerView;
//    public LogContentActivity contentActivity;
    public PageData(String filePath,OnPageDataListener onPageDataListener){
        mPath = filePath;
        mOnPageDataListener = onPageDataListener;
    }
    public void setPageSize(int pageSize){
        PAGE_SIZE = pageSize;
    }
    int requstpageNum;
    public String getData(int position,int itemCount){
        int pageNum = position/PAGE_SIZE;
        int index = position%PAGE_SIZE;


        if( cachePageDatas.get(pageNum)==null){
            requstpageNum = pageNum;
            mOnPageDataListener.requestLoadNewPage(pageNum);
            return "";
        }else{
            if(index>=cachePageDatas.get(pageNum).size()){
                return "";
            }else{
                return cachePageDatas.get(pageNum).get(index);
            }

        }


    }

    String[] mKeywords;
    boolean firstRead = false;
    public void firstRead(String keyword){
        if(TextUtils.isEmpty(keyword)){
            mKeywords = null;
        }else{
            if(keyword.trim().length() == 0){
                mKeywords = null;
            }else{
                mKeywords = keyword.trim().split(" ");
            }
        }

        cachePageDatas.clear();
        mCachedPageNums.clear();
        mPagePosition.clear();
        firstRead = true;
        readPage(0,mKeywords);
    }

    private void readPage(int pageNum, String[] mKeywords){

        if(cachePageDatas.get(pageNum)==null){
            if(!mRunningTask.contains(new Task(mKeywords,pageNum))){
                readFileAsync(pageNum, mKeywords);
            }

        }else{
            if(pageNum==0&& firstRead){
                mOnPageDataListener.onReadFirstPage(cachePageDatas.get(pageNum));
                firstRead = false;
            }
        }

    }

    /**
     * 可见区域的item位置发生变化，要重新缓存
     * @param minP 最小的索引
     * @param maxP 最大的索引
     */
    public void onVisiblePositionChange(int minP,int maxP,boolean isUp){
        int minPageNum = minP/PAGE_SIZE;
        int maxPageNum = maxP/PAGE_SIZE;
        int minWillCachePageNum = minPageNum-2;
        int maxWillCachePageNum = maxPageNum+2;
        if(minWillCachePageNum<0){
            minWillCachePageNum = 0;
        }
//        Log.e("wangq","onVisiblePositionChange:minP=="+minP+",maxP="+maxP);
//        Log.e("wangq","onVisiblePositionChange:mCachedPageNums=="+ mCachedPageNums.size());
        // 已经缓存的页码排序
        Collections.sort(mCachedPageNums);
        if(mCachedPageNums.size()==0){
            return;
        }
        int minCachedPageNum = mCachedPageNums.get(0);
        int maxCachedPageNum = mCachedPageNums.get(mCachedPageNums.size()-1);
        // 已经缓存的最小页码小于将要缓存的最小页码
        if(minCachedPageNum<minWillCachePageNum){
            while (minCachedPageNum<minWillCachePageNum){
                //去掉之前的缓存的最小页码
                List<String>list = cachePageDatas.get(minCachedPageNum);
                list.clear();
                cachePageDatas.remove(minCachedPageNum);
                mCachedPageNums.remove(new Integer(minCachedPageNum));
                minCachedPageNum++;
            }

        }
        // 已经缓存的最大页码大于将要缓存的最大页码
        if(maxCachedPageNum>maxWillCachePageNum){
            while (maxCachedPageNum>maxWillCachePageNum){
                cachePageDatas.remove(maxCachedPageNum);
                mCachedPageNums.remove(new Integer(maxCachedPageNum));
                maxCachedPageNum --;
            }

        }
        if(isUp){
            for(int i = maxWillCachePageNum;i>=minWillCachePageNum;i--){
                readPage(i,mKeywords);
            }
        }else{
            for(int i = minWillCachePageNum;i<maxWillCachePageNum+1;i++){
                readPage(i,mKeywords);
            }
        }


    }

    private void onReachMaxLines(int maxLine){
        if(mOnPageDataListener!=null){
            mOnPageDataListener.onReadFileEnd(maxLine);
        }

    }



    private void readFileAsync(int pageNum, String[] keywords) {

        try {
            if(mRandomAccessFile == null){
                mRandomAccessFile = new RandomAccessFile(mPath, "r");
            }
        } catch (FileNotFoundException e) {
            release();
        }

        mRunningTask.add(new Task(keywords,pageNum));
        new ReadFileTask(pageNum,keywords).execute(mRandomAccessFile);
    }


    public interface OnPageDataListener{
        public void onReadFileEnd(int maxLines);
        public void onReadFirstPage(List<String> data);
        public void requestLoadNewPage(int pageNum);
        public void onFinishLoadNewPage(int pageNum);
    }

    public void release(){
        if (mRandomAccessFile != null) {
            try {
                mRandomAccessFile.close();
            } catch (IOException e) {

            }
        }
        mRandomAccessFile = null;
    }

    private void onReadFileAsync(int pageNum, List<String> result, String[] keywords){
        if(result.size()>0){// 读取结束
            cachePageDatas.put(pageNum, result);
            mCachedPageNums.add(pageNum);
        }
        if(pageNum==0&& firstRead){
            mOnPageDataListener.onReadFirstPage(result);
            firstRead = false;
        }
        mRunningTask.remove(new Task(keywords,pageNum));
        mOnPageDataListener.onFinishLoadNewPage(pageNum);
//        if(requstpageNum == pageNum){
//            contentActivity.hideLoadingDialog();
//        }
    }

    private class ReadFileTask extends AsyncTask<RandomAccessFile, Integer, List<String> > {
        int mPageNum;
        String[] mKeywords;
        public ReadFileTask(int pageNum, String[] keywords){
            mPageNum = pageNum;
            mKeywords = keywords;
        }




        // 方法2：doInBackground（）
        // 作用：接收输入参数、执行任务中的耗时操作、返回 线程任务执行的结果
        // 注：必须复写，从而自定义线程任务
        @Override
        protected List<String>  doInBackground(RandomAccessFile... params) {
            List<String> result = new ArrayList<String>();
//            Log.e("wangq","doInBackgroundmPageNum=="+mPageNum);
            long startPosition = mPagePosition.get(mPageNum)==null?0:mPagePosition.get(mPageNum);
            if(mPageNum == 0){
               if(mPagePosition.get(mPageNum)==null){
                   startPosition = 0;
               }else{
                   startPosition = mPagePosition.get(mPageNum);
               }
            }else{
                if(mPagePosition.get(mPageNum)==null){
                  return result;
                }else{
                    startPosition = mPagePosition.get(mPageNum);
                }
            }
            try {
                if(mRandomAccessFile == null){
                    mRandomAccessFile = new RandomAccessFile(mPath, "r");
                }

                if(startPosition == mRandomAccessFile.length()){
                    return result;
                }
                mRandomAccessFile.seek(startPosition);
                String line = null;

                long position = 0;
                int lineNum = 0;
                while ((line = mRandomAccessFile.readLine()) != null && lineNum < PAGE_SIZE) {
                    line = new String(line.getBytes("ISO-8859-1"), "utf-8");

                    boolean isContains = true;
                    if(mKeywords!=null){
                        for(String keyword:mKeywords){
                            if(keyword.trim().length()>0){
                                isContains = isContains&&line.contains(keyword);
                            }
                        }
                    }

                    if(isContains){
                        if (mPageNum == 0) {
                            mPagePosition.put(0, startPosition);
                        }
                        lineNum++;
                        position = mRandomAccessFile.getFilePointer();
                        result.add(line);
                    }
                }

                if(line==null){
                    int maxLines = (mPageNum)* PAGE_SIZE+lineNum;
                    onReachMaxLines(maxLines);

                }else{
                    mPagePosition.put(mPageNum + 1, position);
                }
            } catch (FileNotFoundException e) {

                release();
            } catch (IOException e) {

                release();
            } finally {

            }
            return result;

        }


        // 方法4：onPostExecute（）
        // 作用：接收线程任务执行结果、将执行结果显示到UI组件
        // 注：必须复写，从而自定义UI操作
        @Override
        protected void onPostExecute(List<String>  fileData) {

            onReadFileAsync(mPageNum,fileData, mKeywords);
        }

        // 方法5：onCancelled()
        // 作用：将异步任务设置为：取消状态
        @Override
        protected void onCancelled() {

        }
    }

    class Task{
        public Task( String[] keywords,int pageNum){
            this.keywords = keywords;
            this.pageNum = pageNum;
        }
        String[] keywords;
        int pageNum;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Task task = (Task) o;
            return pageNum == task.pageNum &&
                    Objects.equals(keywords, task.keywords);
        }

        @Override
        public int hashCode() {
            return Objects.hash(keywords, pageNum);
        }
    }
}
