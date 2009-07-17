CREATE TABLE IF NOT EXISTS `heroes` (
  `charId` DECIMAL(11,0) NOT NULL DEFAULT 0,
  `char_name` VARCHAR(45) NOT NULL DEFAULT '',
  `class_id` DECIMAL(3,0) NOT NULL DEFAULT 0,
  `count` DECIMAL(3,0) NOT NULL DEFAULT 0,
  `played` DECIMAL(1,0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`)
) DEFAULT CHARSET=utf8;