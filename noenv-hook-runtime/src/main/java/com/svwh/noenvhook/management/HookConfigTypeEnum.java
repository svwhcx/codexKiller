package com.svwh.noenvhook.management;

public interface HookConfigTypeEnum {

    int CUSTOMER = 0;

    int CLICK = 5;

    int DIALOG = 6;

    int TEXT_SET = 8;

    int TOAST_SHOW = 9;

    int ACTIVITY_LOG = 10;

    int SCREEN = 16;

    int SIGNATURE = 17;

    int VPN = 26;

    int FILE_WRITE = 27;

    int FILE_READ = 28;

    int FILE_DELETE = 29;

    int ASSETS = 31;

    int DIGEST = 34;

    int CIPHER = 35;

    static boolean requiresTarget(int type) {
        switch (type) {
            case ASSETS:
            case FILE_READ:
            case FILE_WRITE:
            case FILE_DELETE:
            case SIGNATURE:
            case SCREEN:
            case DIGEST:
            case CIPHER:
                return false;
            default:
                return true;
        }
    }
}
