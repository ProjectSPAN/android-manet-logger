LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := libnativetask

LOCAL_SRC_FILES := android_adhoc_manet_nativ_NativeTask.c

include $(BUILD_SHARED_LIBRARY)
