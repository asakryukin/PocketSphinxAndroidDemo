LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := PocketSphinxAndroidDemo
LOCAL_SRC_FILES := PocketSphinxAndroidDemo.cpp

include $(BUILD_SHARED_LIBRARY)
OPENCV_CAMERA_MODULES  := on
OPENCV_INSTALL_MODULES := on
OPENCV_LIB_TYPE        := STATIC
# Generic OpenCV.mk
#include $(NVPACK_PATH)/OpenCV-2.4.8.2-Tegra-sdk/sdk/native/jni/OpenCV.mk
# Tegra optimized OpenCV.mk
include /Users/andrey/NVPACK/OpenCV-2.4.8.2-Tegra-sdk/sdk/native/jni/OpenCV.mk