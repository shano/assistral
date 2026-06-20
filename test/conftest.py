import os
import subprocess
import pytest

ADB = os.path.expanduser("~/Android/Sdk/platform-tools/adb")
APP_PACKAGE = "org.shano.assistral"
CDP_PORT = 9222


def pytest_configure(config):
    result = subprocess.run(
        [ADB, "shell", "pidof", APP_PACKAGE],
        capture_output=True,
        text=True,
    )
    pid = result.stdout.strip().replace("\r", "").split()
    if not pid:
        pytest.exit(f"App {APP_PACKAGE} not running — run 'make run' first")
    pid = pid[0]

    fwd = subprocess.run(
        [
            ADB,
            "forward",
            f"tcp:{CDP_PORT}",
            f"localabstract:webview_devtools_remote_{pid}",
        ],
        capture_output=True,
        text=True,
    )
    if fwd.returncode != 0:
        pytest.exit(f"adb forward failed: {fwd.stderr.strip()}")
