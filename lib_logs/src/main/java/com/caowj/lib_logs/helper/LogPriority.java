package com.caowj.lib_logs.helper;

/**
 * Log 输出级别
 *
 * @author : 袁兵兵
 * @version : 0.5.7
 * @since : 2019-10-10
 */
public enum LogPriority {
    Verbose {
        @Override
        int priority() {
            return 0;
        }

        @Override
        public String toString() {
            return "V";
        }
    },
    Debug {
        @Override
        int priority() {
            return 1;
        }

        @Override
        public String toString() {
            return "D";
        }
    },
    Info {
        @Override
        int priority() {
            return 2;
        }

        @Override
        public String toString() {
            return "I";
        }
    },
    Warn {
        @Override
        int priority() {
            return 3;
        }

        @Override
        public String toString() {
            return "W";
        }
    },
    Error {
        @Override
        int priority() {
            return 4;
        }

        @Override
        public String toString() {
            return "E";
        }
    },
    Fatal {
        @Override
        int priority() {
            return 5;
        }

        @Override
        public String toString() {
            return "F";
        }
    },
    Silent {
        @Override
        int priority() {
            return 6;
        }

        @Override
        public String toString() {
            return "S";
        }
    }; //禁止输出

    abstract int priority();
}
