# Few words about the repository: #
Here you will find the well-known L2JFree 1.3.0/CT2.3 Gracia Final server pack from 2010 with its full history included. By creating this repository my main goal was to keep the latest stable version of L2JFree archived and to make it useable with the nowdays used development environment & technologies. (Java JDK 8+, Maven 3, Latest Dependencies, POM.xmls, and a modern refactored project structure for a logical & enjoyable development.)

Although you have to understand one thing: despite the fact that I have made it compilable & runnable it does not mean that it is a plug & play type of software (As we all know none of L2J based projects are.), and also it is a project from 2010 extended with few essential updates. So I am not giving you any kind of support for this pack, you have to use it on your own risk. If you do not like the structural changes I made/or the pack at all - then do not use it. Also you have your own possibility to get the original & outdated L2JFree through the savormix repository and try to re-create my changes with more or less luck depends on your programming skills. Also you have the possibility to reset my (or savormix's) repository via "git reset --hard <commit hash>" command on your own to an older revision in case it is what you really want.

By all the refactors I made, my intention was to re-create the image of a modern L2J project which was invented by L2JFree Genesis. (gameobjects package & L2Player, etc)

Basically the repository was made mostly for myself in case I need a stable CT2.3 pack, but see how graceful I am, I let you to use it.

# Changelog in short: #
1. GIT related settings & files are added.

2. The latest L2EmuUnique's POM.xmls & project structure is adopted.

3. The never implemented EclipseLink feature is removed. (I had no mood to maintain it, sorry.)
4. Few outdated files are removed.

5. The latest L2JFree Genesis project formatter is adopted, also the source code is formatted with it. Also imports are organized too.

6. Few project settings are changed from default in order to avoid 1600+ warnings.

7. The latest L2JServer's console/GUI Database Installer is adopted.

8. The project is in the posession of "IDE mode" which is often called "eclipse-runnable-mode". In short you can run LoginServer and GameServer classes through Eclipse in Debug mode in order to do a productive and easy development.

9. The project structure is completly refactored to match/or atleast be similar to the image of L2JFree Genesis invented project structure which is similar to L2EmuUnique's project structure.

10. All the dependencies are updated to the latest available version. (The exceptions are listed & reasoned in code with comments. Pay attention!)

# Known Bugs: #
1. 'enchant_skill_trees.sql' is not executable fully by the built-in L2JServer's database installer, you have to execute it by Navicat or phpMyAdmin. Also with some versions of MySQL it is not possible. Try & luck. However it is does not worth to be fixed. Here comes my advice: if you plan to use this pack as a base of your project/server you should implement a new skill enchanting system. Nowdays you can find better skill enchanting systems in many open source projects which requires on DP side nothing more then a 20-40 lines long XML file instead of that heavy SQL file.


# Links: #

http://svn.l2jfree.com/l2jfree/

https://github.com/savormix/