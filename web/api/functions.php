<?php
/**
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

/**
 * Gets a database handle
 * 
 * @return resource Database handle
 */
function getDatabase() {
	require('database.php');
	$db = new mysqli($SERVER,$USERNAME,$PASSWORD,$DATABASE) or die("{\"error\":\"Could not connect to database\"}");
	return $db;
}

/**
 * Returns reference values for the given array. Via http://php.net/manual/en/mysqli-stmt.bind-param.php#96770
 *
 * @return array of reference values
 */
function refValues($arr) { 
    if (strnatcmp(phpversion(),'5.3') >= 0) { //Reference is required for PHP 5.3+  
        $refs = array(); 
        foreach ($arr as $key => $value) {
			$refs[$key] = &$arr[$key]; 
		}
        return $refs; 
    } 
    return $arr; 
}

/**
 * Executes a prepared statement with multiple rows returned
 *
 * @return array of results
 */
function executeMultiParam($db,$query,$bind_types,$binds,$false=FALSE) {
	$res = $db->prepare($query) or die("prepare failed: ".mysqli_error($db));
	$params = array_merge(array($bind_types),$binds);
	call_user_func_array(array($res,'bind_param'),refValues($params));
	$res->execute();
	$fetch = $res->get_result();
	while ($result = $fetch->fetch_assoc()) {
		$data[] = $result;
	}
	if (empty($data)) { return $false; }
	else { return $data; }
}

/**
 * Converts a magic value to its corresponding string
 *
 * @return The string associated with a given magic type and number
 */
function getMagicString($type,$val=NULL) {
	// Todo:Check valid type
	$magic = array();
	$magic['move'] = array("walking","crouching","sprinting","swimming","falling","climbing","flying","diving","minecart","boat","pig","horse");
	$magic['fish'] = array("fish","treasure","junk");
	$magic['projectile'] = array("normal","flaming","pearl","unhatched","hatched","snowball","4hatched");
	if (!isset($val) && isset($magic[$type])) { return $magic[$type]; }
	elseif (isset($magic[$type][$val])) { return $magic[$type][$val]; }
	else { return NULL; }
}

/**
 * Converts a magic string to its corresponding number
 *
 * @return The number associated with the given type and string
 */
function getMagicNumber($type,$string=NULL) {
	$magic = array();
	if ($magic[$type] = getMagicString($type)) { $magic[$type] = array_flip($magic[$type]); }
	if ((!isset($string)) && isset($magic[$type])) { return $magic[$type]; }
	elseif (isset($magic[$type][$string])) { return $magic[$type][$string]; }
	else { return NULL; }
}

/**
 * Gets the ID associated with the given username.
 *
 * @return the ID of the specified user
 */
function getUserID($username) {
	$db = getDatabase();
	$data = $db->prepare("SELECT id FROM players WHERE name=?");
	$data->bind_param("s",$username);
	$data->execute();
	$data->bind_result($data);
	$data->fetch();
	if (isset($data)) return $data;
	else return FALSE;
}

/**
 * Gets the username associated with the given player ID.
 *
 * @return the username of the specified user
 */
function getUsername($id) {
	if (is_numeric($id)) {
		$db = getDatabase();
		$data = $db->prepare("SELECT name FROM players WHERE id=?");
		$data->bind_param("i",$id);
		$data->execute();
		$data->bind_result($data);
		$data->fetch();
		return $data;
	}
	else {
		return FALSE;
	}
}

/**
 * Verifies the validity of a stat type and subtype tracked by Statcraft.
 *
 * @return boolean 1 if type is valid, 0 if the type is invalid, and -1 if the type is valid but subtype is invalid.
 */
function validStatType($type) {
	// Check validity of requested stat type/subtype
	$validTables = "animals_bred,blocks,buckets,damage,deaths,death_by_cause,eating,enchants_done,enter_bed,fallen,fires_started,fish_caught,highest_level,item,joins,jumps,kills,leave_bed,messages,move,on_fire,play_time,projectile,seen,first_join_time,last_join_time,last_leave_time,last_spoken_time,sleep,shearing,tab_complete,tnt_detonated,tools_broken,words_spoken,world_change,xp_gained";
	$validTables = explode(",", $validTables);
	if (in_array($type, $validTables)) {
		return 1;
	}
	else {
		// invalid type
		return 0;
	}
}

/**
 * Returns the subtypes associated with the input type
 *
 * @return An array of subtypes if input type has subtypes, or NULL if not
 */
function getSubtypes($type) {
	$subtypes['blocks'] = 	array("broken","placed");
	$subtypes['buckets'] = 	array("filled","emptied");
	$subtypes['damage'] = 	array("taken","dealt");
	$subtypes['deaths'] = 	array("world","type");
	$subtypes['fish_caught'] = 	array("fish","junk","treasure");
	$subtypes['item'] = 	array("dropped","pickedup","brewed","cooked","crafted");
	$subtypes['projectile'] = array("arrow","egg","pearl","snowball");
	$subtypes['arrow'] = array("normal","flaming");
	$subtypes['egg'] = array("unhatched","hatched","4hatched");
	$subtypes['seen'] = array("first_join_time","last_join_time","last_leave_time","last_spoke_time");
	if (isset($subtypes[$type])) { return $subtypes[$type]; }
	else { return NULL;	} // No subtype.
}

/**
 * Convert a list of usernames to IDs for use in a query
 *
 * @return A copy of the input array with valid usernames converted to IDs
 */
function convertUsernamestoIDs($arr) {
	$db = getDatabase();
	$query = "SELECT name,id FROM players WHERE "; // start a query
	$binds = array();
	foreach ($arr as $i=>$name) { // for each term
		$query .= "name = ? OR "; // add a term to find this potential username
			$bind_types .= "s";
			array_push($binds,$name);
	}
	$query = substr($query,0,-4); // drop the hanging OR
	$result = executeMultiParam($db,$query,$bind_types,$binds); // execute query
	$replace = array();
	foreach ($result as $i=>$data) { // set up replacement array
		$replace[strtolower($data['name'])] = $data['id']; 
	}
	foreach ($arr as $key=>$name) { // for each username in the list
		if (isset($replace[$name])) $arr[$key] = $replace[$name]; // replace it with the ID if one was found
	}
	return $arr;
}

/**
 * Convert a list of IDs to usernames for use in output
 *
 * @return A copy of the input array with valid IDs converted to usernames
 */
function convertIDstoUsernames($arr) {
	$db = getDatabase();
	$query = "SELECT name,id FROM players WHERE "; // start a query
	$binds = array();
	foreach ($arr as $i=>$amt) { // for each term
		$query .= "id = ? OR "; // add a term to find this potential username
			$bind_types .= "s";
			array_push($binds,$i);
	}
	$query = substr($query,0,-4); // drop the hanging OR
	$result = executeMultiParam($db,$query,$bind_types,$binds); // execute query
	if ($_GET['debug']) { print_r($result); } 
	$replace = array();
	foreach ((array)$result as $i=>$data) { // set up replacement array
		$replace[$data['id']] = $data['name']; 
	}
	if ($_GET['debug']) { print_r($replace); }
	foreach ($arr as $id=>$amt) { // for each ID in the list
		if (isset($replace[$id])) {
			$arr[$replace[$id]] = $amt; // replace it with the username if one was found
			unset($arr[$id]); // remove entry with raw ID
		}
	}
	return $arr;
}

/**
 * Get stats of specified user and type from Statcraft database
 *
 * @return Array of requested user stats
 */
 
function getUserStats($uid,$type,$subtype=NULL,$parameters=NULL,$subpath=NULL,$subparameters=NULL) {
	$uid = (int)$uid; // TODO:Error check UID
	$type = strtolower($type);
	
	$result = validStatType($type); // Check validity of type
	if ($result == 1) {
		// valid type/subtype
		$db = getDatabase();
		$query = "SELECT * FROM {$type} WHERE id =?"; // start a query string
			$bind_types = "i";
			$binds = array($uid);
		
		// STAT:BLOCKS
		if ($type == "blocks") {
			
			// determine subtype
			if ((strtolower($subtype) == "broken")) $type = "block_break";
			elseif ((strtolower($subtype) == "placed")) $type = "block_place";
			else {
				$parameters = $subtype; // no valid subtype, so assume this is a block list
				$type = "block_break"; // default to broken
			}
			$query = "SELECT * FROM {$type} WHERE id = ?"; // update query string
				$bind_types = "i";
				$binds = array($uid);
			if (isset($parameters)) { // we have a list of specific block(s)
				$query .= " AND ("; // add onto query
				$parameters = explode(",",$parameters); // explode into array
				foreach ($parameters as $i=>$val) { // For each block in list
					$val = explode("-",$val); // more explosions, and escaping!
					if (!isset($val[1])) $val[1] = 0; // default to 0 if damage is unspecified 
					$query .= "(blockid = ?"; // get this block
						$bind_types .= "i";
						array_push($binds,$val[0]);
					if (strtolower($val[1]) != "all") { // if we're not looking for "all"
						$query .= " AND damage = ?"; // get this damage
							$bind_types .= "i";
							array_push($binds,$val[1]);
					}
					$query .= ") OR "; // prepare for next term
				}
				$query = substr($query,0,-4); // drop the hanging OR
				$query .= ")"; // close the parenthesis
			}
		}
		
		// STAT:BUCKET
		elseif ($type == "buckets") {
			// determine subtype
			if ((strtolower($subtype) == "filled")) $type = "bucket_fill";
			elseif ((strtolower($subtype) == "emptied")) $type = "bucket_empty";
			else {
				$parameters = $subtype; // no valid subtype, so assume this is a bucket type list (so useful)
			}
			$query = "SELECT * FROM {$type} WHERE id = ?"; // update query string
				$bind_types = "i";
				$binds = array($uid);
			if (isset($parameters)) {
				$query .= " AND ("; // but wait!
				$thistype = explode(",",$parameters); // explode
				foreach ($thistype as $i=>$thissubtype) { // for each type
					$query .= "type = ? OR "; // get this type
						$bind_types .= "s";
						array_push($binds,$thissubtype);
				}
				$query = substr($query,0,-4); // drop the hanging OR
				$query .= ")"; // close the parenthesis
			}
		}
		
		// STAT:DAMAGE
		elseif ($type == "damage") {
			// determine subtype
			if ((strtolower($subtype) == "taken")) $type = "damage_taken";
			elseif ((strtolower($subtype) == "dealt")) $type = "damage_dealt";
			else {
				$parameters = $subtype; // no valid subtype, so assume this is a damage cause list
			}
			$query = "SELECT * FROM {$type} WHERE id = ?"; // update query string
				$bind_types = "i";
				$binds = array($uid);
			if (isset($parameters)) {
				$query .= " AND ("; // but wait!
				$thistype = explode(",",$parameters); // explode
				$thistype = convertUsernamestoIDs($thistype); // convert usernames in array to IDs
				foreach ($thistype as $i=>$thissubtype) { // for each type
					$query .= "entity = ? OR "; // get this type
						$bind_types .= "s";
						array_push($binds,$thissubtype);
				}
				$query = substr($query,0,-4); // drop the hanging OR
				$query .= ")"; // close the parenthesis
			}
		}
		

		// STAT:EATING
		elseif (($type == "eating") && (isset($subtype))) {
		
		}
		
		// STAT:KILLS
		elseif (($type == "kills") && (isset($subtype))) {
			$query .= " AND ("; // but wait!
			$subtype = explode(",",$subtype); // explode
			$subtype = convertUsernamestoIDs($subtype); // convert usernames to IDs
			foreach ($subtype as $i=>$thissubtype) { // for each type
				$query .= "entity = ? OR "; // get this type
					$bind_types .= "s";
					array_push($binds,$thissubtype);
			}
			$query = substr($query,0,-4); // drop the hanging OR
			$query .= ")"; // close the parenthesis
		}
		
		/// STAT:MESSAGES
		elseif ($type == "messages") { // if we rename the database or endpoint we can remove this block
			$query = "SELECT * from messages_spoken WHERE id = ?";
		}
		
		// STAT:SLEEP
		elseif ($type == "sleep") { // .. and this one
			$query = "SELECT * from time_slept WHERE id = ?";
		}
		
		// STAT:DEATHS
		elseif ($type == "deaths") {
			$query = "SELECT * from death_by_cause WHERE id = ?";
			if (isset($subtype)) {
				if ($subtype == "world") {
					if (isset($parameters)) {
						$query .= " AND (";
						// world id
						$query .= "(world = ?";
							$bind_types .= "s";
							array_push($binds,$parameters);
						if (isset($subpath)) {
							// cause
							$query .= ") AND (cause = ?";
								$bind_types .="s";
								array_push($binds,$subpath);
						}					
						$query .= "));";
					}
				}
				elseif ($subtype == "cause") {
					if (isset($parameters)) {
						$query .= " AND (";
						// cause
						$query .= "(cause = ?";
							$bind_types .= "s";
							array_push($binds,$parameters);
						if (isset($subpath)) {
							// world id
							$query .= ") AND (world = ?";
								$bind_types .="s";
								array_push($binds,$subpath);
						}
						$query .= "));";
					}
				}
			}
			else {
				$query = "SELECT * from death WHERE id = ?";
			}
		}
		
		
		// STAT:FISH_CAUGHT
		elseif (($type == "fish_caught") && (isset($subtype))) {
			$query .= " AND ("; // but wait!
			$subtype = explode(",",$subtype); // explode
			foreach ($subtype as $i=>$thissubtype) { // for each type
				$thissubtype = getMagicNumber("fish",$thissubtype);
				$query .= "type = ? OR "; // get this type
					$bind_types .= "i";
					array_push($binds,$thissubtype);
			}
			$query = substr($query,0,-4); // drop the hanging OR
			$query .= ")"; // close the parenthesis .... mmmmm delicious copypasta
		}
		
		// STAT:ITEM
		elseif (($type == "item") && (isset($subtype))) {
			// determine subtype
			if ((strtolower($subtype) == "dropped")) $type = "item_drops";
			elseif ((strtolower($subtype) == "pickedup")) $type = "item_pickups";
			elseif ((strtolower($subtype) == "brewed")) $type = "items_brewed";
			elseif ((strtolower($subtype) == "cooked")) $type = "items_cooked";
			elseif ((strtolower($subtype) == "crafted")) $type = "items_crafted";
			else {
				$parameters = $subtype; // no valid subtype, so assume this is an item type list
				$type = "item_pickups"; // default to pickups
			}
			$query = "SELECT * FROM {$type} WHERE id = ?"; // update query string
				$bind_types = "i";
				$binds = array($uid);
			if (isset($parameters)) { // we have a list of specific block(s)
				$query .= " AND ("; // add onto query
				$parameters = explode(",",$parameters); // explode into array
				foreach ($parameters as $i=>$val) { // For each block in list
					$val = explode("-",$val); // more explosions!
					if (!isset($val[1])) $val[1] = 0; // default to 0 if damage is unspecified 
					$query .= "(item = ?"; // get this block
						$bind_types .= "i";
						array_push($binds,$val[0]);
					if (strtolower($val[1]) != "all") { // if we're not looking for "all"
						$query .= " AND damage = ?"; // get this damage
							$bind_types .= "i";
							array_push($binds,$val[1]);
					}
					$query .= ") OR "; // prepare for next term
				}
				$query = substr($query,0,-4); // drop the hanging OR
				$query .= ")"; // close the parenthesis
			}
		}
		
		// STAT:MOVE
		elseif (($type == "move") && (isset($subtype))) {
			$query .= " AND ("; // but wait!
			$subtype = explode(",",$subtype); // explode
			foreach ($subtype as $i=>$thissubtype) { // for each type
				$thissubtype = getMagicNumber("move",$thissubtype);
				$query .= "vehicle = ? OR "; // get this type
					$bind_types .= "i";
					array_push($binds,$thissubtype);
			}
			$query = substr($query,0,-4); // drop the hanging OR
			$query .= ")"; // close the parenthesiss
		}
		
		// STAT:PROJECTILE
		elseif ($type == "projectile" && (isset($subtype))) {
			if (isset($parameters)) { // arrow or egg types
				$getsubtypes = explode(",",$parameters); // make array of parameters
			}
			$query = "SELECT * from projectiles WHERE id = ?";
			if (!isset($getsubtypes)) { $getsubtypes = getSubtypes($subtype); } // get defaults if nothing was specified
			if (isset($getsubtypes)){ // could be null for snowballs etc.
				$query .= " AND ("; // add onto query
				foreach ($getsubtypes as $i=>$thissubtype) { // for each projectile type
					$subtypes[$i] = getMagicNumber($type,$thissubtype); // convert to magic numbers
					$query .= "(type = ?"; // get this projectile type
						$bind_types .= "i";
						array_push($binds,$subtypes[$i]);
					$query .= ") OR "; // prepare for next term
				}
				$query = substr($query,0,-4); // drop the hanging OR
				$query .= ")"; // close the parenthesis
			}
			else { // snowballs etc.
				$query = "SELECT * from projectiles WHERE id = ? AND type = ?";
					$bind_types .= "i";
					array_push($binds,getMagicNumber($type,$subtype));
			}
		}
		
		// STAT:LAST_SPOKEN_TIME
		elseif ($type == "last_spoken_time") { // Todo: Remove this block upon plugin implementation
			return NULL;
		}
		
		// STAT:TOOLS_BROKEN
		elseif (($type == "tools_broken") && isset($subtype)) { // I really feel like this should be an /item endpoint instead
			if (isset($subtype)) { // we have a list of specific block(s)
				$query .= " AND ("; // add onto query
				$subtype = explode(",",$subtype); // explode into array
				foreach ($subtype as $i=>$val) { // For each item in list
					$query .= "(item = ?"; // get this item
						$bind_types .= "i";
						array_push($binds,$val);
					$query .= ") OR "; // prepare for next term
				}
				$query = substr($query,0,-4); // drop the hanging OR
				$query .= ")"; // close the parenthesis
			}
		}
		
		// STAT:WORDS_SPOKEN
		elseif ($type == "words_spoken") {
			$query = "SELECT * FROM word_frequency WHERE id = ?";
			if (isset($subtype)) { // words!
				$words = explode(",",$subtype);
				$query .= " AND (";
				foreach ($words as $word) {
					$query .= "(word = ?";
						$bind_types .= "s";
						array_push($binds,$word); // yay prepared statements
					$query .= ") OR "; // prepare for next term
				}
				$query = substr($query,0,-4); // drop the hanging OR
				$query .= ")"; // close the parenthesis
			}
		}
		
		if (isset($_GET['debug'])) { print $query."\n"; print $bind_types."\n"; print_r($binds); }
		return executeMultiParam($db,$query,$bind_types,$binds,0);
	}
	//elseif ($result == -1) { die("{\"error\":\"Invalid subtype\"}"); }
	else { die("{\"error\":\"Invalid type 1\"}"); }
}

/**
 * Get total stats of specified type from Statcraft database
 *
 * @return Array of requested stats
 */
function getTotalStats($type,$subtype=NULL,$parameters=NULL) {
	// Check validity of type
	$result = validStatType($type,$subtype);
	if ($result == 1) {
		// valid type/subtype
		print "total ".$type." ".$subtype;
		
		$db = getDatabase();
	}
	elseif ($result == -1) {
		die("{\"error\":\"Invalid subtype\"}");
	}
	elseif ($result == 0) {
		die("{\"error\":\"Invalid type\"}");
	}
}
?>
