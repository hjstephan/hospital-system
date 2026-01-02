// EPA Service Functions

async function checkEPAConnection() {
    const banner = document.getElementById('epaStatusBanner');
    const statusText = document.getElementById('epaStatusText');

    try {
        const response = await fetch(`${EPA_API_URL}/test-connection`);

        if (!response.ok) {
            const text = await response.text().catch(() => '');
            console.error('EPA test-connection responded with', response.status, text);
            banner.className = 'epa-status-banner disconnected';
            statusText.textContent = `EPA-Fehler (${response.status})`;
            return;
        }

        let data;
        try {
            data = await response.json();
        } catch (parseErr) {
            const text = await response.text().catch(() => '');
            console.error('Failed to parse JSON from EPA test-connection:', parseErr, text);
            banner.className = 'epa-status-banner disconnected';
            statusText.textContent = 'EPA-Antwort ungültig';
            return;
        }

        if (data && data.connected) {
            banner.className = 'epa-status-banner';
            statusText.textContent = 'EPA-Verbindung aktiv ✓';
        } else {
            banner.className = 'epa-status-banner disconnected';
            statusText.textContent = 'EPA nicht erreichbar ✗';
        }
    } catch (error) {
        console.error('Error checking EPA connection:', error);
        banner.className = 'epa-status-banner disconnected';
        statusText.textContent = 'EPA-Verbindung fehlgeschlagen ✗';
    }
}

async function showEPAStats() {
    try {
        const response = await fetch(`${EPA_API_URL}/statistics`);
        const stats = await response.json();

        const statsHTML = `
            <div class="stats-grid">
                <div class="stat-card">
                    <h3>${stats.total}</h3>
                    <p>Gesamt Patienten</p>
                </div>
                <div class="stat-card">
                    <h3>${stats.epaEnabled}</h3>
                    <p>EPA aktiviert</p>
                </div>
                <div class="stat-card">
                    <h3>${stats.synced}</h3>
                    <p>Synchronisiert</p>
                </div>
                <div class="stat-card">
                    <h3>${stats.errors}</h3>
                    <p>Fehler</p>
                </div>
            </div>
        `;

        document.getElementById('statsContainer').innerHTML = statsHTML;
    } catch (error) {
        await showAlert('Fehler beim Laden der Statistiken: ' + error.message, 'Fehler');
    }
}

async function syncToEPA(patientId) {
    if (!(await showConfirm('Patient jetzt zur EPA synchronisieren?'))) return;

    try {
        const response = await fetch(`${EPA_API_URL}/sync/${patientId}`, {
            method: 'POST'
        });

        const result = await response.json();

        if (result.success) {
            await showAlert('✓ Patient erfolgreich zur EPA synchronisiert!\nEPA-ID: ' + result.epaId, 'Erfolg');
            refreshPatients();
        } else {
            await showAlert('✗ Synchronisation fehlgeschlagen:\n' + result.error, 'Fehler');
        }
    } catch (error) {
        await showAlert('Fehler bei EPA-Synchronisation: ' + error.message, 'Fehler');
    }
}

async function syncAllToEPA() {
    if (!(await showConfirm('Alle Patienten mit EPA-Einwilligung jetzt synchronisieren?'))) return;

    try {
        const response = await fetch(`${EPA_API_URL}/sync-all`, {
            method: 'POST'
        });

        const result = await response.json();
        await showAlert(
            `Synchronisation abgeschlossen:\n✓ Erfolgreich: ${result.success}\n✗ Fehlgeschlagen: ${result.failed}\nGesamt: ${result.total}`,
            'Ergebnis'
        );
        refreshPatients();
    } catch (error) {
        await showAlert('Fehler bei Massen-Synchronisation: ' + error.message, 'Fehler');
    }
}