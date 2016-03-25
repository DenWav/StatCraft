-- StatCraft Database Creation Script Generated on Fri Mar 25 01:55:38 CDT 2016
--
-- Database: statcraft
-- ----------------------------------------------------------------------------

--
-- Table: `animals_bred`
--
DROP TABLE IF EXISTS `animals_bred`;
CREATE TABLE `animals_bred` (
  `id` int(10) unsigned NOT NULL,
  `animal` varchar(50) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`animal`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `block_break`
--
DROP TABLE IF EXISTS `block_break`;
CREATE TABLE `block_break` (
  `id` int(10) unsigned NOT NULL,
  `blockid` smallint(6) NOT NULL,
  `damage` smallint(6) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`blockid`,`damage`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `block_place`
--
DROP TABLE IF EXISTS `block_place`;
CREATE TABLE `block_place` (
  `id` int(10) unsigned NOT NULL,
  `blockid` smallint(6) NOT NULL,
  `damage` smallint(6) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`blockid`,`damage`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `bucket_empty`
--
DROP TABLE IF EXISTS `bucket_empty`;
CREATE TABLE `bucket_empty` (
  `id` int(10) unsigned NOT NULL,
  `type` tinyint(4) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `bucket_fill`
--
DROP TABLE IF EXISTS `bucket_fill`;
CREATE TABLE `bucket_fill` (
  `id` int(10) unsigned NOT NULL,
  `type` tinyint(4) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `damage_dealt`
--
DROP TABLE IF EXISTS `damage_dealt`;
CREATE TABLE `damage_dealt` (
  `id` int(10) unsigned NOT NULL,
  `entity` varchar(50) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`entity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `damage_taken`
--
DROP TABLE IF EXISTS `damage_taken`;
CREATE TABLE `damage_taken` (
  `id` int(10) unsigned NOT NULL,
  `entity` varchar(50) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`entity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `death`
--
DROP TABLE IF EXISTS `death`;
CREATE TABLE `death` (
  `id` int(10) unsigned NOT NULL,
  `message` varchar(200) NOT NULL,
  `world` varchar(50) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`message`,`world`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `death_by_cause`
--
DROP TABLE IF EXISTS `death_by_cause`;
CREATE TABLE `death_by_cause` (
  `id` int(10) unsigned NOT NULL,
  `cause` varchar(50) NOT NULL,
  `world` varchar(50) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`cause`,`world`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `eating`
--
DROP TABLE IF EXISTS `eating`;
CREATE TABLE `eating` (
  `id` int(10) unsigned NOT NULL,
  `food` varchar(20) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`food`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `enchants_done`
--
DROP TABLE IF EXISTS `enchants_done`;
CREATE TABLE `enchants_done` (
  `id` int(10) unsigned NOT NULL,
  `item` smallint(6) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`item`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `fires_started`
--
DROP TABLE IF EXISTS `fires_started`;
CREATE TABLE `fires_started` (
  `id` int(10) unsigned NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `fish_caught`
--
DROP TABLE IF EXISTS `fish_caught`;
CREATE TABLE `fish_caught` (
  `id` int(10) unsigned NOT NULL,
  `item` smallint(6) NOT NULL,
  `damage` smallint(6) NOT NULL,
  `type` tinyint(4) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`item`,`damage`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `highest_level`
--
DROP TABLE IF EXISTS `highest_level`;
CREATE TABLE `highest_level` (
  `id` int(10) unsigned NOT NULL,
  `level` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `item_drops`
--
DROP TABLE IF EXISTS `item_drops`;
CREATE TABLE `item_drops` (
  `id` int(10) unsigned NOT NULL,
  `item` smallint(6) NOT NULL,
  `damage` smallint(6) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`item`,`damage`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `item_pickups`
--
DROP TABLE IF EXISTS `item_pickups`;
CREATE TABLE `item_pickups` (
  `id` int(10) unsigned NOT NULL,
  `item` smallint(6) NOT NULL,
  `damage` smallint(6) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`item`,`damage`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `items_brewed`
--
DROP TABLE IF EXISTS `items_brewed`;
CREATE TABLE `items_brewed` (
  `id` int(10) unsigned NOT NULL,
  `item` smallint(6) NOT NULL,
  `damage` smallint(6) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`item`,`damage`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `items_cooked`
--
DROP TABLE IF EXISTS `items_cooked`;
CREATE TABLE `items_cooked` (
  `id` int(10) unsigned NOT NULL,
  `item` smallint(6) NOT NULL,
  `damage` smallint(6) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`item`,`damage`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `items_crafted`
--
DROP TABLE IF EXISTS `items_crafted`;
CREATE TABLE `items_crafted` (
  `id` int(10) unsigned NOT NULL,
  `item` smallint(6) NOT NULL,
  `damage` smallint(6) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`item`,`damage`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `joins`
--
DROP TABLE IF EXISTS `joins`;
CREATE TABLE `joins` (
  `id` int(10) unsigned NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `jumps`
--
DROP TABLE IF EXISTS `jumps`;
CREATE TABLE `jumps` (
  `id` int(10) unsigned NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  `world` varchar(50) NOT NULL,
  UNIQUE KEY `id` (`id`,`world`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `kicks`
--
DROP TABLE IF EXISTS `kicks`;
CREATE TABLE `kicks` (
  `id` int(10) unsigned NOT NULL,
  `amount` int(11) NOT NULL,
  `reason` varchar(100) NOT NULL,
  UNIQUE KEY `id` (`id`,`reason`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `kills`
--
DROP TABLE IF EXISTS `kills`;
CREATE TABLE `kills` (
  `id` int(10) unsigned NOT NULL,
  `entity` varchar(50) NOT NULL,
  `type` tinyint(4) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`entity`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `messages_spoken`
--
DROP TABLE IF EXISTS `messages_spoken`;
CREATE TABLE `messages_spoken` (
  `id` int(10) unsigned NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  `words_spoken` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `move`
--
DROP TABLE IF EXISTS `move`;
CREATE TABLE `move` (
  `id` int(10) unsigned NOT NULL,
  `vehicle` tinyint(4) NOT NULL,
  `distance` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`vehicle`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `on_fire`
--
DROP TABLE IF EXISTS `on_fire`;
CREATE TABLE `on_fire` (
  `id` int(10) unsigned NOT NULL,
  `time` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `play_time`
--
DROP TABLE IF EXISTS `play_time`;
CREATE TABLE `play_time` (
  `id` int(10) unsigned NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `players`
--
DROP TABLE IF EXISTS `players`;
CREATE TABLE `players` (
  `uuid` binary(16) NOT NULL,
  `name` varchar(16) NOT NULL,
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`uuid`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `projectiles`
--
DROP TABLE IF EXISTS `projectiles`;
CREATE TABLE `projectiles` (
  `amount` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `type` smallint(5) unsigned NOT NULL,
  `total_distance` int(10) unsigned NOT NULL,
  `max_throw` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `seen`
--
DROP TABLE IF EXISTS `seen`;
CREATE TABLE `seen` (
  `id` int(10) unsigned NOT NULL,
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
  `id` int(10) unsigned NOT NULL,
  `color` tinyint(4) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`color`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `sleep`
--
DROP TABLE IF EXISTS `sleep`;
CREATE TABLE `sleep` (
  `id` int(10) unsigned NOT NULL,
  `enter_bed` int(11) NOT NULL,
  `leave_bed` int(11) NOT NULL,
  `time_slept` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `tab_complete`
--
DROP TABLE IF EXISTS `tab_complete`;
CREATE TABLE `tab_complete` (
  `id` int(10) unsigned NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `tnt_detonated`
--
DROP TABLE IF EXISTS `tnt_detonated`;
CREATE TABLE `tnt_detonated` (
  `id` int(10) unsigned NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `tools_broken`
--
DROP TABLE IF EXISTS `tools_broken`;
CREATE TABLE `tools_broken` (
  `id` int(10) unsigned NOT NULL,
  `item` smallint(6) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`item`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `word_frequency`
--
DROP TABLE IF EXISTS `word_frequency`;
CREATE TABLE `word_frequency` (
  `id` int(10) unsigned NOT NULL,
  `word` varchar(100) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `world_change`
--
DROP TABLE IF EXISTS `world_change`;
CREATE TABLE `world_change` (
  `id` int(10) unsigned NOT NULL,
  `from_world` varchar(50) NOT NULL,
  `to_world` varchar(50) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`from_world`,`to_world`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table: `xp_gained`
--
DROP TABLE IF EXISTS `xp_gained`;
CREATE TABLE `xp_gained` (
  `id` int(10) unsigned NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Generated in 20.962359ms
