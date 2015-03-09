DoesNotCompute
==============

A computer mod that implements an emulated 65816 processor.

src/main/java - contains the main source code

src/main/resources - contains the assets (textures, sounds, models, etc.)

gradlew (gradlew.bat) - run this with the argument "build" to generate the compiled code

This project was coded around Minecraft Forge/FML 11.14.0.1273 (Minecraft 1.8), use that version for best compatibility.

Overview
========

Currently, this mod only implements only a few blocks and a single item. The main block (named Computer, not-so-creatively) acts as an interface to a fully emulated 65816 processor within the block's tile entity. Using the GUI, players are able to provide input to the computer and program it (initially in 65816 assembly). The item in the mod is a floppy disk, intended for sharing data and programs between computers.

Currently planned but not implemented features:
- Computer peripherals, manipulated through memory maps
- Software to come pre-programmed on the computer so you don't need to program it in assembly (!!!)
- A sound to play when the floppy drive is active
- Global limit on the number of instructions/cycles performed on all computers per second or per tick in order to keep server loads down
- Add more I/O to the computer to make it actually useful for external devices