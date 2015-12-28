LOCAL_PATH := $(call my-dir)

CM_DTB_FILES = $(wildcard $(TOP)/$(TARGET_KERNEL_SOURCE)/arch/arm/boot/*.dtb)
CM_DTS_FILE = $(lastword $(subst /, ,$(1)))
DTB_FILE := $(addprefix $(KERNEL_OUT)/arch/arm/boot/,$(patsubst %.dts,%.dtb,$(call CM_DTS_FILE,$(1))))
ZIMG_FILE = $(addprefix $(KERNEL_OUT)/arch/arm/boot/,$(patsubst %.dts,%-zImage,$(call CM_DTS_FILE,$(1))))
KERNEL_ZIMG = $(KERNEL_OUT)/arch/arm/boot/zImage

define append-cm-dtb
mkdir -p $(KERNEL_OUT)/arch/arm/boot;\
$(foreach d, $(CM_DTB_FILES), \
    cat $(KERNEL_ZIMG) $(call DTB_FILE,$(d)) > $(call ZIMG_FILE,$(d));)
endef


## Build and run dtbtool
DTBTOOL := $(HOST_OUT_EXECUTABLES)/dtbToolCM$(HOST_EXECUTABLE_SUFFIX)
INSTALLED_DTIMAGE_TARGET := $(PRODUCT_OUT)/dt.img
RAW_DTIMAGE_TARGET := $(PRODUCT_OUT)/dt.img.raw

$(RAW_DTIMAGE_TARGET): $(DTBTOOL) $(TARGET_OUT_INTERMEDIATES)/KERNEL_OBJ/usr $(INSTALLED_KERNEL_TARGET)
	@echo -e ${CL_CYN}"Start DT image: $@"${CL_RST}
	$(call append-cm-dtb)
	$(call pretty,"Target raw dt image: $(RAW_DTIMAGE_TARGET)")
	$(hide) $(DTBTOOL) -2 -o $(RAW_DTIMAGE_TARGET) -s $(BOARD_KERNEL_PAGESIZE) -p $(KERNEL_OUT)/scripts/dtc/ $(KERNEL_OUT)/arch/arm/boot/
	@echo -e ${CL_CYN}"Made DT image: $@"${CL_RST}

$(INSTALLED_DTIMAGE_TARGET): $(RAW_DTIMAGE_TARGET)
	$(call pretty,"Target dt image: $(INSTALLED_DTIMAGE_TARGET)")
	lz4 -9 < $(RAW_DTIMAGE_TARGET) > $@ || \
		lz4c -c1 -y $(RAW_DTIMAGE_TARGET) $@

## Overload bootimg generation: Same as the original, + --dt arg
LZMA_BOOT_RAMDISK := $(PRODUCT_OUT)/ramdisk-lzma.img

$(LZMA_BOOT_RAMDISK): $(BUILT_RAMDISK_TARGET)
	gunzip -f < $(BUILT_RAMDISK_TARGET) | xz --format=lzma -e > $@

$(INSTALLED_BOOTIMAGE_TARGET): $(MKBOOTIMG) $(INTERNAL_BOOTIMAGE_FILES) $(INSTALLED_DTIMAGE_TARGET) $(LZMA_BOOT_RAMDISK)
	$(call pretty,"Target boot image: $@")
	$(hide) $(MKBOOTIMG) $(INTERNAL_BOOTIMAGE_ARGS) $(BOARD_MKBOOTIMG_ARGS) --dt $(INSTALLED_DTIMAGE_TARGET) --output $@ --ramdisk $(LZMA_BOOT_RAMDISK)
	$(hide) $(call assert-max-image-size,$@,$(BOARD_BOOTIMAGE_PARTITION_SIZE),raw)
	@echo -e ${CL_CYN}"Made boot image: $@"${CL_RST}

LZMA_RECOVERY_RAMDISK := $(PRODUCT_OUT)/ramdisk-recovery-lzma.img

$(LZMA_RECOVERY_RAMDISK): $(recovery_ramdisk)
	gunzip -f < $(recovery_ramdisk) | lzma -e > $@

## Overload recoveryimg generation: Same as the original, + --dt arg
$(INSTALLED_RECOVERYIMAGE_TARGET): $(MKBOOTIMG) $(INSTALLED_DTIMAGE_TARGET) \
		$(LZMA_RECOVERY_RAMDISK) \
		$(recovery_kernel)
	$(call build-recoveryimage-target, $@)
	$(hide) $(MKBOOTIMG) $(INTERNAL_RECOVERYIMAGE_ARGS) $(BOARD_MKBOOTIMG_ARGS) --dt $(INSTALLED_DTIMAGE_TARGET) --output $@ --ramdisk $(LZMA_RECOVERY_RAMDISK)
	@echo -e ${CL_CYN}"Made recovery image: $@"${CL_RST}
