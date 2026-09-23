-- Google addresses and Maps URLs can exceed the original 64-character limit.
ALTER TABLE PlaceMaster
    MODIFY COLUMN googleAddress TEXT NULL;
