import os
from PIL import Image, ImageDraw, ImageFont

SCRIPT_DIR = os.path.abspath(os.path.dirname(__file__))
REPORT_DIR = os.path.abspath(os.path.join(SCRIPT_DIR, '..'))
ASSETS_DIR = os.path.join(REPORT_DIR, 'assets')
DIAGRAMS_DIR = os.path.join(ASSETS_DIR, 'diagrams')

def create_final_product_image():
    W, H = 2000, 1000
    img = Image.new('RGB', (W, H), '#0F172A')
    draw = ImageDraw.Draw(img)

    try:
        f_title = ImageFont.truetype('segoeuib.ttf', 36)
        f_subtitle = ImageFont.truetype('segoeuib.ttf', 24)
        f_desc = ImageFont.truetype('segoeui.ttf', 17)
        f_badge = ImageFont.truetype('segoeuib.ttf', 16)
    except:
        f_title = ImageFont.load_default()
        f_subtitle = f_title
        f_desc = f_title
        f_badge = f_title

    # Header
    title_text = "FLUX PROTOTYPE ASSEMBLY & LIVE TELEMETRY DEPLOYMENT"
    tb = draw.textbbox((0, 0), title_text, font=f_title)
    draw.text(((W - (tb[2]-tb[0]))//2, 30), title_text, fill='#F8FAFC', font=f_title)

    card_w = 930
    card_h = 860
    gap = 40
    left_x = (W - (2 * card_w + gap)) // 2
    top_y = 95

    # --- LEFT CARD: ASSEMBLED HARDWARE DEVICE ---
    c1_x = left_x
    draw.rounded_rectangle([c1_x, top_y, c1_x + card_w, top_y + card_h], radius=20, fill='#1E293B', outline='#334155', width=2)
    # Badge
    draw.rounded_rectangle([c1_x + 25, top_y + 20, c1_x + 240, top_y + 56], radius=8, fill='#0284C7')
    draw.text((c1_x + 38, top_y + 26), "PHYSICAL PROTOTYPE", fill='#FFFFFF', font=f_badge)
    draw.text((c1_x + 260, top_y + 25), "Calibrated ESP32 + DS18B20 Assembly", fill='#94A3B8', font=f_desc)

    # Device image
    dev_path = os.path.join(REPORT_DIR, '..', 'androidApp', 'app', 'src', 'main', 'res', 'drawable', 'esp32_board.png')
    if os.path.exists(dev_path):
        dev_im = Image.open(dev_path).convert('RGBA')
        dev_im = dev_im.resize((820, 546), Image.Resampling.LANCZOS)
        img.paste(dev_im, (c1_x + 55, top_y + 80), dev_im)

    # Bullet details at bottom of Left Card
    draw.text((c1_x + 40, top_y + 650), "• Complete Integrated Edge Node:", fill='#38BDF8', font=f_subtitle)
    draw.text((c1_x + 40, top_y + 695), "  Custom 3D-printed flame-retardant PETG wall enclosure (85x55x30 mm).", fill='#E2E8F0', font=f_desc)
    draw.text((c1_x + 40, top_y + 730), "• Live Thermal Sampling & Display:", fill='#38BDF8', font=f_desc)
    draw.text((c1_x + 40, top_y + 755), "  Continuous 1Hz 12-bit DS18B20 sampling with live SSD1306 OLED carousel.", fill='#E2E8F0', font=f_desc)
    draw.text((c1_x + 40, top_y + 790), "• Acoustic Polarity Tuning: BuzzerActiveLow = true for boot-time silence.", fill='#34D399', font=f_desc)
    draw.text((c1_x + 40, top_y + 820), "• Navigation Controls: Tactile switches for 8-screen carousel and mute.", fill='#CBD5E1', font=f_desc)


    # --- RIGHT CARD: MOBILE COMPANION APP & CLOUD STREAM ---
    c2_x = c1_x + card_w + gap
    draw.rounded_rectangle([c2_x, top_y, c2_x + card_w, top_y + card_h], radius=20, fill='#1E293B', outline='#334155', width=2)
    # Badge
    draw.rounded_rectangle([c2_x + 25, top_y + 20, c2_x + 230, top_y + 56], radius=8, fill='#7C3AED')
    draw.text((c2_x + 38, top_y + 26), "LIVE APP TELEMETRY", fill='#FFFFFF', font=f_badge)
    draw.text((c2_x + 250, top_y + 25), "Android 14/15 Device Synchronization", fill='#94A3B8', font=f_desc)

    # Two mobile screens side-by-side inside right card
    s1_path = os.path.join(REPORT_DIR, 'assets', 'ui_screens', 'ui_alerts_center.png')
    s2_path = os.path.join(REPORT_DIR, 'assets', 'ui_screens', 'ui_device_details.png')
    
    if os.path.exists(s1_path) and os.path.exists(s2_path):
        s1 = Image.open(s1_path).convert('RGBA').resize((270, 520), Image.Resampling.LANCZOS)
        s2 = Image.open(s2_path).convert('RGBA').resize((270, 520), Image.Resampling.LANCZOS)
        # Rounded frame for phones
        draw.rounded_rectangle([c2_x + 115, top_y + 85, c2_x + 395, top_y + 615], radius=16, fill='#0F172A', outline='#64748B', width=2)
        draw.rounded_rectangle([c2_x + 445, top_y + 85, c2_x + 725, top_y + 615], radius=16, fill='#0F172A', outline='#64748B', width=2)
        img.paste(s1, (c2_x + 120, top_y + 90), s1)
        img.paste(s2, (c2_x + 450, top_y + 90), s2)

    draw.text((c2_x + 40, top_y + 650), "• Dual-Tier Communication Active:", fill='#A78BFA', font=f_subtitle)
    draw.text((c2_x + 40, top_y + 695), "  Instant 1Hz BLE GATT streaming alongside Wi-Fi AP & Station provisioning.", fill='#E2E8F0', font=f_desc)
    draw.text((c2_x + 40, top_y + 730), "• Zero Mock Data Ingestion:", fill='#A78BFA', font=f_desc)
    draw.text((c2_x + 40, top_y + 755), "  100% genuine hardware telemetry buffered in Room DB and Supabase Cloud.", fill='#E2E8F0', font=f_desc)
    draw.text((c2_x + 40, top_y + 790), "• Automated Mobile Alerts: 1.2s push notification via OneSignal REST v1.", fill='#34D399', font=f_desc)
    draw.text((c2_x + 40, top_y + 820), "• Production Deployment: Deployed on physical Android device (RZCX71Q65BB).", fill='#CBD5E1', font=f_desc)

    out_file = os.path.join(DIAGRAMS_DIR, 'img_final_product.png')
    img.save(out_file, dpi=(300, 300), quality=95)
    print(f"[+] Final product image successfully created: {out_file} ({W}x{H})")

if __name__ == '__main__':
    create_final_product_image()

