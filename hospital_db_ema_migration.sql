-- EPA-Erweiterungen für die Patienten-Tabelle
ALTER TABLE patients 
ADD COLUMN epa_id VARCHAR(100) UNIQUE,
ADD COLUMN epa_sync_status VARCHAR(20) DEFAULT 'pending',
ADD COLUMN epa_last_sync TIMESTAMP,
ADD COLUMN epa_sync_error TEXT,
ADD COLUMN epa_enabled BOOLEAN DEFAULT true,
ADD COLUMN epa_consent_date TIMESTAMP;

-- Index für EPA-ID
CREATE INDEX idx_patients_epa_id ON patients(epa_id);
CREATE INDEX idx_patients_epa_sync_status ON patients(epa_sync_status);
CREATE INDEX idx_patients_epa_enabled ON patients(epa_enabled);

-- EPA Sync Log Tabelle für Audit-Trail
CREATE TABLE epa_sync_log (
    id SERIAL PRIMARY KEY,
    patient_id INTEGER REFERENCES patients(id) ON DELETE CASCADE,
    sync_type VARCHAR(20) NOT NULL, -- 'create', 'update', 'fetch'
    sync_status VARCHAR(20) NOT NULL, -- 'success', 'error'
    epa_id VARCHAR(100),
    fhir_payload TEXT,
    error_message TEXT,
    sync_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sync_duration_ms INTEGER,
    user_id VARCHAR(100)
);

-- Index für Sync Log
CREATE INDEX idx_epa_sync_log_patient ON epa_sync_log(patient_id);
CREATE INDEX idx_epa_sync_log_timestamp ON epa_sync_log(sync_timestamp);
CREATE INDEX idx_epa_sync_log_status ON epa_sync_log(sync_status);

-- EPA Konfiguration Tabelle
CREATE TABLE epa_configuration (
    id SERIAL PRIMARY KEY,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT,
    description TEXT,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100)
);

-- Standard EPA-Konfiguration
INSERT INTO epa_configuration (config_key, config_value, description) VALUES
('epa_base_url', 'https://epa-system.example.com/api', 'Basis-URL des EPA-Systems'),
('epa_api_key', '', 'API-Schlüssel für EPA-Zugriff'),
('epa_enabled', 'true', 'Globale EPA-Integration aktiviert/deaktiviert'),
('epa_auto_sync', 'false', 'Automatische Synchronisation bei Änderungen'),
('epa_sync_interval_minutes', '60', 'Intervall für automatische Synchronisation'),
('epa_timeout_seconds', '30', 'Timeout für EPA-Anfragen'),
('epa_retry_attempts', '3', 'Anzahl Wiederholungsversuche bei Fehlern');

-- View für EPA-Statistiken
CREATE OR REPLACE VIEW epa_statistics AS
SELECT 
    COUNT(*) as total_patients,
    SUM(CASE WHEN epa_enabled = true THEN 1 ELSE 0 END) as epa_enabled_count,
    SUM(CASE WHEN epa_sync_status = 'synced' THEN 1 ELSE 0 END) as synced_count,
    SUM(CASE WHEN epa_sync_status = 'pending' THEN 1 ELSE 0 END) as pending_count,
    SUM(CASE WHEN epa_sync_status = 'error' THEN 1 ELSE 0 END) as error_count,
    MAX(epa_last_sync) as last_successful_sync
FROM patients;

-- View für nicht synchronisierte Patienten
CREATE OR REPLACE VIEW epa_pending_patients AS
SELECT 
    id,
    first_name,
    last_name,
    insurance_number,
    epa_sync_status,
    epa_last_sync,
    epa_sync_error,
    EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - COALESCE(epa_last_sync, admission_date)))/3600 as hours_since_last_sync
FROM patients
WHERE epa_enabled = true 
  AND (epa_sync_status = 'pending' OR epa_sync_status = 'error')
ORDER BY admission_date DESC;

-- Funktion für automatische EPA-Synchronisation bei Patient-Änderungen
CREATE OR REPLACE FUNCTION trigger_epa_sync()
RETURNS TRIGGER AS $$
BEGIN
    -- Nur bei EPA-aktivierten Patienten
    IF NEW.epa_enabled = true THEN
        -- Setze Status auf pending bei relevanten Änderungen
        IF OLD.first_name <> NEW.first_name 
           OR OLD.last_name <> NEW.last_name
           OR OLD.date_of_birth <> NEW.date_of_birth
           OR OLD.gender <> NEW.gender
           OR OLD.phone <> NEW.phone
           OR OLD.email <> NEW.email
           OR OLD.address <> NEW.address
           OR OLD.blood_type <> NEW.blood_type
           OR OLD.allergies <> NEW.allergies
           OR OLD.emergency_contact_name <> NEW.emergency_contact_name
           OR OLD.emergency_contact_phone <> NEW.emergency_contact_phone THEN
            
            NEW.epa_sync_status = 'pending';
        END IF;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger für automatische EPA-Status-Änderung
CREATE TRIGGER patient_epa_change_trigger
BEFORE UPDATE ON patients
FOR EACH ROW
EXECUTE FUNCTION trigger_epa_sync();

-- Funktion zum Bereinigen alter Sync Logs (älter als 90 Tage)
CREATE OR REPLACE FUNCTION cleanup_old_epa_logs()
RETURNS void AS $$
BEGIN
    DELETE FROM epa_sync_log 
    WHERE sync_timestamp < CURRENT_TIMESTAMP - INTERVAL '90 days';
END;
$$ LANGUAGE plpgsql;

-- Kommentare für Dokumentation
COMMENT ON COLUMN patients.epa_id IS 'Eindeutige ID des Patienten in der elektronischen Patientenakte';
COMMENT ON COLUMN patients.epa_sync_status IS 'Status der EPA-Synchronisation: pending, synced, error, disabled';
COMMENT ON COLUMN patients.epa_last_sync IS 'Zeitpunkt der letzten erfolgreichen EPA-Synchronisation';
COMMENT ON COLUMN patients.epa_sync_error IS 'Fehlermeldung bei gescheiterter EPA-Synchronisation';
COMMENT ON COLUMN patients.epa_enabled IS 'Gibt an, ob Patient der EPA-Synchronisation zugestimmt hat';
COMMENT ON COLUMN patients.epa_consent_date IS 'Datum der EPA-Einwilligungserklärung';

COMMENT ON TABLE epa_sync_log IS 'Audit-Log für alle EPA-Synchronisationsvorgänge';
COMMENT ON TABLE epa_configuration IS 'Konfigurationsparameter für EPA-Integration';

-- Beispiel-Update für bestehende Patienten
-- Setze EPA-Status für alle bestehenden aktiven Patienten
UPDATE patients 
SET epa_enabled = true, 
    epa_sync_status = 'pending',
    epa_consent_date = CURRENT_TIMESTAMP
WHERE status = 'active' 
  AND epa_enabled IS NULL;