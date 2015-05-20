StatCraft
=========

StatCraft is an easy to use and setup plugin for keeping very in-depth stats on your CraftBukkit server. Most individual
statistics have a list of sub-statistics that go with it. StatCraft is planned to support at least 44 different stats, but
that list is not final and may grow in the future. StatCraft uses the server stats for some stats, but it keeps track
of the majority of it's own stats itself. It uses MySQL databases to store the stats, using the Querydsl library
for most of the database interfacing.

Statistics Supported
--------------------

|                  Stat Name                      |                         Status                     |
|-------------------------------------------------|----------------------------------------------------|
| Player Deaths                                   | **Implemented**                                    |
| Blocks Broken                                   | **Implemented**                                    |
| Blocks Placed                                   | **Implemented**                                    |
| Time Played                                     | Implementation In Progress - Will Use Server Stats |
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
| Distance Traveled                               | Unimplemented - Will Use Server Stats              |
| Number Of Kills                                 | **Implemented**                                    |
| Number Of Times Jumped                          | Unimplemented                                      |
| Distance Fallen                                 | Unimplemented - Will Use Server Stats              |
| Number Of Deaths In Each World                  | **Implemented**                                    |
| Eggs Thrown                                     | **Implemented**                                    |
| Chickens Hatched                                | **Implemented**                                    |
| Ender Pearls Thrown                             | **Implemented**                                    |
| Snow Balls Thrown                               | **Implemented**                                      |
| Animals Bred                                    | Unimplemented                                      |
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

#### JDK 8 is recommended.

Unfortunately there is a bit of setup for the first build before you can actually start working with the code, but
just remember you only have to do it once.

First, you need to have a MySQL server installed on your build machine. This is needed for the build process as
Querydsl uses a template table to generate its classes. Until your first build you won't be able to work with the code
correctly because it will be filled with errors, as the Querydsl classes haven't been generated yet. You can download
MySQL [here](http://dev.mysql.com/downloads/mysql/).

Once you have MySQL installed, execute the `statcraft.sql` file in this project in MySQL. This will create the
`statcraft` database and all of the tables that go in it. Next, you either need to create a `statcraft` user that is
only accessible by `localhost`, with no password, modify the `pom.xml` to reflect the login information you want to use
for Querydsl to reach the `statcraft` database. If you decide to not modify the `pom.xml`, then I would advise you to
make sure the `statcraft` user is only accessibly by `localhost`, for security reasons.

Once you have MySQL setup properly, and the `statcraft` database created, with all of the tables, and the `statcraft`
user created, or modified the `pom.xml` to allow the Querydsl plugin to access the `statcraft` database, you can finally
build the project.

Make sure you are in StatCraft's root directory and run the command:

`mvn clean package`

When that is finished, look in the `target/` folder, and it will have the compiled .jar file.

Note, once you have the MySQL setup correctly, you won't need to worry about it again.

Further Info
------------

This project is still very much a work-in-progress, and it is not very close to release. However, if you have any
questions or want to contact me for any reason, you can find me in IRC:

`irc.spi.gt`

I am usually in *#spigot* and *#spigot-dev*, but you can always just message me, username is DemonWav.
If you can't  find me there, you should always be able to message me in `chat.freenode.net`
