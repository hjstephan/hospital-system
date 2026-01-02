// Patient Service - API calls

async function loadPatients(filter = '') {
    const tableDiv = document.getElementById('patientsTable');

    // Initial: Zeige zufällige Patienten
    if (window.isInitialLoad && !filter) {
        tableDiv.innerHTML = '<div class="loading">Lade initiale Patientenauswahl...</div>';
        try {
            const response = await fetch(`${API_URL}/random?limit=50`);
            if (!response.ok) {
                throw new Error(`Status ${response.status}`);
            }
            const patients = await response.json();
            renderPatients(patients, false);
            window.isInitialLoad = false;
        } catch (error) {
            tableDiv.innerHTML = `<p style="text-align: center; padding: 40px; color: #6c757d;">Fehler: ${error.message}</p>`;
        }
        return;
    }

    // Bei Filterung: Reset und neue Suche
    if (filter !== window.currentSearchQuery) {
        window.currentSearchQuery = filter;
        window.searchOffset = 0;
        window.hasMoreResults = true;
        tableDiv.innerHTML = '<div class="loading">Suche Patienten...</div>';
    }

    // Keine Suche ohne Filter nach Initial-Load
    if (!filter) {
        return;
    }

    try {
        const url = `${API_URL}/search?q=${encodeURIComponent(filter)}&offset=${window.searchOffset}&limit=${window.SEARCH_LIMIT}`;
        const response = await fetch(url);

        if (!response.ok) {
            throw new Error(`Status ${response.status}`);
        }

        const data = await response.json();
        window.hasMoreResults = data.hasMore;

        if (window.searchOffset === 0) {
            renderPatients(data.patients, true, data.total);
        } else {
            appendPatients(data.patients);
        }

        window.isLoadingMore = false;
    } catch (error) {
        tableDiv.innerHTML = `<p style="text-align: center; padding: 40px; color: #6c757d;">Fehler: ${error.message}</p>`;
        window.isLoadingMore = false;
    }
}

async function editPatient(id) {
    try {
        const response = await fetch(`${API_URL}/${id}`);
        const patient = await response.json();

        window.currentEditId = id;
        document.getElementById('firstName').value = patient.firstName;
        document.getElementById('lastName').value = patient.lastName;
        document.getElementById('dateOfBirth').value = patient.dateOfBirth;
        document.getElementById('gender').value = patient.gender;
        document.getElementById('phone').value = patient.phone || '';
        document.getElementById('email').value = patient.email || '';
        document.getElementById('address').value = patient.address || '';
        document.getElementById('insuranceNumber').value = patient.insuranceNumber;
        document.getElementById('bloodType').value = patient.bloodType || '';
        document.getElementById('allergies').value = patient.allergies || '';
        document.getElementById('emergencyContactName').value = patient.emergencyContactName || '';
        document.getElementById('emergencyContactPhone').value = patient.emergencyContactPhone || '';
        document.getElementById('status').value = patient.status;
        document.getElementById('epaEnabled').checked = patient.epaEnabled !== false;

        openModal(true);
    } catch (error) {
        await showAlert('Fehler beim Laden des Patienten: ' + error.message, 'Fehler');
    }
}

async function deletePatient(id) {
    if (!(await showConfirm('Möchten Sie diesen Patienten wirklich löschen?'))) {
        return;
    }

    try {
        const response = await fetch(`${API_URL}/${id}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            await showAlert('Patient erfolgreich gelöscht', 'Erfolg');
            refreshPatients();
        } else {
            await showAlert('Fehler beim Löschen', 'Fehler');
        }
    } catch (error) {
        await showAlert('Fehler: ' + error.message, 'Fehler');
    }
}

async function handlePatientFormSubmit(e) {
    e.preventDefault();

    const patientData = {
        firstName: document.getElementById('firstName').value,
        lastName: document.getElementById('lastName').value,
        dateOfBirth: document.getElementById('dateOfBirth').value,
        gender: document.getElementById('gender').value,
        phone: document.getElementById('phone').value,
        email: document.getElementById('email').value,
        address: document.getElementById('address').value,
        insuranceNumber: document.getElementById('insuranceNumber').value,
        bloodType: document.getElementById('bloodType').value,
        allergies: document.getElementById('allergies').value,
        emergencyContactName: document.getElementById('emergencyContactName').value,
        emergencyContactPhone: document.getElementById('emergencyContactPhone').value,
        status: document.getElementById('status').value,
        epaEnabled: document.getElementById('epaEnabled').checked
    };

    try {
        const url = window.currentEditId ? `${API_URL}/${window.currentEditId}` : API_URL;
        const method = window.currentEditId ? 'PUT' : 'POST';

        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(patientData)
        });

        if (response.ok) {
            const savedPatient = await response.json();
            await showAlert(
                window.currentEditId ? 'Patient erfolgreich aktualisiert' : 'Patient erfolgreich erstellt',
                'Erfolg'
            );

            // EPA-Einwilligung setzen
            if (savedPatient.id && patientData.epaEnabled) {
                await fetch(`${EPA_API_URL}/consent/${savedPatient.id}?enabled=true`, {
                    method: 'PUT'
                });
            }

            closeModal();
            refreshPatients();
        } else {
            const error = await response.json();
            await showAlert('Fehler: ' + (error.error || 'Unbekannter Fehler'), 'Fehler');
        }
    } catch (error) {
        await showAlert('Fehler: ' + error.message, 'Fehler');
    }
}

function refreshPatients() {
    const searchInput = document.getElementById('searchInput');
    searchInput.value = '';
    window.isInitialLoad = true;
    window.currentSearchQuery = '';
    window.searchOffset = 0;
    window.hasMoreResults = true;
    loadPatients();
}