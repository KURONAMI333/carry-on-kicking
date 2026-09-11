"""Generate the addon's original mono effects. Requires NumPy and FFmpeg."""

from pathlib import Path
import subprocess

import numpy as np

RATE = 44100
DESTINATION = (
    Path(__file__).resolve().parents[1]
    / "common/src/main/resources/assets/carryonkick/sounds"
)


def filtered_noise(
    rng: np.random.Generator, seconds: float, center: float, width: float
) -> np.ndarray:
    count = round(RATE * seconds)
    spectrum = np.fft.rfft(rng.standard_normal(count))
    frequency = np.fft.rfftfreq(count, 1 / RATE)
    band = np.exp(-0.5 * ((frequency - center) / width) ** 2)
    band *= np.minimum(frequency / 150, 1) ** 2
    result = np.fft.irfft(spectrum * band, count)
    return result / max(np.sqrt(np.mean(result**2)), 1e-10)


def save(name: str, samples: np.ndarray, rms_db: float) -> None:
    samples = samples * (10 ** (rms_db / 20)) / np.sqrt(np.mean(samples**2))
    peak = float(np.max(np.abs(samples)))
    if peak > 0.94:
        samples *= 0.94 / peak
    subprocess.run(
        [
            "ffmpeg",
            "-hide_banner",
            "-loglevel",
            "error",
            "-y",
            "-f",
            "f32le",
            "-ar",
            str(RATE),
            "-ac",
            "1",
            "-i",
            "pipe:0",
            "-c:a",
            "libvorbis",
            "-q:a",
            "5",
            "-map_metadata",
            "-1",
            str(DESTINATION / f"{name}.ogg"),
        ],
        input=samples.astype("<f4").tobytes(),
        check=True,
    )
    print(
        f"{name}: {len(samples) / RATE:.3f}s, peak {20*np.log10(np.max(np.abs(samples))):.1f}dBFS"
    )


def main() -> None:
    DESTINATION.mkdir(parents=True, exist_ok=True)
    # A quiet, low rustle with periodic boundaries. Pitch movement happens during playback.
    rng = np.random.default_rng(8201)
    duration = 6.0
    timeline = np.arange(round(RATE * duration)) / RATE
    rustle = filtered_noise(rng, duration, 540, 210)
    rustle *= 0.8 + 0.2 * np.sin(2 * np.pi * 18 * timeline / duration)
    save("windup", rustle, -25)

    for number in range(1, 9):
        rng = np.random.default_rng(8210 + number)
        duration = 1.0
        timeline = np.arange(RATE) / RATE
        # Bright, immediate contact with a short body; the tail is deliberately quiet.
        envelope = (1 - np.exp(-timeline / 0.012)) * np.exp(-timeline / 0.055)
        strike = (
            filtered_noise(rng, duration, 3100 + rng.uniform(-220, 220), 1250)
            * envelope
        )
        body = np.sin(2 * np.pi * (135 * timeline - 40 * timeline**2)) * np.exp(
            -timeline / 0.045
        )
        body *= 1 - np.exp(-timeline / 0.003)
        save(f"kick_{number}", strike + 0.2 * body, -23)

    for number in range(1, 5):
        rng = np.random.default_rng(8230 + number)
        duration = 0.16
        timeline = np.arange(round(RATE * duration)) / RATE
        envelope = np.sin(np.pi * timeline / duration) ** 2
        release = filtered_noise(rng, duration, 1250, 400) * envelope
        save(f"drop_{number}", release, -26)


if __name__ == "__main__":
    main()
