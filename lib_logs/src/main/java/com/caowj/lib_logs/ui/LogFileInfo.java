package com.caowj.lib_logs.ui;

import java.util.Objects;

public class LogFileInfo {
   public String fileName;
   public String displaySize;
   public boolean isSelected;

    public LogFileInfo( String fileName){
        this.fileName = fileName;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogFileInfo fileInfo = (LogFileInfo) o;
        return Objects.equals(fileName, fileInfo.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName);
    }
}
