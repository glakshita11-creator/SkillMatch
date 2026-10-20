const fs = require('fs');
const path = require('path');

const targetDir = 'C:\\Users\\glaks\\.gemini\\antigravity-ide\\brain\\3c93dff7-1752-467d-ab96-f661bb107e57\\screenshots';
if (!fs.existsSync(targetDir)) {
    fs.mkdirSync(targetDir, { recursive: true });
}

let messageId = 1;
function sendCommand(ws, method, params = {}) {
    return new Promise((resolve, reject) => {
        const id = messageId++;
        const handler = (evt) => {
            const data = JSON.parse(evt.data);
            if (data.id === id) {
                ws.removeEventListener('message', handler);
                if (data.error) reject(data.error);
                else resolve(data.result);
            }
        };
        ws.addEventListener('message', handler);
        ws.send(JSON.stringify({ id, method, params }));
    });
}

function sleep(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms));
}

async function capture(ws, filename) {
    const res = await sendCommand(ws, 'Page.captureScreenshot', { format: 'png' });
    const buffer = Buffer.from(res.data, 'base64');
    const dest = path.join(targetDir, filename);
    fs.writeFileSync(dest, buffer);
    console.log(`Saved screenshot: ${filename} (${buffer.length} bytes)`);
}

async function run() {
    // 1. Create a new tab
    const newTabRes = await fetch('http://localhost:9222/json/new?about:blank', { method: 'PUT' });
    const tab = await newTabRes.json();
    console.log('Opened tab:', tab.id);

    const ws = new WebSocket(tab.webSocketDebuggerUrl);

    await new Promise((resolve, reject) => {
        ws.onopen = resolve;
        ws.onerror = reject;
    });

    await sendCommand(ws, 'Page.enable');
    await sendCommand(ws, 'Runtime.enable');
    await sendCommand(ws, 'DOM.enable');

    try {
        // --- SCREEN 1: Landing Page ---
        console.log('Navigating to landing page...');
        await sendCommand(ws, 'Page.navigate', { url: 'http://localhost:8080/index.html' });
        await sleep(2000);
        await capture(ws, '01_landing_page.png');

        // --- SCREEN 2: Opportunities Catalog ---
        console.log('Navigating to opportunities...');
        await sendCommand(ws, 'Page.navigate', { url: 'http://localhost:8080/opportunities.html' });
        await sleep(1500);
        await capture(ws, '02_opportunities.png');

        // --- SCREEN 3: Login Portal ---
        console.log('Navigating to login page...');
        await sendCommand(ws, 'Page.navigate', { url: 'http://localhost:8080/login.html' });
        await sleep(1000);
        await capture(ws, '03_login_page.png');

        // --- LOGIN AS STUDENT ---
        console.log('Logging in as student...');
        await sendCommand(ws, 'Runtime.evaluate', {
            expression: `
                document.getElementById('login-email').value = 'arun.kumar@student.edu';
                document.getElementById('login-password').value = 'Password@123';
                document.getElementById('btn-submit-login').click();
            `
        });
        await sleep(2000);

        // --- SCREEN 4: Student Dashboard ---
        await capture(ws, '04_student_dashboard.png');

        // --- SCREEN 5: Student Applications ---
        console.log('Navigating to student applications...');
        await sendCommand(ws, 'Page.navigate', { url: 'http://localhost:8080/student-applications.html' });
        await sleep(1500);
        await capture(ws, '05_student_applications.png');

        // --- SCREEN 6: Visual Journey Stepper Modal ---
        console.log('Opening Track Status visual stepper...');
        await sendCommand(ws, 'Runtime.evaluate', {
            expression: `
                const trackBtns = document.querySelectorAll('button');
                for (const b of trackBtns) {
                    if (b.textContent.includes('Track Status')) {
                        b.click();
                        break;
                    }
                }
            `
        });
        await sleep(1000);
        await capture(ws, '06_student_journey_stepper.png');

        // --- LOGOUT & LOGIN AS RECRUITER ---
        console.log('Logging in as Recruiter...');
        await sendCommand(ws, 'Page.navigate', { url: 'http://localhost:8080/login.html' });
        await sleep(1000);
        await sendCommand(ws, 'Runtime.evaluate', {
            expression: `
                document.getElementById('login-email').value = 'recruiter@techcorp.com';
                document.getElementById('login-password').value = 'Password@123';
                document.getElementById('btn-submit-login').click();
            `
        });
        await sleep(2000);

        // --- SCREEN 7: Company Recruiter Dashboard ---
        await capture(ws, '07_company_dashboard.png');

        // --- SCREEN 8: Company Applicant Review Pipeline ---
        console.log('Navigating to recruiter applicants pipeline...');
        await sendCommand(ws, 'Page.navigate', { url: 'http://localhost:8080/company-applicants.html' });
        await sleep(1500);
        await capture(ws, '08_company_pipeline.png');

        // --- LOGOUT & LOGIN AS ADMIN ---
        console.log('Logging in as Administrator...');
        await sendCommand(ws, 'Page.navigate', { url: 'http://localhost:8080/login.html' });
        await sleep(1000);
        await sendCommand(ws, 'Runtime.evaluate', {
            expression: `
                document.getElementById('login-email').value = 'admin@skillmatch.edu';
                document.getElementById('login-password').value = 'Admin@123';
                document.getElementById('btn-submit-login').click();
            `
        });
        await sleep(2000);

        // --- SCREEN 9: Admin Dashboard ---
        await capture(ws, '09_admin_dashboard.png');

        // --- SCREEN 10: Admin Opportunity Audit ---
        console.log('Navigating to admin opportunities...');
        await sendCommand(ws, 'Page.navigate', { url: 'http://localhost:8080/admin-opportunities.html' });
        await sleep(1500);
        await capture(ws, '10_admin_audit.png');

        console.log('ALL 10 OUTPUT SCREENS CAPTURED SUCCESSFULLY!');
    } finally {
        ws.close();
        await fetch(`http://localhost:9222/json/close/${tab.id}`, { method: 'GET' }).catch(() => {});
    }
}

run().catch(err => {
    console.error('Capture script error:', err);
    process.exit(1);
});
