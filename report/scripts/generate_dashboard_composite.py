import os
from PIL import Image, ImageDraw, ImageFont

SCRIPT_DIR = os.path.abspath(os.path.dirname(__file__))
REPORT_DIR = os.path.abspath(os.path.join(SCRIPT_DIR, '..'))
ASSETS_DIR = os.path.join(REPORT_DIR, 'assets')
DIAGRAMS_DIR = os.path.join(ASSETS_DIR, 'diagrams')
UI_DIR = os.path.join(ASSETS_DIR, 'ui_screens')

def create_dashboard_composite():
    W, H = 2400, 1300
    img = Image.new('RGB', (W, H), '#0F172A')
    draw = ImageDraw.Draw(img)

    try:
        f_title = ImageFont.truetype('segoeuib.ttf', 40)
        f_subtitle = ImageFont.truetype('segoeuib.ttf', 24)
        f_cap = ImageFont.truetype('segoeuib.ttf', 20)
        f_desc = ImageFont.truetype('segoeui.ttf', 16)
    except:
        f_title = ImageFont.load_default()
        f_subtitle = f_title
        f_cap = f_title
        f_desc = f_title

    # Header
    title_text = "FLUX ANDROID COMPANION DASHBOARD & REAL-TIME USER INTERFACE"
    tb = draw.textbbox((0, 0), title_text, font=f_title)
    draw.text(((W - (tb[2]-tb[0]))//2, 35), title_text, fill='#F8FAFC', font=f_title)

    sub = "High-Precision Telemetry • Wi-Fi Quick Provisioning • Push Alert Center • Diagnostics"
    s_tb = draw.textbbox((0, 0), sub, font=f_subtitle)
    draw.text(((W - (s_tb[2]-s_tb[0]))//2, 85), sub, fill='#94A3B8', font=f_subtitle)

    # 4 Phones
    screens = [
        ("ui_device_details.png", "(a) Hardware & Sensor Details", "Real-time thermal metrics, 1-Wire link status,\nESP32 chip details, and MAC address."),
        ("ui_wifi_sheet.png", "(b) Wi-Fi Quick Sheet", "Instant network capture ('kERALA VISION')\nfor 1-tap ESP32 credential provisioning."),
        ("ui_alerts_center.png", "(c) Alerts & Safety Push", "Active status log, 15-min hysteresis records,\nand push notification history."),
        ("ui_settings.png", "(d) System Settings & Sound", "Active-low buzzer mute, unit conversion (°C/°F),\nand automatic calibration offsets.")
    ]

    card_w = 520
    gap = 40
    left_x = (W - (4 * card_w + 3 * gap)) // 2
    top_y = 145
    card_h = 1100

    for i, (fn, cap, desc) in enumerate(screens):
        cx = left_x + i * (card_w + gap)
        # Card container
        draw.rounded_rectangle([cx, top_y, cx + card_w, top_y + card_h], radius=20, fill='#1E293B', outline='#334155', width=2)

        # Phone mockup inside card
        phone_w = 440
        phone_h = 880
        px = cx + (card_w - phone_w) // 2
        py = top_y + 25

        draw.rounded_rectangle([px, py, px + phone_w, py + phone_h], radius=28, fill='#020617', outline='#64748B', width=3)
        # Speaker notch
        draw.rounded_rectangle([px + phone_w//2 - 40, py + 8, px + phone_w//2 + 40, py + 14], radius=3, fill='#334155')

        # Load screen
        im_path = os.path.join(UI_DIR, fn)
        if os.path.exists(im_path):
            sc_im = Image.open(im_path).convert('RGBA')
            sc_im = sc_im.resize((phone_w - 20, phone_h - 36), Image.Resampling.LANCZOS)
            img.paste(sc_im, (px + 10, py + 22), sc_im)

        # Caption
        c_tb = draw.textbbox((0, 0), cap, font=f_cap)
        draw.text((cx + (card_w - (c_tb[2]-c_tb[0]))//2, top_y + 925), cap, fill='#38BDF8', font=f_cap)

        # Description lines
        d_lines = desc.split('\n')
        for j, line in enumerate(d_lines):
            ltb = draw.textbbox((0, 0), line, font=f_desc)
            draw.text((cx + (card_w - (ltb[2]-ltb[0]))//2, top_y + 965 + j * 26), line, fill='#CBD5E1', font=f_desc)

    out_file = os.path.join(DIAGRAMS_DIR, 'img_dashboard_screens.png')
    img.save(out_file, dpi=(300, 300), quality=95)
    print(f"[+] Dashboard composite created: {out_file} ({W}x{H})")

if __name__ == '__main__':
    create_dashboard_composite()

