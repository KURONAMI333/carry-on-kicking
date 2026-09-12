"""Export the confirmed Carry On Kicking icon at release sizes."""

from __future__ import annotations

import hashlib
from pathlib import Path

from PIL import Image

BRANDING_DIR = Path(__file__).resolve().parent
MOD_ROOT = BRANDING_DIR.parent
SOURCE = BRANDING_DIR / "carry_on_kicking_icon_512_CONFIRMED.png"
SOURCE_SHA256 = "6923d92b99f606aedbebe77da5e5d6a55ca84c24208340686538b81762b5548b"
OUTPUT_SIZES = (256, 128, 64)
IN_JAR_SIZE = 256


def main() -> None:
    source_bytes = SOURCE.read_bytes()
    actual_hash = hashlib.sha256(source_bytes).hexdigest()
    if actual_hash != SOURCE_SHA256:
        raise ValueError(f"unexpected confirmed icon SHA-256: {actual_hash}")

    with Image.open(SOURCE) as opened:
        if opened.size != (512, 512):
            raise ValueError(f"unexpected confirmed icon size: {opened.size}")
        source = opened.convert("RGB")

    variants: dict[int, Image.Image] = {}
    for size in OUTPUT_SIZES:
        image = source.resize((size, size), Image.Resampling.LANCZOS)
        variants[size] = image
        destination = BRANDING_DIR / f"carry_on_kicking_icon_{size}.png"
        image.save(destination, optimize=True)
        print(f"wrote {destination} ({size}x{size})")

    logo_destination = MOD_ROOT / "common" / "src" / "main" / "resources" / "logo.png"
    logo_destination.parent.mkdir(parents=True, exist_ok=True)
    variants[IN_JAR_SIZE].save(logo_destination, optimize=True)
    print(f"wrote {logo_destination} ({IN_JAR_SIZE}x{IN_JAR_SIZE})")


if __name__ == "__main__":
    main()
