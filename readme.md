StatCraft
=========

StatCraft is an easy to use and setup plugin for keeping very in-depth stats on your CraftBukkit server. Every individual
statistic has a list of sub-statistics that go with it. StatCraft is planned to support at least 44 different stats, but
that list is not final and may grow in the future. StatCraft uses the server stats for some stats, but it keeps track
of the majority of it's own stats itself. It saves all of the stats in JSON format, and a web front-end is planned to be
added in the future to access the most specific stats.

Statistics Supported
--------------------

|                  Stat Name                      |                         Status                     |
|-------------------------------------------------|----------------------------------------------------|
| Player Deaths                                   | Implemented                                        |
| Blocks Broken                                   | Implemented                                        |
| Blocks Placed                                   | Implemented                                        |
| Time Played                                     | Implementation In Progress - Will Use Server Stats |
| Items Crafted                                   | Implemented                                        |
| Time On Fire                                    | Implemented                                        |
| World Changes (Nether Portal, End Portal, etc.) | Implemented                                        |
| Tools Broken                                    | Implemented                                        |
| Arrows Shot                                     | Implemented                                        |
| Last Time Joined                                | Implemented                                        |
| Last Time Left                                  | Implemented                                        |
| Buckets Filled                                  | Implemented                                        |
| Buckets Emptied                                 | Implemented                                        |
| Items Dropped                                   | Implemented                                        |
| Items Picked Up                                 | Implemented                                        |
| Beds Entered                                    | Implemented                                        |
| Beds Left                                       | Implemented                                        |
| Time Slept                                      | Implemented                                        |
| Words Spoken                                    | Implemented                                        |
| Messages Spoken                                 | Implemented                                        |
| Damage Taken                                    | Implemented                                        |
| Fish Caught                                     | Implemented                                        |
| Number Of Joins                                 | Implemented                                        |
| XP Gained                                       | Implemented                                        |
| Distance Traveled                               | Unimplemented - Will Use Server Stats              |
| Number Of Kills                                 | Implemented                                        |
| Number Of Times Jumped                          | Unimplemented                                      |
| Distance Fallen                                 | Unimplemented - Will Use Server Stats              |
| Number Of Deaths In Each World                  | Implemented                                        |
| Eggs Thrown                                     | Unimplemented                                      |
| Chickens Hatched                                | Unimplemented                                      |
| Ender Pearls Thrown                             | Unimplemented                                      |
| Animals Bred                                    | Unimplemented                                      |
| TNT Detonated                                   | Unimplemented                                      |
| Enchants Done                                   | Unimplemented                                      |
| Highest Level Achieved                          | Implemented                                        |
| Damage Dealt                                    | Implemented                                        |
| Items Brewed                                    | Unimplemented                                      |
| Items Cooked                                    | Unimplemented                                      |
| Fires Started                                   | Unimplemented                                      |
| Ore Mined                                       | Implementation In Progress                         |
| Number Of Tab Completes Used                    | Implemented                                        |
| Number Of Times A Player Has Eaten              | Unimplemented                                      |
| Number Of Times A Player Has Sheared            | Unimplemented                                      |

Compiling
---------

### Need Maven? [Get Maven Here](http://maven.apache.org/download.cgi)

Make sure you are in StatCraft's root directory and run the command:

`mvm clean package`

When that is finished, look in the `target/` folder, and it will have the compiled .jar file.

Further Info
------------

This project is still very much a work-in-progress, and it is not very close to release. However, if you have any questions
or want to contact me for any reason, you can find me in IRC:

`irc.esper.net`

I am usually in *#bukkit* and *#bukkitdev*, but you can always just message me, username is DemonWav.

