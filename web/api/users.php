<?php
/**
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

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
				print json_encode($allresult,JSON_NUMERIC_CHECK);
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
				// TODO:result handler
				print json_encode($allresult,JSON_NUMERIC_CHECK);
			}

			// STAT:DAMAGE			
			elseif ($type == "damage") {
			
				if (in_array($subtype,getSubtypes($type))) { // if this is a valid subtype, use it
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
				// TODO:result handler
				print json_encode($allresult,JSON_NUMERIC_CHECK);
			}
			
			// STAT:DEATHS
			elseif ($type == "deaths") {

					$result = array("total"=>0);
					if (($subtype == "world") && ($stats['deaths'] = getUserStats($uid,deaths,$subtype,$sstype,$terms[7],$terms[8]))) {
						foreach ($stats['deaths'] as $i=>$data) {
							if ($subtype == "world") {
								print_r($data);
								if ($i == 0) { $result[$world['data']] = array(); $count[$data['world']] = -1; }
								$count[$data['world']] += 1;
								$result[$data['world']][$count[$data['world']]]['cause'] = htmlspecialchars(utf8_encode($data['cause']));
								$result[$data['world']][$count[$data['world']]]['amount'] = $data['amount'];
							}
							else {
								$result['deaths'][$i]['message'] = htmlspecialchars(utf8_encode($data['message']));
								$result['deaths'][$i]['world'] = $data['world'];
								$result['deaths'][$i]['amount'] = $data['amount'];
							}
							$result['total'] += $data['amount'];
						}
					}
					if (($subtype == "cause") && ($stats['cause'] = getUserStats($uid,death_by_cause,$subtype,$sstype,$terms[7],$terms[8]))) {
						foreach ($stats['cause'] as $i=>$data) {
							if ($subtype == "world") {
								$result[$data['world']][$i]['cause'] = $data['cause'];
								$result[$data['world']][$i]['amount'] = $data['amount'];
							}
							else {
								if ($i == 0) $result['cause']['cause'] = $data['cause'];
								$result['cause'][$i]['world'] = $data['world'];
								$result['cause'][$i]['amount'] = $data['amount'];
							}
						}
					}
				
				print json_encode($result,32);
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
				print json_encode($result,JSON_NUMERIC_CHECK);
			}
			
			// STAT:FIRST_JOIN_TIME
			elseif ($type == "first_join_time") {
				$stats = getUserStats($uid,$type,$subtype,$sstype);
				// TODO:Result handler
				print json_encode($stats[0]['time'],JSON_NUMERIC_CHECK);
			}
			
			// STAT:FISH_CAUGHT
			elseif ($type == "fish_caught") {
				if (!in_array($subtype,getSubtypes($type))) { $subtype = NULL;	}
				$stats = getUserStats($uid,$type,$subtype,$sstype);
				$fish = array();
				foreach ((array)$stats as $i=>$data) {
					$fish[getMagicString("fish",$data['type'])][$data['item'].":".$data['damage']] = $data['amount'];
				}
				// TODO:Result handler
				print json_encode($fish,JSON_NUMERIC_CHECK);
			}
			
			// STAT:HIGHEST_LEVEL
			elseif ($type == "highest_level") {
				$stats = getUserStats($uid,$type,$subtype,$sstype);
				// TODO:Result handler
				print json_encode($stats[0]['level'],JSON_NUMERIC_CHECK);
			}
			
			// STAT:ITEM
			elseif ($type == "item") {
				// TODO:Abstract these subtype selections
				if (in_array($subtype,getSubtypes($type))) { // if this is a valid subtype, use it
					$stats[$subtype] = getUserStats($uid,$type,$subtype,$sstype);
				}
				else { // otherwise default to all
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
				print json_encode($allresult,JSON_NUMERIC_CHECK);
			}
			
			// STAT:MESSAGES		
			elseif ($type == "messages") {
				$stats = getUserStats($uid,$type,$subtype,$sstype);
				$result = array();
				$result['messages'] = $stats[0]['amount'];
				$result['words'] = $stats[0]['words_spoken'];
				// Todo:result handler
				print json_encode($result,JSON_NUMERIC_CHECK);
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
				print json_encode($result,48); // 48 = JSON_NUMERIC_CHECK[32] + JSON_FORCE_OBJECT[16]			
			}
			
			// STAT:ON_FIRE
			elseif ($type == "on_fire") {
				$stats = getUserStats($uid,$type,$subtype,$sstype);
				if (empty($stats[0]['time'])) { $stats[0]['time'] = 0; }
				// Todo:result handler
				print json_encode($stats[0]['time'],JSON_NUMERIC_CHECK);
			}
			
			// STAT:PROJECTILE
			elseif ($type == "projectile") {
				// determine subtype copypasta ..
				if (in_array($subtype,getSubtypes($type))) { // if this is a valid subtype, use it
					$stats[$subtype] = getUserStats($uid,$type,$subtype,$sstype);
				}
				else { // otherwise default to all
					if (isset($subtype)) $sstype = $subtype; // and this is now a possible item list
					$stats['arrow'] = getUserStats($uid,$type,arrow,$sstype);
					$stats['egg'] = getUserStats($uid,$type,egg,$sstype);
					$stats['pearl'] = getUserStats($uid,$type,pearl,$sstype);
					$stats['snowball'] = getUserStats($uid,$type,snowball,$sstype);
				}
				$result = array(); // initializing arrays makes me feel like a good person
				foreach ($stats as $i=>$data) { 
					foreach ((array)$data as $j=>$moredata) {
						if (!empty($moredata)) {
							if ($res = getSubtypes($i)) { // eww.. I do not like this
								$result[$i][getMagicString($type,$moredata['type'])]['amount'] = $moredata['amount'];
								$result[$i][getMagicString($type,$moredata['type'])]['total_distance'] = $moredata['total_distance'];
								$result[$i][getMagicString($type,$moredata['type'])]['max_throw'] = $moredata['max_throw'];
							}
							else { // there must be a cleaner way
								$result[$i]['amount'] = $moredata['amount'];
								$result[$i]['total_distance'] = $moredata['total_distance'];
								$result[$i]['max_throw'] = $moredata['max_throw'];
							}
						}
					}
				}
				// TODO:Result handler
				print json_encode($result,JSON_NUMERIC_CHECK);
			}

			// STAT:SEEN
			elseif ($type == "seen") {
				$result = array();
				if (in_array($subtype,getSubtypes($type))) { // if this is a valid subtype, use it
					$stats[] = getUserStats($uid,$subtype,NULL,$sstype);
					$result = $stats[0][0]['time'];
				}
				else { // otherwise default to all
					$stats['fjoin'] = getUserStats($uid,first_join_time,NULL,$sstype);
					$stats['ljoin'] = getUserStats($uid,last_join_time,NULL,$sstype);
					$stats['lquit'] = getUserStats($uid,last_leave_time,NULL,$sstype);
					$stats['lspoke'] = getUserStats($uid,last_spoken_time,NULL,$sstype);
					$result['first_join_time'] = $stats['fjoin'][0]['time'];
					$result['last_join_time'] = $stats['ljoin'][0]['time'];
					$result['last_leave_time'] = $stats['lquit'][0]['time'];
					$result['last_spoken_time'] = $stats['lspoke'][0]['time'];
				}
				print json_encode($result,JSON_NUMERIC_CHECK);
			}
			 
			// STAT:TOOLS_BROKEN
			elseif ($type == "tools_broken") {
				$stats = getUserStats($uid,$type,$subtype,$sstype);
				$result = array();
				foreach ($stats as $i=>$data) {
					$result[$data['item']] = $data['amount'];
				}
				// TODO:result handler
				print json_encode($result,JSON_NUMERIC_CHECK );
			}
			
			// STAT:WORDS_SPOKEN
			elseif ($type == "words_spoken") {
				$stats = getUserStats($uid,$type,$subtype,$sstype);
				$result = array();
				$result['unique'] = 0;
				$result['total'] = 0;
				foreach ($stats as $i=>$data) {
					$result['words'][$i]['word'] = htmlspecialchars($data['word']);
					$result['words'][$i]['amount'] = $data['amount'];
					$result['unique'] += 1;
					$result['total'] += $data['amount'];
				}
				print json_encode($result,JSON_NUMERIC_CHECK);
			}
			
			else {
				// default .. works for 4 or 5 stats!
				$stats = getUserStats($uid,$type,$subtype,$sstype);
				print json_encode(($res=$stats[0]['amount'])?$res:0,JSON_NUMERIC_CHECK); // dirty
			}
			
		}
		else {
			// TODO:Error handler
			die("{\"error\":\"Invalid username\"}");
		}
	}
	elseif ($result == 0) {
		// TODO:Error handler
		die("{\"error\":\"Invalid type 2\"}");
	}
	else {
		// invalid result
	}
?>
