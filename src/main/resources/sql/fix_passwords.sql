-- Fix password encryption for existing test users
USE account_dashboard;

-- Update test user passwords to MD5 encrypted values
-- Password '123456' MD5 hash: e10adc3949ba59abbe56e057f20f883e
-- Password 'admin123' MD5 hash: 0192023a7bbd73250516f069df18b500

UPDATE `user` SET `password` = 'e10adc3949ba59abbe56e057f20f883e' WHERE `username` = 'testuser';
UPDATE `user` SET `password` = '0192023a7bbd73250516f069df18b500' WHERE `username` = 'admin';
