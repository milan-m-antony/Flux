import os
from PIL import Image, ImageDraw, ImageFont

SCRIPT_DIR = os.path.abspath(os.path.dirname(__file__))
REPORT_DIR = os.path.abspath(os.path.join(SCRIPT_DIR, '..'))
ASSETS_DIR = os.path.join(REPORT_DIR, 'assets')
DIAGRAMS_DIR = os.path.join(ASSETS_DIR, 'diagrams')

def create_hardware_components_image():
    W, H = 2000, 1100
    # Clean modern dark slate background like reference
    img = Image.new('RGB', (W, H), '#0F172A')
    draw = ImageDraw.Draw(img)

    try:
        f_title = ImageFont.truetype('segoeuib.ttf', 38)
        f_name = ImageFont.truetype('segoeuib.ttf', 24)
        f_desc = ImageFont.truetype('segoeui.ttf', 16)
        f_badge = ImageFont.truetype('segoeuib.ttf', 15)
    except:
        f_title = ImageFont.load_default()
        f_name = f_title
        f_desc = f_title
        f_badge = f_title

    # Header title
    title_text = "FLUX EMBEDDED HARDWARE SUBSYSTEM & SENSING MODULES"
    tb = draw.textbbox((0, 0), title_text, font=f_title)
    draw.text(((W - (tb[2]-tb[0]))//2, 35), title_text, fill='#F8FAFC', font=f_title)

    # 5 Component Cards in a grid: 3 on top, 2 on bottom (or 5 across)
    # Let's do 3 on top row, 2 large on bottom row:
    # Top row: ESP32 DevKit V1, DS18B20 Digital Probe, SSD1306 0.96" OLED
    # Bottom row: Active-Low Buzzer & Push Buttons, Flux Custom 3D Enclosure Unit

    card_bg = '#1E293B'
    card_border = '#334155'

    # --- TOP ROW: 3 CARDS ---
    top_w = 600
    top_h = 440
    gap = 40
    left_x = (W - (3 * top_w + 2 * gap)) // 2
    top_y = 105

    # Card 1: ESP32 DevKit V1
    c1_x = left_x
    draw.rounded_rectangle([c1_x, top_y, c1_x + top_w, top_y + top_h], radius=18, fill=card_bg, outline=card_border, width=2)
    # Badge
    draw.rounded_rectangle([c1_x + 20, top_y + 18, c1_x + 160, top_y + 50], radius=8, fill='#0284C7')
    draw.text((c1_x + 32, top_y + 24), "MICROCONTROLLER", fill='#FFFFFF', font=f_badge)
    # Chip Image
    esp_path = os.path.join(DIAGRAMS_DIR, 'esp32_chip_icon.png')
    if os.path.exists(esp_path):
        esp_im = Image.open(esp_path).convert('RGBA')
        esp_im = esp_im.resize((150, 275), Image.Resampling.LANCZOS)
        img.paste(esp_im, (c1_x + 35, top_y + 75), esp_im)
    draw.text((c1_x + 215, top_y + 75), "ESP32 DevKit V1", fill='#38BDF8', font=f_name)
    draw.text((c1_x + 215, top_y + 115), "• Dual-Core Xtensa LX6 (240MHz)", fill='#CBD5E1', font=f_desc)
    draw.text((c1_x + 215, top_y + 145), "• Wi-Fi 802.11 b/g/n + BLE 4.2 Radios", fill='#CBD5E1', font=f_desc)
    draw.text((c1_x + 215, top_y + 175), "• 38 GPIO Pins / 520KB SRAM / 4MB Flash", fill='#CBD5E1', font=f_desc)
    draw.text((c1_x + 215, top_y + 205), "• FreeRTOS Preemptive Task Scheduling", fill='#CBD5E1', font=f_desc)
    draw.text((c1_x + 215, top_y + 235), "• Hardware 1-Wire & I2C Masters", fill='#CBD5E1', font=f_desc)
    draw.text((c1_x + 215, top_y + 265), "• Micro-USB 5V Power Interface", fill='#CBD5E1', font=f_desc)
    draw.text((c1_x + 215, top_y + 305), "Role: Core Edge Brain & Telemetry Gateway", fill='#38BDF8', font=f_desc)

    # Card 2: DS18B20 Digital Probe
    c2_x = c1_x + top_w + gap
    draw.rounded_rectangle([c2_x, top_y, c2_x + top_w, top_y + top_h], radius=18, fill=card_bg, outline=card_border, width=2)
    # Badge
    draw.rounded_rectangle([c2_x + 20, top_y + 18, c2_x + 180, top_y + 50], radius=8, fill='#059669')
    draw.text((c2_x + 32, top_y + 24), "PRECISION SENSOR", fill='#FFFFFF', font=f_badge)
    # Probe Image
    pr_path = os.path.join(DIAGRAMS_DIR, 'probe_icon.png')
    if os.path.exists(pr_path):
        pr_im = Image.open(pr_path).convert('RGBA')
        pr_im = pr_im.resize((190, 190), Image.Resampling.LANCZOS)
        img.paste(pr_im, (c2_x + 20, top_y + 90), pr_im)
    draw.text((c2_x + 230, top_y + 75), "DS18B20 Probe", fill='#34D399', font=f_name)
    draw.text((c2_x + 230, top_y + 115), "• Waterproof Stainless Steel Probe", fill='#CBD5E1', font=f_desc)
    draw.text((c2_x + 230, top_y + 145), "• Dallas 1-Wire Bus (GPIO 4)", fill='#CBD5E1', font=f_desc)
    draw.text((c2_x + 230, top_y + 175), "• 12-Bit Digital Conversion (0.0625°C)", fill='#CBD5E1', font=f_desc)
    draw.text((c2_x + 230, top_y + 205), "• Laboratory Accuracy: ±0.5°C", fill='#CBD5E1', font=f_desc)
    draw.text((c2_x + 230, top_y + 235), "• Range: -55°C to +125°C (-67°F to +257°F)", fill='#CBD5E1', font=f_desc)
    draw.text((c2_x + 230, top_y + 265), "• 4.7kΩ External Pull-Up on Data Bus", fill='#CBD5E1', font=f_desc)
    draw.text((c2_x + 230, top_y + 305), "Role: High-Precision Thermal Acquisition", fill='#34D399', font=f_desc)

    # Card 3: SSD1306 0.96" OLED
    c3_x = c2_x + top_w + gap
    draw.rounded_rectangle([c3_x, top_y, c3_x + top_w, top_y + top_h], radius=18, fill=card_bg, outline=card_border, width=2)
    # Badge
    draw.rounded_rectangle([c3_x + 20, top_y + 18, c3_x + 160, top_y + 50], radius=8, fill='#7C3AED')
    draw.text((c3_x + 32, top_y + 24), "LOCAL DISPLAY", fill='#FFFFFF', font=f_badge)
    # OLED Image
    oled_path = os.path.join(DIAGRAMS_DIR, 'oled_screen_icon.png')
    if os.path.exists(oled_path):
        ol_im = Image.open(oled_path).convert('RGBA')
        ol_im = ol_im.resize((190, 115), Image.Resampling.LANCZOS)
        img.paste(ol_im, (c3_x + 20, top_y + 120), ol_im)
    draw.text((c3_x + 230, top_y + 75), "SSD1306 0.96\" OLED", fill='#A78BFA', font=f_name)
    draw.text((c3_x + 230, top_y + 115), "• 128x64 Monochrome Dot Matrix", fill='#CBD5E1', font=f_desc)
    draw.text((c3_x + 230, top_y + 145), "• Fast I2C Bus (SDA=21, SCL=22)", fill='#CBD5E1', font=f_desc)
    draw.text((c3_x + 230, top_y + 175), "• 8 Contextual UI Carousel Screens", fill='#CBD5E1', font=f_desc)
    draw.text((c3_x + 230, top_y + 205), "• Real-time Waveform & Flame Indicator", fill='#CBD5E1', font=f_desc)
    draw.text((c3_x + 230, top_y + 235), "• High-contrast Daylight Readable", fill='#CBD5E1', font=f_desc)
    draw.text((c3_x + 230, top_y + 265), "• Ultra-low 25mA Power Consumption", fill='#CBD5E1', font=f_desc)
    draw.text((c3_x + 230, top_y + 305), "Role: Standalone Edge Telemetry Interface", fill='#A78BFA', font=f_desc)


    # --- BOTTOM ROW: 2 WIDE CARDS ---
    bot_w = 920
    bot_h = 460
    bot_y = top_y + top_h + 35
    bot_left_x = (W - (2 * bot_w + gap)) // 2

    # Card 4: Audio Alarm & Tactile Navigation Buttons
    c4_x = bot_left_x
    draw.rounded_rectangle([c4_x, bot_y, c4_x + bot_w, bot_y + bot_h], radius=18, fill=card_bg, outline=card_border, width=2)
    # Badge
    draw.rounded_rectangle([c4_x + 20, bot_y + 18, c4_x + 190, bot_y + 50], radius=8, fill='#D97706')
    draw.text((c4_x + 32, bot_y + 24), "SAFETY & CONTROLS", fill='#FFFFFF', font=f_badge)
    
    # Buzzer Graphic
    bx_x, bx_y = c4_x + 35, bot_y + 75
    draw.ellipse([bx_x, bx_y, bx_x + 100, bx_y + 100], fill='#475569', outline='#0284C7', width=3)
    draw.ellipse([bx_x + 28, bx_y + 28, bx_x + 72, bx_y + 72], fill='#0F172A')
    draw.ellipse([bx_x + 38, bx_y + 38, bx_x + 62, bx_y + 62], fill='#E2E8F0')
    draw.text((bx_x + 8, bx_y + 108), "ACTIVE-LOW", fill='#F59E0B', font=f_badge)

    draw.text((c4_x + 160, bot_y + 75), "Active-Low Piezo Buzzer (GPIO 25)", fill='#F59E0B', font=f_name)
    draw.text((c4_x + 160, bot_y + 110), "• Polarity Configuration: BuzzerActiveLow = true for boot-time silence", fill='#CBD5E1', font=f_desc)
    draw.text((c4_x + 160, bot_y + 138), "• Continuous 3-Pip Alarm Pattern upon thermal limit breach (> 28°C / < 18°C)", fill='#CBD5E1', font=f_desc)
    draw.text((c4_x + 160, bot_y + 166), "• Immediate auto-silence when temperature normalizes within safe zone", fill='#CBD5E1', font=f_desc)

    # Buttons Graphic
    btn_path = os.path.join(DIAGRAMS_DIR, 'buttons_icon.png')
    if os.path.exists(btn_path):
        bt_im = Image.open(btn_path).convert('RGBA')
        bt_im = bt_im.resize((150, 75), Image.Resampling.LANCZOS)
        img.paste(bt_im, (c4_x + 35, bot_y + 235), bt_im)

    draw.text((c4_x + 210, bot_y + 230), "Dual Tactile Push Buttons (GPIO 32, 33)", fill='#F472B6', font=f_name)
    draw.text((c4_x + 210, bot_y + 265), "• Button NEXT (GPIO 32): Instant OLED screen cycle across 8 dashboard views", fill='#CBD5E1', font=f_desc)
    draw.text((c4_x + 210, bot_y + 295), "• Button SELECT/ACTION (GPIO 33): Manual buzzer alarm silence & profile toggling", fill='#CBD5E1', font=f_desc)
    draw.text((c4_x + 210, bot_y + 325), "• Internal 45kΩ Pull-Up Resistors with hardware-level debouncing logic", fill='#CBD5E1', font=f_desc)
    draw.text((c4_x + 20, bot_y + 390), "Role: Fail-Safe Local Acoustic Protection & Physical User Control", fill='#F59E0B', font=f_desc)


    # Card 5: Assembled Product Enclosure Unit
    c5_x = c4_x + bot_w + gap
    draw.rounded_rectangle([c5_x, bot_y, c5_x + bot_w, bot_y + bot_h], radius=18, fill=card_bg, outline=card_border, width=2)
    # Badge
    draw.rounded_rectangle([c5_x + 20, bot_y + 18, c5_x + 180, bot_y + 50], radius=8, fill='#DB2777')
    draw.text((c5_x + 32, bot_y + 24), "INTEGRATED UNIT", fill='#FFFFFF', font=f_badge)

    # Assembled device image
    enc_path = os.path.join(REPORT_DIR, '..', 'androidApp', 'app', 'src', 'main', 'res', 'drawable', 'esp32_board.png')
    if os.path.exists(enc_path):
        enc_im = Image.open(enc_path).convert('RGBA')
        enc_im = enc_im.resize((350, 233), Image.Resampling.LANCZOS)
        img.paste(enc_im, (c5_x + 25, bot_y + 90), enc_im)

    draw.text((c5_x + 400, bot_y + 75), "Flux Custom 3D Enclosure", fill='#F472B6', font=f_name)
    draw.text((c5_x + 400, bot_y + 115), "• Flame-Retardant PETG Chassis", fill='#CBD5E1', font=f_desc)
    draw.text((c5_x + 400, bot_y + 145), "• Front Bezel for 0.96\" OLED Display", fill='#CBD5E1', font=f_desc)
    draw.text((c5_x + 400, bot_y + 175), "• Three Tactile Button Access Wells", fill='#CBD5E1', font=f_desc)
    draw.text((c5_x + 400, bot_y + 205), "• Waterproof Strain-Relief Cable Gland", fill='#CBD5E1', font=f_desc)
    draw.text((c5_x + 400, bot_y + 235), "• Acoustic Buzzer Ventilation Grille", fill='#CBD5E1', font=f_desc)
    draw.text((c5_x + 400, bot_y + 265), "• Dual Screw Tabs for Secure Wall Mounting", fill='#CBD5E1', font=f_desc)
    draw.text((c5_x + 400, bot_y + 295), "• Compact Form Factor: 85 x 55 x 30 mm", fill='#CBD5E1', font=f_desc)
    draw.text((c5_x + 400, bot_y + 340), "Status: 100% Assembled & Calibrated Test Rig", fill='#34D399', font=f_desc)

    out_file = os.path.join(DIAGRAMS_DIR, 'img_hardware_components.png')
    img.save(out_file, dpi=(300, 300), quality=95)
    print(f"[+] Hardware components image successfully created: {out_file} ({W}x{H})")

if __name__ == '__main__':
    create_hardware_components_image()

