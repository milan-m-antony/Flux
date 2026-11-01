import os
from PIL import Image, ImageDraw, ImageFont

SCRIPT_DIR = os.path.abspath(os.path.dirname(__file__))
REPORT_DIR = os.path.abspath(os.path.join(SCRIPT_DIR, '..'))
ASSETS_DIR = os.path.join(REPORT_DIR, 'assets')
DIAGRAMS_DIR = os.path.join(ASSETS_DIR, 'diagrams')

def create_block_diagram():
    # Ultra crisp 300dpi diagram: 2400 x 1360
    W, H = 2400, 1360
    img = Image.new('RGB', (W, H), '#FFFFFF')
    draw = ImageDraw.Draw(img)

    # Fonts
    try:
        f_title = ImageFont.truetype('segoeuib.ttf', 44)
        f_col_title = ImageFont.truetype('segoeuib.ttf', 28)
        f_col_sub = ImageFont.truetype('segoeuib.ttf', 18)
        f_card_title = ImageFont.truetype('segoeuib.ttf', 22)
        f_card_sub = ImageFont.truetype('segoeui.ttf', 17)
        f_card_bold = ImageFont.truetype('segoeuib.ttf', 17)
        f_arrow = ImageFont.truetype('segoeuib.ttf', 15)
        f_tag = ImageFont.truetype('segoeuib.ttf', 15)
    except:
        f_title = ImageFont.load_default()
        f_col_title = f_title
        f_col_sub = f_title
        f_card_title = f_title
        f_card_sub = f_title
        f_card_bold = f_title
        f_arrow = f_title
        f_tag = f_title

    # Draw Title
    title_text = "FLUX SMART TELEMETRY & MONITORING SYSTEM — ARCHITECTURAL BLOCK DIAGRAM"
    bbox = draw.textbbox((0, 0), title_text, font=f_title)
    draw.text(((W - (bbox[2] - bbox[0])) // 2, 32), title_text, fill='#0F172A', font=f_title)

    # 4 Columns
    col_w = 510
    col_gap = 90
    left_margin = (W - (4 * col_w + 3 * col_gap)) // 2  # exactly centered
    top_y = 115
    col_h = 1200

    cols_info = [
        ("Physical Layer\n(Sensors & Hardware)", "#E0F2FE", "#0284C7", "#0369A1"),
        ("Processing & Connectivity\n(ESP32 DevKit V1)", "#FFEDD5", "#EA580C", "#C2410C"),
        ("Cloud & Notification Layer\n(Render & Supabase)", "#DCFCE7", "#16A34A", "#15803D"),
        ("User Interface & Action Layer\n(Android & Push)", "#F3E8FF", "#9333EA", "#7E22CE")
    ]

    col_boxes = []
    for i, (title, bg_col, border_col, header_col) in enumerate(cols_info):
        cx = left_margin + i * (col_w + col_gap)
        col_boxes.append((cx, top_y, cx + col_w, top_y + col_h))
        # Column container with rounded corners
        draw.rounded_rectangle([cx, top_y, cx + col_w, top_y + col_h], radius=24, fill=bg_col, outline=border_col, width=3)
        # Column header box
        head_h = 92
        draw.rounded_rectangle([cx + 12, top_y + 12, cx + col_w - 12, top_y + head_h], radius=16, fill=header_col)
        # Split header text
        lines = title.split('\n')
        tb1 = draw.textbbox((0, 0), lines[0], font=f_col_title)
        tb2 = draw.textbbox((0, 0), lines[1], font=f_col_sub)
        draw.text((cx + (col_w - (tb1[2]-tb1[0]))//2, top_y + 20), lines[0], fill='#FFFFFF', font=f_col_title)
        draw.text((cx + (col_w - (tb2[2]-tb2[0]))//2, top_y + 56), lines[1], fill='#F1F5F9', font=f_col_sub)

    # ================= COLUMN 1: PHYSICAL LAYER =================
    c1_x = col_boxes[0][0]
    p1_box = [c1_x + 16, top_y + 110, c1_x + col_w - 16, top_y + col_h - 18]
    draw.rounded_rectangle(p1_box, radius=18, fill='#FFFFFF', outline='#BAE6FD', width=2)

    # Sub-card Header
    draw.rounded_rectangle([p1_box[0]+12, p1_box[1]+10, p1_box[2]-12, p1_box[1]+48], radius=10, fill='#F0F9FF', outline='#0284C7', width=1)
    sub_title = "Thermal Acquisition & Local I/O Subsystem"
    st_b = draw.textbbox((0, 0), sub_title, font=f_card_bold)
    draw.text((p1_box[0] + (col_w - 32 - (st_b[2]-st_b[0]))//2, p1_box[1]+18), sub_title, fill='#0369A1', font=f_card_bold)

    # 1.1 DS18B20 Digital Probe
    c1_y1 = p1_box[1] + 60
    draw.rounded_rectangle([p1_box[0]+12, c1_y1, p1_box[2]-12, c1_y1 + 225], radius=14, fill='#F8FAFC', outline='#CBD5E1', width=1)
    probe_img_path = os.path.join(DIAGRAMS_DIR, 'probe_icon.png')
    if os.path.exists(probe_img_path):
        pr_im = Image.open(probe_img_path).convert('RGBA')
        pr_im = pr_im.resize((150, 150), Image.Resampling.LANCZOS)
        img.paste(pr_im, (p1_box[0] + 20, c1_y1 + 35), pr_im)
    draw.text((p1_box[0] + 185, c1_y1 + 20), "DS18B20 Digital Probe", fill='#0F172A', font=f_card_title)
    draw.text((p1_box[0] + 185, c1_y1 + 58), "• Waterproof Stainless Steel Probe", fill='#334155', font=f_card_sub)
    draw.text((p1_box[0] + 185, c1_y1 + 88), "• Dallas 1-Wire Protocol (GPIO 4)", fill='#334155', font=f_card_sub)
    draw.text((p1_box[0] + 185, c1_y1 + 118), "• 12-Bit High Precision (±0.5°C)", fill='#0369A1', font=f_card_bold)
    draw.text((p1_box[0] + 185, c1_y1 + 148), "• Operating Range: -55°C to +125°C", fill='#64748B', font=f_card_sub)
    draw.text((p1_box[0] + 185, c1_y1 + 178), "• 4.7kΩ External Pull-Up on Data Line", fill='#64748B', font=f_card_sub)

    # 1.2 SSD1306 OLED Display
    c1_y2 = c1_y1 + 242
    draw.rounded_rectangle([p1_box[0]+12, c1_y2, p1_box[2]-12, c1_y2 + 230], radius=14, fill='#F8FAFC', outline='#CBD5E1', width=1)
    oled_img_path = os.path.join(DIAGRAMS_DIR, 'oled_screen_icon.png')
    if os.path.exists(oled_img_path):
        ol_im = Image.open(oled_img_path).convert('RGBA')
        ol_im = ol_im.resize((155, 95), Image.Resampling.LANCZOS)
        img.paste(ol_im, (p1_box[0] + 18, c1_y2 + 65), ol_im)
    draw.text((p1_box[0] + 185, c1_y2 + 20), "SSD1306 0.96\" OLED", fill='#0F172A', font=f_card_title)
    draw.text((p1_box[0] + 185, c1_y2 + 58), "• 128x64 Monochrome Matrix", fill='#334155', font=f_card_sub)
    draw.text((p1_box[0] + 185, c1_y2 + 88), "• I2C Interface (SDA 21, SCL 22)", fill='#334155', font=f_card_sub)
    draw.text((p1_box[0] + 185, c1_y2 + 118), "• 8 Contextual UI Carousel Screens", fill='#0369A1', font=f_card_bold)
    draw.text((p1_box[0] + 185, c1_y2 + 148), "• Live Flame & Real-Time Waveform", fill='#64748B', font=f_card_sub)
    draw.text((p1_box[0] + 185, c1_y2 + 178), "• Low 25mA Operating Current", fill='#64748B', font=f_card_sub)

    # 1.3 Active-Low Piezo Buzzer
    c1_y3 = c1_y2 + 248
    draw.rounded_rectangle([p1_box[0]+12, c1_y3, p1_box[2]-12, c1_y3 + 225], radius=14, fill='#F8FAFC', outline='#CBD5E1', width=1)
    # Buzzer icon
    bx_x, bx_y = p1_box[0] + 28, c1_y3 + 45
    draw.ellipse([bx_x, bx_y, bx_x + 125, bx_y + 125], fill='#334155', outline='#0F172A', width=3)
    draw.ellipse([bx_x + 35, bx_y + 35, bx_x + 90, bx_y + 90], fill='#0F172A')
    draw.ellipse([bx_x + 48, bx_y + 48, bx_x + 77, bx_y + 77], fill='#E2E8F0')
    draw.rounded_rectangle([bx_x + 5, bx_y + 135, bx_x + 120, bx_y + 160], radius=4, fill='#FEF3C7')
    draw.text((bx_x + 15, bx_y + 138), "ACTIVE-LOW", fill='#D97706', font=f_tag)

    draw.text((p1_box[0] + 185, c1_y3 + 20), "Active-Low Piezo Buzzer", fill='#0F172A', font=f_card_title)
    draw.text((p1_box[0] + 185, c1_y3 + 58), "• Audible On-Site Warning Alarm", fill='#334155', font=f_card_sub)
    draw.text((p1_box[0] + 185, c1_y3 + 88), "• Driven via GPIO 25 Digital Out", fill='#334155', font=f_card_sub)
    draw.text((p1_box[0] + 185, c1_y3 + 118), "• Absolute Boot Silence Guarantee", fill='#D97706', font=f_card_bold)
    draw.text((p1_box[0] + 185, c1_y3 + 148), "• 3-Pip Breach Alarm Pattern", fill='#64748B', font=f_card_sub)
    draw.text((p1_box[0] + 185, c1_y3 + 178), "• Auto-Silencing Recovery Logic", fill='#16A34A', font=f_card_bold)

    # 1.4 Dual Tactile Buttons
    c1_y4 = c1_y3 + 242
    draw.rounded_rectangle([p1_box[0]+12, c1_y4, p1_box[2]-12, c1_y4 + 215], radius=14, fill='#F8FAFC', outline='#CBD5E1', width=1)
    btn_img_path = os.path.join(DIAGRAMS_DIR, 'buttons_icon.png')
    if os.path.exists(btn_img_path):
        bt_im = Image.open(btn_img_path).convert('RGBA')
        bt_im = bt_im.resize((145, 70), Image.Resampling.LANCZOS)
        img.paste(bt_im, (p1_box[0] + 20, c1_y4 + 65), bt_im)
    draw.text((p1_box[0] + 185, c1_y4 + 20), "Dual Tactile Push Buttons", fill='#0F172A', font=f_card_title)
    draw.text((p1_box[0] + 185, c1_y4 + 58), "• Button NEXT: GPIO 32", fill='#334155', font=f_card_sub)
    draw.text((p1_box[0] + 185, c1_y4 + 88), "• Button SELECT/ACTION: GPIO 33", fill='#334155', font=f_card_sub)
    draw.text((p1_box[0] + 185, c1_y4 + 118), "• Internal Pull-Up, Active-Low", fill='#0369A1', font=f_card_bold)
    draw.text((p1_box[0] + 185, c1_y4 + 148), "• Manual Alarm Mute & Profile Switch", fill='#64748B', font=f_card_sub)
    draw.text((p1_box[0] + 185, c1_y4 + 178), "• Debounced Hardware Interrupts", fill='#64748B', font=f_card_sub)


    # ================= COLUMN 2: PROCESSING & CONNECTIVITY =================
    c2_x = col_boxes[1][0]
    
    # 2.1 Regulated Power Supply
    p2_y1 = top_y + 110
    draw.rounded_rectangle([c2_x + 16, p2_y1, c2_x + col_w - 16, p2_y1 + 115], radius=16, fill='#FFFFFF', outline='#FED7AA', width=2)
    draw.rounded_rectangle([c2_x + 28, p2_y1 + 22, c2_x + 95, p2_y1 + 90], radius=10, fill='#EA580C')
    draw.text((c2_x + 42, p2_y1 + 42), "5V", fill='#FFFFFF', font=f_card_bold)
    draw.text((c2_x + 115, p2_y1 + 24), "Regulated Power Supply", fill='#0F172A', font=f_card_title)
    draw.text((c2_x + 115, p2_y1 + 60), "5V DC USB-C / Adapter → 3.3V LDO", fill='#334155', font=f_card_sub)
    draw.text((c2_x + 115, p2_y1 + 85), "Stable power rail for MCU and sensors", fill='#64748B', font=f_tag)

    # 2.2 Wireless Radios
    p2_y2 = p2_y1 + 130
    draw.rounded_rectangle([c2_x + 16, p2_y2, c2_x + col_w - 16, p2_y2 + 135], radius=16, fill='#FFFFFF', outline='#FED7AA', width=2)
    draw.rounded_rectangle([c2_x + 28, p2_y2 + 20, c2_x + 105, p2_y2 + 65], radius=8, fill='#0284C7')
    draw.text((c2_x + 36, p2_y2 + 30), "Wi-Fi", fill='#FFFFFF', font=f_card_bold)
    draw.rounded_rectangle([c2_x + 28, p2_y2 + 75, c2_x + 105, p2_y2 + 120], radius=8, fill='#2563EB')
    draw.text((c2_x + 42, p2_y2 + 85), "BLE", fill='#FFFFFF', font=f_card_bold)
    draw.text((c2_x + 125, p2_y2 + 22), "Dual Wireless Radios", fill='#0F172A', font=f_card_title)
    draw.text((c2_x + 125, p2_y2 + 58), "• Wi-Fi 802.11 b/g/n (Cloud Post)", fill='#334155', font=f_card_sub)
    draw.text((c2_x + 125, p2_y2 + 88), "• BLE 4.2 GATT Telemetry (1Hz)", fill='#2563EB', font=f_card_bold)

    # 2.3 ESP32 Microcontroller Core
    p2_y3 = p2_y2 + 150
    draw.rounded_rectangle([c2_x + 16, p2_y3, c2_x + col_w - 16, p2_y3 + 430], radius=18, fill='#FFFFFF', outline='#EA580C', width=3)
    esp_img_path = os.path.join(DIAGRAMS_DIR, 'esp32_chip_icon.png')
    if os.path.exists(esp_img_path):
        esp_im = Image.open(esp_img_path).convert('RGBA')
        esp_im = esp_im.resize((135, 250), Image.Resampling.LANCZOS)
        img.paste(esp_im, (c2_x + 26, p2_y3 + 80), esp_im)
    draw.text((c2_x + 175, p2_y3 + 22), "ESP32 DevKit V1 Core", fill='#0F172A', font=f_card_title)
    draw.text((c2_x + 175, p2_y3 + 60), "• Dual-Core Xtensa LX6 240MHz", fill='#334155', font=f_card_sub)
    draw.text((c2_x + 175, p2_y3 + 92), "• FreeRTOS Task Scheduling", fill='#C2410C', font=f_card_bold)
    draw.text((c2_x + 175, p2_y3 + 124), "• SensorManager (12-Bit CRC Check)", fill='#334155', font=f_card_sub)
    draw.text((c2_x + 175, p2_y3 + 156), "• DisplayManager (8-Screen OLED)", fill='#334155', font=f_card_sub)
    draw.text((c2_x + 175, p2_y3 + 188), "• BuzzerManager (Active-Low Mute)", fill='#334155', font=f_card_sub)
    draw.text((c2_x + 175, p2_y3 + 220), "• BluetoothManager (1Hz GATT)", fill='#2563EB', font=f_card_bold)
    draw.text((c2_x + 175, p2_y3 + 252), "• StorageManager (NVS Preferences)", fill='#334155', font=f_card_sub)
    draw.text((c2_x + 175, p2_y3 + 284), "• Wi-Fi AP & Station Provisioning", fill='#334155', font=f_card_sub)
    draw.text((c2_x + 175, p2_y3 + 325), "520KB SRAM • 4MB Flash Memory", fill='#64748B', font=f_card_bold)
    draw.text((c2_x + 175, p2_y3 + 355), "Hardware CRC Data Integrity Check", fill='#16A34A', font=f_card_bold)

    # 2.4 Autonomous Safety Logic
    p2_y4 = p2_y3 + 445
    draw.rounded_rectangle([c2_x + 16, p2_y4, c2_x + col_w - 16, p2_y4 + 175], radius=16, fill='#FFFFFF', outline='#FED7AA', width=2)
    draw.text((c2_x + 30, p2_y4 + 18), "Autonomous Threshold Logic", fill='#0F172A', font=f_card_title)
    draw.text((c2_x + 30, p2_y4 + 55), "• High Safe Threshold: > 28.0°C (Breach)", fill='#DC2626', font=f_card_bold)
    draw.text((c2_x + 30, p2_y4 + 85), "• Low Safe Threshold: < 18.0°C (Breach)", fill='#2563EB', font=f_card_bold)
    draw.text((c2_x + 30, p2_y4 + 115), "• Immediate On-Site Acoustic Alarm Trigger", fill='#B45309', font=f_card_bold)
    draw.text((c2_x + 30, p2_y4 + 142), "• Auto-Recovery Silence upon Safe Re-entry", fill='#16A34A', font=f_card_bold)

    # 2.5 Custom Enclosure Base
    p2_y5 = p2_y4 + 190
    draw.rounded_rectangle([c2_x + 16, p2_y5, c2_x + col_w - 16, p2_y5 + 105], radius=14, fill='#FFF7ED', outline='#FDBA74', width=2)
    draw.text((c2_x + 30, p2_y5 + 18), "Custom 3D-Printed Wall-Mount Case", fill='#9A3412', font=f_card_bold)
    draw.text((c2_x + 30, p2_y5 + 50), "Flame-retardant PETG enclosure with probe grommet,", fill='#431407', font=f_card_sub)
    draw.text((c2_x + 30, p2_y5 + 75), "OLED display bezel, and tactile button cutouts", fill='#431407', font=f_card_sub)


    # ================= COLUMN 3: CLOUD & NOTIFICATION LAYER =================
    c3_x = col_boxes[2][0]

    # 3.1 Render Cloud Docker Backend
    p3_y1 = top_y + 110
    draw.rounded_rectangle([c3_x + 16, p3_y1, c3_x + col_w - 16, p3_y1 + 195], radius=16, fill='#FFFFFF', outline='#86EFAC', width=2)
    draw.rounded_rectangle([c3_x + 28, p3_y1 + 22, c3_x + 115, p3_y1 + 75], radius=10, fill='#0284C7')
    draw.text((c3_x + 36, p3_y1 + 38), "DOCKER", fill='#FFFFFF', font=f_tag)
    draw.text((c3_x + 130, p3_y1 + 22), "Render Cloud Backend", fill='#0F172A', font=f_card_title)
    draw.text((c3_x + 130, p3_y1 + 58), "• Node.js 22 TypeScript API Microservice", fill='#334155', font=f_card_sub)
    draw.text((c3_x + 130, p3_y1 + 88), "• Express REST Ingestion Endpoints", fill='#334155', font=f_card_sub)
    draw.text((c3_x + 130, p3_y1 + 118), "• Payload Validation & Rate-Limiting", fill='#15803D', font=f_card_bold)
    draw.text((c3_x + 28, p3_y1 + 155), "Live URL: https://flux-telemetry-backend-glwn.onrender.com", fill='#64748B', font=f_tag)

    # 3.2 Supabase PostgreSQL Cloud DB
    p3_y2 = p3_y1 + 210
    draw.rounded_rectangle([c3_x + 16, p3_y2, c3_x + col_w - 16, p3_y2 + 235], radius=16, fill='#FFFFFF', outline='#86EFAC', width=2)
    draw.rounded_rectangle([c3_x + 28, p3_y2 + 25, c3_x + 115, p3_y2 + 82], radius=10, fill='#16A34A')
    draw.text((c3_x + 38, p3_y2 + 42), "PSQL", fill='#FFFFFF', font=f_card_bold)
    draw.text((c3_x + 130, p3_y2 + 22), "Supabase Cloud Database", fill='#0F172A', font=f_card_title)
    draw.text((c3_x + 130, p3_y2 + 60), "• PostgreSQL Relational Persistence", fill='#334155', font=f_card_sub)
    draw.text((c3_x + 130, p3_y2 + 90), "• Tables: telemetry, alerts, state", fill='#334155', font=f_card_sub)
    draw.text((c3_x + 130, p3_y2 + 120), "• Row Level Security (RLS) Policies", fill='#15803D', font=f_card_bold)
    draw.text((c3_x + 130, p3_y2 + 150), "• Realtime Replication Engine", fill='#334155', font=f_card_sub)
    draw.text((c3_x + 130, p3_y2 + 180), "• 30-Day Historical Data Pruning", fill='#64748B', font=f_tag)
    draw.text((c3_x + 28, p3_y2 + 205), "Under 7% Supabase free tier resource utilization", fill='#64748B', font=f_tag)

    # 3.3 pg_net Asynchronous Webhook
    p3_y3 = p3_y2 + 250
    draw.rounded_rectangle([c3_x + 16, p3_y3, c3_x + col_w - 16, p3_y3 + 145], radius=16, fill='#FFFFFF', outline='#86EFAC', width=2)
    draw.text((c3_x + 30, p3_y3 + 20), "pg_net Webhook Trigger", fill='#0F172A', font=f_card_title)
    draw.text((c3_x + 30, p3_y3 + 58), "• Database Trigger on INSERT into alert_events", fill='#334155', font=f_card_sub)
    draw.text((c3_x + 30, p3_y3 + 88), "• Non-blocking async HTTP POST to Edge Function", fill='#15803D', font=f_card_bold)
    draw.text((c3_x + 30, p3_y3 + 115), "• Zero database query blocking during network delays", fill='#64748B', font=f_tag)

    # 3.4 Supabase Edge Function
    p3_y4 = p3_y3 + 160
    draw.rounded_rectangle([c3_x + 16, p3_y4, c3_x + col_w - 16, p3_y4 + 195], radius=16, fill='#FFFFFF', outline='#86EFAC', width=2)
    draw.rounded_rectangle([c3_x + 28, p3_y4 + 25, c3_x + 115, p3_y4 + 75], radius=10, fill='#7C3AED')
    draw.text((c3_x + 40, p3_y4 + 40), "EDGE", fill='#FFFFFF', font=f_tag)
    draw.text((c3_x + 130, p3_y4 + 22), "flux-alert-evaluator", fill='#0F172A', font=f_card_title)
    draw.text((c3_x + 130, p3_y4 + 58), "• Serverless Deno TypeScript Runtime", fill='#334155', font=f_card_sub)
    draw.text((c3_x + 130, p3_y4 + 88), "• 15-Minute Anti-Flap Hysteresis Window", fill='#C2410C', font=f_card_bold)
    draw.text((c3_x + 130, p3_y4 + 118), "• Isolates Private OneSignal REST API Keys", fill='#15803D', font=f_card_bold)
    draw.text((c3_x + 130, p3_y4 + 148), "• Payload Validation & Error Handling", fill='#64748B', font=f_card_sub)
    draw.text((c3_x + 28, p3_y4 + 172), "Suppresses repeated alerts during borderline thermal fluctuations", fill='#64748B', font=f_tag)

    # 3.5 OneSignal Push Service
    p3_y5 = p3_y4 + 210
    draw.rounded_rectangle([c3_x + 16, p3_y5, c3_x + col_w - 16, p3_y5 + 160], radius=16, fill='#FFFFFF', outline='#EA580C', width=2)
    draw.rounded_rectangle([c3_x + 28, p3_y5 + 25, c3_x + 120, p3_y5 + 75], radius=10, fill='#EA580C')
    draw.text((c3_x + 36, p3_y5 + 40), "1SIGNAL", fill='#FFFFFF', font=f_tag)
    draw.text((c3_x + 135, p3_y5 + 22), "OneSignal Push Pipeline", fill='#0F172A', font=f_card_title)
    draw.text((c3_x + 135, p3_y5 + 58), "• Enterprise REST API v1 Notification Pipeline", fill='#334155', font=f_card_sub)
    draw.text((c3_x + 135, p3_y5 + 88), "• Sub-1.2s Delivery Latency to User Phone", fill='#EA580C', font=f_card_bold)
    draw.text((c3_x + 135, p3_y5 + 118), "• Targeted Segment & External ID Delivery", fill='#64748B', font=f_card_sub)


    # ================= COLUMN 4: USER INTERFACE & ACTION LAYER =================
    c4_x = col_boxes[3][0]

    # 4.1 Android Companion App
    p4_y1 = top_y + 110
    draw.rounded_rectangle([c4_x + 16, p4_y1, c4_x + col_w - 16, p4_y1 + 450], radius=18, fill='#FFFFFF', outline='#C4B5FD', width=2)
    phone_path = os.path.join(DIAGRAMS_DIR, 'phone_mockup.png')
    if os.path.exists(phone_path):
        ph_im = Image.open(phone_path).convert('RGBA')
        ph_im = ph_im.resize((175, 368), Image.Resampling.LANCZOS)
        img.paste(ph_im, (c4_x + 28, p4_y1 + 42), ph_im)
    draw.text((c4_x + 220, p4_y1 + 30), "Android Companion App", fill='#0F172A', font=f_card_title)
    draw.text((c4_x + 220, p4_y1 + 75), "• Modern Jetpack Compose UI", fill='#334155', font=f_card_sub)
    draw.text((c4_x + 220, p4_y1 + 108), "• Circular Thermal Gauge", fill='#7E22CE', font=f_card_bold)
    draw.text((c4_x + 220, p4_y1 + 140), "• Dynamic Units (°C / °F)", fill='#334155', font=f_card_sub)
    draw.text((c4_x + 220, p4_y1 + 172), "• Offline Room Database Buffer", fill='#15803D', font=f_card_bold)
    draw.text((c4_x + 220, p4_y1 + 204), "  (Zero Mock Data Policy)", fill='#64748B', font=f_tag)
    draw.text((c4_x + 220, p4_y1 + 236), "• Wi-Fi Quick Sheet Provisioning", fill='#334155', font=f_card_sub)
    draw.text((c4_x + 220, p4_y1 + 268), "• BLE Live 1Hz Telemetry Sync", fill='#2563EB', font=f_card_bold)
    draw.text((c4_x + 220, p4_y1 + 300), "• OneSignal SDK Push Integration", fill='#EA580C', font=f_card_bold)
    draw.text((c4_x + 220, p4_y1 + 334), "• Hardware Health Diagnostics", fill='#334155', font=f_card_sub)
    draw.text((c4_x + 220, p4_y1 + 375), "Target: Android 14/15 Device (RZCX71Q65BB)", fill='#64748B', font=f_tag)

    # 4.2 Push Notification Banner
    p4_y2 = p4_y1 + 465
    draw.rounded_rectangle([c4_x + 16, p4_y2, c4_x + col_w - 16, p4_y2 + 195], radius=16, fill='#FFFFFF', outline='#F87171', width=2)
    draw.rounded_rectangle([c4_x + 28, p4_y2 + 20, c4_x + 75, p4_y2 + 65], radius=10, fill='#EF4444')
    draw.text((c4_x + 48, p4_y2 + 28), "!", fill='#FFFFFF', font=f_card_bold)
    draw.text((c4_x + 85, p4_y2 + 25), "Flux Thermal Alert • Just now", fill='#991B1B', font=f_card_bold)
    draw.text((c4_x + 30, p4_y2 + 75), "[ALERT] HIGH TEMPERATURE BREACH!", fill='#B91C1C', font=f_card_bold)
    draw.text((c4_x + 30, p4_y2 + 108), "Probe: 28.5°C exceeds high safety threshold (28.0°C).", fill='#334155', font=f_card_sub)
    draw.text((c4_x + 30, p4_y2 + 138), "Tap notification to view live telemetry graph & device history.", fill='#64748B', font=f_card_sub)
    draw.text((c4_x + 30, p4_y2 + 165), "High-priority heads-up channel with sound & vibration", fill='#B91C1C', font=f_tag)

    # 4.3 On-Site Acoustic Alarm Burst
    p4_y3 = p4_y2 + 210
    draw.rounded_rectangle([c4_x + 16, p4_y3, c4_x + col_w - 16, p4_y3 + 145], radius=16, fill='#FFFBEB', outline='#FCD34D', width=2)
    draw.text((c4_x + 30, p4_y3 + 20), "On-Site Acoustic Alarm", fill='#92400E', font=f_card_title)
    draw.text((c4_x + 30, p4_y3 + 58), "• Continuous 3-Pip Audible Alarm Burst", fill='#B45309', font=f_card_bold)
    draw.text((c4_x + 30, p4_y3 + 88), "• Operates fully offline without internet or cloud connection", fill='#334155', font=f_card_sub)
    draw.text((c4_x + 30, p4_y3 + 115), "• Immediate physical safety warning for on-site operators", fill='#92400E', font=f_tag)

    # 4.4 Cloud Web Analytics
    p4_y4 = p4_y3 + 160
    draw.rounded_rectangle([c4_x + 16, p4_y4, c4_x + col_w - 16, p4_y4 + 110], radius=14, fill='#F8FAFC', outline='#CBD5E1', width=2)
    draw.text((c4_x + 30, p4_y4 + 18), "Cloud Management & Historical Charts", fill='#0F172A', font=f_card_bold)
    draw.text((c4_x + 30, p4_y4 + 48), "REST API endpoints for real-time telemetry streaming,", fill='#475569', font=f_card_sub)
    draw.text((c4_x + 30, p4_y4 + 75), "historical data export, and multi-sensor dashboard views", fill='#475569', font=f_card_sub)


    # ================= CONNECTING ARROWS =================
    arrow_color = '#DC2626'
    
    def draw_thick_arrow(x1, y1, x2, y2, label="", label_pos=(0, 0), label_above=True):
        draw.line([x1, y1, x2, y2], fill=arrow_color, width=5)
        # Arrowhead pointing right
        head_len = 16
        draw.polygon([(x2, y2), (x2 - head_len, y2 - 9), (x2 - head_len, y2 + 9)], fill=arrow_color)
        if label:
            tb = draw.textbbox((0, 0), label, font=f_arrow)
            lw = tb[2] - tb[0]
            lh = tb[3] - tb[1]
            lx = (x1 + x2)//2 - lw//2 + label_pos[0]
            ly = y1 - lh - 12 if label_above else y1 + 8
            ly += label_pos[1]
            draw.rounded_rectangle([lx - 5, ly - 3, lx + lw + 5, ly + lh + 3], radius=6, fill='#FFFFFF', outline=arrow_color, width=1)
            draw.text((lx, ly), label, fill=arrow_color, font=f_arrow)

    # 1. Physical Layer -> Processing (ESP32)
    g1_x1 = col_boxes[0][2]
    g1_x2 = col_boxes[1][0]
    draw_thick_arrow(g1_x1, top_y + 450, g1_x2, top_y + 450, "1-Wire/I2C/GPIO")

    # 2. Processing (ESP32) -> Cloud Layer (Wi-Fi HTTPS)
    g2_x1 = col_boxes[1][2]
    g2_x2 = col_boxes[2][0]
    draw_thick_arrow(g2_x1, top_y + 190, g2_x2, top_y + 190, "Wi-Fi HTTPS")

    # 3. Processing (ESP32) -> Cloud Layer (BLE / Telemetry Ingestion)
    draw_thick_arrow(g2_x1, top_y + 350, g2_x2, top_y + 350, "Direct REST")

    # 4. Cloud Layer -> User Interface (OneSignal Push)
    g3_x1 = col_boxes[2][2]
    g3_x2 = col_boxes[3][0]
    draw_thick_arrow(g3_x1, top_y + 560, g3_x2, top_y + 560, "Push Notification")

    # 5. Direct Buzzer Arrow
    draw_thick_arrow(g3_x1, top_y + 920, g3_x2, top_y + 920, "Local Alarm", label_above=False)

    out_file = os.path.join(DIAGRAMS_DIR, 'img_block_diagram.png')
    img.save(out_file, dpi=(300, 300), quality=95)
    print(f"[+] Block diagram successfully created: {out_file} ({W}x{H})")

if __name__ == '__main__':
    create_block_diagram()
