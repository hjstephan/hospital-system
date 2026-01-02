// UI Utility Functions

function escapeHtml(unsafe) {
    return String(unsafe)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function showDialog(message, options = {}) {
    const title = options.title || 'Hinweis';
    const confirm = options.confirm || false;
    const okText = options.okText || 'OK';
    const cancelText = options.cancelText || 'Abbrechen';

    return new Promise((resolve) => {
        let modal = document.getElementById('dialogModal');
        if (!modal) {
            modal = document.createElement('div');
            modal.id = 'dialogModal';
            modal.className = 'modal';
            modal.innerHTML = `
                <div class="modal-content">
                    <div class="modal-header">
                        <h2 id="dialogTitle"></h2>
                        <button class="close-btn" id="dialogCloseBtn">&times;</button>
                    </div>
                    <div class="modal-body" id="dialogBody"></div>
                    <div style="padding:16px; display:flex; justify-content:flex-end; gap:8px;">
                        <button id="dialogCancelBtn" class="btn-danger">${escapeHtml(cancelText)}</button>
                        <button id="dialogOkBtn" class="btn-primary">${escapeHtml(okText)}</button>
                    </div>
                </div>`;
            document.body.appendChild(modal);
        }

        const titleEl = document.getElementById('dialogTitle');
        const bodyEl = document.getElementById('dialogBody');
        const okBtn = document.getElementById('dialogOkBtn');
        const cancelBtn = document.getElementById('dialogCancelBtn');
        const closeBtn = document.getElementById('dialogCloseBtn');

        titleEl.textContent = title;
        bodyEl.innerHTML = `<p style="white-space:pre-wrap;">${escapeHtml(message)}</p>`;

        okBtn.textContent = okText;
        cancelBtn.textContent = cancelText;
        cancelBtn.style.display = confirm ? 'inline-block' : 'none';

        function cleanup() {
            modal.classList.remove('active');
            okBtn.onclick = null;
            cancelBtn.onclick = null;
            closeBtn.onclick = null;
        }

        okBtn.onclick = () => { cleanup(); resolve(true); };
        cancelBtn.onclick = () => { cleanup(); resolve(false); };
        closeBtn.onclick = () => { cleanup(); resolve(false); };

        modal.classList.add('active');
    });
}

async function showAlert(message, title = 'Hinweis') {
    await showDialog(message, { title, confirm: false, okText: 'OK' });
}

async function showConfirm(message, title = 'Bitte bestätigen') {
    return await showDialog(message, { title, confirm: true, okText: 'Ja', cancelText: 'Nein' });
}

function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('de-DE');
}

function openModal(editMode = false) {
    document.getElementById('patientModal').classList.add('active');
    document.getElementById('modalTitle').textContent = editMode ? 'Patient bearbeiten' : 'Neuer Patient';
}

function closeModal() {
    document.getElementById('patientModal').classList.remove('active');
    document.getElementById('patientForm').reset();
    window.currentEditId = null;
}