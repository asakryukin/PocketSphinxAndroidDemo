LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)


OPENCV_CAMERA_MODULES:=on
OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=STATIC
include ../../../Downloads/OpenCV-2.4.9-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := PocketSphinxAndroidDemo
LOCAL_SRC_FILES := PocketSphinxAndroidDemo.cpp

include $(BUILD_SHARED_LIBRARY)
