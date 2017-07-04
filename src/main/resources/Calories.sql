
CREATE DATABASE IF NOT EXISTS calories_app;

USE calories_app;

CREATE TABLE IF NOT EXISTS `users` (
  `user_id` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `role` varchar(20) NOT NULL,
  `email` varchar(255) NOT NULL,
  `token` text,
  `password` varchar(100) NOT NULL,
  `target_calories` float NOT NULL DEFAULT 100, 
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `meals` (
  `meal_id` varchar(255) NOT NULL PRIMARY KEY,
  `user_id` varchar(255) NOT NULL,
  `item_name` varchar(255) NOT NULL,
  `calories` float NOT NULL,
  `email` varchar(255),
  `meal_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `email` (`email`);
  
ALTER TABLE `meals`
  ADD KEY `user_id` (`user_id`);

ALTER TABLE `meals`
  ADD CONSTRAINT `meals_fk_constraint` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

ALTER TABLE `users`
  ADD COLUMN `provider` varchar(50);
  
DELIMITER $$
CREATE TRIGGER udpate_email 
    BEFORE INSERT ON calories_app.meals
    FOR EACH ROW 
BEGIN
  DECLARE userEmail varchar(255);
  set userEmail = (select email from calories_app.users where user_id = new.user_id);
  set new.email=userEmail;
END$$
DELIMITER 
