import asyncio
from pathlib import Path

import pytest
from playwright.async_api import async_playwright

CDP_URL = "http://localhost:9222"
SCREENSHOTS = Path(__file__).parent / "screenshots"


async def find_mistral_page(browser):
    for ctx in browser.contexts:
        for pg in ctx.pages:
            if "mistral.ai" in pg.url:
                return pg
    return None


@pytest.mark.asyncio
async def test_enter_inserts_newline():
    SCREENSHOTS.mkdir(exist_ok=True)

    async with async_playwright() as p:
        browser = await p.chromium.connect_over_cdp(CDP_URL)

        page = await find_mistral_page(browser)
        assert page is not None, "No mistral.ai page open — log in and open chat"

        editor = page.locator('[contenteditable="true"]').first
        await editor.wait_for(state="visible", timeout=5000)

        await editor.click()
        await page.keyboard.press("Control+a")
        await page.keyboard.press("Delete")
        await page.keyboard.type("Hello test")

        await page.screenshot(path=str(SCREENSHOTS / "before_enter.png"))
        initial_html = await editor.evaluate("el => el.innerHTML")

        await page.keyboard.press("Enter")
        await asyncio.sleep(0.3)

        await page.screenshot(path=str(SCREENSHOTS / "after_enter.png"))
        result_html = await editor.evaluate("el => el.innerHTML")

        print(f"\nBefore: {initial_html}")
        print(f"After:  {result_html}")

        assert result_html.strip() not in ("", "<p><br></p>", "<p></p>"), (
            f"Editor cleared — message submitted. HTML: {result_html}"
        )

        has_linebreak = "<br>" in result_html or result_html.count("<p>") >= 2
        assert has_linebreak, (
            f"No newline inserted (spaces or unchanged). HTML: {result_html}"
        )
