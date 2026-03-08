ANDROID_HOME  ?= $(HOME)/Android/Sdk
JAVA_HOME     ?= /var/lib/flatpak/app/com.google.AndroidStudio/current/active/files/extra/jbr
AVDMANAGER    := $(ANDROID_HOME)/cmdline-tools/latest/bin/avdmanager
EMULATOR      := $(ANDROID_HOME)/emulator/emulator
AVD           ?= assistral-dev
SYSTEM_IMAGE  := system-images;android-36.1;google_apis_playstore;x86_64
APP_PACKAGE   := org.shano.assistral
APP_ACTIVITY  := org.shano.assistral.MainActivity

export ANDROID_HOME
export JAVA_HOME
export PATH := $(JAVA_HOME)/bin:$(ANDROID_HOME)/platform-tools:$(ANDROID_HOME)/emulator:$(ANDROID_HOME)/cmdline-tools/latest/bin:$(PATH)

.PHONY: install debug release clean check-env setup avds create-avd emulator launch run logcat logcat-urls logcat-audio

## Install build dependencies (run once, requires toolbox)
setup:
	sudo dnf install -y \
		android-tools \
		make \
		pulseaudio-libs \
		libpng \
		libX11 \
		libXext \
		libXrender \
		libGL \
		mesa-libGL \
		libxcb \
		libXcomposite \
		libXdamage \
		libXfixes \
		libXrandr \
		libXi \
		libxkbcommon \
		nss \
		nspr \
		libxkbfile

## List available AVDs
avds:
	@$(EMULATOR) -list-avds

## Create a default AVD for testing (requires SDK cmdline-tools installed via Android Studio)
create-avd:
	$(AVDMANAGER) create avd \
		--name $(AVD) \
		--package "$(SYSTEM_IMAGE)" \
		--device "pixel_6" \
		--force

## Wait for a running emulator/device to be ready (start it via Android Studio Device Manager first)
emulator:
	@if adb devices | grep -q emulator; then \
		echo "Emulator already online."; \
	else \
		echo "No emulator detected. Start one via Android Studio Device Manager, then re-run."; \
		echo "Waiting for device to come online (Ctrl+C to abort)..."; \
		adb wait-for-device; \
		echo "Waiting for boot to complete..."; \
		until adb shell getprop sys.boot_completed 2>/dev/null | grep -q 1; do sleep 2; done; \
		sleep 3; \
		echo "Emulator ready."; \
	fi

## Build and install the debug APK
install: check-env
	@if ! adb devices | grep -q emulator; then \
		echo "ERROR: No emulator running. Run 'make emulator' or start one via Android Studio AVD Manager first."; \
		exit 1; \
	fi
	@echo "Restarting adb server to ensure SDK adb sees the emulator..."
	@adb kill-server
	@adb start-server
	@adb wait-for-device
	@until adb shell getprop sys.boot_completed 2>/dev/null | grep -q 1; do sleep 2; done
	@adb devices
	./gradlew installDebug

## Launch the app on the connected device/emulator
launch:
	adb shell am start -n $(APP_PACKAGE)/$(APP_ACTIVITY)

## Full cycle: start emulator, build, install, launch
run: emulator install launch

## Build a debug APK without installing
debug: check-env
	./gradlew assembleDebug

## Build a release APK
release: check-env
	./gradlew assembleRelease

## Clean build outputs
clean:
	./gradlew clean

## Live logcat for the app (Ctrl+C to stop)
logcat:
	adb logcat -s assistral:D WebView:D chromium:D

## Live logcat showing only blocked/allowed URL decisions
logcat-urls:
	adb logcat -s assistral:D | grep --line-buffered -iE "ALLOWLIST|Blocked access"

## Live logcat for audio and permission events
logcat-audio:
	adb logcat | grep --line-buffered -iE "assistral|audio|microphone|PermissionRequest|RECORD_AUDIO|AAudio|TinyALSA"

## Print resolved env for troubleshooting
check-env:
	@echo "JAVA_HOME:    $(JAVA_HOME)"
	@echo "ANDROID_HOME: $(ANDROID_HOME)"
	@java -version 2>&1 | head -1
	@adb devices
