DoesNotCompute
==============

A computer mod that implements an emulated 6502 processor.

src/main/java - contains the main source code

src/main/resources - contains the assets (textures, sounds, models, etc.)

doc - contains the javadoc for the project

gradlew (gradlew.bat) - run this with the argument "build" to generate the compiled code

This project was coded around Minecraft Forge/FML 11.14.0.1273 (Minecraft 1.8), use that version for best compatibility.

Overview
========

Currently, this mod only implements a single block and a single item. The block (named Computer, not-so-creatively) acts as an interface to a fully emulated 6502 processor within the block's tile entity. Using the GUI, players are able to provide input to the computer and program it (initially in 6502 assembly). The item in the mod is a floppy disk, intended for sharing data and programs between computers.

Currently planned but not implemented features:
- More processor integration, eventually matching the instruction set of the RPC/8e from RedPower 2.
- Computer peripherals, manipulated through memory maps
- Software to come pre-programmed on the computer so you don't need to program it in assembly (!!!)
- Realistic floppy disks, manipulated through sectors instead of individual bytes
- Load times on the floppy drive to make it slower than RAM accesses and a sound to play when the drive is active
- Real number of CPU cycles for each instruction instead of executing a solid twenty thousand instructions per second
- Global limit on the number of instructions/cycles performed on all computers per second or per tick in order to keep server loads down
- Add more I/O to the computer to make it actually useful for external devices