# Begonia upstream source audit

**Audit date:** 2026-11-21  
**Scope:** public repository metadata and source-tree structure only. No source was copied into GiggleOS, no vendor binary was redistributed, and no device build or hardware test was performed.

## Candidate repositories

| Component | Candidate | Branch inspected | Branch commit | Repository update observed |
|---|---|---|---|---|
| Device tree | [android_device_redmi_begonia](https://github.com/xiaomi-begonia-dev/android_device_redmi_begonia) | `fourteen` | `a2633ca858f5327563a7ec56640bb5fc9a657f9f` | 2024-09-12 |
| Kernel | [android_kernel_xiaomi_mt6785](https://github.com/xiaomi-begonia-dev/android_kernel_xiaomi_mt6785) | `fifteen` | `80d45b2c963e6a9e558988b130d938ea612b40ec` | 2025-01-11 |
| Vendor blobs | [android_vendor_redmi_begonia](https://github.com/xiaomi-begonia-dev/android_vendor_redmi_begonia) | `fourteen` | `79f5e97aba06997cad36f3a265f12097d1864f23` | 2024-11-15 |
| Firmware blobs | [android_vendor_redmi_begonia-firmware](https://github.com/xiaomi-begonia-dev/android_vendor_redmi_begonia-firmware) | `fourteen` | `c48f366093aa9e54c5ee273cefb62728c798b193` | 2024-06-26 |

These are candidate inputs for further review, not approved GiggleOS dependencies.

## Findings

### Device tree

- The `fourteen` branch identifies the product as Redmi Note 8 Pro / `begonia`, targets MediaTek `mt6785`, and contains device-specific board, VINTF, init, partition and SELinux configuration.
- Its product makefile inherits Halcyon common configuration and MediaTek-specific components. It is not a self-contained vanilla-AOSP device port.
- `halcyon.dependencies` references kernel, vendor, firmware and IMS repositories.
- **Path mismatch requiring resolution:** `BoardConfig.mk` points `TARGET_KERNEL_SOURCE` to `kernel/xiaomi/mt6785`, while `halcyon.dependencies` declares the kernel checkout path as `kernel/xiaomi/6785`. Do not start a build until the intended path is confirmed against the exact manifest/revision.
- The device-tree repository exposes branches `aosp-13`, `fourteen`, `halcyon` and `wip`; there is no branch named `fifteen` in the branch list inspected.

### Kernel

- The `fifteen` branch contains `arch/arm64/configs/begonia_user_defconfig` and MediaTek begonia device-tree sources.
- The repository also exposes `fourteen` and `aosp-13` branches. The device tree and kernel branch pairing is therefore not established by matching branch names alone.
- The kernel repository includes the upstream `COPYING` file. Kernel licensing obligations must be preserved independently from the device-tree license.

### Vendor and firmware

- The device tree's `proprietary-files.txt` identifies its listed blobs as originating from a Redmi Note 8 Pro Android 11 build (`V12.5.8.0.RGGMIXM`). This is an age/compatibility warning for a newer Android base, not proof that every blob is compatible.
- The vendor and firmware repositories contain proprietary payloads. Their redistribution rights and exact extraction/source provenance have not been established in this audit.
- Do not mirror, bundle or publish these blobs in GiggleOS until their licensing and distribution terms are reviewed.

## Compatibility and release blockers

1. The device tree branch is Android 14-oriented, while the selected kernel candidate branch is named `fifteen`; compatibility must be demonstrated from manifests and build history, not inferred from names.
2. Resolve the kernel checkout path mismatch.
3. Pin the exact manifest and every dependency commit. The current dependency file uses remote aliases that must be resolved to their actual repository URLs and revisions.
4. Audit the device tree's Halcyon-specific product dependencies, MediaTek hardware repositories, sepolicy and framework modifications.
5. Establish licensing and redistribution status for vendor and firmware payloads.
6. Compare the device configuration with the exact physical handset variant and firmware currently installed.
7. Only then choose the AOSP base and create a reproducible Linux build manifest.

## Decision

**No upstream source set is approved for a GiggleOS build yet.** The repositories are useful research candidates, but their branches, kernel path, Halcyon dependencies, vendor provenance and licensing remain unresolved. The AOSP base remains unselected.

No flashing package, recovery image or device-specific boot image should be produced from this candidate set until all blockers are resolved and a separate recovery/test plan exists.
