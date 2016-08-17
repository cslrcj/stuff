###############################################################################
# GoogleCamera
LOCAL_PATH := $(call my-dir)

my_archs := arm
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

include $(CLEAR_VARS)
LOCAL_MODULE := GoogleCamera
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_TAGS := optional
LOCAL_BUILT_MODULE_STEM := package.apk
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)
#LOCAL_PRIVILEGED_MODULE :=
LOCAL_CERTIFICATE := PRESIGNED
#LOCAL_OVERRIDES_PACKAGES := Camera2
LOCAL_SRC_FILES := $(LOCAL_MODULE).apk
LOCAL_REQUIRED_MODULES :=
LOCAL_PREBUILT_JNI_LIBS := \
    @lib/$(my_src_abi)/libRSSupport.so \
    @lib/$(my_src_abi)/libgcam.so \
    @lib/$(my_src_abi)/libgcam_swig_jni.so \
    @lib/$(my_src_abi)/libjni_jpegutil.so \
    @lib/$(my_src_abi)/libjni_tinyplanet.so \
    @lib/$(my_src_abi)/libjpeg.so \
    @lib/$(my_src_abi)/liblightcycle.so \
    @lib/$(my_src_abi)/libnativehelper_compat_libc++.so \
    @lib/$(my_src_abi)/librefocus.so \
    @lib/$(my_src_abi)/librs.layered_filter_f32.so \
    @lib/$(my_src_abi)/librs.layered_filter_fast_f32.so \
    @lib/$(my_src_abi)/librsjni.so
LOCAL_MODULE_TARGET_ARCH := $(my_src_arch)
include $(BUILD_PREBUILT)
