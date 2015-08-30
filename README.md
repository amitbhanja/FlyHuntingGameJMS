# FlyHuntingGameJMS
The game is about hunting a fly with a fly flap. On the GUI a fly randomly appears. By pressing the mouse on
top of the fly, the fly was “hunted”. The player who caught the fly first gets a point. Once the
fly was hunted it re-appears at a different position. All players see the same fly at the same
position. Also, all players see the current points of all other players. 

The players can be at different locations. The coordination among the players is done using Java Messaging Service.
Each client manages one fly on its own and exchanges the related information with the other clients.

The GUI for the Client is Swing based.
The GUI shows the fly
The GUI shows a list of all players with their current points scored
The GUI notifies the player when a fly was hunted

Apache ActiveMQ Version 5.9.0 is used as message broker. 

Instructions for running the application is in the Readme.txt file.
