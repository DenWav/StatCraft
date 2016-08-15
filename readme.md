StatCraft <a href="https://tc.demonwav.com/viewType.html?buildTypeId=StatCraft_Build&guest=1"><img src="https://tc.demonwav.com/app/rest/builds/buildType(id:StatCraft_Build)/statusIcon"/></a>
=========

StatCraft is an easy to use and setup plugin for keeping very in-depth stats on your CraftBukkit server. Most individual
statistics have a list of sub-statistics that go with it. StatCraft is planned to support at least 44 different stats, but
that list is not final and may grow in the future. StatCraft uses the server stats for some stats, but it keeps track
of the majority of it's own stats itself. It uses a MySQL databas to store the stats, using the QueryDSL library
for most of the database interfacing.

Statistics Supported
--------------------

|                  Stat Name                      |                         Status                     |
|-------------------------------------------------|----------------------------------------------------|
| Player Deaths                                   | **Implemented**                                    |
| Blocks Broken                                   | **Implemented**                                    |
| Blocks Placed                                   | **Implemented**                                    |
| Time Played                                     | **Implemented**                                    |
| Items Crafted                                   | **Implemented**                                    |
| Time On Fire                                    | **Implemented**                                    |
| World Changes (Nether Portal, End Portal, etc.) | **Implemented**                                    |
| Tools Broken                                    | **Implemented**                                    |
| Arrows Shot                                     | **Implemented**                                    |
| Last Time Joined                                | **Implemented**                                    |
| Last Time Left                                  | **Implemented**                                    |
| Buckets Filled                                  | **Implemented**                                    |
| Buckets Emptied                                 | **Implemented**                                    |
| Items Dropped                                   | **Implemented**                                    |
| Items Picked Up                                 | **Implemented**                                    |
| Beds Entered                                    | **Implemented**                                    |
| Beds Left                                       | **Implemented**                                    |
| Time Slept                                      | **Implemented**                                    |
| Words Spoken                                    | **Implemented**                                    |
| Messages Spoken                                 | **Implemented**                                    |
| Damage Taken                                    | **Implemented**                                    |
| Fish Caught                                     | **Implemented**                                    |
| Number Of Joins                                 | **Implemented**                                    |
| XP Gained                                       | **Implemented**                                    |
| Distance Traveled                               | **Implemented**                                    |
| Number Of Kills                                 | **Implemented**                                    |
| Number Of Times Jumped                          | **Implemented**                                    |
| Distance Fallen                                 | **Implemented**                                    |
| Number Of Deaths In Each World                  | **Implemented**                                    |
| Eggs Thrown                                     | **Implemented**                                    |
| Chickens Hatched                                | **Implemented**                                    |
| Ender Pearls Thrown                             | **Implemented**                                    |
| Snow Balls Thrown                               | **Implemented**                                    |
| Animals Bred                                    | Unimplemented *Temporarily on hold*                |
| TNT Detonated                                   | Unimplemented                                      |
| Enchants Done                                   | Unimplemented                                      |
| Highest Level Achieved                          | **Implemented**                                    |
| Damage Dealt                                    | **Implemented**                                    |
| Items Brewed                                    | Unimplemented                                      |
| Items Cooked                                    | Unimplemented                                      |
| Fires Started                                   | Unimplemented                                      |
| Number Of Tab Completes Used                    | **Implemented**                                    |
| Things Eaten                                    | Unimplemented                                      |
| Sheep sheared                                   | Unimplemented                                      |

Compiling
---------

### Need Maven? [Get Maven Here](http://maven.apache.org/download.cgi)

#### JDK 8 is required.

If you are just modifying the plugin without changing any of the database structure, then there is no setup needed.

Make sure you are in StatCraft's root directory and run the command:

`mvn clean package`

When that is finished, look in the `target/` folder, and it will have the compiled .jar file.

If you need to modify the database structure, execute `statcraft.sql` in MySQL to create the correct database structure
to begin with, or just run a currently working version of StatCraft so it will create the database. Once you have done
this, modify the pom.xml to allow it to connect to the database. Once you have done that, make the changes to the
database structure directly on the database itself. After you have made the necessary changes, run this command:

`mvn clean statcraft:generate-sql package`

for full build, or just:

`mvn statcraft:generate-sql`

if you just want to create the sql file.

This will generate the `statcraft.sql` file that the StatCraft Maven plugin uses to generate the classes and Table enum,
this is so that changes to the database are automatically represented in the code, and any errors will result in
compile-time issues, rather than hard to debug runtime problems.

Further Info
------------

Source for the StatCraft Maven Plugin [here](https://github.com/DemonWav/StatCraftMavenPlugin).

This project is still very much a work-in-progress, and it is not very close to release. However, if you have any
questions or want to contact me for any reason, you can find me in IRC:

`irc.spi.gt`

I am usually in *#paper* and *#spigot*, but you can always just message me, username is DemonWav.
