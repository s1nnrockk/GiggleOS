$ErrorActionPreference = "Stop"

if (-not (Get-Command cargo -ErrorAction SilentlyContinue)) {
    throw "Rust is not installed. Install Rust from https://rustup.rs/"
}
if (-not (Get-Command cargo-ndk -ErrorAction SilentlyContinue)) {
    throw "cargo-ndk is missing. Run: cargo install cargo-ndk"
}

rustup target add aarch64-linux-android
cargo ndk -t arm64-v8a build --release

$source = Join-Path $PSScriptRoot "target/aarch64-linux-android/release/libgiggle_setup_core.so"
$destination = Join-Path $PSScriptRoot "../app/src/main/jniLibs/arm64-v8a"
New-Item -ItemType Directory -Force -Path $destination | Out-Null
Copy-Item $source (Join-Path $destination "libgiggle_setup_core.so") -Force
Write-Host "Rust core built for arm64-v8a and copied into Android app."
