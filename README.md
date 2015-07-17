DoesNotCompute
==============

A computer mod that implements an emulated 65EL02 processor.
Credit for the original design and implementation of the 65EL02, a modified version of the 65816 instruction set, goes to Eloraam, author of the now defunct RedPower 2 mod.
The main operating system for the computer is a Forth interpreter, which was also written by Eloraam and is being used without permission. I intend on discontinuing the Forth interpreter as soon as possible and defaulting to my own system as soon as I become proficient enough at assembly to write one.

src/main/java - contains the main source code

src/main/resources - contains the assets (textures, sounds, models, etc.)

gradlew (gradlew.bat) - run this with the argument "build" to generate the compiled code under build/libs/

This project was coded around Minecraft Forge/FML 11.14.3.1450 (Minecraft 1.8), use that version for best compatibility.

Overview
========

The main block in the mod (named Computer, not-so-creatively) acts as an interface to a fully emulated 65EL02 processor within the block's tile entity. Using the GUI, players are able to provide input to the computer and program it (initially in 65EL02 assembly). The mod also includes a floppy disk, intended for sharing data and programs between computers.

Currently planned but not implemented features:
- Global limit on the number of instructions/cycles performed on all computers per second or per tick in order to keep server loads down
- Add more I/O to the computer to make it actually useful for external devices
- Implement some other interesting devices that are no longer used from that era (tape drives, punch cards, mainframes (!), etc.)
