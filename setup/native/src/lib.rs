use jni::objects::{JObject, JString};
use jni::sys::{jboolean, jint, jstring, JNI_FALSE, JNI_TRUE};
use jni::JNIEnv;
use sha2::{Digest, Sha256};

const LAST_STEP: i32 = 11;
const MAX_TRANSFER_FLAGS: i32 = 0b1_1111;

fn next_step(current: i32) -> i32 {
    current.saturating_add(1).clamp(0, LAST_STEP)
}

fn previous_step(current: i32) -> i32 {
    current.saturating_sub(1).clamp(0, LAST_STEP)
}

fn valid_transfer_flags(flags: i32) -> bool {
    flags >= 0 && (flags & !MAX_TRANSFER_FLAGS) == 0
}

fn sha256_hex(input: &str) -> String {
    let digest = Sha256::digest(input.as_bytes());
    digest.iter().map(|byte| format!("{byte:02x}")).collect()
}

#[no_mangle]
pub extern "system" fn Java_org_giggleos_setup_MainActivity_nativeNextStep(
    _env: JNIEnv<'_>,
    _this: JObject<'_>,
    current: jint,
) -> jint {
    next_step(current)
}

#[no_mangle]
pub extern "system" fn Java_org_giggleos_setup_MainActivity_nativePreviousStep(
    _env: JNIEnv<'_>,
    _this: JObject<'_>,
    current: jint,
) -> jint {
    previous_step(current)
}

#[no_mangle]
pub extern "system" fn Java_org_giggleos_setup_MainActivity_nativeValidateTransferFlags(
    _env: JNIEnv<'_>,
    _this: JObject<'_>,
    flags: jint,
) -> jboolean {
    if valid_transfer_flags(flags) { JNI_TRUE } else { JNI_FALSE }
}

#[no_mangle]
pub extern "system" fn Java_org_giggleos_setup_MainActivity_nativeSha256(
    mut env: JNIEnv<'_>,
    _this: JObject<'_>,
    input: JString<'_>,
) -> jstring {
    let value: String = match env.get_string(&input) {
        Ok(value) => value.into(),
        Err(_) => String::new(),
    };
    let output = sha256_hex(&value);
    env.new_string(output)
        .map(|value| value.into_raw())
        .unwrap_or(std::ptr::null_mut())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn navigation_stays_inside_wizard() {
        assert_eq!(next_step(0), 1);
        assert_eq!(next_step(11), 11);
        assert_eq!(previous_step(0), 0);
        assert_eq!(previous_step(11), 10);
    }

    #[test]
    fn transfer_flags_reject_unknown_bits() {
        assert!(valid_transfer_flags(0));
        assert!(valid_transfer_flags(31));
        assert!(!valid_transfer_flags(-1));
        assert!(!valid_transfer_flags(32));
    }

    #[test]
    fn digest_is_stable() {
        assert_eq!(
            sha256_hex("giggleos"),
            "cd826ac77b68c021b9c7ace797032fdcdaf7b4eeeeedcefa55fb1bbe3faf53fb"
        );
    }
}
