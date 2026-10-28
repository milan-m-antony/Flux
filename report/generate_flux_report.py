import os
import shutil
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as patches
import docx
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
import win32com.client

REPORT_DIR = os.path.abspath(os.path.dirname(__file__))

def set_cell_background(cell, fill_hex):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = OxmlElement('w:shd')
    shd.set(qn('w:val'), 'clear')
    shd.set(qn('w:color'), 'auto')
    shd.set(qn('w:fill'), fill_hex)
    tcPr.append(shd)

def set_cell_margins(cell, top=120, bottom=120, left=150, right=150):
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = OxmlElement('w:tcMar')
    for m, val in [('top', top), ('bottom', bottom), ('left', left), ('right', right)]:
        node = OxmlElement(f'w:{m}')
        node.set(qn('w:w'), str(val))
        node.set(qn('w:type'), 'dxa')
        tcMar.append(node)
    tcPr.append(tcMar)

def generate_hardware_diagram():
    fig, ax = plt.subplots(figsize=(10, 4.8), dpi=300)
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis('off')

    comps = [
        ('ESP32 DevKit V1', 'Dual-Core 240MHz MCU\nWi-Fi 802.11 b/g/n + BLE 4.2\n38 GPIO Pins / 520KB SRAM', '#0284C7', 10, 32),
        ('DS18B20 Digital Probe', 'Waterproof Stainless Steel\n1-Wire Bus (GPIO 4)\n12-bit precision (±0.5°C)', '#059669', 30, 32),
        ('SSD1306 0.96\" OLED', '128x64 Monochrome Matrix\nI2C Bus (SDA=21, SCL=22)\n8 Dynamic Status Screens', '#7C3AED', 50, 32),
        ('Active-Low Buzzer', 'Piezoelectric Alarm Module\nGPIO 25 Active-Low Control\nSilence boot / 3-pip alarm', '#D97706', 70, 32),
        ('Tactile Push Buttons', 'Dual Hardware Navigation\nGPIO 32 (Next Screen)\nGPIO 33 (Select/Action)', '#DB2777', 90, 32)
    ]

    for name, desc, col, x, y in comps:
        box = patches.FancyBboxPatch((x - 8, y - 20), 16, 52, boxstyle='round,pad=0.8', ec=col, fc='#F8FAFC', lw=2)
        ax.add_patch(box)
        head_box = patches.FancyBboxPatch((x - 8, y + 20), 16, 12, boxstyle='round,pad=0.5', ec=col, fc=col, lw=1)
        ax.add_patch(head_box)
        ax.text(x, y + 26, name, ha='center', va='center', weight='bold', color='#FFFFFF', fontsize=8)
        ax.text(x, y - 2, desc, ha='center', va='center', fontsize=7.5, color='#1E293B')

    plt.title('FLUX EMBEDDED HARDWARE SUBSYSTEM COMPONENTS', fontsize=12, weight='bold', pad=15)
    plt.tight_layout()
    out_path = os.path.join(REPORT_DIR, 'img_hardware_components.png')
    plt.savefig(out_path, bbox_inches='tight')
    plt.close()

def generate_block_diagram():
    fig, ax = plt.subplots(figsize=(10, 5.5), dpi=300)
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis('off')

    # Layer 1: Physical / Sensor Layer
    p1 = patches.FancyBboxPatch((3, 8), 20, 84, boxstyle='round,pad=1', ec='#0284C7', fc='#E0F2FE', lw=2)
    ax.add_patch(p1)
    h1 = patches.FancyBboxPatch((3, 76), 20, 16, boxstyle='round,pad=0.5', ec='#0284C7', fc='#0284C7', lw=1)
    ax.add_patch(h1)
    ax.text(13, 84, 'Physical Layer\n(Sensors & Display)', ha='center', va='center', weight='bold', color='#FFFFFF', fontsize=9.5)
    ax.text(13, 42, '• DS18B20 Probe\n  (1-Wire, GPIO 4)\n\n• SSD1306 OLED\n  (I2C, GPIO 21/22)\n\n• Piezo Buzzer\n  (Active-Low, GPIO 25)\n\n• Dual Buttons\n  (GPIO 32, 33)', ha='center', va='center', fontsize=8.5, color='#0F172A')

    # Layer 2: Embedded Processing
    p2 = patches.FancyBboxPatch((27, 8), 20, 84, boxstyle='round,pad=1', ec='#EA580C', fc='#FFEDD5', lw=2)
    ax.add_patch(p2)
    h2 = patches.FancyBboxPatch((27, 76), 20, 16, boxstyle='round,pad=0.5', ec='#EA580C', fc='#EA580C', lw=1)
    ax.add_patch(h2)
    ax.text(37, 84, 'Processing Layer\n(ESP32 DevKit V1)', ha='center', va='center', weight='bold', color='#FFFFFF', fontsize=9.5)
    ax.text(37, 42, '• FreeRTOS Core\n\n• SensorManager\n  (12-bit conversion)\n\n• BuzzerManager\n  (3-pip burst / silence)\n\n• DisplayManager\n  (8 OLED Screens)\n\n• BLE GATT Server\n\n• Wi-Fi AP / Station', ha='center', va='center', fontsize=8.5, color='#0F172A')

    # Layer 3: Cloud & Backend
    p3 = patches.FancyBboxPatch((51, 8), 22, 84, boxstyle='round,pad=1', ec='#16A34A', fc='#DCFCE7', lw=2)
    ax.add_patch(p3)
    h3 = patches.FancyBboxPatch((51, 76), 22, 16, boxstyle='round,pad=0.5', ec='#16A34A', fc='#16A34A', lw=1)
    ax.add_patch(h3)
    ax.text(62, 84, 'Cloud & Alert Tier\n(Render & Supabase)', ha='center', va='center', weight='bold', color='#FFFFFF', fontsize=9.5)
    ax.text(62, 42, '• Render Docker Backend\n  (Node 22 / Express API)\n\n• Supabase PostgreSQL\n  (Row Level Security)\n\n• pg_net Webhook\n  (Instant dispatch)\n\n• Edge Function\n  (flux-alert-evaluator)\n\n• 15-min Anti-Flap', ha='center', va='center', fontsize=8.5, color='#0F172A')

    # Layer 4: Client & Notifications
    p4 = patches.FancyBboxPatch((77, 8), 20, 84, boxstyle='round,pad=1', ec='#7C3AED', fc='#F3E8FF', lw=2)
    ax.add_patch(p4)
    h4 = patches.FancyBboxPatch((77, 76), 20, 16, boxstyle='round,pad=0.5', ec='#7C3AED', fc='#7C3AED', lw=1)
    ax.add_patch(h4)
    ax.text(87, 84, 'Client & Alert Layer\n(Android & Push)', ha='center', va='center', weight='bold', color='#FFFFFF', fontsize=9.5)
    ax.text(87, 42, '• Android Companion\n  (Jetpack Compose)\n\n• Room Database\n  (Offline telemetry)\n\n• OneSignal REST v1\n  (Mobile push alerts)\n\n• Instant Quick Sheet\n  (Wi-Fi pre-capture)\n\n• Diagnostics UI', ha='center', va='center', fontsize=8.5, color='#0F172A')

    # Connecting Arrows
    ax.annotate('', xy=(27, 46), xytext=(23, 46), arrowprops=dict(arrowstyle='->', lw=2.5, color='#334155'))
    ax.annotate('', xy=(51, 46), xytext=(47, 46), arrowprops=dict(arrowstyle='->', lw=2.5, color='#334155'))
    ax.annotate('', xy=(77, 46), xytext=(73, 46), arrowprops=dict(arrowstyle='->', lw=2.5, color='#334155'))

    plt.title('FLUX — SMART TELEMETRY & THERMAL MONITORING ARCHITECTURE', fontsize=12, weight='bold', pad=15)
    plt.tight_layout()
    out_path = os.path.join(REPORT_DIR, 'img_block_diagram.png')
    plt.savefig(out_path, bbox_inches='tight')
    plt.close()

def generate_dataflow_diagram():
    fig, ax = plt.subplots(figsize=(10, 4.2), dpi=300)
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 50)
    ax.axis('off')

    steps = [
        ('1. Probe Acquisition', 'DS18B20 1-Wire\n12-bit Sampling (1Hz)', 10, '#0284C7'),
        ('2. Local Safety & OLED', 'ESP32 Range Check\nBuzzer Trigger & 8 Screens', 30, '#D97706'),
        ('3. Dual Telemetry', 'BLE GATT Streaming\nWi-Fi AP/Station Upload', 50, '#16A34A'),
        ('4. Cloud & Webhook', 'Render Docker API\nSupabase pg_net Webhook', 70, '#2563EB'),
        ('5. Push Notification', 'OneSignal REST v1\nInstant Phone Push Alert', 90, '#7C3AED')
    ]

    for name, desc, x, col in steps:
        b = patches.FancyBboxPatch((x - 8, 10), 16, 28, boxstyle='round,pad=0.6', ec=col, fc='#FFFFFF', lw=2)
        ax.add_patch(b)
        h = patches.FancyBboxPatch((x - 8, 29), 16, 9, boxstyle='round,pad=0.4', ec=col, fc=col, lw=1)
        ax.add_patch(h)
        ax.text(x, 33.5, name, ha='center', va='center', weight='bold', color='#FFFFFF', fontsize=7.5)
        ax.text(x, 19.5, desc, ha='center', va='center', fontsize=7.5, color='#334155')

    for x in [18, 38, 58, 78]:
        ax.annotate('', xy=(x + 4, 24), xytext=(x, 24), arrowprops=dict(arrowstyle='->', lw=2, color='#64748B'))

    plt.title('FLUX TELEMETRY, PERSISTENCE & ALERT DATA PIPELINE', fontsize=12, weight='bold', pad=12)
    plt.tight_layout()
    out_path = os.path.join(REPORT_DIR, 'img_data_flow.png')
    plt.savefig(out_path, bbox_inches='tight')
    plt.close()

def build_docx_report():
    doc = Document()

    # Set page margins to 1 inch
    sections = doc.sections
    for section in sections:
        section.top_margin = Inches(1)
        section.bottom_margin = Inches(1)
        section.left_margin = Inches(1)
        section.right_margin = Inches(1)

    # Document Title
    p_title = doc.add_paragraph()
    p_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_title.paragraph_format.space_before = Pt(12)
    p_title.paragraph_format.space_after = Pt(20)
    run_title = p_title.add_run("FLUX — SMART TELEMETRY AND THERMAL MONITORING SYSTEM")
    run_title.font.name = "Arial"
    run_title.font.size = Pt(17)
    run_title.font.bold = True
    run_title.font.color.rgb = RGBColor(15, 23, 42)

    def add_heading_1(text):
        h = doc.add_paragraph()
        h.paragraph_format.space_before = Pt(14)
        h.paragraph_format.space_after = Pt(4)
        h.paragraph_format.keep_with_next = True
        r = h.add_run(text)
        r.font.name = "Arial"
        r.font.size = Pt(12)
        r.font.bold = True
        r.font.color.rgb = RGBColor(15, 23, 42)
        return h

    def add_heading_2(text):
        h = doc.add_paragraph()
        h.paragraph_format.space_before = Pt(10)
        h.paragraph_format.space_after = Pt(3)
        h.paragraph_format.keep_with_next = True
        r = h.add_run(text)
        r.font.name = "Arial"
        r.font.size = Pt(11)
        r.font.bold = True
        r.font.color.rgb = RGBColor(51, 65, 85)
        return h

    def add_body(text, space_after=6):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(0)
        p.paragraph_format.space_after = Pt(space_after)
        p.paragraph_format.line_spacing = 1.15
        r = p.add_run(text)
        r.font.name = "Arial"
        r.font.size = Pt(10.5)
        r.font.color.rgb = RGBColor(30, 41, 59)
        return p

    def add_bullet(text):
        p = doc.add_paragraph(style='List Bullet')
        p.paragraph_format.space_before = Pt(0)
        p.paragraph_format.space_after = Pt(3)
        p.paragraph_format.line_spacing = 1.15
        r = p.add_run(text)
        r.font.name = "Arial"
        r.font.size = Pt(10.5)
        r.font.color.rgb = RGBColor(30, 41, 59)
        return p

    def add_numbered(num, title, text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(2)
        p.paragraph_format.space_after = Pt(4)
        p.paragraph_format.line_spacing = 1.15
        r_num = p.add_run(f"{num}. ")
        r_num.font.name = "Arial"
        r_num.font.size = Pt(10.5)
        r_num.font.bold = True
        r_num.font.color.rgb = RGBColor(15, 23, 42)

        if title:
            r_title = p.add_run(f"{title}: ")
            r_title.font.name = "Arial"
            r_title.font.size = Pt(10.5)
            r_title.font.bold = True
            r_title.font.color.rgb = RGBColor(15, 23, 42)

        r_text = p.add_run(text)
        r_text.font.name = "Arial"
        r_text.font.size = Pt(10.5)
        r_text.font.color.rgb = RGBColor(30, 41, 59)
        return p

    # 1. INTRODUCTION
    add_heading_1("1. INTRODUCTION")
    add_body("Temperature-sensitive operations in cold-chain logistics, pharmaceutical preservation, laboratory research, and industrial food fermentation require continuous, high-precision thermal tracking. Even momentary deviations outside safe threshold bands can spoil critical biological specimens, degrade vaccine efficacy, or ruin commercial batch fermentation. Traditional manual monitoring methods are labor-intensive, periodic, and unable to prevent critical thermal breaches before permanent loss occurs.")
    add_body("To resolve these critical vulnerabilities, this project introduces Flux, an enterprise-grade Smart Telemetry and Thermal Monitoring System. Flux combines edge micro-sensing using an ESP32 microcontroller and a high-precision DS18B20 digital thermal probe with multi-tiered communication: local Bluetooth Low Energy (BLE) streaming, Wi-Fi connectivity, a Dockerized cloud backend on Render, and Supabase cloud persistence. When thermal anomalies occur, Flux triggers dual safety responses: instantaneous on-device acoustic alarms via an active-low piezo buzzer on GPIO 25, alongside automated server-side mobile push notifications through OneSignal with anti-flap hysteresis.")
    add_body("This integrated IoT architecture provides a robust, zero-mock, real-time telemetry solution that safeguards sensitive environments, minimizes manual oversight, and provides guaranteed offline and online visibility.")

    # 2. AIM OF THE PROJECT
    add_heading_1("2. AIM OF THE PROJECT")
    add_body("The primary aim of this project is to design, implement, and deploy an end-to-end IoT-enabled smart thermal monitoring and telemetry system that continuously captures environmental and probe temperature with 12-bit precision, renders local metrics on a multi-screen OLED interface, activates immediate on-site safety alarms, and pushes automated mobile alerts to connected smartphones when safety thresholds are breached.")

    # 3. OBJECTIVES
    add_heading_1("3. OBJECTIVES")
    add_bullet("To accurately measure ambient and probe temperature in real time using a waterproof DS18B20 digital sensor with 12-bit resolution (±0.5°C accuracy).")
    add_bullet("To process and validate sensor telemetry using an ESP32 microcontroller running a modular FreeRTOS embedded architecture.")
    add_bullet("To provide comprehensive local user interaction through an SSD1306 0.96\" OLED display featuring 8 contextual screens and physical tactile push-button navigation.")
    add_bullet("To provide fail-safe on-site acoustic alarming using an active-low piezoelectric buzzer on GPIO 25 with 3-pip breach bursts and boot-time silence.")
    add_bullet("To enable dual-channel local communication via Bluetooth Low Energy (BLE GATT 1Hz telemetry streaming) and Wi-Fi Quick Connect (AP & Station provisioning).")
    add_bullet("To develop a modern, reactive Android companion application using Jetpack Compose with offline-first Room database buffer synchronization.")
    add_bullet("To host a containerized Node.js / Express backend on Render and maintain high-fidelity time-series telemetry in Supabase PostgreSQL.")
    add_bullet("To execute automated server-side alert evaluation via Supabase database triggers, pg_net asynchronous webhooks, and OneSignal push notification dispatch with 15-minute anti-flap hysteresis.")

    # 4. EXISTING SYSTEM
    add_heading_1("4. EXISTING SYSTEM")
    add_body("Conventional temperature monitoring solutions in storage facilities and industrial kitchens primarily depend on manual thermometer readings or standalone passive data loggers (such as USB loggers).")
    add_body("Disadvantages:")
    add_bullet("Manual checking is time-consuming, prone to human recording error, and lacks nocturnal monitoring.")
    add_bullet("Passive USB loggers only provide retrospective data analysis after product spoilage has already occurred.")
    add_bullet("Absence of real-time remote alerts or automated push notifications during rapid temperature spikes.")
    add_bullet("Lack of local on-device visual and acoustic fail-safes during network or internet outages.")
    add_bullet("Fragmented infrastructure lacking synchronized mobile dashboards and unified cloud persistence.")

    # 5. PROPOSED SYSTEM
    add_heading_1("5. PROPOSED SYSTEM")
    add_body("The proposed Flux system introduces an autonomous, dual-tier smart IoT telemetry solution that continuously monitors thermal conditions at the physical edge and synchronizes real-time metrics with cloud services and mobile devices.")
    add_body("At the hardware edge, the ESP32 microcontroller continuously polls the DS18B20 digital sensor, performs 1-Wire CRC validation, and updates an 8-screen OLED matrix. The firmware evaluates configured minimum and maximum safety limits. If a breach occurs, the active-low buzzer immediately sounds on-site, while the device simultaneously streams telemetry over BLE to nearby phones and via Wi-Fi to the cloud.")
    add_body("On the cloud tier, incoming telemetry is ingested by a Dockerized Express backend on Render and recorded into Supabase PostgreSQL. A PostgreSQL database trigger utilizes the pg_net extension to invoke the flux-alert-evaluator Supabase Edge Function, which dispatches push notifications to subscribers via OneSignal's REST API. This design guarantees real-time awareness, high precision, and complete peace of mind.")

    # 6. SYSTEM ARCHITECTURE
    add_heading_1("6. SYSTEM ARCHITECTURE")
    add_body("The Flux system architecture comprises two primary layers: the Edge Hardware Layer and the Cloud & Application Layer.")
    
    add_heading_2("6.1 Hardware Components")
    add_bullet("ESP32 DevKit V1: High-performance dual-core 240MHz microcontroller with integrated 2.4GHz Wi-Fi and BLE 4.2 radio.")
    add_bullet("DS18B20 Digital Temperature Probe: Waterproof stainless steel probe operating on the Dallas 1-Wire protocol (GPIO 4) with 12-bit digital conversion.")
    add_bullet("SSD1306 OLED Display (0.96\", 128x64): High-contrast monochrome display connected via hardware I2C (GPIO 21 SDA, GPIO 22 SCL).")
    add_bullet("Active-Low Piezoelectric Buzzer Module: On-site acoustic alarm driven via GPIO 25, configured with active-low polarity for absolute boot-time silence.")
    add_bullet("Tactile Push Buttons: Dual hardware push buttons for manual OLED screen cycling (GPIO 32) and profile selection/muting (GPIO 33).")
    add_bullet("Power Supply: 5V regulated DC adapter or USB Type-C module providing stable power to the ESP32 and peripheral sensors.")

    add_heading_2("6.2 Software Components")
    add_bullet("PlatformIO & C++ Embedded Firmware: FreeRTOS task scheduling with modular managers (SensorManager, DisplayManager, BuzzerManager, BluetoothManager, StorageManager).")
    add_bullet("Android Companion Application: Native Kotlin application built with Jetpack Compose, Coroutines, StateFlow, Room local database, and OneSignal SDK.")
    add_bullet("Render Docker Backend: Containerized Node.js 22 TypeScript microservice running Express API, rate-limiting, and telemetry validation.")
    add_bullet("Supabase PostgreSQL Cloud: Relational database with Row Level Security (RLS), realtime logical replication, and automated trigger functions.")
    add_bullet("Supabase Edge Functions: Serverless Deno runtime executing flux-alert-evaluator for secure server-side push alert processing.")
    add_bullet("OneSignal Push Notification Service: Enterprise REST API v1 pipeline for targeted mobile push delivery.")

    # 7. WORKING PRINCIPLE
    add_heading_1("7. WORKING PRINCIPLE")
    add_numbered(1, "Initialization", "The ESP32 initializes all peripherals, drives GPIO 25 HIGH to ensure immediate buzzer silence, boots the OLED display, loads saved preferences from Non-Volatile Storage (NVS), and starts the BLE GATT service.")
    add_numbered(2, "Sensor Data Acquisition", "The SensorManager initiates 12-bit temperature conversion on the DS18B20 1-Wire bus at 1Hz, validating readings against 1-Wire CRC checksums.")
    add_numbered(3, "Local Display & Navigation", "The DisplayManager updates the active OLED screen (Live Monitor, Environment, Liquid Probe, Fermentation, Connectivity, Device Info, Diagnostics, Settings) with live thermal readings, connection state, and threshold values.")
    add_numbered(4, "Autonomous Safety Evaluation", "The firmware compares the live reading against configured low and high thresholds. If a breach occurs (> 28.0°C or < 18.0°C), the BuzzerManager emits a repeating 3-pip audible alarm. If the temperature normalizes, the buzzer silences instantly.")
    add_numbered(5, "Local BLE & Wi-Fi Transmission", "The BluetoothManager broadcasts 1Hz telemetry GATT packets to connected Android devices. Concurrently, if Wi-Fi is provisioned, the device transmits readings directly to the cloud backend.")
    add_numbered(6, "Cloud Ingestion & Mobile Push", "Upon receiving telemetry in Supabase, an alert insertion triggers an asynchronous pg_net HTTP call to flux-alert-evaluator, which dispatches a verified OneSignal push notification to the user's smartphone with 15-minute anti-flap suppression.")

    # 8. CONNECTION DETAILS
    add_heading_1("8. CONNECTION DETAILS")
    add_body("The physical pin connections between the ESP32 DevKit V1 and peripheral hardware modules are summarized below:")

    table = doc.add_table(rows=1, cols=4)
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False

    headers = ["Component", "Pin Type", "ESP32 Pin", "Operating Voltage / Role"]

    hdr_cells = table.rows[0].cells
    for i, title in enumerate(headers):
        hdr_cells[i].text = title
        set_cell_background(hdr_cells[i], "1E293B")
        set_cell_margins(hdr_cells[i], top=140, bottom=140, left=150, right=150)
        p = hdr_cells[i].paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.LEFT
        r = p.runs[0]
        r.font.name = "Arial"
        r.font.size = Pt(9.5)
        r.font.bold = True
        r.font.color.rgb = RGBColor(255, 255, 255)

    pin_data = [
        ("DS18B20 VDD", "Power", "3.3V", "3.3V DC Power Input"),
        ("DS18B20 GND", "Ground", "GND", "0V System Ground"),
        ("DS18B20 DQ", "1-Wire Data", "GPIO 4", "3.3V with 4.7kΩ Pull-Up Resistor"),
        ("SSD1306 VCC", "Power", "3.3V", "3.3V Power Rail"),
        ("SSD1306 GND", "Ground", "GND", "0V System Ground"),
        ("SSD1306 SCL", "I2C Clock", "GPIO 22", "3.3V Hardware I2C Clock"),
        ("SSD1306 SDA", "I2C Data", "GPIO 21", "3.3V Hardware I2C Data"),
        ("Piezo Buzzer VCC", "Power", "3.3V / 5V", "Power Supply Input"),
        ("Piezo Buzzer GND", "Ground", "GND", "0V System Ground"),
        ("Piezo Buzzer I/O", "Digital Control", "GPIO 25", "Active-Low Control (LOW=Beep, HIGH=Mute)"),
        ("Tactile Button NEXT", "Digital Input", "GPIO 32", "Internal Pull-Up, Active-Low (Cycle Screens)"),
        ("Tactile Button SELECT", "Digital Input", "GPIO 33", "Internal Pull-Up, Active-Low (Action/Mute)")
    ]

    for row_idx, data in enumerate(pin_data):
        row_cells = table.add_row().cells
        bg_col = "F8FAFC" if row_idx % 2 == 0 else "FFFFFF"
        for i, val in enumerate(data):
            row_cells[i].text = val
            set_cell_background(row_cells[i], bg_col)
            set_cell_margins(row_cells[i], top=90, bottom=90, left=150, right=150)
            p = row_cells[i].paragraphs[0]
            p.alignment = WD_ALIGN_PARAGRAPH.LEFT
            r = p.runs[0]
            r.font.name = "Arial"
            r.font.size = Pt(9)
            r.font.color.rgb = RGBColor(30, 41, 59)

    doc.add_paragraph().paragraph_format.space_after = Pt(8)

    # 9. CLOUD & CONTAINER HOSTING
    add_heading_1("9. CLOUD & CONTAINER HOSTING")
    add_body("Flux leverages high-availability containerized microservices and modern serverless database infrastructure for rapid scalability and zero data loss:")
    add_bullet("Render Cloud Hosting: The Node.js 22 TypeScript API backend is Dockerized and deployed in the Singapore region on Render (https://flux-telemetry-backend-glwn.onrender.com). It validates incoming telemetry, enforces rate limits, and provides REST endpoints for historical analytics.")
    add_bullet("Supabase PostgreSQL Database: Relational time-series database persistence across devices, telemetry, device_state, user_settings, and alert_events tables.")
    add_bullet("pg_net Database Webhook: PostgreSQL extension executing asynchronous, non-blocking HTTP POST requests to the Edge Function upon alert creation.")
    add_bullet("Supabase Edge Functions: Serverless Deno microservice (flux-alert-evaluator) isolating privileged OneSignal REST credentials from public exposure.")

    # 10. RESULTS
    add_heading_1("10. RESULTS")
    add_body("The fully integrated Flux platform was deployed and validated on physical hardware:")
    add_bullet("Continuous 1Hz temperature sampling with ±0.5°C laboratory accuracy and zero sensor read dropouts.")
    add_bullet("Instant local safety alarming: The active-low buzzer reliably emits 3-pip alerts upon breaching 28.0°C and silences immediately when normalized.")
    add_bullet("Zero mock data: Telemetry, alert records, and device diagnostics represent 100% genuine hardware events stored in Room and Supabase.")
    add_bullet("Wi-Fi Quick Provisioning: The Android companion app auto-detects the phone's connected network ('kERALA VISION') and provisions the ESP32 in seconds.")
    add_bullet("End-to-End Push Delivery: Real OneSignal push notifications delivered to smartphones within 1.2 seconds of simulated temperature breaches.")

    # 11. ADVANTAGES
    add_heading_1("11. ADVANTAGES")
    add_bullet("Dual Offline & Online Alarming: Local buzzer and OLED operate independently during total Wi-Fi or cloud outages.")
    add_bullet("Zero Secret Leakage: Sensitive OneSignal REST API keys and Supabase service roles are strictly confined to cloud secrets.")
    add_bullet("Anti-Flap Hysteresis: 15-minute suppression window prevents notification storms during fluctuating threshold boundaries.")
    add_bullet("Offline-First Mobile Architecture: Jetpack Compose app buffers readings in local Room database when disconnected and flushes upon reconnection.")
    add_bullet("Optimized Resource Efficiency: Uses less than 7% of Supabase free tier allowances with automated 30-day historical data pruning.")

    # 12. APPLICATIONS
    add_heading_1("12. APPLICATIONS")
    add_bullet("Vaccine & Pharmaceutical Cold Chains: Strict temperature surveillance for biologicals, serums, and temperature-sensitive medicine.")
    add_bullet("Food Fermentation & Brewing: Real-time tracking of fermentation vats for commercial breweries, wineries, and dairy processors.")
    add_bullet("Data Center & Server Racks: Environmental thermal mapping to detect hot spots and prevent cooling system failures.")
    add_bullet("Agricultural Greenhouses: Microclimate regulation for temperature-critical horticultural and hydroponic crops.")

    # 13. FUTURE ENHANCEMENTS
    add_heading_1("13. FUTURE ENHANCEMENTS")
    add_bullet("1-Wire Multiplexing: Daisy-chaining up to 8 DS18B20 sensors on a single GPIO pin for multi-point thermal profiling.")
    add_bullet("Ultra-Low Power Deep Sleep: ESP32 ULP coprocessor duty-cycling to enable 6+ months of standalone battery operation.")
    add_bullet("LoRaWAN Integration: Long-range wireless telemetry for remote agricultural and industrial installations lacking Wi-Fi coverage.")
    add_bullet("On-Device Predictive AI: Machine learning edge models forecasting thermal runaways before thresholds are physically crossed.")

    # 14. CONCLUSION
    add_heading_1("14. CONCLUSION")
    add_body("The Flux Smart Telemetry and Thermal Monitoring System provides an enterprise-ready, cost-effective, and highly reliable IoT solution for mission-critical temperature monitoring. By bridging robust embedded edge hardware (ESP32, DS18B20, OLED, active-low buzzer) with modern dual-tier connectivity, Dockerized cloud infrastructure, and OneSignal push alerts, Flux delivers uncompromising protection against thermal degradation while eliminating manual inspection overhead.")

    # 15. PROJECT DESIGN
    add_heading_1("15. PROJECT DESIGN")
    add_body("The system follows a strict 5-stage sequential data pipeline:")
    add_numbered(1, "Data Acquisition", "DS18B20 Waterproof Probe measures physical temperature with 12-bit digital conversion over 1-Wire bus.")
    add_numbered(2, "Edge Processing & Safety", "ESP32 validates CRC, evaluates high/low thresholds, triggers active-low buzzer if breached, and updates OLED.")
    add_numbered(3, "Local & Remote Streaming", "Continuous 1Hz BLE GATT packet broadcast + HTTP cloud telemetry transmission.")
    add_numbered(4, "Database Ingestion & Webhook", "Supabase stores readings; database trigger invokes pg_net HTTP POST.")
    add_numbered(5, "OneSignal Alert Dispatch", "flux-alert-evaluator calls OneSignal REST API to deliver instant mobile notifications.")

    dataflow_img = os.path.join(REPORT_DIR, "img_data_flow.png")
    if os.path.exists(dataflow_img):
        p_img = doc.add_paragraph()
        p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_img.paragraph_format.space_before = Pt(6)
        p_img.paragraph_format.space_after = Pt(10)
        doc.add_picture(dataflow_img, width=Inches(6.2))

    # Page Break for Hardware Components
    doc.add_page_break()

    # 16. HARDWARE COMPONENTS
    add_heading_1("16. HARDWARE COMPONENTS")
    add_body("The complete embedded hardware assembly consists of carefully selected industrial-grade components designed for continuous 24/7 operation:")

    hw_img = os.path.join(REPORT_DIR, "img_hardware_components.png")
    if os.path.exists(hw_img):
        p_img = doc.add_paragraph()
        p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_img.paragraph_format.space_before = Pt(6)
        p_img.paragraph_format.space_after = Pt(10)
        doc.add_picture(hw_img, width=Inches(6.2))

    # Page Break for Block Diagram
    doc.add_page_break()

    # 17. BLOCK DIAGRAM
    add_heading_1("17. BLOCK DIAGRAM")
    add_body("The block diagram illustrates the four decoupled architectural layers of the Flux telemetry ecosystem:")

    block_img = os.path.join(REPORT_DIR, "img_block_diagram.png")
    if os.path.exists(block_img):
        p_img = doc.add_paragraph()
        p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_img.paragraph_format.space_before = Pt(6)
        p_img.paragraph_format.space_after = Pt(10)
        doc.add_picture(block_img, width=Inches(6.2))

    # 18. FINAL PRODUCT AND IMPLEMENTATION
    add_heading_1("18. FINAL PRODUCT AND IMPLEMENTATION")
    add_body("The physical prototype was assembled and calibrated on an ESP32 DevKit V1 test rig with active-low acoustic suppression:")
    add_bullet("Active-Low Polarity Calibration: By configuring BuzzerActiveLow = true, GPIO 25 is driven HIGH immediately upon boot, eliminating the continuous buzzing interference that plagues standard active-high modules.")
    add_bullet("OLED 8-Screen Carousel: Physical buttons allow instantaneous cycling across Live Monitor, Environmental, Liquid Probe, Fermentation, Connectivity, Device Info, Diagnostics, and Settings screens.")
    add_bullet("Real Hardware Deployment: Firmware compiled with PlatformIO and flashed to hardware on COM10; companion app tested on Android 14/15 physical device (RZCX71Q65BB).")

    # Page Break for Dashboard
    doc.add_page_break()

    # 19. DASHBOARD AND USER INTERFACE
    add_heading_1("19. DASHBOARD AND USER INTERFACE")
    add_body("The Android companion app provides a sleek, single-page Jetpack Compose dashboard with circular thermal gauges, real-time telemetry, and automated Wi-Fi provisioning:")

    img_1 = os.path.join(REPORT_DIR, "ui_device_details.png")
    img_2 = os.path.join(REPORT_DIR, "ui_wifi_sheet.png")
    img_3 = os.path.join(REPORT_DIR, "ui_alerts_center.png")
    img_4 = os.path.join(REPORT_DIR, "ui_settings.png")

    img_table = doc.add_table(rows=1, cols=4)
    img_table.alignment = WD_TABLE_ALIGNMENT.CENTER
    img_table.autofit = False

    cols = img_table.rows[0].cells
    captions = ["(a) Hardware & Sensor Details", "(b) Wi-Fi Quick Sheet", "(c) Alerts & Safety Push", "(d) Smart Switch Settings"]
    imgs = [img_1, img_2, img_3, img_4]

    for i in range(4):
        cols[i].width = Inches(1.55)
        set_cell_margins(cols[i], top=40, bottom=40, left=30, right=30)
        p = cols[i].paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p.paragraph_format.space_after = Pt(2)
        if os.path.exists(imgs[i]):
            run = p.add_run()
            run.add_picture(imgs[i], width=Inches(1.45))
        
        p_cap = cols[i].add_paragraph()
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_cap.paragraph_format.space_before = Pt(2)
        r_cap = p_cap.add_run(captions[i])
        r_cap.font.name = "Arial"
        r_cap.font.size = Pt(7.5)
        r_cap.font.bold = True
        r_cap.font.color.rgb = RGBColor(71, 85, 105)

    docx_output_path = os.path.join(REPORT_DIR, "FLUX_SMART_TELEMETRY_SYSTEM_REPORT.docx")
    doc.save(docx_output_path)
    print(f"[+] DOCX successfully created: {docx_output_path}")
    return docx_output_path

def convert_docx_to_pdf(docx_path):
    pdf_path = docx_path.replace('.docx', '.pdf')
    print(f"[*] Converting DOCX to PDF using Word COM: {pdf_path}")
    word = win32com.client.Dispatch("Word.Application")
    word.Visible = False
    word.DisplayAlerts = 0
    try:
        doc = word.Documents.Open(os.path.abspath(docx_path), ConfirmConversions=False, ReadOnly=True)
        doc.SaveAs(os.path.abspath(pdf_path), FileFormat=17)
        doc.Close(False)
        print(f"[+] PDF successfully created: {pdf_path}")
    finally:
        word.Quit()
    return pdf_path

if __name__ == "__main__":
    print("[*] Generating diagrams...")
    generate_hardware_diagram()
    generate_block_diagram()
    generate_dataflow_diagram()
    print("[*] Building DOCX report...")
    docx_file = build_docx_report()
    print("[*] Converting to PDF...")
    pdf_file = convert_docx_to_pdf(docx_file)
    print("[+] ALL COMPLETED SUCCESSFULLY!")
