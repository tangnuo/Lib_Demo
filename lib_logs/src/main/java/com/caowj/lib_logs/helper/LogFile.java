package com.caowj.lib_logs.helper;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class LogFile {
    static  int LOG_FILE_MAX_SIZE = Constance.LOG_FILE_MAX_SIZE; //10M
    private String mFolderPath;
    private LogTypeEnum mFileType;
    private RandomAccessFile mRandomAccessFile;
    private String mFileName= null;
    FileOutputStream logOutputStream = null;
    File mFile = null;

    public LogFile(Context context, LogTypeEnum logType) {
        mFolderPath = Constance.GLOBAL_PATH
                + File.separator
                + Constance.LOG_FOLDER_NAME_MAP.get(logType);
        mFileType = logType;
    }

    public String getFilePath() throws IOException {
        return Constance.findAvailableFilePath(mFolderPath);
    }

    public String getFolderPath(){
        return mFolderPath;
    }


    public FileOutputStream getFileOutputStream(int willWriteLength) throws IOException {
        if (logOutputStream == null) {
            mFileName =getFilePath();
            logOutputStream = new FileOutputStream(mFileName);
            mFile = new File(mFileName);
        }

        if(!mFile.exists()){// 文件被删掉
            mFileName =getFilePath();
            logOutputStream = new FileOutputStream(mFileName);
            mFile = new File(mFileName);
        }

        if (logOutputStream.getChannel().size() + willWriteLength > LOG_FILE_MAX_SIZE) {
            logOutputStream.close();
            Constance.appendEndTime(mFileName);
            mFileName =getFilePath();
            logOutputStream = new FileOutputStream(mFileName);
            mFile = new File(mFileName);
        }
        return logOutputStream;
    }


    public RandomAccessFile getRandomAccessFile(int willWriteLength) throws IOException {
        if (mRandomAccessFile == null) {
            mFileName =getFilePath();
            mRandomAccessFile = new RandomAccessFile(mFileName, "rw");
            mFile = new File(mFileName);
        }

        if(!mFile.exists()){// 文件被删掉
            mFileName =getFilePath();
            mRandomAccessFile = new RandomAccessFile(mFileName, "rw");
            mFile = new File(mFileName);
        }

        if (mRandomAccessFile.length() + willWriteLength > LOG_FILE_MAX_SIZE) {
            mRandomAccessFile.close();
            Constance.appendEndTime(mFileName);
            mFileName =getFilePath();
            mRandomAccessFile = new RandomAccessFile(mFileName, "rw");
            mFile = new File(mFileName);
        }
        return mRandomAccessFile;
    }



    public void close() {
        if (mRandomAccessFile != null) {
            try {
                mRandomAccessFile.close();
            } catch (IOException e) {

            }
        }
    }

}
