// All API calls go through the Gateway
const RENTAL_API   = '/api/rentals';
const PAYMENT_API  = '/api/payments';
const DAMAGE_API   = '/api/damage-reports';
const PENALTY_API  = '/api/penalties';

// ─── Navigation ───────────────────────────────────────────────────────────────

function showService(name) {
    document.querySelectorAll('.service-section').forEach(s => s.classList.remove('active'));
    document.querySelectorAll('.service-btn').forEach(b => b.classList.remove('active'));
    document.getElementById(`${name}-section`).classList.add('active');
    event.target.classList.add('active');
}

function showTab(service, tab) {
    const section = document.getElementById(`${service}-section`);
    section.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    section.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
    event.target.classList.add('active');
    document.getElementById(`${service}-${tab}-tab`).classList.add('active');
    if (service === 'rental' && tab === 'list') loadRentals();
    if (service === 'payment' && tab === 'list') loadPayments();
    if (service === 'damage' && tab === 'list') loadDamages();
}

// ─── Utilities ────────────────────────────────────────────────────────────────

function showResponse(service, type, message, data = null) {
    const div = document.getElementById(`${service}-response`);
    div.className = `response ${type}`;
    div.innerHTML = `<p><strong>${message}</strong></p>
        ${data ? `<pre>${JSON.stringify(data, null, 2)}</pre>` : ''}`;
    div.style.display = 'block';
    setTimeout(() => div.style.display = 'none', 10000);
}

function formatDate(d) {
    return d ? new Date(d).toLocaleDateString('vi-VN') : '—';
}

function formatMoney(n) {
    return n != null ? new Intl.NumberFormat('vi-VN').format(n) + ' ₫' : '—';
}

// ─── RENTAL SERVICE ───────────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
    const today = new Date();
    const start = new Date(today); start.setDate(start.getDate() + 1);
    const end   = new Date(today); end.setDate(end.getDate() + 7);
    document.getElementById('r-startDate').valueAsDate = start;
    document.getElementById('r-endDate').valueAsDate   = end;

    document.getElementById('rental-create-form').addEventListener('submit', createRental);
    document.getElementById('inspection-form').addEventListener('submit', submitInspection);
    document.getElementById('payment-create-form').addEventListener('submit', createPayment);
    document.getElementById('process-form').addEventListener('submit', processPayment);
    document.getElementById('refund-form').addEventListener('submit', createRefund);
    document.getElementById('damage-create-form').addEventListener('submit', createDamageReport);
    document.getElementById('damage-update-form').addEventListener('submit', updateDamageStatus);

    loadRentals();
});

async function createRental(e) {
    e.preventDefault();
    const data = {
        customerId:     document.getElementById('r-customerId').value.trim(),
        vehicleId:      document.getElementById('r-vehicleId').value.trim(),
        startDate:      document.getElementById('r-startDate').value + 'T00:00:00',
        endDate:        document.getElementById('r-endDate').value + 'T23:59:59',
        pickupLocation: document.getElementById('r-pickupLocation').value.trim(),
        returnLocation: document.getElementById('r-returnLocation').value.trim(),
        dailyRate:      parseFloat(document.getElementById('r-dailyRate').value),
        depositAmount:  parseFloat(document.getElementById('r-depositAmount').value),
    };
    try {
        const res = await fetch(RENTAL_API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        const result = await res.json();
        if (res.ok) {
            showResponse('rental', 'success', `✅ Tạo đơn thuê thành công! ID: ${result.rentalId}`, result);
            document.getElementById('rental-create-form').reset();
        } else {
            showResponse('rental', 'error', '❌ Lỗi: ' + (result.message || res.status), result);
        }
    } catch (err) {
        showResponse('rental', 'error', '❌ Không kết nối được: ' + err.message);
    }
}

async function loadRentals() {
    const status = document.getElementById('r-statusFilter').value;
    let url = RENTAL_API + '?page=0&size=20';
    if (status) url += `&status=${status}`;
    try {
        const res = await fetch(url);
        const data = await res.json();
        const rentals = data.content || [];
        const div = document.getElementById('rentals-list');
        if (!rentals.length) { div.innerHTML = '<p class="empty">Không có đơn thuê nào</p>'; return; }
        div.innerHTML = rentals.map(r => `
            <div class="list-item">
                <div class="item-header">
                    <strong>ID: ${r.rentalId}</strong>
                    <span class="status status-${r.status.toLowerCase()}">${r.status}</span>
                </div>
                <div class="item-details">
                    <p>👤 Customer: ${r.customerId} &nbsp;|&nbsp; 🚗 Vehicle: ${r.vehicleId}</p>
                    <p>📅 ${formatDate(r.startDate)} → ${formatDate(r.endDate)}</p>
                    <p>💰 Tổng: ${formatMoney(r.totalCost)} &nbsp;|&nbsp; Cọc: ${formatMoney(r.depositAmount)}</p>
                    ${r.penaltyAmount > 0 ? `<p class="penalty-text">⚠️ Phạt: ${formatMoney(r.penaltyAmount)}</p>` : ''}
                </div>
            </div>`).join('');
    } catch (err) {
        showResponse('rental', 'error', '❌ Không tải được danh sách: ' + err.message);
    }
}

async function rentalAction(action) {
    const id = document.getElementById('r-actionId').value.trim();
    if (!id) { alert('Vui lòng nhập Rental ID'); return; }
    const map = {
        confirm: `${RENTAL_API}/${id}/confirm`,
        pickup:  `${RENTAL_API}/${id}/pickup`,
        return:  `${RENTAL_API}/${id}/return`,
        complete:`${RENTAL_API}/${id}/complete`,
        cancel:  `${RENTAL_API}/${id}/cancel?reason=User+cancelled`,
    };
    try {
        const res = await fetch(map[action], { method: 'PATCH' });
        const result = await res.json();
        if (res.ok) showResponse('rental', 'success', `✅ ${action.toUpperCase()} thành công!`, result);
        else        showResponse('rental', 'error', '❌ Lỗi: ' + (result.message || res.status), result);
        loadRentals();
    } catch (err) {
        showResponse('rental', 'error', '❌ Lỗi: ' + err.message);
    }
}

async function getRentalHistory() {
    const id = document.getElementById('r-actionId').value.trim();
    if (!id) { alert('Vui lòng nhập Rental ID'); return; }
    try {
        const res = await fetch(`${RENTAL_API}/${id}/history`);
        const result = await res.json();
        if (res.ok) showResponse('rental', 'success', `📜 Lịch sử đơn thuê`, result);
        else        showResponse('rental', 'error', '❌ Không tìm thấy', result);
    } catch (err) {
        showResponse('rental', 'error', '❌ Lỗi: ' + err.message);
    }
}

async function submitInspection(e) {
    e.preventDefault();
    const id = document.getElementById('r-inspectionId').value.trim();
    const data = {
        hasDamage: document.getElementById('r-hasDamage').checked,
        inspectionNotes: document.getElementById('r-inspectionNotes').value,
    };
    try {
        const res = await fetch(`${RENTAL_API}/${id}/inspection`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        const result = await res.json();
        if (res.ok) {
            showResponse('rental', 'success', '✅ Hoàn tất kiểm tra!', result);
            document.getElementById('inspection-form').reset();
            if (data.hasDamage) alert('⚠️ Phát hiện hư hại! Hãy tạo báo cáo trong tab Damage & Penalty.');
        } else {
            showResponse('rental', 'error', '❌ Lỗi: ' + (result.message || res.status), result);
        }
    } catch (err) {
        showResponse('rental', 'error', '❌ Lỗi: ' + err.message);
    }
}

// ─── PAYMENT SERVICE ──────────────────────────────────────────────────────────

async function createPayment(e) {
    e.preventDefault();
    const data = {
        rentalId:    document.getElementById('p-rentalId').value.trim(),
        customerId:  document.getElementById('p-customerId').value.trim(),
        paymentType: document.getElementById('p-paymentType').value,
        amount:      parseFloat(document.getElementById('p-amount').value),
        paymentMethod: document.getElementById('p-method').value,
        description: document.getElementById('p-description').value,
    };
    try {
        const res = await fetch(PAYMENT_API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        const result = await res.json();
        if (res.ok) {
            showResponse('payment', 'success', `✅ Tạo thanh toán thành công! ID: ${result.paymentId}`, result);
            document.getElementById('payment-create-form').reset();
        } else {
            showResponse('payment', 'error', '❌ Lỗi: ' + (result.message || res.status), result);
        }
    } catch (err) {
        showResponse('payment', 'error', '❌ Không kết nối được: ' + err.message);
    }
}

async function loadPayments() {
    const status   = document.getElementById('p-statusFilter').value;
    const rentalId = document.getElementById('p-rentalIdFilter').value.trim();
    let url = PAYMENT_API + '?page=0&size=20';
    if (status) url += `&status=${status}`;
    try {
        const res  = await fetch(url);
        const data = await res.json();
        let payments = data.content || [];
        if (rentalId) payments = payments.filter(p => p.rentalId && p.rentalId.includes(rentalId));
        const div = document.getElementById('payments-list');
        if (!payments.length) { div.innerHTML = '<p class="empty">Không có thanh toán nào</p>'; return; }
        div.innerHTML = payments.map(p => `
            <div class="list-item">
                <div class="item-header">
                    <strong>ID: ${p.paymentId}</strong>
                    <span class="status status-${p.status.toLowerCase()}">${p.status}</span>
                </div>
                <div class="item-details">
                    <p>📋 Rental: ${p.rentalId} &nbsp;|&nbsp; 👤 Customer: ${p.customerId}</p>
                    <p>💳 ${p.paymentType} — ${p.paymentMethod}</p>
                    <p>💰 ${formatMoney(p.amount)} &nbsp;|&nbsp; 📅 ${formatDate(p.createdAt)}</p>
                    ${p.description ? `<p>📝 ${p.description}</p>` : ''}
                </div>
            </div>`).join('');
    } catch (err) {
        showResponse('payment', 'error', '❌ Không tải được danh sách: ' + err.message);
    }
}

async function processPayment(e) {
    e.preventDefault();
    const id   = document.getElementById('p-processId').value.trim();
    const data = { transactionReference: document.getElementById('p-transactionRef').value };
    try {
        const res = await fetch(`${PAYMENT_API}/${id}/process`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        const result = await res.json();
        if (res.ok) {
            showResponse('payment', 'success', '✅ Xử lý thanh toán thành công!', result);
            document.getElementById('process-form').reset();
            loadPayments();
        } else {
            showResponse('payment', 'error', '❌ Lỗi: ' + (result.message || res.status), result);
        }
    } catch (err) {
        showResponse('payment', 'error', '❌ Lỗi: ' + err.message);
    }
}

async function createRefund(e) {
    e.preventDefault();
    const id     = document.getElementById('p-refundId').value.trim();
    const amount = document.getElementById('p-refundAmount').value;
    const reason = document.getElementById('p-refundReason').value;
    try {
        const res = await fetch(
            `${PAYMENT_API}/${id}/refund?amount=${amount}&reason=${encodeURIComponent(reason)}`,
            { method: 'POST' }
        );
        const result = await res.json();
        if (res.ok) {
            showResponse('payment', 'success', '✅ Tạo hoàn tiền thành công!', result);
            document.getElementById('refund-form').reset();
            loadPayments();
        } else {
            showResponse('payment', 'error', '❌ Lỗi: ' + (result.message || res.status), result);
        }
    } catch (err) {
        showResponse('payment', 'error', '❌ Lỗi: ' + err.message);
    }
}

// ─── DAMAGE & PENALTY SERVICE ─────────────────────────────────────────────────

async function createDamageReport(e) {
    e.preventDefault();
    const data = {
        rentalId:    document.getElementById('d-rentalId').value.trim(),
        vehicleId:   document.getElementById('d-vehicleId').value.trim(),
        damageType:  document.getElementById('d-damageType').value,
        severity:    document.getElementById('d-severity').value,
        description: document.getElementById('d-description').value.trim(),
        reportedBy:  'STAFF',
    };
    try {
        const res = await fetch(DAMAGE_API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        const result = await res.json();
        if (res.ok) {
            showResponse('damage', 'success', `✅ Tạo báo cáo hư hại thành công! ID: ${result.damageId}`, result);
            document.getElementById('damage-create-form').reset();
        } else {
            showResponse('damage', 'error', '❌ Lỗi: ' + (result.message || res.status), result);
        }
    } catch (err) {
        showResponse('damage', 'error', '❌ Không kết nối được: ' + err.message);
    }
}

async function loadDamages() {
    const status = document.getElementById('d-statusFilter').value;
    let url = DAMAGE_API + '?page=0&size=20';
    if (status) url += `&status=${status}`;
    try {
        const res  = await fetch(url);
        const data = await res.json();
        const damages = data.content || [];
        const div = document.getElementById('damages-list');
        if (!damages.length) { div.innerHTML = '<p class="empty">Không có báo cáo hư hại nào</p>'; return; }
        div.innerHTML = damages.map(d => `
            <div class="list-item">
                <div class="item-header">
                    <strong>ID: ${d.damageId}</strong>
                    <span class="status status-${d.status.toLowerCase()}">${d.status}</span>
                </div>
                <div class="item-details">
                    <p>🚗 Rental: ${d.rentalId} &nbsp;|&nbsp; 🚙 Vehicle: ${d.vehicleId}</p>
                    <p>⚠️ ${d.damageType} — Mức độ: ${d.severity}</p>
                    <p>📝 ${d.description}</p>
                    <p>💰 Chi phí sửa: ${formatMoney(d.repairCost)}</p>
                    <p>📅 ${formatDate(d.reportedDate)}</p>
                </div>
            </div>`).join('');
    } catch (err) {
        showResponse('damage', 'error', '❌ Không tải được danh sách: ' + err.message);
    }
}

async function updateDamageStatus(e) {
    e.preventDefault();
    const id         = document.getElementById('d-damageId').value.trim();
    const status     = document.getElementById('d-newStatus').value;
    const repairCost = document.getElementById('d-repairCost').value;
    try {
        const res = await fetch(`${DAMAGE_API}/${id}?status=${status}&repairCost=${repairCost}`, {
            method: 'PATCH'
        });
        const result = await res.json();
        if (res.ok) {
            showResponse('damage', 'success', `✅ Cập nhật thành công! Status: ${status}`, result);
            document.getElementById('damage-update-form').reset();
            loadDamages();
        } else {
            showResponse('damage', 'error', '❌ Lỗi: ' + (result.message || res.status), result);
        }
    } catch (err) {
        showResponse('damage', 'error', '❌ Lỗi: ' + err.message);
    }
}
