-- StatCraft Database Creation Script Generated on Fri Mar 25 13:55:44 CDT 2016
--
-- Database: statcraft
-- ----------------------------------------------------------------------------

--
-- Table: `animals_bred`
--
DROP TABLE IF EXISTS `animals_bred`;
CREATE TABLE `animals_bred` (
  `id` int(11) NOT NULL,
  `animal` varchar(50) NOT NULL,
  `amount` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`animal`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `block_break`
--
DROP TABLE IF EXISTS `block_break`;
CREATE TABLE `block_break` (
  `id` int(11) NOT NULL,
  `blockid` smallint(6) NOT NULL,
  `damage` smallint(6) NOT NULL,
  `amount` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`blockid`,`damage`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `block_place`
--
DROP TABLE IF EXISTS `block_place`;
CREATE TABLE `block_place` (
  `id` int(11) NOT NULL,
  `blockid` smallint(6) NOT NULL,
  `damage` smallint(6) NOT NULL,
  `amount` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`blockid`,`damage`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `bucket_empty`
--
DROP TABLE IF EXISTS `bucket_empty`;
CREATE TABLE `bucket_empty` (
  `id` int(11) NOT NULL,
  `type` tinyint(4) NOT NULL,
  `amount` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`type`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `bucket_fill`
--
DROP TABLE IF EXISTS `bucket_fill`;
CREATE TABLE `bucket_fill` (
  `id` int(11) NOT NULL,
  `type` tinyint(4) NOT NULL,
  `amount` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`type`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `damage_dealt`
--
DROP TABLE IF EXISTS `damage_dealt`;
CREATE TABLE `damage_dealt` (
  `id` int(11) NOT NULL,
  `entity` varchar(50) NOT NULL,
  `amount` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`entity`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `damage_taken`
--
DROP TABLE IF EXISTS `damage_taken`;
CREATE TABLE `damage_taken` (
  `id` int(11) NOT NULL,
  `entity` varchar(50) NOT NULL,
  `amount` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`entity`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `death`
--
DROP TABLE IF EXISTS `death`;
CREATE TABLE `death` (
  `id` int(11) NOT NULL,
  `message` varchar(200) NOT NULL,
  `world_id` int(11) NOT NULL,
  `amount` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`message`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `death_by_cause`
--
DROP TABLE IF EXISTS `death_by_cause`;
CREATE TABLE `death_by_cause` (
  `id` int(11) NOT NULL,
  `cause` varchar(50) NOT NULL,
  `world_id` int(11) NOT NULL,
  `amount` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`cause`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `eating`
--
DROP TABLE IF EXISTS `eating`;
CREATE TABLE `eating` (
  `id` int(11) NOT NULL,
  `food` varchar(20) NOT NULL,
  `amount` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`food`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `enchants_done`
--
DROP TABLE IF EXISTS `enchants_done`;
CREATE TABLE `enchants_done` (
  `id` int(11) NOT NULL,
  `item` smallint(6) NOT NULL,
  `amount` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`item`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `fires_started`
--
DROP TABLE IF EXISTS `fires_started`;
CREATE TABLE `fires_started` (
  `id` int(11) NOT NULL,
  `amount` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `fish_caught`
--
DROP TABLE IF EXISTS `fish_caught`;
CREATE TABLE `fish_caught` (
  `id` int(11) NOT NULL,
  `item` smallint(6) NOT NULL,
  `damage` smallint(6) NOT NULL,
  `type` tinyint(4) NOT NULL,
  `amount` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`item`,`damage`,`type`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `highest_level`
--
DROP TABLE IF EXISTS `highest_level`;
CREATE TABLE `highest_level` (
  `id` int(11) NOT NULL,
  `level` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `item_drops`
--
DROP TABLE IF EXISTS `item_drops`;
CREATE TABLE `item_drops` (
  `id` int(11) NOT NULL,
  `item` smallint(6) NOT NULL,
  `damage` smallint(6) NOT NULL,
  `amount` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`item`,`damage`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `item_pickups`
--
DROP TABLE IF EXISTS `item_pickups`;
CREATE TABLE `item_pickups` (
  `id` int(11) NOT NULL,
  `item` smallint(6) NOT NULL,
  `damage` smallint(6) NOT NULL,
  `amount` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`item`,`damage`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `items_brewed`
--
DROP TABLE IF EXISTS `items_brewed`;
CREATE TABLE `items_brewed` (
  `id` int(11) NOT NULL,
  `item` smallint(6) NOT NULL,
  `damage` smallint(6) NOT NULL,
  `amount` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`item`,`damage`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `items_cooked`
--
DROP TABLE IF EXISTS `items_cooked`;
CREATE TABLE `items_cooked` (
  `id` int(11) NOT NULL,
  `item` smallint(6) NOT NULL,
  `damage` smallint(6) NOT NULL,
  `amount` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`item`,`damage`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `items_crafted`
--
DROP TABLE IF EXISTS `items_crafted`;
CREATE TABLE `items_crafted` (
  `id` int(11) NOT NULL,
  `item` smallint(6) NOT NULL,
  `damage` smallint(6) NOT NULL,
  `amount` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`item`,`damage`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `joins`
--
DROP TABLE IF EXISTS `joins`;
CREATE TABLE `joins` (
  `id` int(11) NOT NULL,
  `amount` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `jumps`
--
DROP TABLE IF EXISTS `jumps`;
CREATE TABLE `jumps` (
  `id` int(11) NOT NULL,
  `amount` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `kicks`
--
DROP TABLE IF EXISTS `kicks`;
CREATE TABLE `kicks` (
  `id` int(11) NOT NULL,
  `amount` int(11) NOT NULL,
  `reason` varchar(100) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`reason`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `kills`
--
DROP TABLE IF EXISTS `kills`;
CREATE TABLE `kills` (
  `id` int(11) NOT NULL,
  `entity` varchar(50) NOT NULL,
  `type` tinyint(4) NOT NULL,
  `amount` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`entity`,`type`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `messages_spoken`
--
DROP TABLE IF EXISTS `messages_spoken`;
CREATE TABLE `messages_spoken` (
  `id` int(11) NOT NULL,
  `amount` int(11) NOT NULL,
  `words_spoken` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `move`
--
DROP TABLE IF EXISTS `move`;
CREATE TABLE `move` (
  `id` int(11) NOT NULL,
  `vehicle` tinyint(4) NOT NULL,
  `distance` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`vehicle`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `on_fire`
--
DROP TABLE IF EXISTS `on_fire`;
CREATE TABLE `on_fire` (
  `id` int(11) NOT NULL,
  `time` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `play_time`
--
DROP TABLE IF EXISTS `play_time`;
CREATE TABLE `play_time` (
  `id` int(11) NOT NULL,
  `amount` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `players`
--
DROP TABLE IF EXISTS `players`;
CREATE TABLE `players` (
  `uuid` binary(16) NOT NULL,
  `name` varchar(16) NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`uuid`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

--
-- Table: `projectiles`
--
DROP TABLE IF EXISTS `projectiles`;
CREATE TABLE `projectiles` (
  `amount` int(11) NOT NULL,
  `id` int(11) NOT NULL,
  `type` smallint(5) NOT NULL,
  `total_distance` int(11) NOT NULL,
  `max_throw` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`type`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `seen`
--
DROP TABLE IF EXISTS `seen`;
CREATE TABLE `seen` (
  `id` int(11) NOT NULL,
  `first_join_time` int(11) NOT NULL,
  `last_join_time` int(11) NOT NULL,
  `last_leave_time` int(11) NOT NULL,
  `last_spoke_time` int(11) NOT NULL,
  UNIQUE KEY `unique_id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `shearing`
--
DROP TABLE IF EXISTS `shearing`;
CREATE TABLE `shearing` (
  `id` int(11) NOT NULL,
  `color` tinyint(4) NOT NULL,
  `amount` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`color`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `sleep`
--
DROP TABLE IF EXISTS `sleep`;
CREATE TABLE `sleep` (
  `id` int(11) NOT NULL,
  `enter_bed` int(11) NOT NULL,
  `leave_bed` int(11) NOT NULL,
  `time_slept` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `tab_complete`
--
DROP TABLE IF EXISTS `tab_complete`;
CREATE TABLE `tab_complete` (
  `id` int(11) NOT NULL,
  `amount` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `tnt_detonated`
--
DROP TABLE IF EXISTS `tnt_detonated`;
CREATE TABLE `tnt_detonated` (
  `id` int(11) NOT NULL,
  `amount` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `tools_broken`
--
DROP TABLE IF EXISTS `tools_broken`;
CREATE TABLE `tools_broken` (
  `id` int(11) NOT NULL,
  `item` smallint(6) NOT NULL,
  `amount` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`item`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `word_frequency`
--
DROP TABLE IF EXISTS `word_frequency`;
CREATE TABLE `word_frequency` (
  `id` int(11) NOT NULL,
  `word` varchar(100) NOT NULL,
  `amount` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`word`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `world_change`
--
DROP TABLE IF EXISTS `world_change`;
CREATE TABLE `world_change` (
  `id` int(11) NOT NULL,
  `from_world` int(11) NOT NULL,
  `to_world` int(11) NOT NULL,
  `amount` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`from_world`,`to_world`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `worlds`
--
DROP TABLE IF EXISTS `worlds`;
CREATE TABLE `worlds` (
  `world_id` int(11) NOT NULL AUTO_INCREMENT,
  `uuid` binary(16) NOT NULL,
  `world_name` varchar(50) NOT NULL,
  PRIMARY KEY (`world_id`),
  UNIQUE KEY `id` (`uuid`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

--
-- Table: `xp_gained`
--
DROP TABLE IF EXISTS `xp_gained`;
CREATE TABLE `xp_gained` (
  `id` int(11) NOT NULL,
  `amount` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`,`world_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Generated in 17.060555ms
