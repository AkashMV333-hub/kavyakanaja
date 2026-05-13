# Setting Up JAVA_HOME for Android Gradle Build

Your build is failing because JAVA_HOME points to an invalid JDK path. Here's how to fix it:

## Option 1: Set JAVA_HOME Temporarily (Current PowerShell Session Only)

Run this command in PowerShell:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
```

Then immediately test the build:

```powershell
.\gradlew.bat :app:compileDebugKotlin --no-daemon
```

## Option 2: Set JAVA_HOME Permanently (Recommended)

This sets it for all future PowerShell sessions and Android Studio.

**Find your Java installation first:**

```powershell
Get-Item 'C:\Program Files\Java\*' -Include 'jdk-*'
```

This will list available JDKs. Common options:
- `C:\Program Files\Java\jdk-11`
- `C:\Program Files\Java\jdk-17`
- `C:\Program Files\Java\jdk-21` (if valid)

**Then set JAVA_HOME permanently:**

```powershell
setx JAVA_HOME "C:\Program Files\Java\jdk-17"
```

After running this, **close the current PowerShell window and open a new one** for the change to take effect.

## Option 3: Configure Android Studio's Gradle JDK (Easiest)

If you don't want to set JAVA_HOME globally:

1. Open **Android Studio**
2. Go to **File → Settings → Build, Execution, Deployment → Gradle**
3. Under **Gradle JDK**, select **"Use embedded JDK"** (or choose a valid local JDK)
4. Click **OK**
5. Rebuild the project

## Verify Java Installation

Test that Java is working:

```powershell
java -version
```

Should show something like:
```
openjdk version "17.0.1" 2021-10-19
...
```

## Run the Build

Once JAVA_HOME is fixed, run:

```powershell
cd C:\Users\mvaka\AndroidStudioProjects\KavyaKanaja
.\gradlew.bat clean assembleDebug --stacktrace
```

If successful, you'll see:
```
BUILD SUCCESSFUL
```

## Troubleshooting

If you still get "JAVA_HOME is set to an invalid directory":

1. Check the path exists:
   ```powershell
   Test-Path 'C:\Program Files\Java\jdk-17\bin\java.exe'
   ```
   Should return `True`

2. If the JDK doesn't exist, download it:
   - Visit: https://www.oracle.com/java/technologies/downloads/
   - Download JDK 17 or 21
   - Install to `C:\Program Files\Java\`

3. After setting JAVA_HOME, **restart Android Studio and PowerShell**

## Quick Test After JAVA_HOME Fix

Run this to verify the calendar feature compiles:

```powershell
.\gradlew.bat :app:compileDebugKotlin --no-daemon
```

Expected output:
```
BUILD SUCCESSFUL in XXs
```

No errors should appear!

