// Patient Rendering Functions

function renderPatients(patients, sorted = false, totalCount = null) {
    const tableDiv = document.getElementById('patientsTable');

    if (!Array.isArray(patients) || patients.length === 0) {
        tableDiv.innerHTML = '<p style="text-align: center; padding: 40px; color: #6c757d;">Keine Patienten gefunden</p>';
        return;
    }

    let headerInfo = sorted && totalCount ?
        `<p style="margin-bottom: 10px; color: var(--text-secondary);">Gefunden: ${totalCount} Patienten</p>` :
        `<p style="margin-bottom: 10px; color: var(--text-secondary);">Zufällige Auswahl von ${patients.length} Patienten</p>`;

    let html = headerInfo + `
        <div id="tableContainer">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Geburtsdatum</th>
                        <th>Vers.-Nr.</th>
                        <th>Blutgruppe</th>
                        <th>Status</th>
                        <th>EPA-Status</th>
                        <th>Aktionen</th>
                    </tr>
                </thead>
                <tbody id="patientTableBody">
    `;

    patients.forEach(p => {
        html += createPatientRow(p);
    });

    html += '</tbody></table>';

    if (sorted && window.hasMoreResults) {
        html += '<div id="loadMoreContainer" style="text-align: center; padding: 20px;"><button class="btn-primary" onclick="loadMorePatients()">Weitere Ergebnisse laden</button></div>';
    }

    html += '</div>';
    tableDiv.innerHTML = html;

    if (sorted && window.hasMoreResults) {
        setupInfiniteScroll();
    }
}

function createPatientRow(p) {
    return `
        <tr>
            <td>${p.id}</td>
            <td><strong>${p.firstName} ${p.lastName}</strong></td>
            <td>${formatDate(p.dateOfBirth)}</td>
            <td>${p.insuranceNumber}</td>
            <td>${p.bloodType || '-'}</td>
            <td><span class="status-badge">${p.status === 'active' ? 'Aktiv' : 'Entlassen'}</span></td>
            <td><span class="epa-badge">${p.epaEnabled ? (p.epaSyncStatus || 'Pending') : 'Aus'}</span></td>
            <td>
                <div class="action-buttons">
                    <button class="btn-edit" title="Patient bearbeiten" onclick="editPatient(${p.id})">✏️</button>
                    <button class="btn-edit" title="Zur EPA synchronisieren" onclick="syncToEPA(${p.id})">🔄</button>
                    <button class="btn-edit" title="Patient löschen" onclick="deletePatient(${p.id})">❌</button>
                </div>
            </td>
        </tr>
    `;
}

function appendPatients(patients) {
    const tbody = document.getElementById('patientTableBody');
    if (!tbody) return;

    patients.forEach(p => {
        const row = createPatientRow(p);
        const rowContent = row.replace(/<\/?tr>/g, '');
        const tr = document.createElement('tr');
        tr.innerHTML = rowContent;
        tbody.appendChild(tr);
    });

    const loadMoreBtn = document.querySelector('#loadMoreContainer button');
    if (loadMoreBtn) {
        loadMoreBtn.textContent = 'Weitere Ergebnisse laden';
        loadMoreBtn.disabled = false;
    }
}

async function loadMorePatients() {
    if (window.isLoadingMore || !window.hasMoreResults) return;

    window.isLoadingMore = true;
    window.searchOffset += window.SEARCH_LIMIT;

    const loadMoreBtn = document.querySelector('#loadMoreContainer button');
    if (loadMoreBtn) {
        loadMoreBtn.textContent = 'Lädt...';
        loadMoreBtn.disabled = true;
    }

    await loadPatients(window.currentSearchQuery);

    if (!window.hasMoreResults) {
        const container = document.getElementById('loadMoreContainer');
        if (container) container.remove();
    }
}

function setupInfiniteScroll() {
    const tableContainer = document.getElementById('tableContainer');
    if (!tableContainer) return;

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting && window.hasMoreResults && !window.isLoadingMore && window.currentSearchQuery) {
                loadMorePatients();
            }
        });
    }, { threshold: 0.5 });

    const loadMoreContainer = document.getElementById('loadMoreContainer');
    if (loadMoreContainer) {
        observer.observe(loadMoreContainer);
    }
}