<#
.SYNOPSIS
    Flux Mobile Screen Capture & Clipboard Companion
.DESCRIPTION
    Captures pixel-perfect screenshots of the connected Android device via ADB,
    copies the image directly to the Windows Clipboard for instant Ctrl+V pasting into Antigravity chat,
    and saves high-resolution PNGs to the local screenshots folder.
.EXAMPLE
    .\screenshot_tool.ps1           # Launches floating companion pill docked to Flux Mobile Screen
    .\screenshot_tool.ps1 -Snap     # Instantly captures, copies to clipboard, and exits
#>

[CmdletBinding()]
param (
    [switch]$Snap,
    [string]$DeviceId
)

Add-Type -AssemblyName PresentationFramework, PresentationCore, WindowsBase
Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing

$adb = "C:\Android\Sdk\platform-tools\adb.exe"
if (-not (Test-Path $adb)) {
    $adb = (Get-Command adb -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Source -ErrorAction SilentlyContinue)
    if (-not $adb) { $adb = "adb.exe" }
}

$screenshotDir = "c:\project\Flux\screenshots"
if (-not (Test-Path $screenshotDir)) {
    New-Item -ItemType Directory -Path $screenshotDir -Force | Out-Null
}

function Get-TargetDevice {
    if ($DeviceId) { return $DeviceId }
    $devicesOutput = & $adb devices
    $deviceLines = @($devicesOutput | Where-Object { $_ -match "\s+device$" })
    if ($deviceLines.Count -gt 0) {
        return ($deviceLines[0] -split "\s+")[0].Trim()
    }
    return $null
}

function Capture-MobileScreenshot {
    $dev = Get-TargetDevice
    if (-not $dev) {
        [System.Windows.Forms.MessageBox]::Show("No authorized Android device connected via ADB.", "Flux Capture Error", [System.Windows.Forms.MessageBoxButtons]::OK, [System.Windows.Forms.MessageBoxIcon]::Warning)
        return $null
    }

    $timestamp = (Get-Date).ToString("yyyyMMdd_HHmmss")
    $savePath = "$screenshotDir\flux_$timestamp.png"

    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = $adb
    $psi.Arguments = "-s $dev exec-out screencap -p"
    $psi.UseShellExecute = $false
    $psi.RedirectStandardOutput = $true
    $psi.CreateNoWindow = $true

    try {
        $proc = [System.Diagnostics.Process]::Start($psi)
        $fs = [System.IO.File]::Create($savePath)
        $proc.StandardOutput.BaseStream.CopyTo($fs)
        $fs.Close()
        $proc.WaitForExit()

        if ((Get-Item $savePath).Length -gt 1000) {
            # Load and copy to clipboard
            $img = [System.Drawing.Image]::FromFile($savePath)
            [System.Windows.Forms.Clipboard]::SetImage($img)
            $img.Dispose()
            [System.Media.SystemSounds]::Asterisk.Play()
            return $savePath
        } else {
            Remove-Item $savePath -Force -ErrorAction SilentlyContinue
            return $null
        }
    } catch {
        return $null
    }
}

# --- CLI ONE-SHOT SNAP MODE ---
if ($Snap) {
    $result = Capture-MobileScreenshot
    if ($result) {
        Write-Host "[+] Screenshot saved: $result" -ForegroundColor Green
        Write-Host "[+] Copied to Windows Clipboard! Paste with Ctrl+V into Antigravity chat." -ForegroundColor Cyan
    } else {
        Write-Host "[x] Failed to capture mobile screenshot." -ForegroundColor Red
        exit 1
    }
    exit 0
}

# --- FLOATING WPF COMPANION TOOLBAR ---
Add-Type @"
  using System;
  using System.Runtime.InteropServices;
  public struct RECT {
    public int Left;
    public int Top;
    public int Right;
    public int Bottom;
  }
  public class Win32Helper {
    [DllImport("user32.dll", SetLastError = true)]
    public static extern IntPtr FindWindow(string lpClassName, string lpWindowName);
    [DllImport("user32.dll")]
    [return: MarshalAs(UnmanagedType.Bool)]
    public static extern bool GetWindowRect(IntPtr hWnd, out RECT lpRect);
  }
"@

[xml]$xaml = @"
<Window xmlns="http://schemas.microsoft.com/winfx/2006/xaml/presentation"
        xmlns:x="http://schemas.microsoft.com/winfx/2006/xaml"
        Title="Flux Screenshot Bar"
        Height="42" Width="260"
        WindowStyle="None"
        AllowsTransparency="True"
        Background="Transparent"
        Topmost="True"
        ShowInTaskbar="False"
        ResizeMode="NoResize">
    <Border Background="#E60F172A"
            BorderBrush="#FF6B00"
            BorderThickness="1.5"
            CornerRadius="21"
            Padding="4,2,4,2">
        <Border.Effect>
            <DropShadowEffect BlurRadius="12" Color="#000000" Opacity="0.65" ShadowDepth="3"/>
        </Border.Effect>
        <Grid VerticalAlignment="Center">
            <Grid.ColumnDefinitions>
                <ColumnDefinition Width="*"/>
                <ColumnDefinition Width="34"/>
                <ColumnDefinition Width="28"/>
            </Grid.ColumnDefinitions>

            <!-- Snap Button -->
            <Button Name="BtnSnap" Grid.Column="0"
                    Background="#FF6B00"
                    Foreground="#FFFFFF"
                    FontFamily="Segoe UI Semibold"
                    FontSize="12"
                    FontWeight="Bold"
                    BorderThickness="0"
                    Height="30"
                    Margin="2,0,4,0"
                    Cursor="Hand">
                <Button.Template>
                    <ControlTemplate TargetType="Button">
                        <Border Name="bdr" Background="{TemplateBinding Background}" CornerRadius="15">
                            <ContentPresenter HorizontalAlignment="Center" VerticalAlignment="Center"/>
                        </Border>
                        <ControlTemplate.Triggers>
                            <Trigger Property="IsMouseOver" Value="True">
                                <Setter TargetName="bdr" Property="Background" Value="#FF8533"/>
                            </Trigger>
                            <Trigger Property="IsPressed" Value="True">
                                <Setter TargetName="bdr" Property="Background" Value="#E05500"/>
                            </Trigger>
                        </ControlTemplate.Triggers>
                    </ControlTemplate>
                </Button.Template>
                <StackPanel Orientation="Horizontal" VerticalAlignment="Center">
                    <TextBlock Text="📸 " FontSize="13"/>
                    <TextBlock Name="TxtSnapLabel" Text="Snap &amp; Copy" VerticalAlignment="Center"/>
                </StackPanel>
            </Button>

            <!-- Open Folder Button -->
            <Button Name="BtnFolder" Grid.Column="1"
                    Background="#1E293B"
                    Foreground="#94A3B8"
                    FontSize="13"
                    BorderThickness="0"
                    Height="30"
                    Width="30"
                    Margin="0,0,2,0"
                    ToolTip="Open Screenshots Folder"
                    Cursor="Hand">
                <Button.Template>
                    <ControlTemplate TargetType="Button">
                        <Border Name="bdrFolder" Background="{TemplateBinding Background}" CornerRadius="15">
                            <TextBlock Text="📁" HorizontalAlignment="Center" VerticalAlignment="Center"/>
                        </Border>
                        <ControlTemplate.Triggers>
                            <Trigger Property="IsMouseOver" Value="True">
                                <Setter TargetName="bdrFolder" Property="Background" Value="#334155"/>
                            </Trigger>
                        </ControlTemplate.Triggers>
                    </ControlTemplate>
                </Button.Template>
            </Button>

            <!-- Close Button -->
            <Button Name="BtnClose" Grid.Column="2"
                    Background="Transparent"
                    Foreground="#94A3B8"
                    FontSize="12"
                    FontWeight="Bold"
                    BorderThickness="0"
                    Height="30"
                    Width="24"
                    Cursor="Hand">
                <Button.Template>
                    <ControlTemplate TargetType="Button">
                        <Border Name="bdrClose" Background="{TemplateBinding Background}" CornerRadius="12">
                            <TextBlock Text="✕" HorizontalAlignment="Center" VerticalAlignment="Center" Foreground="#94A3B8"/>
                        </Border>
                        <ControlTemplate.Triggers>
                            <Trigger Property="IsMouseOver" Value="True">
                                <Setter TargetName="bdrClose" Property="Background" Value="#475569"/>
                            </Trigger>
                        </ControlTemplate.Triggers>
                    </ControlTemplate>
                </Button.Template>
            </Button>
        </Grid>
    </Border>
</Window>
"@

$reader = (New-Object System.Xml.XmlNodeReader $xaml)
$window = [System.Windows.Markup.XamlReader]::Load($reader)

$btnSnap = $window.FindName("BtnSnap")
$txtLabel = $window.FindName("TxtSnapLabel")
$btnFolder = $window.FindName("BtnFolder")
$btnClose = $window.FindName("BtnClose")

# Allow dragging the pill from anywhere
$window.Add_MouseLeftButtonDown({
    $window.DragMove()
})

# Close action
$btnClose.Add_Click({
    $window.Close()
})

# Open screenshots folder
$btnFolder.Add_Click({
    if (Test-Path $screenshotDir) {
        [System.Diagnostics.Process]::Start("explorer.exe", $screenshotDir) | Out-Null
    }
})

# Snap & Copy click action
$btnSnap.Add_Click({
    $txtLabel.Text = "Capturing..."
    $window.Dispatcher.Invoke([Action]{}, [System.Windows.Threading.DispatcherPriority]::Render)

    $saved = Capture-MobileScreenshot

    if ($saved) {
        $txtLabel.Text = "✅ Copied (Ctrl+V)!"
        $btnSnap.Background = [System.Windows.Media.BrushConverter]::new().ConvertFromString("#10B981")
        
        # Reset after 2 seconds
        $timer = New-Object System.Windows.Threading.DispatcherTimer
        $timer.Interval = [TimeSpan]::FromSeconds(2.0)
        $timer.Add_Tick({
            $txtLabel.Text = "Snap & Copy"
            $btnSnap.Background = [System.Windows.Media.BrushConverter]::new().ConvertFromString("#FF6B00")
            $timer.Stop()
        })
        $timer.Start()
    } else {
        $txtLabel.Text = "⚠️ Failed"
        Start-Sleep -Milliseconds 800
        $txtLabel.Text = "Snap & Copy"
    }
})

# Initial Docking near "Flux Mobile Screen" window
$window.Add_Loaded({
    $hwnd = [Win32Helper]::FindWindow($null, "Flux Mobile Screen")
    if ($hwnd -ne [IntPtr]::Zero) {
        $rect = New-Object RECT
        if ([Win32Helper]::GetWindowRect($hwnd, [ref]$rect)) {
            $window.Left = [Math]::Max(10, $rect.Left + 15)
            $window.Top = [Math]::Max(10, $rect.Top - 48)
        }
    } else {
        # Default top-center of screen
        $window.Left = ([System.Windows.SystemParameters]::PrimaryScreenWidth - $window.Width) / 2
        $window.Top = 30
    }
})

$window.ShowDialog() | Out-Null
