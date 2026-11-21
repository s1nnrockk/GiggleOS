#!/usr/bin/env python3
"""Validate GiggleOS device profile metadata. This does not build firmware."""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DEVICE_ROOT = ROOT / "devices"
ALLOWED_SUPPORT = {"research", "bringup", "testing", "supported", "unsupported"}
ALLOWED_BUILD = {"not_built", "building", "built", "failed"}
ALLOWED_TEST = {"not_tested", "partial", "passed", "failed"}


def fail(path: Path, message: str) -> None:
    raise ValueError(f"{path.relative_to(ROOT)}: {message}")


def validate(path: Path) -> list[str]:
    data = json.loads(path.read_text(encoding="utf-8"))
    if data.get("schema_version") != 1:
        fail(path, "schema_version must be 1")

    device = data.get("device")
    project = data.get("giggleos")
    compatibility = data.get("compatibility")
    if not isinstance(device, dict) or not isinstance(project, dict):
        fail(path, "device and giggleos objects are required")
    if not isinstance(compatibility, dict):
        fail(path, "compatibility object is required")

    required = ("manufacturer", "marketing_name", "codename", "soc", "platform", "gpu", "architecture")
    for key in required:
        if not isinstance(device.get(key), str) or not device[key].strip():
            fail(path, f"device.{key} must be a non-empty string")

    codename = device["codename"]
    if path.parent.name != codename:
        fail(path, f"directory name must match device.codename ({codename})")
    if device["architecture"] not in {"arm64", "arm"}:
        fail(path, "device.architecture must be arm64 or arm")

    ram = device.get("ram_gb")
    if ram is not None and (not isinstance(ram, int) or ram <= 0):
        fail(path, "device.ram_gb must be a positive integer or null")
    model_numbers = device.get("model_numbers")
    if not isinstance(model_numbers, list) or any(not isinstance(item, str) for item in model_numbers):
        fail(path, "device.model_numbers must be a list of strings")

    if project.get("support_status") not in ALLOWED_SUPPORT:
        fail(path, f"giggleos.support_status must be one of {sorted(ALLOWED_SUPPORT)}")
    if project.get("build_status") not in ALLOWED_BUILD:
        fail(path, f"giggleos.build_status must be one of {sorted(ALLOWED_BUILD)}")
    if project.get("hardware_test_status") not in ALLOWED_TEST:
        fail(path, f"giggleos.hardware_test_status must be one of {sorted(ALLOWED_TEST)}")
    if not isinstance(project.get("first_target"), bool):
        fail(path, "giggleos.first_target must be boolean")
    if not isinstance(compatibility.get("android_apk_support_goal"), bool):
        fail(path, "compatibility.android_apk_support_goal must be boolean")
    if not isinstance(compatibility.get("tested"), bool):
        fail(path, "compatibility.tested must be boolean")

    if project["support_status"] == "supported" and (
        project["build_status"] != "built" or project["hardware_test_status"] != "passed"
    ):
        fail(path, "supported devices must have a built image and passed hardware tests")

    return [codename, device["marketing_name"], project["support_status"]]


def validate_upstream_candidates(path: Path, known_devices: set[str]) -> str:
    data = json.loads(path.read_text(encoding="utf-8"))
    if data.get("schema_version") != 1:
        fail(path, "schema_version must be 1")
    codename = data.get("device")
    if not isinstance(codename, str) or codename != path.parent.name:
        fail(path, "device must match the parent directory name")
    if codename not in known_devices:
        fail(path, "candidate manifest has no matching device.json profile")
    if data.get("status") != "research_only_not_approved_for_build":
        fail(path, "upstream candidates must remain research_only_not_approved_for_build")

    candidates = data.get("candidates")
    required_components = {"device_tree", "kernel", "vendor", "firmware"}
    if not isinstance(candidates, dict) or not required_components.issubset(candidates):
        fail(path, f"candidates must include {sorted(required_components)}")

    for component in sorted(required_components):
        entry = candidates[component]
        if not isinstance(entry, dict):
            fail(path, f"candidates.{component} must be an object")
        repository = entry.get("repository")
        branch = entry.get("branch")
        commit = entry.get("commit")
        if not isinstance(repository, str) or not repository.startswith("https://github.com/"):
            fail(path, f"candidates.{component}.repository must be a GitHub HTTPS URL")
        if not isinstance(branch, str) or not branch.strip():
            fail(path, f"candidates.{component}.branch must be non-empty")
        if not isinstance(commit, str) or re.fullmatch(r"[0-9a-f]{40}", commit) is None:
            fail(path, f"candidates.{component}.commit must be a full lowercase Git SHA")
        if entry.get("build_status") != "not_tested":
            fail(path, f"candidates.{component}.build_status must remain not_tested")

    blockers = data.get("blockers")
    if not isinstance(blockers, list) or not blockers or any(not isinstance(item, str) or not item.strip() for item in blockers):
        fail(path, "blockers must be a non-empty list of descriptions")
    return codename


def main() -> int:
    profiles = sorted(DEVICE_ROOT.glob("*/device.json"))
    if not profiles:
        print("ERROR: no device profiles found", file=sys.stderr)
        return 1

    try:
        seen: set[str] = set()
        first_targets = 0
        for path in profiles:
            codename, name, status = validate(path)
            if codename in seen:
                fail(path, f"duplicate codename: {codename}")
            seen.add(codename)
            data = json.loads(path.read_text(encoding="utf-8"))
            if data["giggleos"]["first_target"]:
                first_targets += 1
            print(f"OK  {codename}: {name} [{status}]")
        if first_targets != 1:
            raise ValueError(f"expected exactly one first_target device, found {first_targets}")

        candidate_manifests = sorted(DEVICE_ROOT.glob("*/upstream-candidates.json"))
        for path in candidate_manifests:
            codename = validate_upstream_candidates(path, seen)
            print(f"OK  {codename}: upstream candidates pinned for research only")
    except (OSError, json.JSONDecodeError, ValueError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1

    print(f"Validated {len(profiles)} device profile(s) and {len(candidate_manifests)} upstream candidate manifest(s). No firmware was built.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
