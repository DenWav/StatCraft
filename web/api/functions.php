<?php
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
function refValues($arr)
{ 
    if (strnatcmp(phpversion(),'5.3') >= 0) //Reference is required for PHP 5.3+ 
    { 
        $refs = array(); 
        foreach($arr as $key => $value) 
            $refs[$key] = &$arr[$key]; 
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
function getMagicString($type,$val) {
	// Todo:Check valid type
	$magic = array();
	$magic['move'] = array("walking","crouching","sprinting","swimming","falling","climbing","flying","diving","minecart","boat","pig","horse");
	$magic['fish'] = array("fish","treasure","junk");
	if (isset($magic[$type][$val])) { return $magic[$type][$val]; }
	else { return NULL; }
}

/**
 * Converts a magic string to its corresponding number
 *
 * @return The number associated with the given type and string
 */
function getMagicNumber($type,$string) {
	$magic = array();
	$magic['move'] = array("walking"=>0,"crouching"=>1,"sprinting"=>2,"swimming"=>3,"falling"=>4,"climbing"=>5,"flying"=>6,"diving"=>7,"minecart"=>8,"boat"=>9,"pig"=>10,"horse"=>11);
	$magic['fish'] = array("fish"=>0,"treasure"=>1,"junk"=>2);
	if (isset($magic[$type][$string])) { return $magic[$type][$string]; }
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
function validStatType($type,$subtype=FALSE) {
	// Check validity of requested stat type/subtype
	$validTables = "animals_bred,blocks,buckets,damage,deaths,eating,enchants_done,enter_bed,fallen,fires_started,first_join_time,fish_caught,highest_level,item,joins,jumps,kills,last_join_time,last_leave_time,leave_bed,messages_spoken,move,on_fire,play_time,projectile,shearing,tab_complete,time_slept,tnt_detonated,tools_broken,word_frequency,words_spoken,world_change,xp_gained";
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
	$subtypes['fish'] = 	array("fish","junk","treasure");
	$subtypes['item'] = 	array("dropped","pickedup","brewed","cooked","crafted");
	if (isset($subtypes[$type])) { return $subtypes[$type]; }
	else {
		// Type does not have a subtype.
		return NULL;
	}
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
 
function getUserStats($uid,$type,$subtype=NULL,$parameters=NULL) {
	$uid = (int)$uid; // TODO:Error check UID
	$type = strtolower($type);
	
	$result = validStatType($type,$subtype); // Check validity of type
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
		
		// STAT:DEATHS
		elseif ($type == "deaths") {
		
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
		
		if (isset($_GET['debug'])) { print $query."\n"; print $bind_types."\n"; print_r($binds); }
		return executeMultiParam($db,$query,$bind_types,$binds,0);
	}
	//elseif ($result == -1) { die("{\"error\":\"Invalid subtype\"}"); }
	else { die("{\"error\":\"Invalid type\"}"); }
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