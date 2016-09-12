# CopyRight goodix
# By Warren Zhao
ifeq ($(strip $(GOODIX_FINGER_UNLOCK_SUPPORT)),yes)
LOCAL_PATH := $(call my-dir)
$(shell mkdir -p  $(PRODUCT_OUT)/system/bin)
$(shell mkdir -p  $(PRODUCT_OUT)/system/lib64)

$(shell cp -a $(LOCAL_PATH)/bin $(PRODUCT_OUT)/system/)
$(shell cp -a $(LOCAL_PATH)/lib64 $(PRODUCT_OUT)/system/)
include $(call all-subdir-makefiles)
endif

