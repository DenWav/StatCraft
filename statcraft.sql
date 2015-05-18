-- MySQL dump 10.13  Distrib 5.6.17, for Win64 (x86_64)
--
-- Host: localhost    Database: statcraft
-- ------------------------------------------------------
-- Server version	5.6.22-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `animals_bred`
--

DROP TABLE IF EXISTS `animals_bred`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `animals_bred` (
  `id` int(10) unsigned NOT NULL,
  `animal` varchar(50) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`animal`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `arrows_shot`
--

DROP TABLE IF EXISTS `arrows_shot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `arrows_shot` (
  `id` int(10) unsigned NOT NULL,
  `type` tinyint(4) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `block_break`
--

DROP TABLE IF EXISTS `block_break`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `block_break` (
  `id` int(10) unsigned NOT NULL,
  `blockid` smallint(6) NOT NULL,
  `damage` smallint(6) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`blockid`,`damage`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `block_place`
--

DROP TABLE IF EXISTS `block_place`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `block_place` (
  `id` int(10) unsigned NOT NULL,
  `blockid` smallint(6) NOT NULL,
  `damage` smallint(6) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`blockid`,`damage`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bucket_empty`
--

DROP TABLE IF EXISTS `bucket_empty`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bucket_empty` (
  `id` int(10) unsigned NOT NULL,
  `type` tinyint(4) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bucket_fill`
--

DROP TABLE IF EXISTS `bucket_fill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bucket_fill` (
  `id` int(10) unsigned NOT NULL,
  `type` tinyint(4) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `damage_dealt`
--

DROP TABLE IF EXISTS `damage_dealt`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `damage_dealt` (
  `id` int(10) unsigned NOT NULL,
  `entity` varchar(50) NOT NULL,
  `type` tinyint(4) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`entity`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `damage_taken`
--

DROP TABLE IF EXISTS `damage_taken`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `damage_taken` (
  `id` int(10) unsigned NOT NULL,
  `entity` varchar(50) NOT NULL,
  `type` tinyint(4) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`entity`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `death`
--

DROP TABLE IF EXISTS `death`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `death` (
  `id` int(10) unsigned NOT NULL,
  `message` varchar(200) NOT NULL,
  `world` varchar(50) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`message`,`world`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `death_by_cause`
--

DROP TABLE IF EXISTS `death_by_cause`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `death_by_cause` (
  `id` int(10) unsigned NOT NULL,
  `cause` varchar(50) NOT NULL,
  `type` tinyint(4) NOT NULL,
  `world` varchar(50) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`cause`,`type`,`world`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `eating`
--

DROP TABLE IF EXISTS `eating`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `eating` (
  `id` int(10) unsigned NOT NULL,
  `food` varchar(20) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`food`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `eggs_thrown`
--

DROP TABLE IF EXISTS `eggs_thrown`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `eggs_thrown` (
  `id` int(10) unsigned NOT NULL,
  `hatched` tinyint(1) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`hatched`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `enchants_done`
--

DROP TABLE IF EXISTS `enchants_done`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `enchants_done` (
  `id` int(10) unsigned NOT NULL,
  `item` smallint(6) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`item`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ender_pearls`
--

DROP TABLE IF EXISTS `ender_pearls`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ender_pearls` (
  `id` int(10) unsigned NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  `distance` int(10) unsigned NOT NULL,
  `max_throw` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `enter_bed`
--

DROP TABLE IF EXISTS `enter_bed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `enter_bed` (
  `id` int(10) unsigned NOT NULL,
  `time` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `fallen`
--

DROP TABLE IF EXISTS `fallen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fallen` (
  `id` int(10) unsigned NOT NULL,
  `distance` bigint(20) NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `fires_started`
--

DROP TABLE IF EXISTS `fires_started`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fires_started` (
  `id` int(10) unsigned NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `fish_caught`
--

DROP TABLE IF EXISTS `fish_caught`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fish_caught` (
  `id` int(10) unsigned NOT NULL,
  `item` smallint(6) NOT NULL,
  `damage` smallint(6) NOT NULL,
  `type` tinyint(4) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`item`,`damage`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `highest_level`
--

DROP TABLE IF EXISTS `highest_level`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `highest_level` (
  `id` int(10) unsigned NOT NULL,
  `level` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `item_drops`
--

DROP TABLE IF EXISTS `item_drops`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `item_drops` (
  `id` int(10) unsigned NOT NULL,
  `item` smallint(6) NOT NULL,
  `damage` smallint(6) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`item`,`damage`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `item_pickups`
--

DROP TABLE IF EXISTS `item_pickups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `item_pickups` (
  `id` int(10) unsigned NOT NULL,
  `item` smallint(6) NOT NULL,
  `damage` smallint(6) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`item`,`damage`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `items_brewed`
--

DROP TABLE IF EXISTS `items_brewed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `items_brewed` (
  `id` int(10) unsigned NOT NULL,
  `item` smallint(6) NOT NULL,
  `damage` smallint(6) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`item`,`damage`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `items_cooked`
--

DROP TABLE IF EXISTS `items_cooked`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `items_cooked` (
  `id` int(10) unsigned NOT NULL,
  `item` smallint(6) NOT NULL,
  `damage` smallint(6) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`item`,`damage`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `items_crafted`
--

DROP TABLE IF EXISTS `items_crafted`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `items_crafted` (
  `id` int(10) unsigned NOT NULL,
  `item` smallint(6) NOT NULL,
  `damage` smallint(6) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`item`,`damage`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `joins`
--

DROP TABLE IF EXISTS `joins`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `joins` (
  `id` int(10) unsigned NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `jumps`
--

DROP TABLE IF EXISTS `jumps`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `jumps` (
  `id` int(10) unsigned NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `kills`
--

DROP TABLE IF EXISTS `kills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `kills` (
  `id` int(10) unsigned NOT NULL,
  `entity` varchar(50) NOT NULL,
  `type` tinyint(4) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`entity`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `last_join_time`
--

DROP TABLE IF EXISTS `last_join_time`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `last_join_time` (
  `id` int(10) unsigned NOT NULL,
  `time` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `last_leave_time`
--

DROP TABLE IF EXISTS `last_leave_time`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `last_leave_time` (
  `id` int(10) unsigned NOT NULL,
  `time` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `leave_bed`
--

DROP TABLE IF EXISTS `leave_bed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `leave_bed` (
  `id` int(10) unsigned NOT NULL,
  `time` int(11) NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `messages_spoken`
--

DROP TABLE IF EXISTS `messages_spoken`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `messages_spoken` (
  `id` int(10) unsigned NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `move`
--

DROP TABLE IF EXISTS `move`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `move` (
  `id` int(10) unsigned NOT NULL,
  `vehicle` tinyint(4) NOT NULL,
  `distance` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`vehicle`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `on_fire`
--

DROP TABLE IF EXISTS `on_fire`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `on_fire` (
  `id` int(10) unsigned NOT NULL,
  `time` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `play_time`
--

DROP TABLE IF EXISTS `play_time`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `play_time` (
  `id` int(10) unsigned NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `players`
--

DROP TABLE IF EXISTS `players`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `players` (
  `uuid` binary(16) NOT NULL,
  `name` varchar(16) NOT NULL,
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`uuid`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `shearing`
--

DROP TABLE IF EXISTS `shearing`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shearing` (
  `id` int(10) unsigned NOT NULL,
  `color` tinyint(4) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`color`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tab_complete`
--

DROP TABLE IF EXISTS `tab_complete`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tab_complete` (
  `id` int(10) unsigned NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `time_slept`
--

DROP TABLE IF EXISTS `time_slept`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `time_slept` (
  `id` int(10) unsigned NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tnt_detonated`
--

DROP TABLE IF EXISTS `tnt_detonated`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tnt_detonated` (
  `id` int(10) unsigned NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tools_broken`
--

DROP TABLE IF EXISTS `tools_broken`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tools_broken` (
  `id` int(10) unsigned NOT NULL,
  `item` smallint(6) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`item`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `word_frequency`
--

DROP TABLE IF EXISTS `word_frequency`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `word_frequency` (
  `id` int(10) unsigned NOT NULL,
  `word` varchar(100) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `world_change`
--

DROP TABLE IF EXISTS `world_change`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `world_change` (
  `id` int(10) unsigned NOT NULL,
  `from_world` varchar(50) NOT NULL,
  `to_world` varchar(50) NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`,`from_world`,`to_world`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `xp_gained`
--

DROP TABLE IF EXISTS `xp_gained`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `xp_gained` (
  `id` int(10) unsigned NOT NULL,
  `amount` int(10) unsigned NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2015-05-18  2:21:38
