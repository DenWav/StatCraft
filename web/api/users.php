<?php
	header("Content-Type:application/json;charset=utf-8");
	require_once('functions.php');
	
	// Get script return parameters.
	$parameters = array();
	if (isset($_GET['format'])) 	$parameters['format'] = $_GET['format'];
	if (isset($_GET['limit'])) 		$parameters['limit'] = $_GET['limit'];
	if (isset($_GET['order'])) 		$parameters['order'] = $_GET['order'];
	if (isset($_GET['offset'])) 	$parameters['offset'] = $_GET['offset'];
	if (isset($_GET['callback'])) 	$parameters['callback'] = $_GET['callback'];
	if (isset($_GET['display'])) 	$parameters['display'] = $_GET['display'];
	
	// parse REQUEST URI for type/subtype
	$path = explode("?",$_SERVER['REQUEST_URI']);
	$terms = array_map('strtolower',explode("/",$path[0]));
		$username = $terms[3];
		$type = $terms[4];
		$subtype = $terms[5];
		$sstype = $terms[6];
		$ssstype = $terms[7]; // I could do this all day!
	
	// Check validity of type
	$result = validStatType($type);
	if ($result == 1) {
		// valid type/subtype
		$parameters = NULL;
				
		// check username / get id
		$uid = getUserID($username);
		if ($uid != FALSE) { // valid username
			
			// STAT:BLOCKS
			if ($type == "blocks") {
			
				if (in_array($subtype,array("broken","placed"))) { // if this is a valid subtype, use it
					$stats[$subtype] = getUserStats($uid,$type,$subtype,$sstype);
				}
				else { // otherwise default to both
					if (isset($subtype)) $sstype = $subtype; // and this is now a possible block list
					$stats['placed'] = getUserStats($uid,$type,placed,$sstype);
					$stats['broken'] = getUserStats($uid,$type,broken,$sstype);
				}
								
				foreach ($stats as $key=>$alldata) { // for whatever subtypes we want (one or both)
					$result = array(); // create an array
					$result['total'] = 0; // set total element so it shows up at the start
					foreach ($alldata as $i=>$data) { // for each block id
						$result['breakdown'][$i]['blockid'] = $data['blockid'].":".$data['damage'];
						$result['breakdown'][$i]['total'] = $data['amount']; // add this block to result list
						$result['total'] += $data['amount']; // increment total blocks
					}
					// add to final results
					$allresult[$key] = $result;
				}
								
				//TODO:result handler
				$allresult = json_encode($allresult,JSON_NUMERIC_CHECK);
				print $allresult;
			}
			
			// STAT:BUCKETS			
			elseif ($type == "buckets") {
				if (in_array($subtype,array("filled","emptied"))) { // if this is a valid subtype, use it
					$stats[$subtype] = getUserStats($uid,$type,$subtype,$sstype);
				}
				else { // otherwise default to both
					if (isset($subtype)) $sstype = $subtype; // and this is now a possible bucket type
					$stats['filled'] = getUserStats($uid,$type,filled,$sstype);
					$stats['emptied'] = getUserStats($uid,$type,emptied,$sstype);
				}
				foreach ($stats as $key=>$alldata) { // for whatever subtypes we want (one or both)
					$result = array(); // create an array
					$result['total'] = 0; // set total element so it shows up at the start
					foreach ($alldata as $i=>$data) { // for each buket type
						$result[$data['type']] = $data['amount']; // add this to result list
						$result['total'] += $data['amount']; // increment total buckets
					}
					// add to final results
					$allresult[$key] = $result;
				}
				$allresult = json_encode($allresult,JSON_NUMERIC_CHECK);
				print $allresult;
			}

			// STAT:DAMAGE			
			elseif ($type == "damage") {
			
				if (in_array($subtype,array("taken","dealt"))) { // if this is a valid subtype, use it
					$stats[$subtype] = getUserStats($uid,$type,$subtype,$sstype);
				}
				else { // otherwise default to both
					if (isset($subtype)) $sstype = $subtype; // and this is now a possible damage cause
					$stats['taken'] = getUserStats($uid,$type,taken,$sstype);
					$stats['dealt'] = getUserStats($uid,$type,dealt,$sstype);
				}
				foreach ($stats as $key=>$alldata) { // for whatever subtypes we want (one or both)
					$result = array(); // create an array
					$result['total'] = 0; // set total element so it shows up at the start
					foreach ($alldata as $i=>$data) { // for each damage type
						$result[strtolower($data['entity'])] = $data['amount']; // add this to result list
						$result['total'] += $data['amount']; // increment total damage
					}
					$result = convertIDstoUsernames($result); // convert IDs to usernames
					// add to final results
					$allresult[$key] = $result;
				}
				$allresult = json_encode($allresult,JSON_NUMERIC_CHECK);
				print $allresult;
			}
			
			// STAT:DEATHS
			elseif ($type == "deaths") {
			
			}
			
			// STAT:KILLS
			elseif ($type == "kills") {
				$stats = getUserStats($uid,$type,$subtype,$sstype);
				$result = array(); // create the array
				$result['total'] = 0; // set total element so it shows up at the start
				foreach ($stats as $i=>$data) {// for each killed entity
					$result[$data['entity']] = $data['amount']; // add this entity to result list
					$result['total'] += $data['amount']; // increment total kills
				}
				$result = convertIDstoUsernames($result); // convert IDs to usernamess
				// TODO:result handler
				$result = json_encode($result,JSON_NUMERIC_CHECK);
				print $result;
			}
			
			// STAT:FIRST_JOIN_TIME
			elseif ($type == "first_join_time") {
				$stats = getUserStats($uid,$type,$subtype,$sstype);
				$result = array();
				$result['time'] = $stats[0]['time'];
				// TODO:Result handler
				$result = json_encode($result,JSON_NUMERIC_CHECK);
				print $result;
			}
			
			// STAT:FISH_CAUGHT
			elseif ($type == "fish_caught") {
				$stats = getUserStats($uid,$type,$subtype,$sstype);
				$fish = array();
				foreach ($stats as $i=>$data) {
					$fish[getMagicString("fish",$data['type'])][$data['item'].":".$data['damage']] = $data['amount'];
				}
				// TODO:Result handler
				$result = json_encode($fish,JSON_NUMERIC_CHECK);
				print $result;
			}
			
			// STAT:HIGHEST_LEVEL
			elseif ($type == "highest_level") {
				$stats = getUserStats($uid,$type,$subtype,$sstype);
				$result = array();
				$result['level'] = $stats[0]['level'];
				$result = json_encode($result,JSON_NUMERIC_CHECK);
				print $result;
			}
			
			// STAT:ITEM
			elseif ($type == "item") {
				// TODO:Abstract these subtype selections
				if (in_array($subtype,array("dropped","pickedup","brewed","cooked","crafted"))) { // if this is a valid subtype, use it
					$stats[$subtype] = getUserStats($uid,$type,$subtype,$sstype);
				}
				else { // otherwise default to ... ? [All]
					if (isset($subtype)) $sstype = $subtype; // and this is now a possible item list
					$stats['dropped'] = getUserStats($uid,$type,dropped,$sstype);
					$stats['pickedup'] = getUserStats($uid,$type,pickedup,$sstype);
					$stats['brewed'] = getUserStats($uid,$type,brewed,$sstype);
					$stats['cooked'] = getUserStats($uid,$type,cooked,$sstype);
					$stats['crafted'] = getUserStats($uid,$type,crafted,$sstype);
				}
				foreach ($stats as $key=>$alldata) { // for whatever subtypes we want
					$result = array(); // create an array
					$result['total'] = 0; // set total element so it shows up at the start
					if (!empty($alldata)) {
						foreach ($alldata as $i=>$data) { // for each block id
							$result[$data['item'].":".$data['damage']] = $data['amount']; // add this block to result list
							$result['total'] += $data['amount']; // increment total blocks
						}
					}
					// add to final results
					$allresult[$key] = $result;
				}
				// TODO:result handler
				$allresult = json_encode($allresult,JSON_NUMERIC_CHECK);
				print $allresult;
			}
			
			// STAT:MESSAGES_SPOKEN			
			elseif ($type == "messages_spoken") {
				$stats = getUserStats($uid,$type,$subtype,$sstype);
				$result = array();
				$result['messages'] = $stats[0]['amount'];
				$result['words'] = $stats[0]['words_spoken'];
				// Todo:result handler
				$result = json_encode($result,JSON_NUMERIC_CHECK);
				print $result;
			}
			
			// STAT:MOVE
			elseif ($type == "move") {
				$stats = getUserStats($uid,$type,$subtype,$sstype);
				$result = array();
				$result['total'] = 0;
				foreach ($stats as $key=>$data) {
					$result[getMagicString("move",$data['vehicle'])] = $data['distance'];
					$result['total'] += $data['distance'];
				}
				// Todo:result handler
				$result = json_encode($result,48); // 48 = JSON_NUMERIC_CHECK[32] + JSON_FORCE_OBJECT[16]
				print $result;			
			}
			
			// STAT:ON_FIRE
			elseif ($type == "on_fire") {
				$stats = getUserStats($uid,$type,$subtype,$sstype);
				if (empty($stats[0]['time'])) { $stats[0]['time'] = 0; }
				// Todo:result handler
				$result = json_encode($stats[0]['time'],JSON_NUMERIC_CHECK);
				print $result;	
			}
			
			// STAT:PLAY_TIME
			elseif ($type == "play_time") {
				$stats = getUserStats($uid,$type,$subtype,$sstype);
				if (empty($stats[0]['amount'])) { $stats[0]['amount'] = 0; }
				// Todo:result handler
				$result = json_encode($stats[0]['amount'],JSON_NUMERIC_CHECK);
				print $result;					
			}
			
			else {
				// Default
				$stats = getUserStats($uid,$type,$subtype,$sstype);
				$stats = json_encode($stats,JSON_NUMERIC_CHECK);
				print $stats;
			}
			
		}
		else {
			// TODO:Error handler
			die("{\"error\":\"Invalid username\"}");
		}
	}
	elseif ($result == 0) {
		// TODO:Error handler
		die("{\"error\":\"Invalid type\"}");
	}
	else {
		// invalid result
	}
?>