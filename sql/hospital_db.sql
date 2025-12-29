--
-- PostgreSQL database dump
--

\restrict PUEbyjixGf2auNHZlYjb4n13ivN71MfzTSRQaJu30xPVmgzEtudKYWCv7azVou0

-- Dumped from database version 16.11 (Ubuntu 16.11-0ubuntu0.24.04.1)
-- Dumped by pg_dump version 16.11 (Ubuntu 16.11-0ubuntu0.24.04.1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: cleanup_old_epa_logs(); Type: FUNCTION; Schema: public; Owner: hospital_admin
--

CREATE FUNCTION public.cleanup_old_epa_logs() RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
    DELETE FROM epa_sync_log 
    WHERE sync_timestamp < CURRENT_TIMESTAMP - INTERVAL '90 days';
END;
$$;


ALTER FUNCTION public.cleanup_old_epa_logs() OWNER TO hospital_admin;

--
-- Name: trigger_epa_sync(); Type: FUNCTION; Schema: public; Owner: hospital_admin
--

CREATE FUNCTION public.trigger_epa_sync() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.trigger_epa_sync() OWNER TO hospital_admin;

--
-- Name: update_updated_at_column(); Type: FUNCTION; Schema: public; Owner: hospital_admin
--

CREATE FUNCTION public.update_updated_at_column() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.update_updated_at_column() OWNER TO hospital_admin;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: diagnoses; Type: TABLE; Schema: public; Owner: hospital_admin
--

CREATE TABLE public.diagnoses (
    id bigint NOT NULL,
    patient_id bigint,
    diagnosis_code character varying(20),
    diagnosis_name character varying(200) NOT NULL,
    diagnosis_date timestamp(6) without time zone NOT NULL,
    doctor_name character varying(100),
    notes text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    description text
);


ALTER TABLE public.diagnoses OWNER TO hospital_admin;

--
-- Name: diagnoses_id_seq; Type: SEQUENCE; Schema: public; Owner: hospital_admin
--

CREATE SEQUENCE public.diagnoses_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.diagnoses_id_seq OWNER TO hospital_admin;

--
-- Name: diagnoses_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hospital_admin
--

ALTER SEQUENCE public.diagnoses_id_seq OWNED BY public.diagnoses.id;


--
-- Name: epa_configuration; Type: TABLE; Schema: public; Owner: hospital_admin
--

CREATE TABLE public.epa_configuration (
    id integer NOT NULL,
    config_key character varying(100) NOT NULL,
    config_value text,
    description text,
    last_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_by character varying(100)
);


ALTER TABLE public.epa_configuration OWNER TO hospital_admin;

--
-- Name: TABLE epa_configuration; Type: COMMENT; Schema: public; Owner: hospital_admin
--

COMMENT ON TABLE public.epa_configuration IS 'Konfigurationsparameter für EPA-Integration';


--
-- Name: epa_configuration_id_seq; Type: SEQUENCE; Schema: public; Owner: hospital_admin
--

CREATE SEQUENCE public.epa_configuration_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.epa_configuration_id_seq OWNER TO hospital_admin;

--
-- Name: epa_configuration_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hospital_admin
--

ALTER SEQUENCE public.epa_configuration_id_seq OWNED BY public.epa_configuration.id;


--
-- Name: patients; Type: TABLE; Schema: public; Owner: hospital_admin
--

CREATE TABLE public.patients (
    id bigint NOT NULL,
    first_name character varying(100) NOT NULL,
    last_name character varying(100) NOT NULL,
    date_of_birth date NOT NULL,
    gender character varying(20) NOT NULL,
    phone character varying(20),
    email character varying(100),
    address text,
    insurance_number character varying(50) NOT NULL,
    blood_type character varying(5),
    allergies text,
    emergency_contact_name character varying(100),
    emergency_contact_phone character varying(20),
    admission_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    discharge_date timestamp without time zone,
    status character varying(20) DEFAULT 'active'::character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    epa_id character varying(100),
    epa_sync_status character varying(20) DEFAULT 'pending'::character varying,
    epa_last_sync timestamp without time zone,
    epa_sync_error text,
    epa_enabled boolean DEFAULT true,
    epa_consent_date timestamp without time zone
);


ALTER TABLE public.patients OWNER TO hospital_admin;

--
-- Name: COLUMN patients.epa_id; Type: COMMENT; Schema: public; Owner: hospital_admin
--

COMMENT ON COLUMN public.patients.epa_id IS 'Eindeutige ID des Patienten in der elektronischen Patientenakte';


--
-- Name: COLUMN patients.epa_sync_status; Type: COMMENT; Schema: public; Owner: hospital_admin
--

COMMENT ON COLUMN public.patients.epa_sync_status IS 'Status der EPA-Synchronisation: pending, synced, error, disabled';


--
-- Name: COLUMN patients.epa_last_sync; Type: COMMENT; Schema: public; Owner: hospital_admin
--

COMMENT ON COLUMN public.patients.epa_last_sync IS 'Zeitpunkt der letzten erfolgreichen EPA-Synchronisation';


--
-- Name: COLUMN patients.epa_sync_error; Type: COMMENT; Schema: public; Owner: hospital_admin
--

COMMENT ON COLUMN public.patients.epa_sync_error IS 'Fehlermeldung bei gescheiterter EPA-Synchronisation';


--
-- Name: COLUMN patients.epa_enabled; Type: COMMENT; Schema: public; Owner: hospital_admin
--

COMMENT ON COLUMN public.patients.epa_enabled IS 'Gibt an, ob Patient der EPA-Synchronisation zugestimmt hat';


--
-- Name: COLUMN patients.epa_consent_date; Type: COMMENT; Schema: public; Owner: hospital_admin
--

COMMENT ON COLUMN public.patients.epa_consent_date IS 'Datum der EPA-Einwilligungserklärung';


--
-- Name: epa_statistics; Type: VIEW; Schema: public; Owner: hospital_admin
--

CREATE VIEW public.epa_statistics AS
 SELECT count(*) AS total_patients,
    sum(
        CASE
            WHEN (epa_enabled = true) THEN 1
            ELSE 0
        END) AS epa_enabled_count,
    sum(
        CASE
            WHEN ((epa_sync_status)::text = 'synced'::text) THEN 1
            ELSE 0
        END) AS synced_count,
    sum(
        CASE
            WHEN ((epa_sync_status)::text = 'pending'::text) THEN 1
            ELSE 0
        END) AS pending_count,
    sum(
        CASE
            WHEN ((epa_sync_status)::text = 'error'::text) THEN 1
            ELSE 0
        END) AS error_count,
    max(epa_last_sync) AS last_successful_sync
   FROM public.patients;


ALTER VIEW public.epa_statistics OWNER TO hospital_admin;

--
-- Name: epa_sync_log; Type: TABLE; Schema: public; Owner: hospital_admin
--

CREATE TABLE public.epa_sync_log (
    id integer NOT NULL,
    patient_id integer,
    sync_type character varying(20) NOT NULL,
    sync_status character varying(20) NOT NULL,
    epa_id character varying(100),
    fhir_payload text,
    error_message text,
    sync_timestamp timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    sync_duration_ms integer,
    user_id character varying(100)
);


ALTER TABLE public.epa_sync_log OWNER TO hospital_admin;

--
-- Name: TABLE epa_sync_log; Type: COMMENT; Schema: public; Owner: hospital_admin
--

COMMENT ON TABLE public.epa_sync_log IS 'Audit-Log für alle EPA-Synchronisationsvorgänge';


--
-- Name: epa_sync_log_id_seq; Type: SEQUENCE; Schema: public; Owner: hospital_admin
--

CREATE SEQUENCE public.epa_sync_log_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.epa_sync_log_id_seq OWNER TO hospital_admin;

--
-- Name: epa_sync_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hospital_admin
--

ALTER SEQUENCE public.epa_sync_log_id_seq OWNED BY public.epa_sync_log.id;


--
-- Name: medications; Type: TABLE; Schema: public; Owner: hospital_admin
--

CREATE TABLE public.medications (
    id bigint NOT NULL,
    patient_id bigint,
    medication_name character varying(200) NOT NULL,
    dosage character varying(100),
    frequency character varying(100),
    start_date date NOT NULL,
    end_date date,
    prescribed_by character varying(100),
    notes text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    name character varying(200)
);


ALTER TABLE public.medications OWNER TO hospital_admin;

--
-- Name: medications_id_seq; Type: SEQUENCE; Schema: public; Owner: hospital_admin
--

CREATE SEQUENCE public.medications_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.medications_id_seq OWNER TO hospital_admin;

--
-- Name: medications_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hospital_admin
--

ALTER SEQUENCE public.medications_id_seq OWNED BY public.medications.id;


--
-- Name: patients_id_seq; Type: SEQUENCE; Schema: public; Owner: hospital_admin
--

CREATE SEQUENCE public.patients_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.patients_id_seq OWNER TO hospital_admin;

--
-- Name: patients_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hospital_admin
--

ALTER SEQUENCE public.patients_id_seq OWNED BY public.patients.id;


--
-- Name: diagnoses id; Type: DEFAULT; Schema: public; Owner: hospital_admin
--

ALTER TABLE ONLY public.diagnoses ALTER COLUMN id SET DEFAULT nextval('public.diagnoses_id_seq'::regclass);


--
-- Name: epa_configuration id; Type: DEFAULT; Schema: public; Owner: hospital_admin
--

ALTER TABLE ONLY public.epa_configuration ALTER COLUMN id SET DEFAULT nextval('public.epa_configuration_id_seq'::regclass);


--
-- Name: epa_sync_log id; Type: DEFAULT; Schema: public; Owner: hospital_admin
--

ALTER TABLE ONLY public.epa_sync_log ALTER COLUMN id SET DEFAULT nextval('public.epa_sync_log_id_seq'::regclass);


--
-- Name: medications id; Type: DEFAULT; Schema: public; Owner: hospital_admin
--

ALTER TABLE ONLY public.medications ALTER COLUMN id SET DEFAULT nextval('public.medications_id_seq'::regclass);


--
-- Name: patients id; Type: DEFAULT; Schema: public; Owner: hospital_admin
--

ALTER TABLE ONLY public.patients ALTER COLUMN id SET DEFAULT nextval('public.patients_id_seq'::regclass);


--
-- Data for Name: diagnoses; Type: TABLE DATA; Schema: public; Owner: hospital_admin
--

COPY public.diagnoses (id, patient_id, diagnosis_code, diagnosis_name, diagnosis_date, doctor_name, notes, created_at, description) FROM stdin;
\.


--
-- Data for Name: epa_configuration; Type: TABLE DATA; Schema: public; Owner: hospital_admin
--

COPY public.epa_configuration (id, config_key, config_value, description, last_updated, updated_by) FROM stdin;
1	epa_base_url	https://epa-system.example.com/api	Basis-URL des EPA-Systems	2025-12-26 13:41:02.796208	\N
2	epa_api_key		API-Schlüssel für EPA-Zugriff	2025-12-26 13:41:02.796208	\N
3	epa_enabled	true	Globale EPA-Integration aktiviert/deaktiviert	2025-12-26 13:41:02.796208	\N
4	epa_auto_sync	false	Automatische Synchronisation bei Änderungen	2025-12-26 13:41:02.796208	\N
5	epa_sync_interval_minutes	60	Intervall für automatische Synchronisation	2025-12-26 13:41:02.796208	\N
6	epa_timeout_seconds	30	Timeout für EPA-Anfragen	2025-12-26 13:41:02.796208	\N
7	epa_retry_attempts	3	Anzahl Wiederholungsversuche bei Fehlern	2025-12-26 13:41:02.796208	\N
\.


--
-- Data for Name: epa_sync_log; Type: TABLE DATA; Schema: public; Owner: hospital_admin
--

COPY public.epa_sync_log (id, patient_id, sync_type, sync_status, epa_id, fhir_payload, error_message, sync_timestamp, sync_duration_ms, user_id) FROM stdin;
\.


--
-- Data for Name: medications; Type: TABLE DATA; Schema: public; Owner: hospital_admin
--

COPY public.medications (id, patient_id, medication_name, dosage, frequency, start_date, end_date, prescribed_by, notes, created_at, name) FROM stdin;
\.


--
-- Data for Name: patients; Type: TABLE DATA; Schema: public; Owner: hospital_admin
--

COPY public.patients (id, first_name, last_name, date_of_birth, gender, phone, email, address, insurance_number, blood_type, allergies, emergency_contact_name, emergency_contact_phone, admission_date, discharge_date, status, created_at, updated_at, epa_id, epa_sync_status, epa_last_sync, epa_sync_error, epa_enabled, epa_consent_date) FROM stdin;
1	Anna	Müller	1985-03-15	Weiblich	030-12345678	anna.mueller@email.de	Hauptstraße 123, 10115 Berlin	INS-2024-001	A+	Penicillin	\N	\N	2025-12-26 13:41:02.682176	\N	active	2025-12-26 13:41:02.682176	2025-12-26 13:41:02.682176	\N	pending	\N	\N	t	\N
2	Max	Schmidt	1992-07-22	Männlich	030-87654321	max.schmidt@email.de	Nebenstraße 45, 10117 Berlin	INS-2024-002	O+	Keine	\N	\N	2025-12-26 13:41:02.682176	\N	active	2025-12-26 13:41:02.682176	2025-12-26 13:41:02.682176	\N	pending	\N	\N	t	\N
3	Sophie	Weber	1978-11-30	Weiblich	030-55555555	sophie.weber@email.de	Parkweg 7, 10119 Berlin	INS-2024-003	B-	Aspirin	\N	\N	2025-12-26 13:41:02.682176	\N	active	2025-12-26 13:41:02.682176	2025-12-26 13:41:02.682176	\N	pending	\N	\N	t	\N
\.


--
-- Name: diagnoses_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hospital_admin
--

SELECT pg_catalog.setval('public.diagnoses_id_seq', 1, false);


--
-- Name: epa_configuration_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hospital_admin
--

SELECT pg_catalog.setval('public.epa_configuration_id_seq', 7, true);


--
-- Name: epa_sync_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hospital_admin
--

SELECT pg_catalog.setval('public.epa_sync_log_id_seq', 1, false);


--
-- Name: medications_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hospital_admin
--

SELECT pg_catalog.setval('public.medications_id_seq', 1, false);


--
-- Name: patients_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hospital_admin
--

SELECT pg_catalog.setval('public.patients_id_seq', 4, true);


--
-- Name: diagnoses diagnoses_pkey; Type: CONSTRAINT; Schema: public; Owner: hospital_admin
--

ALTER TABLE ONLY public.diagnoses
    ADD CONSTRAINT diagnoses_pkey PRIMARY KEY (id);


--
-- Name: epa_configuration epa_configuration_config_key_key; Type: CONSTRAINT; Schema: public; Owner: hospital_admin
--

ALTER TABLE ONLY public.epa_configuration
    ADD CONSTRAINT epa_configuration_config_key_key UNIQUE (config_key);


--
-- Name: epa_configuration epa_configuration_pkey; Type: CONSTRAINT; Schema: public; Owner: hospital_admin
--

ALTER TABLE ONLY public.epa_configuration
    ADD CONSTRAINT epa_configuration_pkey PRIMARY KEY (id);


--
-- Name: epa_sync_log epa_sync_log_pkey; Type: CONSTRAINT; Schema: public; Owner: hospital_admin
--

ALTER TABLE ONLY public.epa_sync_log
    ADD CONSTRAINT epa_sync_log_pkey PRIMARY KEY (id);


--
-- Name: medications medications_pkey; Type: CONSTRAINT; Schema: public; Owner: hospital_admin
--

ALTER TABLE ONLY public.medications
    ADD CONSTRAINT medications_pkey PRIMARY KEY (id);


--
-- Name: patients patients_epa_id_key; Type: CONSTRAINT; Schema: public; Owner: hospital_admin
--

ALTER TABLE ONLY public.patients
    ADD CONSTRAINT patients_epa_id_key UNIQUE (epa_id);


--
-- Name: patients patients_insurance_number_key; Type: CONSTRAINT; Schema: public; Owner: hospital_admin
--

ALTER TABLE ONLY public.patients
    ADD CONSTRAINT patients_insurance_number_key UNIQUE (insurance_number);


--
-- Name: patients patients_pkey; Type: CONSTRAINT; Schema: public; Owner: hospital_admin
--

ALTER TABLE ONLY public.patients
    ADD CONSTRAINT patients_pkey PRIMARY KEY (id);


--
-- Name: idx_diagnoses_patient; Type: INDEX; Schema: public; Owner: hospital_admin
--

CREATE INDEX idx_diagnoses_patient ON public.diagnoses USING btree (patient_id);


--
-- Name: idx_epa_sync_log_patient; Type: INDEX; Schema: public; Owner: hospital_admin
--

CREATE INDEX idx_epa_sync_log_patient ON public.epa_sync_log USING btree (patient_id);


--
-- Name: idx_epa_sync_log_status; Type: INDEX; Schema: public; Owner: hospital_admin
--

CREATE INDEX idx_epa_sync_log_status ON public.epa_sync_log USING btree (sync_status);


--
-- Name: idx_epa_sync_log_timestamp; Type: INDEX; Schema: public; Owner: hospital_admin
--

CREATE INDEX idx_epa_sync_log_timestamp ON public.epa_sync_log USING btree (sync_timestamp);


--
-- Name: idx_medications_patient; Type: INDEX; Schema: public; Owner: hospital_admin
--

CREATE INDEX idx_medications_patient ON public.medications USING btree (patient_id);


--
-- Name: idx_patients_epa_enabled; Type: INDEX; Schema: public; Owner: hospital_admin
--

CREATE INDEX idx_patients_epa_enabled ON public.patients USING btree (epa_enabled);


--
-- Name: idx_patients_epa_id; Type: INDEX; Schema: public; Owner: hospital_admin
--

CREATE INDEX idx_patients_epa_id ON public.patients USING btree (epa_id);


--
-- Name: idx_patients_epa_sync_status; Type: INDEX; Schema: public; Owner: hospital_admin
--

CREATE INDEX idx_patients_epa_sync_status ON public.patients USING btree (epa_sync_status);


--
-- Name: idx_patients_insurance; Type: INDEX; Schema: public; Owner: hospital_admin
--

CREATE INDEX idx_patients_insurance ON public.patients USING btree (insurance_number);


--
-- Name: idx_patients_last_name; Type: INDEX; Schema: public; Owner: hospital_admin
--

CREATE INDEX idx_patients_last_name ON public.patients USING btree (last_name);


--
-- Name: patients patient_epa_change_trigger; Type: TRIGGER; Schema: public; Owner: hospital_admin
--

CREATE TRIGGER patient_epa_change_trigger BEFORE UPDATE ON public.patients FOR EACH ROW EXECUTE FUNCTION public.trigger_epa_sync();


--
-- Name: patients update_patients_updated_at; Type: TRIGGER; Schema: public; Owner: hospital_admin
--

CREATE TRIGGER update_patients_updated_at BEFORE UPDATE ON public.patients FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: diagnoses diagnoses_patient_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hospital_admin
--

ALTER TABLE ONLY public.diagnoses
    ADD CONSTRAINT diagnoses_patient_id_fkey FOREIGN KEY (patient_id) REFERENCES public.patients(id) ON DELETE CASCADE;


--
-- Name: epa_sync_log epa_sync_log_patient_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hospital_admin
--

ALTER TABLE ONLY public.epa_sync_log
    ADD CONSTRAINT epa_sync_log_patient_id_fkey FOREIGN KEY (patient_id) REFERENCES public.patients(id) ON DELETE CASCADE;


--
-- Name: medications medications_patient_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hospital_admin
--

ALTER TABLE ONLY public.medications
    ADD CONSTRAINT medications_patient_id_fkey FOREIGN KEY (patient_id) REFERENCES public.patients(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

\unrestrict PUEbyjixGf2auNHZlYjb4n13ivN71MfzTSRQaJu30xPVmgzEtudKYWCv7azVou0

