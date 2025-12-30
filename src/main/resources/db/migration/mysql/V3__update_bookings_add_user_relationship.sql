-- V3: Update bookings table to use User relationship instead of customer_name and customer_email

-- Step 1: Add user_id column (nullable initially for data migration)
-- Check if column exists first to make migration idempotent
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'bookings' 
    AND COLUMN_NAME = 'user_id');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE bookings ADD COLUMN user_id BIGINT NULL AFTER time_slot',
    'SELECT "Column user_id already exists" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 2: Create index for user_id (if not exists)
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'bookings' 
    AND INDEX_NAME = 'idx_booking_user_id');

SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_booking_user_id ON bookings(user_id)',
    'SELECT "Index idx_booking_user_id already exists" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 3: Migrate existing data (if any)
-- Only run if customer_email column still exists
SET @customer_email_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'bookings' 
    AND COLUMN_NAME = 'customer_email');

-- Create users from existing bookings' customer_email and customer_name
SET @sql = IF(@customer_email_exists > 0,
    'INSERT INTO users (email, name, created_at, updated_at)
    SELECT 
        customer_email as email,
        customer_name as name,
        MIN(created_at) as created_at,
        MIN(updated_at) as updated_at
    FROM bookings
    WHERE customer_email IS NOT NULL 
      AND customer_email NOT IN (SELECT email FROM users WHERE email IS NOT NULL)
    GROUP BY customer_email, customer_name',
    'SELECT "Columns customer_email/customer_name do not exist, skipping data migration" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 4: Link bookings to users (only if customer_email exists)
SET @sql = IF(@customer_email_exists > 0,
    'UPDATE bookings b
    INNER JOIN users u ON b.customer_email = u.email
    SET b.user_id = u.id
    WHERE b.user_id IS NULL',
    'SELECT "Columns customer_email/customer_name do not exist, skipping user linking" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 5: Remove customer_name and customer_email columns (if they exist)
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'bookings' 
    AND COLUMN_NAME IN ('customer_name', 'customer_email'));

SET @sql = IF(@col_exists > 0,
    'ALTER TABLE bookings DROP COLUMN customer_name, DROP COLUMN customer_email',
    'SELECT "Columns customer_name and customer_email already removed" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 6: Remove the old customer_email index (if exists)
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'bookings' 
    AND INDEX_NAME = 'idx_booking_email');

SET @sql = IF(@index_exists > 0,
    'DROP INDEX idx_booking_email ON bookings',
    'SELECT "Index idx_booking_email does not exist" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 7: Add foreign key constraint (if not exists)
SET @fk_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'bookings' 
    AND CONSTRAINT_NAME = 'fk_bookings_user');

SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE bookings ADD CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT',
    'SELECT "Foreign key fk_bookings_user already exists" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 8: Make user_id NOT NULL (after ensuring all bookings have a user)
-- Only if column is currently nullable
SET @is_nullable = (SELECT IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'bookings' 
    AND COLUMN_NAME = 'user_id');

SET @sql = IF(@is_nullable = 'YES',
    'ALTER TABLE bookings MODIFY COLUMN user_id BIGINT NOT NULL',
    'SELECT "Column user_id is already NOT NULL" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
