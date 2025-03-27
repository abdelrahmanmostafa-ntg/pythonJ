const pcr = require('puppeteer-chromium-resolver');
const fs = require('fs/promises');

async function fileExistsAtPath(filePath) {
    try {
        await fs.access(filePath)
        return true
    } catch {
        return false
    }
}

(async () => {
    try {
        const puppeteerDir = "/usr/local/share/puppeteer"
        const dirExists = await fileExistsAtPath(puppeteerDir)
        if (!dirExists) {
            await fs.mkdir(puppeteerDir, { recursive: true })
        }

        const stats = await pcr({
            downloadPath: puppeteerDir,
        })
        console.log('Chromium downloaded to:', stats.executablePath);
        process.exit(0);
    } catch (e) {
        console.error('Error:', e);
        process.exit(1);
    }
})();