$ErrorActionPreference = "Stop"

if (-not (Get-Command cargo -ErrorAction SilentlyContinue)) {
    throw "Rust is not installed. Install Rust from https://rustup.rs/"
}
if (-not (Get-Command cargo-ndk -ErrorAction SilentlyContinue)) {
    throw "cargo-ndk is missing. Run: cargo install cargo-ndk"
}
if (-not $env:ANDROID_NDK_HOME -and -not $env:ANDROID_NDK_ROOT) {
    throw "Set ANDROID_NDK_HOME to your installed Android NDK directory."
}

rustup target add aarch64-linux-android
cargo ndk -t arm64-v8a -o ../app/src/main/jniLibs build --release

$library = Join-Path $PSScriptRoot "../app/src/main/jniLibs/arm64-v8a/libgiggle_setup_core.so"
if (-not (Test-Path $library)) {
    throw "Rust JNI library was not produced: $library"
}
Write-Host "Rust JNI library built and packaged for arm64-v8a."
