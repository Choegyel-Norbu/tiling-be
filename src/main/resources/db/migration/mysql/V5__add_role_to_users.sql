-- V5: Add role_id column to users table and assign USER role to all existing users

-- Step 1: Add role_id column (nullable initially for data migration)
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'users' 
    AND COLUMN_NAME = 'role_id');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE users ADD COLUMN role_id BIGINT NULL AFTER locale',
    'SELECT "Column role_id already exists" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 2: Create index for role_id (if not exists)
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'users' 
    AND INDEX_NAME = 'idx_user_role_id');

SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_user_role_id ON users(role_id)',
    'SELECT "Index idx_user_role_id already exists" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 3: Assign USER role to all existing users
-- Update all users that don't have a role assigned to use the USER role
UPDATE users u
INNER JOIN roles r ON r.name = 'USER'
SET u.role_id = r.id
WHERE u.role_id IS NULL;

-- Step 4: Add foreign key constraint (if not exists)
SET @fk_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'users' 
    AND CONSTRAINT_NAME = 'fk_users_role');

SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE users ADD CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT',
    'SELECT "Foreign key fk_users_role already exists" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 5: Make role_id NOT NULL (after ensuring all users have a role)
SET @is_nullable = (SELECT IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'users' 
    AND COLUMN_NAME = 'role_id');

SET @sql = IF(@is_nullable = 'YES',
    'ALTER TABLE users MODIFY COLUMN role_id BIGINT NOT NULL',
    'SELECT "Column role_id is already NOT NULL" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

