DoesNotCompute
==============

A computer mod that implements an emulated 6502 processor.

src/main/java ------- contains the main source code
src/main/resources -- contains the assets (textures, etc.)
doc ----------------- contains the javadoc for the project
gradle (gradle.bat) - run this to compile the source code

This project was coded around Minecraft Forge/FML 10.13.2.1235 (Minecraft 1.7.10), use that version for best compatibility.

---Overview---
==============

Currently, this mod only implements a single block, and no items. This block (named Computer, not-so-creatively) acts as an interface to a fully emulated 6502 processor within the block's tile entity. Using the GUI, players are able to provide input to the computer and program it (initially in 6502 assembly).

Currently planned but not implemented features:
- The graphical interface (doh!)
- Floppy disks (computer textures exist)
- More processor integration, eventually matching the instruction set of the RPC/8e from RedPower 2.
- Computer peripherals, manipulated through memory maps
- Software to come pre-programmed on the computer so you don't need to program it in assembly (!!!)
