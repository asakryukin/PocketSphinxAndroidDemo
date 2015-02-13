LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_CAMERA_MODULES:=on
OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=STATIC
include /Users/andrey/Downloads/OpenCV-2.4.9-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_SRC_FILES := PocketSphinxAndroidDemo.cpp
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_LDLIBS += -llog -ldl

LOCAL_MODULE    := PocketSphinxAndroidDemo

LOCAL_SHARED_LIBRARIES += PocketSphinxAndroidDemo

include $(BUILD_SHARED_LIBRARY)
