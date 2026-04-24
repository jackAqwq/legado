$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.2"
$env:PATH = "C:\Program Files\Java\jdk-21.0.2\bin;" + $env:PATH
$env:GRADLE_USER_HOME = "C:\Users\Administrator\.gradle"
Set-Location "C:\Users\Administrator\WorkBuddy\20260421100704\legado"
$tmp = "$env:TEMP\legado_build2.txt"
& "C:\Users\Administrator\.gradle\gradle-mirror\gradle-9.1.0\bin\gradle.bat" --stop 2>$null
Start-Sleep 1
& "C:\Users\Administrator\.gradle\gradle-mirror\gradle-9.1.0\bin\gradle.bat" tasks --no-daemon 2>&1 | Out-File -FilePath $tmp -Encoding utf8
Get-Content $tmp -Encoding UTF8 -Tail 60
