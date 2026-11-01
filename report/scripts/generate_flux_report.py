import os
import shutil
import docx
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
import win32com.client
import pymupdf

SCRIPT_DIR = os.path.abspath(os.path.dirname(__file__))
REPORT_DIR = os.path.abspath(os.path.join(SCRIPT_DIR, '..'))
ASSETS_DIR = os.path.join(REPORT_DIR, 'assets')
DIAGRAMS_DIR = os.path.join(ASSETS_DIR, 'diagrams')

def add_page_border(section):
    """Adds a clean margin line (page border) matching the reference format."""
    sectPr = section._sectPr
    pgBorders = OxmlElement('w:pgBorders')
    pgBorders.set(qn('w:offsetFrom'), 'page')
    for b_name in ['top', 'left', 'bottom', 'right']:
        b = OxmlElement(f'w:{b_name}')
        b.set(qn('w:val'), 'single')
        b.set(qn('w:sz'), '6')        # 0.75 pt single line
        b.set(qn('w:space'), '24')     # 24 pt from page edge (matches reference)
        b.set(qn('w:color'), '000000')
        pgBorders.append(b)
    sectPr.append(pgBorders)

def set_cell_margins(cell, top=60, bottom=60, left=100, right=100):
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = OxmlElement('w:tcMar')
    for m, val in [('top', top), ('bottom', bottom), ('left', left), ('right', right)]:
        node = OxmlElement(f'w:{m}')
        node.set(qn('w:w'), str(val))
        node.set(qn('w:type'), 'dxa')
        tcMar.append(node)
    tcPr.append(tcMar)

def set_table_borders(table, color="D1D5DB", sz="4", val="single"):
    tblPr = table._tbl.tblPr
    tblBorders = OxmlElement('w:tblBorders')
    for border_name in ['top', 'left', 'bottom', 'right', 'insideH', 'insideV']:
        border = OxmlElement(f'w:{border_name}')
        border.set(qn('w:val'), val)
        border.set(qn('w:sz'), sz)
        border.set(qn('w:space'), '0')
        border.set(qn('w:color'), color)
        tblBorders.append(border)
    tblPr.append(tblBorders)

def build_docx_report():
    doc = Document()

    # Configure A4 page dimensions and 1.0 inch margins
    section = doc.sections[0]
    section.page_width = Inches(8.27)
    section.page_height = Inches(11.69)
    section.top_margin = Inches(1.0)
    section.bottom_margin = Inches(1.0)
    section.left_margin = Inches(1.0)
    section.right_margin = Inches(1.0)

    # Add margin line (page border)
    add_page_border(section)

    def add_title(text):
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p.paragraph_format.space_before = Pt(0)
        p.paragraph_format.space_after = Pt(14)
        r = p.add_run(text)
        r.font.name = "Arial"
        r.font.size = Pt(14)
        r.font.bold = True
        r.font.color.rgb = RGBColor(0, 0, 0)
        return p

    def add_heading_1(text, space_before=10, space_after=3):
        h = doc.add_paragraph()
        h.paragraph_format.space_before = Pt(space_before)
        h.paragraph_format.space_after = Pt(space_after)
        h.paragraph_format.keep_with_next = True
        r = h.add_run(text)
        r.font.name = "Arial"
        r.font.size = Pt(12)
        r.font.bold = True
        r.font.color.rgb = RGBColor(0, 0, 0)
        return h

    def add_heading_2(text, space_before=8, space_after=2):
        h = doc.add_paragraph()
        h.paragraph_format.space_before = Pt(space_before)
        h.paragraph_format.space_after = Pt(space_after)
        h.paragraph_format.keep_with_next = True
        r = h.add_run(text)
        r.font.name = "Arial"
        r.font.size = Pt(11.5)
        r.font.bold = True
        r.font.color.rgb = RGBColor(0, 0, 0)
        return h

    def add_body(text, space_after=4, bold_prefix=None):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(0)
        p.paragraph_format.space_after = Pt(space_after)
        p.paragraph_format.line_spacing = 1.15
        if bold_prefix:
            r_pre = p.add_run(bold_prefix)
            r_pre.font.name = "Arial"
            r_pre.font.size = Pt(11)
            r_pre.font.bold = True
            r_pre.font.color.rgb = RGBColor(0, 0, 0)
        r = p.add_run(text)
        r.font.name = "Arial"
        r.font.size = Pt(11)
        r.font.color.rgb = RGBColor(0, 0, 0)
        return p

    def add_bullet(text, bold_prefix=None, space_after=2.5):
        p = doc.add_paragraph(style='List Bullet')
        p.paragraph_format.space_before = Pt(0)
        p.paragraph_format.space_after = Pt(space_after)
        p.paragraph_format.line_spacing = 1.15
        if bold_prefix:
            r_pre = p.add_run(bold_prefix)
            r_pre.font.name = "Arial"
            r_pre.font.size = Pt(11)
            r_pre.font.bold = True
            r_pre.font.color.rgb = RGBColor(0, 0, 0)
        r = p.add_run(text)
        r.font.name = "Arial"
        r.font.size = Pt(11)
        r.font.color.rgb = RGBColor(0, 0, 0)
        return p

    def add_numbered(num, title, text, space_after=2.5):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(0.5)
        p.paragraph_format.space_after = Pt(space_after)
        p.paragraph_format.line_spacing = 1.15
        r_num = p.add_run(f"{num}. ")
        r_num.font.name = "Arial"
        r_num.font.size = Pt(11)
        r_num.font.bold = True
        r_num.font.color.rgb = RGBColor(0, 0, 0)
        if title:
            r_title = p.add_run(f"{title}: ")
            r_title.font.name = "Arial"
            r_title.font.size = Pt(11)
            r_title.font.bold = True
            r_title.font.color.rgb = RGBColor(0, 0, 0)
        r_text = p.add_run(text)
        r_text.font.name = "Arial"
        r_text.font.size = Pt(11)
        r_text.font.color.rgb = RGBColor(0, 0, 0)
        return p

    # ==========================================
    # PAGE 1: TITLE, 1. INTRODUCTION, 2. AIM OF THE PROJECT, 3. OBJECTIVES
    # ==========================================
    add_title("SMART TELEMETRY AND THERMAL MONITORING SYSTEM")

    add_heading_1("1. INTRODUCTION", space_before=2)
    add_body("Temperature-sensitive operations in cold-chain logistics, pharmaceutical preservation, laboratory research, and industrial food fermentation require continuous, high-precision thermal tracking. Often, users run into unexpected thermal deviations, causing product spoilage and potential hazards due to unmonitored temperature breaches. Traditional methods of checking temperature are manual and inaccurate.")
    add_body("To address this issue, this project proposes an IoT-based Smart Telemetry and Thermal Monitoring System that automatically measures environmental and probe temperature and provides alerts when it reaches predefined safety thresholds. The system integrates a waterproof DS18B20 digital thermal probe operating on the Dallas 1-Wire protocol to accurately measure temperature with 12-bit precision. The data is processed by an ESP32 microcontroller, which transmits real-time telemetry to the cloud via Wi-Fi and Bluetooth Low Energy (BLE). Users can monitor thermal conditions through an Android mobile dashboard and receive notifications when safety limits are breached.")
    add_body("This solution offers a low-cost, reliable, and scalable way to manage critical temperature environments efficiently while minimizing manual intervention.")

    add_heading_1("2. AIM OF THE PROJECT", space_before=10)
    add_body("The main aim of this project is to design and implement an IoT-enabled temperature monitoring system that continuously measures the probe temperature, sends notifications when the temperature breaches safe limits, and enables users to monitor it remotely.")

    add_heading_1("3. OBJECTIVES", space_before=10)
    add_bullet("To measure real-time temperature using a waterproof DS18B20 digital sensor with 12-bit resolution.")
    add_bullet("To process sensor data through an ESP32 microcontroller and transmit it to the cloud via Wi-Fi and BLE.")
    add_bullet("To develop a user interface for real-time thermal monitoring.")
    add_bullet("To send automatic alerts when the temperature breaches high or low safety thresholds.")
    add_bullet("To provide a compact, safe, and cost-effective system suitable for industrial and household environments.")

    # Page Break -> PAGE 2
    doc.add_page_break()

    # ==========================================
    # PAGE 2: 4. EXISTING SYSTEM, 5. PROPOSED SYSTEM, 6. SYSTEM ARCHITECTURE (6.1)
    # ==========================================
    add_heading_1("4. EXISTING SYSTEM", space_before=2)
    add_body("The existing methods for temperature monitoring are mostly manual or analog, such as periodic thermometer readings or mechanical dials.")
    add_body("Disadvantages:")
    add_bullet("Manual checking is inaccurate and time-consuming.")
    add_bullet("Passive USB loggers only measure retrospective data after spoilage has occurred.")
    add_bullet("No real-time notifications or automation.")
    add_bullet("Inefficient tracking of thermal trends and lack of on-site fail-safes.")

    add_heading_1("5. PROPOSED SYSTEM", space_before=10)
    add_body("The proposed system introduces an IoT-based smart thermal monitoring device capable of measuring probe temperature accurately and sending data to a cloud server in real time.")
    add_body("The ESP32 microcontroller collects data from a digital probe (via Dallas 1-Wire protocol) and compares it with configured safety thresholds. When the measured temperature breaches set bounds, the system activates an on-site active-low buzzer and sends an alert via Wi-Fi and push notification to the connected user smartphone.")
    add_body("This design eliminates the need for manual checks, offering automation, accuracy, and convenience.")

    add_heading_1("6. SYSTEM ARCHITECTURE", space_before=10)
    add_body("The architecture comprises two main layers – Device Layer and Cloud Layer.")

    add_heading_2("6.1 Hardware Components", space_before=8)
    add_bullet(" Microcontroller with built-in Wi-Fi and BLE for processing and data transmission.", bold_prefix="ESP32 DevKit V1:")
    add_bullet(" Measures liquid and ambient temperatures with 12-bit digital resolution (±0.5°C accuracy).", bold_prefix="DS18B20 Temperature Probe:")
    add_bullet(" High-contrast screen providing 8 dynamic status and telemetry carousel screens.", bold_prefix="SSD1306 0.96\" OLED Display:")
    add_bullet(" On-site acoustic alarm driven via GPIO 25 configured for boot-time silence and 3-pip breach alerts.", bold_prefix="Active-Low Piezo Buzzer:")
    add_bullet(" Dual hardware buttons for OLED screen cycling (GPIO 32) and alarm muting (GPIO 33).", bold_prefix="Tactile Push Buttons:")
    add_bullet(" Powers the ESP32 board and connected sensor peripherals via USB-C or DC adapter.", bold_prefix="Power Adapter (5V):")

    # Page Break -> PAGE 3
    doc.add_page_break()

    # ==========================================
    # PAGE 3: 6.2 SOFTWARE, 7. WORKING PRINCIPLE, 8. CONNECTION DETAILS, 9. CLOUD (START)
    # ==========================================
    add_heading_2("6.2 Software Components", space_before=2)
    add_bullet(" FreeRTOS firmware managing sensor, display, and buzzer tasks.", bold_prefix="PlatformIO & C++:")
    add_bullet(" Containerized Express REST API for real-time telemetry ingestion.", bold_prefix="Render Docker Backend:")
    add_bullet(" PostgreSQL time-series storage with Row Level Security and webhooks.", bold_prefix="Supabase Cloud Database:")
    add_bullet(" Native Jetpack Compose UI with offline-first Room database buffer.", bold_prefix="Android Companion App:")

    add_heading_1("7. WORKING PRINCIPLE", space_before=8)
    add_numbered(1, "Initialization", "The ESP32 boots, silences the buzzer on GPIO 25, boots the OLED, and starts BLE.")
    add_numbered(2, "Data Acquisition", "The DS18B20 digital probe continuously measures temperature with 12-bit conversion at 1Hz.")
    add_numbered(3, "Data Processing", "The ESP32 validates 1-Wire CRC checksums and updates the active OLED carousel screen.")
    add_numbered(4, "Threshold Check", "When measured temperature breaches set safety thresholds (> 28°C or < 18°C), an alert is triggered.")
    add_numbered(5, "Notification", "Data is sent to the cloud via Wi-Fi and displayed on the dashboard or app.")
    add_numbered(6, "Monitoring", "Users can view real-time thermal levels and receive automated push alerts through the dashboard.")

    add_heading_1("8. CONNECTION DETAILS", space_before=8)
    # Table with NO color header, matching reference format exactly
    table = doc.add_table(rows=1, cols=4)
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False
    set_table_borders(table, color="D1D5DB", sz="4", val="single")

    headers = ["Component", "Pin Type", "ESP32 Pin", "Voltage"]
    col_widths = [Inches(1.8), Inches(1.5), Inches(1.6), Inches(1.35)]

    hdr_cells = table.rows[0].cells
    for i, title in enumerate(headers):
        hdr_cells[i].text = title
        hdr_cells[i].width = col_widths[i]
        set_cell_margins(hdr_cells[i], top=40, bottom=40, left=80, right=80)
        p = hdr_cells[i].paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.LEFT
        r = p.runs[0]
        r.font.name = "Arial"
        r.font.size = Pt(10)
        r.font.bold = True
        r.font.color.rgb = RGBColor(0, 0, 0)

    pin_data = [
        ("DS18B20 Probe", "Data (1-Wire)", "GPIO 4", "3.3V"),
        ("SSD1306 OLED", "I2C (SDA/SCL)", "GPIO 21 / 22", "3.3V"),
        ("Piezo Buzzer", "Digital Output", "GPIO 25", "3.3V - 5V"),
        ("Tactile Buttons", "Digital Input", "GPIO 32 / 33", "3.3V")
    ]

    for row_idx, data in enumerate(pin_data):
        row_cells = table.add_row().cells
        for i, val in enumerate(data):
            row_cells[i].text = val
            row_cells[i].width = col_widths[i]
            set_cell_margins(row_cells[i], top=35, bottom=35, left=80, right=80)
            p = row_cells[i].paragraphs[0]
            p.alignment = WD_ALIGN_PARAGRAPH.LEFT
            r = p.runs[0]
            r.font.name = "Arial"
            r.font.size = Pt(9.5)
            r.font.color.rgb = RGBColor(0, 0, 0)

    add_heading_1("9. CLOUD & CONTAINER HOSTING", space_before=8)
    add_body("The backend server can be hosted on Render or any cloud service using Docker containers for scalability.")
    add_bullet(" Ubuntu-based Docker container with open REST endpoints.", bold_prefix="Render Cloud:")

    # Page Break -> PAGE 4
    doc.add_page_break()

    # ==========================================
    # PAGE 4: 9. CLOUD (CONT), 10. RESULTS, 11. ADVANTAGES, 12. APPLICATIONS, 13. FUTURE (PART)
    # ==========================================
    add_bullet(" Containerized services for Express API, database persistence, and dashboard.", bold_prefix="Docker Setup:")
    add_bullet(" Automated via GitHub CI pipeline for ease of scaling and zero-downtime deploys.", bold_prefix="Deployment:")
    add_bullet(" Sensor data stored in Supabase PostgreSQL and visualized via companion dashboard.", bold_prefix="Data Management:")

    add_heading_1("10. RESULTS", space_before=9)
    add_body("The implemented system successfully:")
    add_bullet("Measures real-time probe temperature with 12-bit laboratory accuracy.")
    add_bullet("Sends low and high temperature alerts when readings breach set thresholds.")
    add_bullet("Displays live readings and telemetry waveforms on the cloud dashboard.")
    add_bullet("Enables remote monitoring from any device connected to the internet.")

    add_heading_1("11. ADVANTAGES", space_before=9)
    add_bullet("Accurate real-time thermal monitoring.")
    add_bullet("Automated notifications reduce manual effort.")
    add_bullet("Easy to install and low cost.")
    add_bullet("Can be expanded for multiple thermal probes.")
    add_bullet("Reduces unexpected thermal runaways and improves safety.")

    add_heading_1("12. APPLICATIONS", space_before=9)
    add_bullet(" Monitor vaccines and temperature-sensitive medicine.", bold_prefix="Pharmaceutical Cold Chains:")
    add_bullet(" Continuous tracking for fermentation vats in commercial breweries.", bold_prefix="Food Fermentation & Brewing:")
    add_bullet(" Prevent server downtime during thermal runaways in equipment racks.", bold_prefix="Data Centers & Server Racks:")
    add_bullet(" Regulate microclimates for temperature-critical horticultural crops.", bold_prefix="Agricultural Greenhouses:")

    add_heading_1("13. FUTURE ENHANCEMENTS", space_before=9)
    add_bullet(" Automatically book service when temperature anomalies persist.", bold_prefix="Integration with Maintenance API:")
    add_bullet(" Real-time alerts to smartphones via SMS and multi-channel push.", bold_prefix="Mobile App Notifications (SMS/Push):")

    # Page Break -> PAGE 5
    doc.add_page_break()

    # ==========================================
    # PAGE 5: 13. FUTURE (CONT), 14. CONCLUSION, 15. PROJECT DESIGN, 16. HARDWARE COMPONENTS
    # ==========================================
    add_bullet(" Works with Amazon Alexa or Google Assistant for voice telemetry.", bold_prefix="Voice Assistant Integration:")
    add_bullet(" Adding a secondary probe or gas sensor for safety monitoring.", bold_prefix="Multi-Sensor Detection:")
    add_bullet(" Standalone battery or solar operation for remote field deployment.", bold_prefix="Solar Power Support:")

    add_heading_1("14. CONCLUSION", space_before=8)
    add_body("The Smart Telemetry and Thermal Monitoring System provides an intelligent, automated solution to track temperature levels accurately. It ensures timely alerts, improves user convenience, and enhances safety. The IoT integration makes the system scalable and accessible via the cloud, laying the foundation for future smart home automation applications.")

    add_heading_1("15. PROJECT DESIGN", space_before=8)
    add_body("Data Flow:")
    add_numbered(1, "", "DS18B20 Probe → ESP32: Measures and digitizes probe temperature.")
    add_numbered(2, "", "ESP32 → Cloud: Transmits telemetry data via Wi-Fi.")
    add_numbered(3, "", "Cloud → Dashboard/App: Displays current temperature and alerts.")

    add_heading_1("16. HARDWARE COMPONENTS", space_before=8, space_after=2)
    hw_img = os.path.join(DIAGRAMS_DIR, "img_hardware_components.png")
    if os.path.exists(hw_img):
        p_img = doc.add_paragraph()
        p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_img.paragraph_format.space_before = Pt(2)
        p_img.paragraph_format.space_after = Pt(0)
        doc.add_picture(hw_img, width=Inches(6.25))

    # Page Break -> PAGE 6
    doc.add_page_break()

    # ==========================================
    # PAGE 6: 17. BLOCK DIAGRAM, 18. FINAL PRODUCT AND PRESENTATION
    # ==========================================
    add_heading_1("17. BLOCK DIAGRAM", space_before=0, space_after=1)
    block_img = os.path.join(DIAGRAMS_DIR, "img_block_diagram.png")
    if os.path.exists(block_img):
        p_img = doc.add_paragraph()
        p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_img.paragraph_format.space_before = Pt(1)
        p_img.paragraph_format.space_after = Pt(1)
        doc.add_picture(block_img, width=Inches(6.25), height=Inches(3.54))

    add_heading_1("18. FINAL PRODUCT AND PRESENTATION", space_before=4, space_after=1)
    final_img = os.path.join(DIAGRAMS_DIR, "img_final_product.png")
    if os.path.exists(final_img):
        p_img = doc.add_paragraph()
        p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_img.paragraph_format.space_before = Pt(1)
        p_img.paragraph_format.space_after = Pt(0)
        doc.add_picture(final_img, width=Inches(6.25), height=Inches(3.125))

    # Page Break -> PAGE 7
    doc.add_page_break()

    # ==========================================
    # PAGE 7: 19. DASHBOARD
    # ==========================================
    add_heading_1("19. DASHBOARD", space_before=0, space_after=3)
    dash_img = os.path.join(DIAGRAMS_DIR, "img_dashboard_screens.png")
    if os.path.exists(dash_img):
        p_img = doc.add_paragraph()
        p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_img.paragraph_format.space_before = Pt(2)
        p_img.paragraph_format.space_after = Pt(0)
        doc.add_picture(dash_img, width=Inches(6.25), height=Inches(3.38))

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
    print("[*] Building DOCX report matching reference format...")
    docx_file = build_docx_report()

    print("[*] Converting to PDF...")
    pdf_file = convert_docx_to_pdf(docx_file)

    # Verify total page count
    pdf_doc = pymupdf.open(pdf_file)
    print(f"[+] Verification: PDF has {len(pdf_doc)} pages.")
    for i, p in enumerate(pdf_doc):
        first_line = p.get_text().split('\n')[0].strip() if p.get_text() else ""
        print(f"    Page {i+1}: {first_line[:50]}")
    print("[+] ALL DONE!")
