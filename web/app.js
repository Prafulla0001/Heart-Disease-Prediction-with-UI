// Tab Navigation
function switchTab(tabId) {
    document.querySelectorAll('.view-panel').forEach(el => el.classList.add('hidden'));
    document.querySelectorAll('.nav-btn').forEach(el => {
        el.classList.remove('bg-primary/20', 'text-blue-400', 'bg-white/5');
        el.classList.add('text-slate-300');
    });
    
    document.getElementById(`view-${tabId}`).classList.remove('hidden');
    const navBtn = document.getElementById(`nav-${tabId}`);
    if (navBtn) {
        navBtn.classList.add('bg-primary/20', 'text-blue-400');
        navBtn.classList.remove('text-slate-300');
    }

    const titles = { 'dashboard': 'Platform Overview', 'heart': 'Cardiac Risk Screening', 'metabolic': 'BMI & Diet Tools', 'chat': 'Virtual Assistant' };
    document.getElementById('pageTitle').innerText = titles[tabId];
}

// Dark Mode
let isDark = false;
function toggleDarkMode() {
    isDark = !isDark;
    document.documentElement.classList.toggle('dark');
    document.getElementById('themeIcon').className = isDark ? 'fa-solid fa-sun' : 'fa-solid fa-moon';
}

// Radial Risk Gauge Chart (ApexCharts)
let riskChart = null;

function renderRiskChart(score, level) {
    const color = score >= 70 ? '#EF4444' : (score >= 35 ? '#F59E0B' : '#10B981');
    const options = {
        series: [score],
        chart: { type: 'radialBar', height: 260, animations: { enabled: true, easing: 'easeinout', speed: 800 } },
        plotOptions: {
            radialBar: {
                startAngle: -135,
                endAngle: 135,
                hollow: { size: '65%' },
                track: { background: '#f1f5f9', strokeWidth: '100%' },
                dataLabels: {
                    name: { fontSize: '14px', color: '#64748b', offsetY: 20 },
                    value: { offsetY: -10, fontSize: '32px', color: '#1e293b', fontWeight: 800, formatter: (val) => val + "%" }
                }
            }
        },
        fill: { type: 'gradient', gradient: { shade: 'dark', type: 'horizontal', gradientToColors: [color], stops: [0, 100] } },
        stroke: { lineCap: 'round' },
        colors: [color],
        labels: [level]
    };

    if (riskChart) {
        riskChart.updateOptions(options);
    } else {
        riskChart = new ApexCharts(document.querySelector("#risk-chart"), options);
        riskChart.render();
    }
}

// Init empty chart
document.addEventListener("DOMContentLoaded", () => renderRiskChart(0, "Pending Input"));

// Predict Heart Disease API Call
function fillSample(type) {
    if (type === 'healthy') {
        document.getElementById('h-age').value = 45;
        document.getElementById('h-sex').value = 0;
        document.getElementById('h-cp').value = 3;
        document.getElementById('h-bp').value = 115;
        document.getElementById('h-chol').value = 180;
        document.getElementById('h-fbs').value = 0;
        document.getElementById('h-ecg').value = 0;
        document.getElementById('h-hr').value = 175;
        document.getElementById('h-exang').value = 0;
        document.getElementById('h-oldpeak').value = 0.0;
        document.getElementById('h-slope').value = 0;
        document.getElementById('h-ca').value = 0;
        document.getElementById('h-thal').value = 1;
    } else {
        document.getElementById('h-age').value = 62;
        document.getElementById('h-sex').value = 1;
        document.getElementById('h-cp').value = 0;
        document.getElementById('h-bp').value = 160;
        document.getElementById('h-chol').value = 285;
        document.getElementById('h-fbs').value = 1;
        document.getElementById('h-ecg').value = 1;
        document.getElementById('h-hr').value = 112;
        document.getElementById('h-exang').value = 1;
        document.getElementById('h-oldpeak').value = 3.2;
        document.getElementById('h-slope').value = 2;
        document.getElementById('h-ca').value = 2;
        document.getElementById('h-thal').value = 3;
    }
}

async function submitHeartPrediction() {
    const btn = document.getElementById('btnPredictHeart');
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Processing...';
    btn.disabled = true;

    const data = [
        document.getElementById('h-age').value,
        document.getElementById('h-sex').value,
        document.getElementById('h-cp').value,
        document.getElementById('h-bp').value,
        document.getElementById('h-chol').value,
        document.getElementById('h-fbs').value,
        document.getElementById('h-ecg').value,
        document.getElementById('h-hr').value,
        document.getElementById('h-exang').value,
        document.getElementById('h-oldpeak').value,
        document.getElementById('h-slope').value,
        document.getElementById('h-ca').value,
        document.getElementById('h-thal').value
    ];

    try {
        const response = await fetch('/api/predict/heart', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        
        const result = await response.json();
        
        // Update Chart
        renderRiskChart(result.risk_score, result.risk_level);
        
        // Update Titles
        const diagEl = document.getElementById('res-diag');
        if (result.prediction === "Defective Heart") {
            diagEl.innerHTML = `<span class="text-rose-600"><i class="fa-solid fa-triangle-exclamation"></i> Action Required: Cardiac Abnormality Detected</span>`;
            document.getElementById('res-action').innerText = "1. Schedule Medical Consult: Seek evaluation with a cardiologist.\n2. Diagnostic Follow-up: ECG or Angiography recommended.\n3. Lower Sodium & Stress: Enforce <2g sodium daily.";
        } else {
            diagEl.innerHTML = `<span class="text-emerald-600"><i class="fa-solid fa-check-circle"></i> Low Risk: Healthy Cardiac Profile</span>`;
            document.getElementById('res-action').innerText = "1. Maintain Active Lifestyle: 150 min aerobic exercise weekly.\n2. Nutritional Balance: Keep diet low in trans-fats and high in fiber.\n3. Routine Monitoring: Annual checkups for lipids and BP.";
        }

        // Update Risk Factors list
        const factorsEl = document.getElementById('res-factors');
        factorsEl.innerHTML = '';
        if (result.risk_factors && result.risk_factors.length > 0) {
            result.risk_factors.forEach(f => {
                const li = document.createElement('li');
                li.innerText = f;
                factorsEl.appendChild(li);
            });
        } else {
            factorsEl.innerHTML = '<li>All primary biomarkers are within ideal thresholds.</li>';
        }

        document.getElementById('btnExport').disabled = false;

    } catch (e) {
        alert("Error connecting to AI diagnostic server.");
    } finally {
        btn.innerHTML = '<i class="fa-solid fa-microchip"></i> Analyze AI Cardiac Risk';
        btn.disabled = false;
    }
}

// Chatbot UI
const chatForm = document.getElementById('chatForm');
const chatInput = document.getElementById('chatInput');
const chatHistory = document.getElementById('chat-history');

function sendQuickMsg(text) {
    chatInput.value = text;
    chatForm.dispatchEvent(new Event('submit'));
}

chatForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const text = chatInput.value.trim();
    if (!text) return;
    
    appendMessage(text, 'user');
    chatInput.value = '';
    
    // Add typing indicator
    const typingId = 'typing-' + Date.now();
    const typingHtml = `
        <div id="${typingId}" class="flex items-start gap-3 w-full">
            <div class="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 flex-shrink-0 mt-1"><i class="fa-solid fa-robot text-sm"></i></div>
            <div class="chat-bubble-bot p-4 px-5 max-w-[80%] flex items-center gap-1.5">
                <div class="w-2 h-2 bg-slate-400 rounded-full animate-bounce"></div>
                <div class="w-2 h-2 bg-slate-400 rounded-full animate-bounce" style="animation-delay: 0.1s"></div>
                <div class="w-2 h-2 bg-slate-400 rounded-full animate-bounce" style="animation-delay: 0.2s"></div>
            </div>
        </div>`;
    chatHistory.insertAdjacentHTML('beforeend', typingHtml);
    chatHistory.scrollTop = chatHistory.scrollHeight;

    try {
        const response = await fetch('/api/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ message: text })
        });
        const resJson = await response.json();
        
        document.getElementById(typingId).remove();
        appendMessage(resJson.reply || "Sorry, I encountered an issue.", 'bot');
    } catch(err) {
        document.getElementById(typingId).remove();
        appendMessage("Network error: Could not reach the chatbot service.", 'bot');
    }
});

function appendMessage(text, sender) {
    const time = new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
    let html = '';
    
    if (sender === 'user') {
        html = `
        <div class="flex items-start justify-end gap-3 w-full">
            <div class="flex flex-col items-end">
                <span class="text-[10px] text-slate-400 mb-1 mr-1">You, ${time}</span>
                <div class="chat-bubble-user p-3.5 max-w-[80%] text-sm leading-relaxed">${text}</div>
            </div>
        </div>`;
    } else {
        html = `
        <div class="flex items-start gap-3 w-full">
            <div class="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 flex-shrink-0 mt-4"><i class="fa-solid fa-robot text-sm"></i></div>
            <div class="flex flex-col items-start w-full">
                <span class="text-[10px] text-slate-400 mb-1 ml-1">CardioSaaS AI, ${time}</span>
                <div class="chat-bubble-bot p-3.5 max-w-[80%] text-sm leading-relaxed whitespace-pre-wrap">${text}</div>
            </div>
        </div>`;
    }
    
    chatHistory.insertAdjacentHTML('beforeend', html);
    chatHistory.scrollTop = chatHistory.scrollHeight;
}