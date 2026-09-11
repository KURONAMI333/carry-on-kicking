import os

import nbtlib
from nbtlib.tag import Compound, Int, List

structure = nbtlib.File(
    Compound(
        {
            "size": List[Int]([16, 6, 16]),
            "entities": List[Compound]([]),
            "blocks": List[Compound]([]),
            "palette": List[Compound]([]),
            "DataVersion": Int(3955),
        }
    ),
    gzipped=True,
)
output = os.path.join(
    os.path.dirname(__file__),
    "resources",
    "data",
    "carryonkick",
    "structure",
    "empty3x3x3.nbt",
)
os.makedirs(os.path.dirname(output), exist_ok=True)
structure.save(output)
loaded = nbtlib.load(output)
print("keys:", list(loaded.keys()), "size:", list(loaded["size"]))
