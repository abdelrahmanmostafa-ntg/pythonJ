FROM ghcr.io/codesandbox/devcontainers/universal:latest

# Install system dependencies and clean up
RUN apt-get update && apt-get install -y \
    curl wget jq \
    libsdl2-dev libsdl2-image-dev libsdl2-mixer-dev libsdl2-ttf-dev\
    libnss3 libnspr4 libatk1.0-0 libatk-bridge2.0-0 libcups2 libdrm2 \
    libxkbcommon0 libxcomposite1 libxdamage1 libxfixes3 libxrandr2 libgbm1 libasound2 \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Add and setup initial files
ADD . .
RUN chmod +x ./setup.sh && ./setup.sh

# Setup puppeteer and extract chromium
RUN mkdir -p /tmp/puppeteer-setup \
    && cp ./puppeteer.js /tmp/puppeteer-setup/puppeteer.js \
    && cd /tmp/puppeteer-setup \
    && npm init -y \
    && npm install puppeteer-chromium-resolver@23.0.0 --dd \
    && node ./puppeteer.js \
    && cd / \
    && rm -rf /tmp/puppeteer-setup \
    && npm cache clean --force \
    && rm -rf /root/.npm/_cacache

