$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.2"
$env:PATH = "C:\Program Files\Java\jdk-21.0.2\bin;" + $env:PATH
$env:ANDROID_HOME = "C:\Android"
$env:ANDROID_SDK_ROOT = "C:\Android"

# Accept licenses first
Write-Host "Accepting licenses..."
& "C:\Android\cmdline-tools\cmdline-tools\bin\sdkmanager.bat" --licenses 2>&1 | Select-Object -Last 10

# Install required components
Write-Host "Installing build-tools and platform..."
& "C:\Android\cmdline-tools\cmdline-tools\bin\sdkmanager.bat" "build-tools;36.0.0" "platforms;android-36" 2>&1 | Select-Object -Last 20
