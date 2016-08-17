###############################################################################
# Velvet
LOCAL_PATH := $(call my-dir)

my_archs := arm arm64 x86
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
LOCAL_MODULE := Velvet
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_TAGS := optional
LOCAL_BUILT_MODULE_STEM := package.apk
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)
LOCAL_PRIVILEGED_MODULE := true
LOCAL_CERTIFICATE := PRESIGNED
LOCAL_OVERRIDES_PACKAGES := QuickSearchBox
ifeq ($(my_src_arch),arm)
  LOCAL_SRC_FILES := $(LOCAL_MODULE).apk
else
  LOCAL_SRC_FILES := $(LOCAL_MODULE)_$(my_src_arch).apk
endif
LOCAL_REQUIRED_MODULES := \
    en-US/c_fst \
    en-US/clg \
    en-US/commands.abnf \
    en-US/compile_grammar.config \
    en-US/contacts.abnf \
    en-US/dict \
    en-US/dictation.config \
    en-US/dnn \
    en-US/endpointer_dictation.config \
    en-US/endpointer_voicesearch.config \
    en-US/ep_acoustic_model \
    en-US/g2p_fst \
    en-US/grammar.config \
    en-US/hclg_shotword \
    en-US/hmmlist \
    en-US/hmm_symbols \
    en-US/hotword_classifier \
    en-US/hotword.config \
    en-US/hotword_normalizer \
    en-US/hotword_prompt.txt \
    en-US/hotword_word_symbols \
    en-US/metadata \
    en-US/normalizer \
    en-US/norm_fst \
    en-US/offensive_word_normalizer \
    en-US/phonelist \
    en-US/phone_state_map \
    en-US/rescoring_lm \
    en-US/wordlist
LOCAL_PREBUILT_JNI_LIBS := \
    @lib/$(my_src_abi)/libcronet.so \
    @lib/$(my_src_abi)/libgoogle_speech_jni.so \
    @lib/$(my_src_abi)/libgoogle_speech_micro_jni.so
LOCAL_MODULE_TARGET_ARCH := $(my_src_arch)
include $(BUILD_PREBUILT)

include $(LOCAL_PATH)/OfflineVoiceRecognitionLanguagePacks/Android.mk
