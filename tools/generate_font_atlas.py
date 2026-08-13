from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
FONT = ROOT / "src/main/resources/assets/qte_engine/font/minecraft-five-bold.ttf"
OUTPUT = ROOT / "src/main/resources/assets/qte_engine/textures/font/minecraft_five.png"
COMPACT_OUTPUT = ROOT / "src/main/resources/assets/qte_engine/textures/font/minecraft_five_compact.png"
ROWS = ("ABCDEFGHIJKLMNOPQRS", "TUVWXYZ0123456789 _")


def generate(output: Path, cell: int, font_size: int) -> None:
    font = ImageFont.truetype(str(FONT), font_size)
    atlas = Image.new("RGBA", (len(ROWS[0]) * cell, len(ROWS) * cell), (0, 0, 0, 0))
    draw = ImageDraw.Draw(atlas)
    for row_index, row in enumerate(ROWS):
        for column, glyph in enumerate(row):
            bounds = draw.textbbox((0, 0), glyph, font=font, stroke_width=0)
            width = bounds[2] - bounds[0]
            height = bounds[3] - bounds[1]
            x = column * cell + (cell - width) // 2 - bounds[0]
            y = row_index * cell + (cell - height) // 2 - bounds[1]
            draw.text((x, y), glyph, font=font, fill=(255, 255, 255, 255))
    alpha = atlas.getchannel("A").point(lambda value: 255 if value >= 128 else 0)
    atlas.putalpha(alpha)
    output.parent.mkdir(parents=True, exist_ok=True)
    atlas.save(output, optimize=True)


def main() -> None:
    generate(OUTPUT, cell=14, font_size=16)
    generate(COMPACT_OUTPUT, cell=14, font_size=14)


if __name__ == "__main__":
    main()
