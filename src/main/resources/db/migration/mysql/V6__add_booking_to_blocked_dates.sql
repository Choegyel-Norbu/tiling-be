-- V6: Add booking relationship to blocked_dates table

-- Add booking_id column (nullable to support existing blocked dates without bookings)
ALTER TABLE blocked_dates
ADD COLUMN booking_id BIGINT NULL;

-- Add foreign key constraint
ALTER TABLE blocked_dates
ADD CONSTRAINT fk_blocked_dates_booking 
FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE SET NULL;

-- Add index for better query performance
CREATE INDEX idx_blocked_dates_booking_id ON blocked_dates(booking_id);

