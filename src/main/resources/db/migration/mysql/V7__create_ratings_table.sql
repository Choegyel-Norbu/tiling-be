-- V7: Create ratings table for booking reviews

CREATE TABLE ratings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 10),
    comment TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_ratings_booking 
        FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE UNIQUE INDEX idx_rating_booking_id ON ratings(booking_id);
CREATE INDEX idx_rating_score ON ratings(rating);

