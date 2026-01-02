// API Configuration
const API_URL = '/hospital-management/api/patients';
const EPA_API_URL = '/hospital-management/api/epa';

window.currentEditId = null;
window.isInitialLoad = true;
window.currentSearchQuery = "";
window.searchOffset = 0;
window.SEARCH_LIMIT = 100;
window.isLoadingMore = false;
window.hasMoreResults = true;

// Initialize application
function initApp() {
    checkEPAConnection();
    window.isInitialLoad = true;
    loadPatients('');
    attachEventListeners();
}

// Event Listeners
function attachEventListeners() {
    // Search input
    const searchInput = document.getElementById('searchInput');
    let searchTimeout;

    searchInput.addEventListener('input', (e) => {
        clearTimeout(searchTimeout);
        const query = e.target.value.trim();

        if (query.length === 0) {
            window.isInitialLoad = true;
            window.currentSearchQuery = '';
            window.searchOffset = 0;
            window.hasMoreResults = true;
            loadPatients();
            return;
        }

        if (query.length >= 2) {
            searchTimeout = setTimeout(() => {
                loadPatients(query);
            }, 400);
        }
    });

    // Patient form submit
    document.getElementById('patientForm').addEventListener('submit', handlePatientFormSubmit);
}

// Initialize on page load
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initApp);
} else {
    initApp();
}