-- For installations not using Hibernate ddl-auto=update, run once before deployment.
-- Existing rows remain NULL and are counted as General QR scans.
ALTER TABLE QrScanEvent ADD COLUMN partnerId BIGINT NULL;
