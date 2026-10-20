from __future__ import annotations

import io
import json
import os
import struct
from typing import Final

from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import ec
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
from cryptography.hazmat.primitives.kdf.hkdf import HKDF
from PIL import Image
from pydantic import BaseModel, ConfigDict, Field


# ============================================================================
# 1. COURSEWORK SKELETON: Educational AES-128 GF(2^8) State Transformations
# ============================================================================

# Standard Rijndael S-Box lookup table (256 bytes)
S_BOX: Final[tuple[int, ...]] = (
    0x63, 0x7C, 0x77, 0x7B, 0xF2, 0x6B, 0x6F, 0xC5, 0x30, 0x01, 0x67, 0x2B, 0xFE, 0xD7, 0xAB, 0x76,
    0xCA, 0x82, 0xC9, 0x7D, 0xFA, 0x59, 0x47, 0xF0, 0xAD, 0xD4, 0xA2, 0xAF, 0x9C, 0xA4, 0x72, 0xC0,
    0xB7, 0xFD, 0x93, 0x26, 0x36, 0x3F, 0xF7, 0xCC, 0x34, 0xA5, 0xE5, 0xF1, 0x71, 0xD8, 0x31, 0x15,
    0x04, 0xC7, 0x23, 0xC3, 0x18, 0x96, 0x05, 0x9A, 0x07, 0x12, 0x80, 0xE2, 0xEB, 0x27, 0xB2, 0x75,
    0x09, 0x83, 0x2C, 0x1A, 0x1B, 0x6E, 0x5A, 0xA0, 0x52, 0x3B, 0xD6, 0xB3, 0x29, 0xE3, 0x2F, 0x84,
    0x53, 0xD1, 0x00, 0xED, 0x20, 0xFC, 0xB1, 0x5B, 0x6A, 0xCB, 0xBE, 0x39, 0x4A, 0x4C, 0x58, 0xCF,
    0xD0, 0xEF, 0xAA, 0xFB, 0x43, 0x4D, 0x33, 0x85, 0x45, 0xF9, 0x02, 0x7F, 0x50, 0x3C, 0x9F, 0xA8,
    0x51, 0xA3, 0x40, 0x8F, 0x92, 0x9D, 0x38, 0xF5, 0xBC, 0xB6, 0xDA, 0x21, 0x10, 0xFF, 0xF3, 0xD2,
    0xCD, 0x0C, 0x13, 0xEC, 0x5F, 0x97, 0x44, 0x17, 0xC4, 0xA7, 0x7E, 0x3D, 0x64, 0x5D, 0x19, 0x73,
    0x60, 0x81, 0x4F, 0xDC, 0x22, 0x2A, 0x90, 0x88, 0x46, 0xEE, 0xB8, 0x14, 0xDE, 0x5E, 0x0B, 0xDB,
    0xE0, 0x32, 0x3A, 0x0A, 0x49, 0x06, 0x24, 0x5C, 0xC2, 0xD3, 0xAC, 0x62, 0x91, 0x95, 0xE4, 0x79,
    0xE7, 0xC8, 0x37, 0x6D, 0x8D, 0xD5, 0x4E, 0xA9, 0x6C, 0x56, 0xF4, 0xEA, 0x65, 0x7A, 0xAE, 0x08,
    0xBA, 0x78, 0x25, 0x2E, 0x1C, 0xA6, 0xB4, 0xC6, 0xE8, 0xDD, 0x74, 0x1F, 0x4B, 0xBD, 0x8B, 0x8A,
    0x70, 0x3E, 0xB5, 0x66, 0x48, 0x03, 0xF6, 0x0E, 0x61, 0x35, 0x57, 0xB9, 0x86, 0xC1, 0x1D, 0x9E,
    0xE1, 0xF8, 0x98, 0x11, 0x69, 0xD9, 0x8E, 0x94, 0x9B, 0x1E, 0x87, 0xE9, 0xCE, 0x55, 0x28, 0xDF,
    0x8C, 0xA1, 0x89, 0x0D, 0xBF, 0xE6, 0x42, 0x68, 0x41, 0x99, 0x2D, 0x0F, 0xB0, 0x54, 0xBB, 0x16,
)

StateMatrix = list[list[int]]  # 4x4 matrix of ints (0..255)


def gmul(a: int, b: int) -> int:
    """Multiply two bytes in GF(2^8) modulo P(x) = x^8 + x^4 + x^3 + x + 1 (0x11B)."""
    p: int = 0
    for _ in range(8):
        if b & 1:
            p ^= a
        hi_bit_set: int = a & 0x80
        a = (a << 1) & 0xFF
        if hi_bit_set:
            a ^= 0x1B  # Reduction polynomial x^4 + x^3 + x + 1
        b >>= 1
    return p


def bytes_to_state(block: bytes) -> StateMatrix:
    """Convert 16-byte array into a 4x4 column-major AES state matrix."""
    if len(block) != 16:
        raise ValueError("AES state requires exactly 16 bytes.")
    return [[block[r + 4 * c] for c in range(4)] for r in range(4)]


def state_to_bytes(state: StateMatrix) -> bytes:
    """Convert 4x4 column-major AES state matrix back into 16 bytes."""
    return bytes(state[r][c] for c in range(4) for r in range(4))


def sub_bytes(state: StateMatrix) -> StateMatrix:
    """1. Byte Substitution Layer: Non-linear S-Box lookup."""
    return [[S_BOX[state[r][c]] for c in range(4)] for r in range(4)]


def shift_rows(state: StateMatrix) -> StateMatrix:
    """2a. Diffusion Layer (ShiftRows): Cyclically shift row r left by r offsets."""
    return [state[r][r:] + state[r][:r] for r in range(4)]


def mix_columns(state: StateMatrix) -> StateMatrix:
    """2b. Diffusion Layer (MixColumns): Matrix multiplication over GF(2^8)."""
    new_state: StateMatrix = [[0] * 4 for _ in range(4)]
    for c in range(4):
        s0, s1, s2, s3 = state[0][c], state[1][c], state[2][c], state[3][c]
        new_state[0][c] = gmul(0x02, s0) ^ gmul(0x03, s1) ^ s2 ^ s3
        new_state[1][c] = s0 ^ gmul(0x02, s1) ^ gmul(0x03, s2) ^ s3
        new_state[2][c] = s0 ^ s1 ^ gmul(0x02, s2) ^ gmul(0x03, s3)
        new_state[3][c] = gmul(0x03, s0) ^ s1 ^ s2 ^ gmul(0x02, s3)
    return new_state


def add_round_key(state: StateMatrix, round_key: StateMatrix) -> StateMatrix:
    """3. Key Addition Layer: Bitwise XOR between state and subkey."""
    return [[state[r][c] ^ round_key[r][c] for c in range(4)] for r in range(4)]


# ============================================================================
# 2. DATA VALIDATION SCHEMAS (Pydantic v2)
# ============================================================================

class ProtectedContentPayload(BaseModel):
    """Schema for the manga/webnovel payload and watermark inside the AES vault."""
    model_config = ConfigDict(strict=True, frozen=True)

    reader_id: str = Field(..., min_length=3, description="Watermark ID for leak tracing")
    chapter_id: int = Field(..., gt=0)
    content_text: str = Field(..., min_length=1)


class EncryptedEnvelope(BaseModel):
    """Binary structure packed prior to LSB steganography."""
    model_config = ConfigDict(strict=True, frozen=True)

    nonce: bytes = Field(..., min_length=12, max_length=12)
    ciphertext: bytes = Field(..., min_length=16)

    def to_binary(self) -> bytes:
        """Pack as: [12-byte Nonce][Ciphertext + 16-byte GCM Tag]."""
        return self.nonce + self.ciphertext

    @classmethod
    def from_binary(cls, raw: bytes) -> EncryptedEnvelope:
        if len(raw) < 28:
            raise ValueError("Corrupted envelope: payload shorter than nonce + GCM tag.")
        return cls(nonce=raw[:12], ciphertext=raw[12:])


# ============================================================================
# 3. ECDH HANDSHAKE & HKDF KEY DERIVATION
# ============================================================================

class ECDHPeer:
    """Represents either the Publisher Server or Reader Browser in ECDH."""

    def __init__(self) -> None:
        self._private_key: ec.EllipticCurvePrivateKey = ec.generate_private_key(
            ec.SECP256R1()
        )

    def get_public_bytes(self) -> bytes:
        """Serialize public point (A or B) for HTTP transmission."""
        return self._private_key.public_key().public_bytes(
            encoding=serialization.Encoding.PEM,
            format=serialization.PublicFormat.SubjectPublicKeyInfo,
        )

    def derive_aes_128_key(self, peer_public_bytes: bytes, session_salt: bytes) -> bytes:
        """Compute shared curve secret and pass through HKDF-SHA256 to get a 16-byte AES key."""
        peer_public_key = serialization.load_pem_public_key(peer_public_bytes)
        if not isinstance(peer_public_key, ec.EllipticCurvePublicKey):
            raise TypeError("Expected an EllipticCurvePublicKey.")

        shared_secret: bytes = self._private_key.exchange(ec.ECDH(), peer_public_key)

        # Derive uniform 128-bit (16-byte) AES key using HKDF
        hkdf = HKDF(
            algorithm=hashes.SHA256(),
            length=16,
            salt=session_salt,
            info=b"manga-drm-aes128-key-agreement",
        )
        return hkdf.derive(shared_secret)


# ============================================================================
# 4. LSB STEGANOGRAPHY ENGINE (Lossless PNG)
# ============================================================================

def embed_lsb_payload(cover_png_bytes: bytes, secret_payload: bytes) -> bytes:
    """
    Embed a 32-bit big-endian length header + secret_payload into the LSBs
    of an RGB PNG image.
    """
    with Image.open(io.BytesIO(cover_png_bytes)) as img:
        rgb_img = img.convert("RGB")
        width, height = rgb_img.size
        raw_channels = bytearray(rgb_img.tobytes())

    # Prepend 4-byte unsigned big-endian length header
    framed_data: bytes = struct.pack(">I", len(secret_payload)) + secret_payload
    total_bits: int = len(framed_data) * 8

    if total_bits > len(raw_channels):
        raise ValueError(
            f"Cover image capacity exceeded: need {total_bits} channel bytes, "
            f"have {len(raw_channels)}."
        )

    # Modify LSB of each sequential color channel byte
    bit_idx: int = 0
    for byte_val in framed_data:
        for shift in range(7, -1, -1):
            bit: int = (byte_val >> shift) & 1
            raw_channels[bit_idx] = (raw_channels[bit_idx] & 0xFE) | bit
            bit_idx += 1

    stego_img = Image.frombytes("RGB", (width, height), bytes(raw_channels))
    output_buffer = io.BytesIO()
    stego_img.save(output_buffer, format="PNG", optimize=False)
    return output_buffer.getvalue()


def extract_lsb_payload(stego_png_bytes: bytes) -> bytes:
    """Extract the 32-bit length header from LSBs, then reconstruct the exact payload."""
    with Image.open(io.BytesIO(stego_png_bytes)) as img:
        rgb_img = img.convert("RGB")
        raw_channels: bytes = rgb_img.tobytes()

    if len(raw_channels) < 32:
        raise ValueError("Image is too small to contain a 32-bit length header.")

    # 1. Read first 32 LSBs to reconstruct the 4-byte unsigned length integer
    payload_length: int = 0
    for i in range(32):
        payload_length = (payload_length << 1) | (raw_channels[i] & 1)

    total_payload_bits: int = payload_length * 8
    if 32 + total_payload_bits > len(raw_channels):
        raise ValueError("Corrupted stego header: declared length exceeds image size.")

    # 2. Extract the payload bytes
    extracted = bytearray(payload_length)
    for byte_idx in range(payload_length):
        acc: int = 0
        base_offset: int = 32 + (byte_idx * 8)
        for bit_offset in range(8):
            acc = (acc << 1) | (raw_channels[base_offset + bit_offset] & 1)
        extracted[byte_idx] = acc

    return bytes(extracted)


# ============================================================================
# 5. COMPLETE PIPELINE EXECUTION
# ============================================================================

def main() -> None:
    print("--- [Part A] Verifying Educational AES-128 Round 1 Transformations ---")
    pt_block = b"Ch1: Solo Level!"
    k0_block = bytes.fromhex("2b7e151628aed2a6abf7158809cf4f3c")

    state = bytes_to_state(pt_block)
    round_key_0 = bytes_to_state(k0_block)

    state = add_round_key(state, round_key_0)
    state = sub_bytes(state)
    state = shift_rows(state)
    state = mix_columns(state)
    print(f"Round 1 MixColumns Output (Hex): {state_to_bytes(state).hex()}")
    assert state[0][0] == 0x19, "Manual GF(2^8) trace mismatch!"

    print("\n--- [Part B] Executing Full ECDH + AES-128-GCM + LSB Stego Pipeline ---")
    # 1. Handshake (ECDH)
    server = ECDHPeer()
    reader_browser = ECDHPeer()
    session_salt: bytes = os.urandom(16)

    server_aes_key: bytes = server.derive_aes_128_key(
        reader_browser.get_public_bytes(), session_salt
    )
    reader_aes_key: bytes = reader_browser.derive_aes_128_key(
        server.get_public_bytes(), session_salt
    )
    assert server_aes_key == reader_aes_key
    print(f"1. Derived Shared 128-bit AES Key : {server_aes_key.hex()}")

    # 2. Encryption (AES-128-GCM with Reader_ID Watermark)
    novel_payload = ProtectedContentPayload(
        reader_id="USR-98412-VN",
        chapter_id=42,
        content_text="The double dungeon doors slowly ground open in the dark...",
    )
    plaintext_json: bytes = novel_payload.model_dump_json().encode("utf-8")

    nonce: bytes = os.urandom(12)
    aes_gcm_server = AESGCM(server_aes_key)
    ciphertext: bytes = aes_gcm_server.encrypt(
        nonce, plaintext_json, associated_data=b"chapter-42"
    )
    envelope = EncryptedEnvelope(nonce=nonce, ciphertext=ciphertext)
    print(f"2. Encrypted Envelope Size        : {len(envelope.to_binary())} bytes")

    # 3. Hiding (LSB Steganography into synthetic 100x100 Promotional Cover PNG)
    cover_buffer = io.BytesIO()
    Image.new("RGB", (100, 100), color=(255, 120, 200)).save(cover_buffer, format="PNG")
    stego_png_bytes: bytes = embed_lsb_payload(
        cover_buffer.getvalue(), envelope.to_binary()
    )
    print(f"3. Generated Stego PNG Size       : {len(stego_png_bytes)} bytes")

    # 4. Rendering (Client Browser Extraction & Decryption)
    extracted_raw: bytes = extract_lsb_payload(stego_png_bytes)
    extracted_envelope = EncryptedEnvelope.from_binary(extracted_raw)

    aes_gcm_client = AESGCM(reader_aes_key)
    decrypted_json: bytes = aes_gcm_client.decrypt(
        extracted_envelope.nonce,
        extracted_envelope.ciphertext,
        associated_data=b"chapter-42",
    )
    recovered_payload = ProtectedContentPayload.model_validate_json(decrypted_json)

    print("4. Client Decrypted & Validated Payload:")
    print(json.dumps(recovered_payload.model_dump(), indent=2))


if __name__ == "__main__":
    main()