###############################################################################
# WebView Chromium
LOCAL_PATH := $(call my-dir)

my_archs := arm x86 arm64 x86_64
my_src_arch := $(call get-prebuilt-src-arch, $(my_archs))
ifeq ($(my_src_arch),arm)
my_src_abi := armeabi-v7a
else ifeq ($(my_src_arch),x86)
my_src_abi := x86
else ifeq ($(my_src_arch),arm64)
my_src_abi := arm64-v8a
else ifeq ($(my_src_arch),x86_64)
my_src_abi := x86_64
endif

ifeq ($(PRODUCT_PREBUILT_WEBVIEWCHROMIUM),yes)
include $(CLEAR_VARS)
LOCAL_MODULE := WebViewGoogle
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_TAGS := optional
LOCAL_BUILT_MODULE_STEM := package.apk
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)
#LOCAL_PRIVILEGED_MODULE :=
LOCAL_CERTIFICATE := PRESIGNED
#LOCAL_OVERRIDES_PACKAGES :=
ifeq ($(my_src_arch),arm)
  LOCAL_SRC_FILES := $(LOCAL_MODULE).apk
  LOCAL_PREBUILT_JNI_LIBS := @lib/$(my_src_abi)/libwebviewchromium.so
else ifeq ($(my_src_arch),x86)
  LOCAL_SRC_FILES := $(LOCAL_MODULE)_$(my_src_arch).apk
  LOCAL_PREBUILT_JNI_LIBS := @lib/$(my_src_abi)/libwebviewchromium.so
else ifeq ($(my_src_arch),arm64)
  LOCAL_SRC_FILES := $(LOCAL_MODULE)_$(my_src_arch).apk
  LOCAL_PREBUILT_JNI_LIBS_arm := @lib/armeabi-v7a/libwebviewchromium.so
  LOCAL_PREBUILT_JNI_LIBS_arm64 := @lib/$(my_src_abi)/libwebviewchromium.so
  LOCAL_MULTILIB := both
else ifeq ($(my_src_arch),x86_64)
  LOCAL_SRC_FILES := $(LOCAL_MODULE)_$(my_src_arch).apk
  LOCAL_PREBUILT_JNI_LIBS_x86 := @lib/x86/libwebviewchromium.so
  LOCAL_PREBUILT_JNI_LIBS_x86_64 := @lib/$(my_src_abi)/libwebviewchromium.so
  LOCAL_MULTILIB := both
endif
LOCAL_REQUIRED_MODULES := \
    libwebviewchromium_loader \
    libwebviewchromium_plat_support
include $(BUILD_PREBUILT)
endif
