use jni::objects::{JClass, JString};
use jni::sys::{jboolean, jint, jstring, JNI_FALSE, JNI_TRUE};
use jni::JNIEnv;
use sha2::{Digest, Sha256};

const LAST_STEP: i32 = 11;
const MAX_TRANSFER_FLAGS: i32 = 0b1_1111;

#[no_mangle]
pub extern "system" fn Java_org_giggleos_setup_MainActivity_nativeNextStep(
    _env: JNIEnv,
    _class: JClass,
    current: jint,
) -> jint {
    (current + 1).clamp(0, LAST_STEP)
}

#[no_mangle]
pub extern "system" fn Java_org_giggleos_setup_MainActivity_nativePreviousStep(
    _env: JNIEnv,
    _class: JClass,
    current: jint,
) -> jint {
    (current - 1).clamp(0, LAST_STEP)
}

#[no_mangle]
pub extern "system" fn Java_org_giggleos_setup_MainActivity_nativeValidateTransferFlags(
    _env: JNIEnv,
    _class: JClass,
    flags: jint,
) -> jboolean {
    if flags >= 0 && (flags & !MAX_TRANSFER_FLAGS) == 0 {
        JNI_TRUE
    } else {
        JNI_FALSE
    }
}

#[no_mangle]
pub extern "system" fn Java_org_giggleos_setup_MainActivity_nativeSha256(
    mut env: JNIEnv,
    _class: JClass,
    input: JString,
) -> jstring {
    let value: String = match env.get_string(&input) {
        Ok(value) => value.into(),
        Err(_) => String::new(),
    };
    let digest = Sha256::digest(value.as_bytes());
    let output = digest.iter().map(|byte| format!("{byte:02x}")).collect::<String>();
    env.new_string(output)
        .map(|value| value.into_raw())
        .unwrap_or(std::ptr::null_mut())
}
