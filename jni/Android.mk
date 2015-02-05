LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := PocketSphinxAndroidDemo
LOCAL_SRC_FILES := PocketSphinxAndroidDemo.cpp

include $(BUILD_SHARED_LIBRARY)
