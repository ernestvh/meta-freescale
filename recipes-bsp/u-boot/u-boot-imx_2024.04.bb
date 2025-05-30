# Copyright (C) 2013-2016 Freescale Semiconductor
# Copyright 2018 (C) O.S. Systems Software LTDA.
# Copyright 2017-2024 NXP

require recipes-bsp/u-boot/u-boot.inc
require u-boot-imx-common_${PV}.inc

PROVIDES += "u-boot u-boot-mfgtool"

inherit uuu_bootloader_tag

# The UUU tag goes on the boot partition. For 8+, the boot partition image
# is imx-boot, so disable UUU-tagging here
UUU_BOOTLOADER:mx8-generic-bsp = ""
UUU_BOOTLOADER:mx9-generic-bsp = ""

TOOLCHAIN_OPTIONS:append = " -Wno-error=implicit-function-declaration"

do_deploy:append:mx8m-generic-bsp() {
    # Deploy u-boot-nodtb.bin and fsl-imx8m*-XX.dtb for mkimage to generate boot binary
    if [ -n "${UBOOT_CONFIG}" ]
    then
        for config in ${UBOOT_MACHINE}; do
            i=$(expr $i + 1);
            for type in ${UBOOT_CONFIG}; do
                j=$(expr $j + 1);
                if [ $j -eq $i ]
                then
                    install -d ${DEPLOYDIR}/${BOOT_TOOLS}
                    install -m 0644 ${B}/${config}/u-boot-nodtb.bin ${DEPLOYDIR}/${BOOT_TOOLS}/u-boot-nodtb.bin-${MACHINE}-${type}
                    UBOOT_DTB_NAME_FLAGS="${type}:${UBOOT_DTB_NAME}"
                    for key_value in ${UBOOT_DTB_NAME_FLAGS}; do
                        local type_key="${key_value%%:*}"
                        local dtb_name="${key_value#*:}"
                        if [ "$type_key" = "$type" ]
                        then
                            bbnote "UBOOT_CONFIG = $type, UBOOT_DTB_NAME = $dtb_name"
                            # There is only one ${dtb_name}, the first one. All the other are with the type appended
                            if [ ! -f "${DEPLOYDIR}/${BOOT_TOOLS}/${dtb_name}" ]; then
                                install -m 0644 ${B}/${config}/arch/arm/dts/${dtb_name}  ${DEPLOYDIR}/${BOOT_TOOLS}/${dtb_name}
                            else
                                bbwarn "Use custom wks.in for $dtb_name = $type"
                            fi
                            install -m 0644 ${B}/${config}/arch/arm/dts/${dtb_name}  ${DEPLOYDIR}/${BOOT_TOOLS}/${dtb_name}-${type}
                        fi
                        unset type_key
                        unset dtb_name
                    done

                    unset UBOOT_DTB_NAME_FLAGS
                fi
            done
            unset  j
        done
        unset  i
    fi

    # Deploy CRT.* from u-boot for stmm
    install -m 0644 ${S}/CRT.*     ${DEPLOYDIR}
}

do_deploy:append:mx93-generic-bsp() {
    # Deploy CRT.* from u-boot for stmm
    install -m 0644 ${S}/CRT.*     ${DEPLOYDIR}
}

PACKAGE_ARCH = "${MACHINE_ARCH}"
COMPATIBLE_MACHINE = "(mx6-generic-bsp|mx7-generic-bsp|mx8-generic-bsp|mx9-generic-bsp)"
