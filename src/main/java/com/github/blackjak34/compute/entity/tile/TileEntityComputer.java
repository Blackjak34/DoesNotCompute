package com.github.blackjak34.compute.entity.tile;

import java.util.Arrays;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import com.github.blackjak34.compute.block.BlockComputer;
import com.github.blackjak34.compute.enums.InstructionComputer;
import com.github.blackjak34.compute.enums.StateComputer;

/**
 * The emulator component of the Computer block. Fully
 * emulates an old 6502 processor from the '70s. Players
 * can feed information into the computer through the GUI
 * made available by {@link BlockComputer}.
 * 
 * @author Blackjak34
 * @since 1.0
 */
public class TileEntityComputer extends TileEntity {
	
	/**
	 * The current column on the screen that the cursor
	 * is occupying. Used for rendering and putting data
	 * into the screen buffer.
	 */
	public int cursorX = 0;
	
	/**
	 * The current row on the screen that the cursor
	 * is occupying. Used for rendering and putting data
	 * into the screen buffer.
	 */
	public int cursorY = 0;
	
	/**
	 * The screen buffer, for an 80x50 character screen
	 * size. Writing a byte into this buffer will display
	 * a character on the screen accordingly.
	 */
	public byte[][] screenBuffer = new byte[80][50];
	
	/**
	 * The registers within the 6502. Register A acts as
	 * the 'accumulator' and is primarily the target of
	 * most calculations, Register X and Y are mostly used
	 * for looping over data within memory and storing
	 * values temporarily, and the stack pointer stores
	 * the address on page 1 where the next item on the
	 * stack may be stored. (The most recent value pushed
	 * to the stack may be found by reading the memory at
	 * location stackPointer - 1.)
	 */
	private byte registerA, registerX, registerY, stackPointer = (byte) 0xFF;
	
	/**
	 * The processor status flags. These flags are (within
	 * the actual 6502) stored as individual bits within
	 * a register dedicated to holding them, but this
	 * implementation uses boolean variables for simplicity.
	 * Bit 5 is unused and always remains a logical 1,
	 * leaving seven flags. They are, in ascending order,
	 * the carry, zero, interrupt-disable, decimal-mode,
	 * break, overflow, and sign flags.
	 */
	private boolean flagCarry, flagZero, flagInterrupt, flagDecimal, flagBreak, flagOverflow, flagSign;
	
	/**
	 * The program counter. While at first glance this
	 * appears to be another register, the program counter
	 * is actually 16 bits instead of 8 to allow it to
	 * address up to 65536 bytes of memory. Every time the
	 * tile entity is updated (once per tick), the
	 * instruction at the location pointed to by the
	 * program counter is executed and the program counter
	 * is incremented by the length of that particular
	 * instruction in order to point it to the next one.
	 */
	private int programCounter;
	
	/**
	 * The memory of the computer. This is represented
	 * with a byte array 65536 bytes in length.
	 */
	private byte[] memory = new byte[65536];
	
	/**
	 * The current state of the computer. This state
	 * may be 'RESET', 'HALT', or 'RUN'. While the computer
	 * only executes instructions while its state is RUN,
	 * RESET and HALT also serve to differentiate between
	 * the two settings for the front texture of the
	 * computer block. (see {@link BlockComputer} and
	 * {@link StateComputer})
	 */
	private StateComputer state;
	
	/**
	 * The world time when this tile entity was created.
	 * This value is subtracted from the current world time
	 * in order to produce the amount of time that this
	 * emulator has existed for.
	 */
	private long creationTime;
	
	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound data = new NBTTagCompound();
		writeToNBT(data);
		
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, data);
	}
	
	/**
	 * Tests a specific bit within a byte and returns its
	 * value as a boolean.
	 * 
	 * @param target The byte to be tested
	 * @param offset Which bit to test
	 * @return Whether or not that bit is set
	 */
	private static boolean testBit(byte target, int offset) {
		return (target&(1<<offset)) != 0;
	}
	
	/**
	 * A helper function for accessing memory through an
	 * integer variable. If the value of the index is
	 * greater than the amount of memory available or less
	 * than zero then the function returns 0xFF.
	 * 
	 * @param index The memory location to be read
	 * @return The byte at that location
	 */
	public byte readMemory(int index) {
		if(index > 65535 || index < 0) {return (byte) 0xFF;}
		return memory[index];
	}
	
	/**
	 * Similar in function to writeMemory, but
	 * writes the specified value to the memory location
	 * instead of reading from it. If the index is invalid,
	 * the function simply returns.
	 * 
	 * @param index The memory location to be written to
	 * @param value The value to write
	 */
	public void writeMemory(int index, byte value) {
		if(index > 65535 || index < 0) {return;}
		memory[index] = value;
	}
	
	/**
	 * Pushes a value to the stack. Adds 256 (decimal) to
	 * the stack pointer to put it on page 1, assigns the
	 * memory location at that address to the specified
	 * value, and decrements the stack pointer. This
	 * function does NOT check if the stack is full, and
	 * the stack pointer WILL overflow if pushed too
	 * frequently.
	 * 
	 * @param value The value to be pushed.
	 */
	private void pushStack(byte value) {
		memory[256 + stackPointer] = value;
		stackPointer--;
	}
	
	/**
	 * Pulls a value off of the stack. Increments the stack
	 * pointer, adds 256 (decimal) to it to put it on page
	 * 1, and returns the byte stored at that address. This
	 * function does NOT check if the stack is empty, and
	 * the stack pointer WILL underflow if pulled too
	 * frequently.
	 * 
	 * @return The value pulled off of the stack
	 */
	private byte pullStack() {
		stackPointer++;
		return memory[256 + stackPointer];
	}
	
	/**
	 * The constructor for an emulator instance. Fills the
	 * memory with #$FF and saves the current time as the
	 * time that this computer was created.
	 */
	public TileEntityComputer() {
		Arrays.fill(memory, (byte) 0xFF);
		creationTime = getWorldObj().getTotalWorldTime();
	}
	
	/**
	 * Sets the program counter to the value and wraps it
	 * if it is too large. This has the effect of changing
	 * the code that the emulator is currently executing
	 * to the new location.
	 * 
	 * @param value The address to jump to
	 */
	public void setProgramCounter(int value) {
		programCounter = value;
		programCounter %= 65536;
	}
	
	/**
	 * Sets the computer's current state (see
	 * {@link StateComputer}). The state should always be
	 * changed through this function instead of directly
	 * because the metadata is also set accordingly.
	 * 
	 * @param state The new state to use
	 */
	public void setState(StateComputer state) {
		this.state = state;
		switch(state) {
		case HALT:
			getWorldObj().setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 4, 2);
			break;
		case RESET:
			getWorldObj().setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 2, 2);
			break;
		case RUN:
			getWorldObj().setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 6, 2);
			break;
		}
	}
	
	/**
	 * Returns a String representation of this object. This
	 * includes the values of the A, X, and Y registers, as
	 * well as the stack pointer, and the number of ticks
	 * that this computer has existed for.
	 * 
	 * @return A string representation of this emulator instance
	 */
	@Override
	public String toString() {
		StringBuilder string = new StringBuilder();
		string.append("A:");
		string.append(String.format("%x", registerA).toUpperCase());
		string.append(" X:");
		string.append(String.format("%x", registerX).toUpperCase());
		string.append(" Y:");
		string.append(String.format("%x", registerY).toUpperCase());
		string.append(" S:");
		string.append(String.format("%x", stackPointer).toUpperCase());
		string.append(" T:");
		string.append(getComputerTime());
		
		return string.toString();
	}
	
	/**
	 * Saves all of the data stored within this TileEntity
	 * into NBT format. Automatically called by Forge to
	 * save the data when the chunk containing this
	 * TileEntity is unloaded (by a player moving too far
	 * away, a player leaving the world, or the server
	 * shutting down).This function also appears to be
	 * called at arbitrary times during the game, so don't
	 * use it for running code that should be run on chunk
	 * unload.
	 */
	@Override
	public void writeToNBT(NBTTagCompound data) {
		for(int column=0;column<80;column++) {
			data.setByteArray(("screenBuffer_column" + column), screenBuffer[column]);
		}
		data.setByte("registerA", registerA);
		data.setByte("registerX", registerX);
		data.setByte("registerY", registerY);
		data.setByte("stackPointer", stackPointer);
		data.setInteger("programCounter", programCounter);
		
		data.setBoolean("flagCarry", flagCarry);
		data.setBoolean("flagZero", flagZero);
		data.setBoolean("flagInterrupt", flagInterrupt);
		data.setBoolean("flagDecimal", flagDecimal);
		data.setBoolean("flagBreak", flagBreak);
		data.setBoolean("flagOverflow", flagOverflow);
		data.setBoolean("flagSign", flagSign);
		
		data.setByteArray("memory", memory);
		
		data.setString("state", state.toString());
		data.setLong("creationTime", creationTime);
		
		super.writeToNBT(data);
	}
	
	/**
	 * Loads all of the data stored within an
	 * NBTTagCompound into this TileEntity accordingly.
	 * Automatically called by Forge to load the data
	 * when the chunk containing this TileEntity is
	 * loaded (by a player moving closer, a player joining
	 * the world, or the server starting up). This function
	 * also appears to be called at arbitrary times during
	 * the game, so don't use it for running code that
	 * should be run on chunk load.
	 */
	@Override
	public void readFromNBT(NBTTagCompound data) {
		for(int column=0;column<80;column++) {
			screenBuffer[column] = data.getByteArray("screenBuffer_column" + column);
		}
		registerA = data.getByte("registerA");
		registerX = data.getByte("registerX");
		registerY = data.getByte("registerY");
		stackPointer = data.getByte("stackPointer");
		programCounter = data.getInteger("programCounter");
		
		flagCarry = data.getBoolean("flagCarry");
		flagZero = data.getBoolean("flagZero");
		flagInterrupt = data.getBoolean("flagInterrupt");
		flagDecimal = data.getBoolean("flagDecimal");
		flagBreak = data.getBoolean("flagBreak");
		flagOverflow = data.getBoolean("flagOverflow");
		flagSign = data.getBoolean("flagSign");
		
		memory = data.getByteArray("memory");
		
		state = StateComputer.valueOf(data.getString("state"));
		creationTime = data.getLong("creationTime");
		
		super.readFromNBT(data);
	}
	
	/**
	 * This function is automatically called by Forge once
	 * per tick for all loaded TileEntitys. This is the
	 * main work function of the emulator; it executes an
	 * instruction every time the TileEntity is updated.
	 * You may also call this function manually if you want
	 * the emulator to asynchronously execute a command.
	 */
	@Override
	public void updateEntity() {
		if(state != StateComputer.RUN) {return;}
		
		markDirty();
		
		InstructionComputer instruction = InstructionComputer.getInstruction(readMemory(programCounter) & 0xFF);
		switch(instruction) {
		case ADC_ABS:
			performAddition(readImmAddress(programCounter+1), false);
			break;
		case ADC_ABS_X:
			performAddition(readImmAddress(programCounter+1)+(registerX&0xFF), false);
			break;
		case ADC_ABS_Y:
			performAddition(readImmAddress(programCounter+1)+(registerY&0xFF), false);
			break;
		case ADC_IMM:
			performAddition(programCounter+1, false);
			break;
		case ADC_IND_X:
			performAddition(readImmAddress(readMemory(programCounter+1)+registerX & 0xFF), false);
			break;
		case ADC_IND_Y:
			performAddition(readImmAddress(readMemory(programCounter+1))+(registerY&0xFF), false);
			break;
		case ADC_ZP:
			performAddition(readMemory(programCounter+1), false);
			break;
		case ADC_ZP_X:
			performAddition(readMemory(programCounter+1)+registerX & 0xFF, false);
			break;
		case AND_ABS:
			registerA &= readMemory(readImmAddress(programCounter+1));
			setSignZeroFlags(registerA);
			break;
		case AND_ABS_X:
			registerA &= readMemory(readImmAddress(programCounter+1)+(registerX&0xFF));
			setSignZeroFlags(registerA);
			break;
		case AND_ABS_Y:
			registerA &= readMemory(readImmAddress(programCounter+1)+(registerY&0xFF));
			setSignZeroFlags(registerA);
			break;
		case AND_IMM:
			registerA &= readMemory(programCounter+1);
			setSignZeroFlags(registerA);
			break;
		case AND_IND_X:
			registerA &= readMemory(readImmAddress(readMemory(programCounter+1)+registerX & 0xFF));
			setSignZeroFlags(registerA);
			break;
		case AND_IND_Y:
			registerA &= readMemory(readImmAddress(readMemory(programCounter+1))+(registerY&0xFF));
			setSignZeroFlags(registerA);
			break;
		case AND_ZP:
			registerA &= readMemory(readMemory(programCounter+1));
			setSignZeroFlags(registerA);
			break;
		case AND_ZP_X:
			registerA &= readMemory(readMemory(programCounter+1)+registerX & 0xFF);
			setSignZeroFlags(registerA);
			break;
		case ASL_A:
			flagCarry = registerA>127;
			
			registerA <<= 1;
			setSignZeroFlags(registerA);
			break;
		case ASL_ABS:
			int aslAbsAddress = readImmAddress(programCounter+1);
			byte aslAbsValue = readMemory(aslAbsAddress);
			flagCarry = aslAbsValue>127;
			
			aslAbsValue <<= 1;
			setSignZeroFlags(aslAbsValue);
			
			writeMemory(aslAbsAddress, aslAbsValue);
			break;
		case ASL_ABS_X:
			int aslAbsXAddress = readImmAddress(programCounter+1)+(registerX&0xFF);
			byte aslAbsXValue = readMemory(aslAbsXAddress);
			flagCarry = aslAbsXValue>127;
			
			aslAbsXValue <<= 1;
			setSignZeroFlags(aslAbsXValue);
			
			writeMemory(aslAbsXAddress, aslAbsXValue);
			break;
		case ASL_ZP:
			int aslZpAddress = readMemory(programCounter+1);
			byte aslZpValue = readMemory(aslZpAddress);
			flagCarry = aslZpValue>127;
			
			aslZpValue <<= 1;
			setSignZeroFlags(aslZpValue);
			
			writeMemory(aslZpAddress, aslZpValue);
			break;
		case ASL_ZP_X:
			int aslZpXAddress = readMemory(programCounter+1)+registerX & 0xFF;
			byte aslZpXValue = readMemory(aslZpXAddress);
			flagCarry = aslZpXValue>127;
			
			aslZpXValue <<= 1;
			setSignZeroFlags(aslZpXValue);
			
			writeMemory(aslZpXAddress, aslZpXValue);
			break;
		case BCC_REL:
			if(flagCarry == false) {
				byte relValue = readMemory(programCounter+1);
				programCounter += relValue;
			}
			break;
		case BCS_REL:
			if(flagCarry == true) {
				byte relValue = readMemory(programCounter+1);
				programCounter += relValue;
			}
			break;
		case BEQ_REL:
			if(flagZero == true) {
				byte relValue = readMemory(programCounter+1);
				programCounter += relValue;
			}
			break;
		case BIT_ABS:
			byte bitAbsValue = readMemory(readImmAddress(programCounter+1));
			
			flagZero = (registerA & bitAbsValue) == 0;
			flagSign = testBit(bitAbsValue, 6);
			flagOverflow = testBit(bitAbsValue, 7);
			break;
		case BIT_ZP:
			byte bitZpValue = readMemory(readMemory(programCounter+1));
			
			flagZero = (registerA & bitZpValue) == 0;
			flagSign = testBit(bitZpValue, 6);
			flagOverflow = testBit(bitZpValue, 7);
			break;
		case BMI_REL:
			if(flagSign == true) {
				byte relValue = readMemory(programCounter+1);
				programCounter += relValue;
			}
			break;
		case BNE_REL:
			if(flagZero == false) {
				byte relValue = readMemory(programCounter+1);
				programCounter += relValue;
			}
			break;
		case BPL_REL:
			if(flagSign == false) {
				byte relValue = readMemory(programCounter+1);
				programCounter += relValue;
			}
			break;
		case BRK:
			flagBreak = true;
			break;
		case BVC_REL:
			if(flagOverflow == false) {
				byte relValue = readMemory(programCounter+1);
				programCounter += relValue;
			}
			break;
		case BVS_REL:
			if(flagOverflow == true) {
				byte relValue = readMemory(programCounter+1);
				programCounter += relValue;
			}
			break;
		case CLC:
			flagCarry = false;
			break;
		case CLD:
			flagDecimal = false;
			break;
		case CLI:
			flagInterrupt = false;
			break;
		case CLV:
			flagOverflow = false;
			break;
		case CMP_ABS:
			performComparation(readImmAddress(programCounter+1), registerA);
			break;
		case CMP_ABS_X:
			performComparation(readImmAddress(programCounter+1)+(registerX&0xFF), registerA);
			break;
		case CMP_ABS_Y:
			performComparation(readImmAddress(programCounter+1)+(registerY&0xFF), registerA);
			break;
		case CMP_IMM:
			performComparation(programCounter+1, registerA);
			break;
		case CMP_IND_X:
			performComparation(readImmAddress(readMemory(programCounter+1)+registerX & 0xFF), registerA);
			break;
		case CMP_IND_Y:
			performComparation(readImmAddress(readMemory(programCounter+1))+(registerY&0xFF), registerA);
			break;
		case CMP_ZP:
			performComparation(readMemory(programCounter+1), registerA);
			break;
		case CMP_ZP_X:
			performComparation(readMemory(programCounter+1)+registerX & 0xFF, registerA);
			break;
		case CPX_ABS:
			performComparation(readImmAddress(programCounter+1), registerX);
			break;
		case CPX_IMM:
			performComparation(programCounter+1, registerX);
			break;
		case CPX_ZP:
			performComparation(readMemory(programCounter+1), registerX);
			break;
		case CPY_ABS:
			performComparation(readImmAddress(programCounter+1), registerY);
			break;
		case CPY_IMM:
			performComparation(programCounter+1, registerY);
			break;
		case CPY_ZP:
			performComparation(readMemory(programCounter+1), registerY);
			break;
		case DEC_ABS:
			int decAbsAddress = readImmAddress(programCounter+1);
			byte decAbsValue = readMemory(decAbsAddress);
			
			decAbsValue--;
			setSignZeroFlags(decAbsValue);
			
			writeMemory(decAbsAddress, decAbsValue);
			break;
		case DEC_ABS_X:
			int decAbsXAddress = readImmAddress(programCounter+1)+(registerX&0xFF);
			byte decAbsXValue = readMemory(decAbsXAddress);
			
			decAbsXValue--;
			setSignZeroFlags(decAbsXValue);
			
			writeMemory(decAbsXAddress, decAbsXValue);
			break;
		case DEC_ZP:
			int decZpAddress = readMemory(programCounter+1);
			byte decZpValue = readMemory(decZpAddress);
			
			decZpValue--;
			setSignZeroFlags(decZpValue);
			
			writeMemory(decZpAddress, decZpValue);
			break;
		case DEC_ZP_X:
			int decZpXAddress = readMemory(programCounter+1)+registerX & 0xFF;
			byte decZpXValue = readMemory(decZpXAddress);
			
			decZpXValue--;
			setSignZeroFlags(decZpXValue);
			
			writeMemory(decZpXAddress, decZpXValue);
			break;
		case DEX:
			registerX--;
			setSignZeroFlags(registerX);
			break;
		case DEY:
			registerY--;
			setSignZeroFlags(registerY);
			break;
		case EOR_ABS:
			registerA ^= readMemory(readImmAddress(programCounter+1));
			setSignZeroFlags(registerA);
			break;
		case EOR_ABS_X:
			registerA ^= readMemory(readImmAddress(programCounter+1)+(registerX&0xFF));
			setSignZeroFlags(registerA);
			break;
		case EOR_ABS_Y:
			registerA ^= readMemory(readImmAddress(programCounter+1)+(registerY&0xFF));
			setSignZeroFlags(registerA);
			break;
		case EOR_IMM:
			registerA ^= readMemory(programCounter+1);
			setSignZeroFlags(registerA);
			break;
		case EOR_IND_X:
			registerA ^= readMemory(readImmAddress(readMemory(programCounter+1)+registerX & 0xFF));
			setSignZeroFlags(registerA);
			break;
		case EOR_IND_Y:
			registerA ^= readMemory(readImmAddress(readMemory(programCounter+1))+(registerY&0xFF));
			setSignZeroFlags(registerA);
			break;
		case EOR_ZP:
			registerA ^= readMemory(readMemory(programCounter+1));
			setSignZeroFlags(registerA);
			break;
		case EOR_ZP_X:
			registerA ^= readMemory(readMemory(programCounter+1)+registerX & 0xFF);
			setSignZeroFlags(registerA);
			break;
		case INC_ABS:
			int incAbsAddress = readImmAddress(programCounter+1);
			byte incAbsValue = readMemory(incAbsAddress);
			
			incAbsValue++;
			setSignZeroFlags(incAbsValue);
			
			writeMemory(incAbsAddress, incAbsValue);
			break;
		case INC_ABS_X:
			int incAbsXAddress = readImmAddress(programCounter+1)+(registerX&0xFF);
			byte incAbsXValue = readMemory(incAbsXAddress);
			
			incAbsXValue++;
			setSignZeroFlags(incAbsXValue);
			
			writeMemory(incAbsXAddress, incAbsXValue);
			break;
		case INC_ZP:
			int incZpAddress = readMemory(programCounter+1);
			byte incZpValue = readMemory(incZpAddress);
			
			incZpValue++;
			setSignZeroFlags(incZpValue);
			
			writeMemory(incZpAddress, incZpValue);
			break;
		case INC_ZP_X:
			int incZpXAddress = readMemory(programCounter+1)+registerX & 0xFF;
			byte incZpXValue = readMemory(incZpXAddress);
			
			incZpXValue++;
			setSignZeroFlags(incZpXValue);
			
			writeMemory(incZpXAddress, incZpXValue);
			break;
		case INX:
			registerX++;
			setSignZeroFlags(registerX);
			break;
		case INY:
			registerY++;
			setSignZeroFlags(registerY);
			break;
		case JMP_ABS:
			programCounter = readImmAddress(programCounter+1);
			break;
		case JMP_IND:
			programCounter = readImmAddress(readImmAddress(programCounter+1));
			break;
		case JSR_ABS:
			int addressToPush = programCounter + instruction.getLength() - 1;
			pushStack((byte) (addressToPush >>> 8));
			pushStack((byte) addressToPush);
			
			programCounter = readImmAddress(programCounter+1);
			break;
		case LDA_ABS:
			registerA = readMemory(readImmAddress(programCounter+1));
			setSignZeroFlags(registerA);
			break;
		case LDA_ABS_X:
			registerA = readMemory(readImmAddress(programCounter+1)+(registerX&0xFF));
			setSignZeroFlags(registerA);
			break;
		case LDA_ABS_Y:
			registerA = readMemory(readImmAddress(programCounter+1)+(registerY&0xFF));
			setSignZeroFlags(registerA);
			break;
		case LDA_IMM:
			registerA = readMemory(programCounter+1);
			setSignZeroFlags(registerA);
			break;
		case LDA_IND_X:
			registerA = readMemory(readImmAddress(readMemory(programCounter+1)+registerX & 0xFF));
			setSignZeroFlags(registerA);
			break;
		case LDA_IND_Y:
			registerA = readMemory(readImmAddress(readMemory(programCounter+1))+(registerY&0xFF));
			setSignZeroFlags(registerA);
			break;
		case LDA_ZP:
			registerA = readMemory(readMemory(programCounter+1));
			setSignZeroFlags(registerA);
			break;
		case LDA_ZP_X:
			registerA = readMemory(readMemory(programCounter+1)+registerX & 0xFF);
			setSignZeroFlags(registerA);
			break;
		case LDX_ABS:
			registerX = readMemory(readImmAddress(programCounter+1));
			setSignZeroFlags(registerX);
			break;
		case LDX_ABS_Y:
			registerX = readMemory(readImmAddress(programCounter+1)+(registerY&0xFF));
			setSignZeroFlags(registerX);
			break;
		case LDX_IMM:
			registerX = readMemory(programCounter+1);
			setSignZeroFlags(registerX);
			break;
		case LDX_ZP:
			registerX = readMemory(readMemory(programCounter+1));
			setSignZeroFlags(registerX);
			break;
		case LDX_ZP_Y:
			registerX = readMemory(readMemory(programCounter+1)+registerY & 0xFF);
			setSignZeroFlags(registerX);
			break;
		case LDY_ABS:
			registerY = readMemory(readImmAddress(programCounter+1));
			setSignZeroFlags(registerY);
			break;
		case LDY_ABS_X:
			registerY = readMemory(readImmAddress(programCounter+1)+(registerX&0xFF));
			setSignZeroFlags(registerY);
			break;
		case LDY_IMM:
			registerY = readMemory(programCounter+1);
			setSignZeroFlags(registerY);
			break;
		case LDY_ZP:
			registerY = readMemory(readMemory(programCounter+1));
			setSignZeroFlags(registerY);
			break;
		case LDY_ZP_X:
			registerY = readMemory(readMemory(programCounter+1)+registerX & 0xFF);
			setSignZeroFlags(registerY);
			break;
		case LSR_A:
			flagCarry = testBit(registerA, 0);
			
			registerA >>>= 1;
			setSignZeroFlags(registerA);
			break;
		case LSR_ABS:
			int lsrAbsAddress = readImmAddress(programCounter+1);
			byte lsrAbsValue = readMemory(lsrAbsAddress);
			
			flagCarry = testBit(lsrAbsValue, 0);
			
			lsrAbsValue >>>= 1;
			setSignZeroFlags(lsrAbsValue);
			
			writeMemory(lsrAbsAddress, lsrAbsValue);
			break;
		case LSR_ABS_X:
			int lsrAbsXAddress = readImmAddress(programCounter+1)+(registerX&0xFF);
			byte lsrAbsXValue = readMemory(lsrAbsXAddress);
			
			flagCarry = testBit(lsrAbsXValue, 0);
			
			lsrAbsXValue >>>= 1;
			setSignZeroFlags(lsrAbsXValue);
			
			writeMemory(lsrAbsXAddress, lsrAbsXValue);
			break;
		case LSR_ZP:
			int lsrZpAddress = readMemory(programCounter+1);
			byte lsrZpValue = readMemory(lsrZpAddress);
			
			lsrZpValue >>>= 1;
			setSignZeroFlags(lsrZpValue);
			
			writeMemory(lsrZpAddress, lsrZpValue);
			break;
		case LSR_ZP_X:
			int lsrZpXAddress = readMemory(programCounter+1)+registerX & 0xFF;
			byte lsrZpXValue = readMemory(lsrZpXAddress);
			
			lsrZpXValue >>>= 1;
			setSignZeroFlags(lsrZpXValue);
			
			writeMemory(lsrZpXAddress, lsrZpXValue);
			break;
		case NOP:
			break;
		case ORA_ABS:
			registerA |= readMemory(readImmAddress(programCounter+1));
			setSignZeroFlags(registerA);
			break;
		case ORA_ABS_X:
			registerA |= readMemory(readImmAddress(programCounter+1)+(registerX&0xFF));
			setSignZeroFlags(registerA);
			break;
		case ORA_ABS_Y:
			registerA |= readMemory(readImmAddress(programCounter+1)+(registerY&0xFF));
			setSignZeroFlags(registerA);
			break;
		case ORA_IMM:
			registerA |= readMemory(programCounter+1);
			setSignZeroFlags(registerA);
			break;
		case ORA_IND_X:
			registerA |= readMemory(readImmAddress(readMemory(programCounter+1)+registerX & 0xFF));
			setSignZeroFlags(registerA);
			break;
		case ORA_IND_Y:
			registerA |= readMemory(readImmAddress(readMemory(programCounter+1)+registerY & 0xFF));
			setSignZeroFlags(registerA);
			break;
		case ORA_ZP:
			registerA |= readMemory(readMemory(programCounter+1));
			setSignZeroFlags(registerA);
			break;
		case ORA_ZP_X:
			registerA |= readMemory(readMemory(programCounter+1)+registerX & 0xFF);
			setSignZeroFlags(registerA);
			break;
		case PHA:
			pushStack(registerA);
			break;
		case PHP:
			int pushFlags = flagCarry ? 1 : 0;
			pushFlags += flagZero ? 2 : 0;
			pushFlags += flagInterrupt ? 4 : 0;
			pushFlags += flagDecimal ? 8 : 0;
			pushFlags += flagBreak ? 16 : 0;
			pushFlags += 32;
			pushFlags += flagOverflow ? 64 : 0;
			pushFlags += flagSign ? 128 : 0;
			
			pushStack((byte) pushFlags);
			break;
		case PLA:
			registerA = pullStack();
			break;
		case PLP:
			byte pullFlags = pullStack();
			
			flagCarry = testBit(pullFlags, 0);
			flagZero = testBit(pullFlags, 1);
			flagInterrupt = testBit(pullFlags, 2);
			flagDecimal = testBit(pullFlags, 3);
			flagBreak = testBit(pullFlags, 4);
			flagOverflow = testBit(pullFlags, 6);
			flagSign = testBit(pullFlags, 7);
			break;
		case ROL_A:
			flagCarry = testBit(registerA, 7);
			registerA <<= 1;
			registerA += flagCarry ? 1 : 0;
			
			setSignZeroFlags(registerA);
			break;
		case ROL_ABS:
			int rolAbsAddress = readImmAddress(programCounter+1);
			byte rolAbsValue = readMemory(rolAbsAddress);
			
			flagCarry = testBit(rolAbsValue, 7);
			rolAbsValue <<= 1;
			rolAbsValue += flagCarry ? 1 : 0;
			
			setSignZeroFlags(rolAbsValue);
			writeMemory(rolAbsAddress, rolAbsValue);
			break;
		case ROL_ABS_X:
			int rolAbsXAddress = readImmAddress(programCounter+1)+(registerX%0xFF);
			byte rolAbsXValue = readMemory(rolAbsXAddress);
			
			flagCarry = testBit(rolAbsXValue, 7);
			rolAbsXValue <<= 1;
			rolAbsXValue += flagCarry ? 1 : 0;
			
			setSignZeroFlags(rolAbsXValue);
			writeMemory(rolAbsXAddress, rolAbsXValue);
			break;
		case ROL_ZP:
			int rolZpAddress = readMemory(readMemory(programCounter+1));
			byte rolZpValue = readMemory(rolZpAddress);
			
			flagCarry = testBit(rolZpValue, 7);
			rolZpValue <<= 1;
			rolZpValue += flagCarry ? 1 : 0;
			
			setSignZeroFlags(rolZpValue);
			writeMemory(rolZpAddress, rolZpValue);
			break;
		case ROL_ZP_X:
			int rolZpXAddress = readMemory(readMemory(programCounter+1)+registerX & 0xFF);
			byte rolZpXValue = readMemory(rolZpXAddress);
			
			flagCarry = testBit(rolZpXValue, 7);
			rolZpXValue <<= 1;
			rolZpXValue += flagCarry ? 1 : 0;
			
			setSignZeroFlags(rolZpXValue);
			writeMemory(rolZpXAddress, rolZpXValue);
			break;
		case ROR_A:
			flagCarry = testBit(registerA, 0);
			registerA >>>= 1;
			registerA += flagCarry ? 128 : 0;
			
			setSignZeroFlags(registerA);
			break;
		case ROR_ABS:
			int rorAbsAddress = readImmAddress(programCounter+1);
			byte rorAbsValue = readMemory(rorAbsAddress);
			
			flagCarry = testBit(rorAbsValue, 0);
			rorAbsValue >>>= 1;
			rorAbsValue += flagCarry ? 128 : 0;
			
			setSignZeroFlags(rorAbsValue);
			writeMemory(rorAbsAddress, rorAbsValue);
			break;
		case ROR_ABS_X:
			int rorAbsXAddress = readImmAddress(programCounter+1)+(registerX%0xFF);
			byte rorAbsXValue = readMemory(rorAbsXAddress);
			
			flagCarry = testBit(rorAbsXValue, 0);
			rorAbsXValue >>>= 1;
			rorAbsXValue += flagCarry ? 128 : 0;
			
			setSignZeroFlags(rorAbsXValue);
			writeMemory(rorAbsXAddress, rorAbsXValue);
			break;
		case ROR_ZP:
			int rorZpAddress = readMemory(programCounter+1);
			byte rorZpValue = readMemory(rorZpAddress);
			
			flagCarry = testBit(rorZpValue, 0);
			rorZpValue >>>= 1;
			rorZpValue += flagCarry ? 128 : 0;
			
			setSignZeroFlags(rorZpValue);
			writeMemory(rorZpAddress, rorZpValue);
			break;
		case ROR_ZP_X:
			int rorZpXAddress = readMemory(programCounter+1)+registerX & 0xFF;
			byte rorZpXValue = readMemory(rorZpXAddress);
			
			flagCarry = testBit(rorZpXValue, 0);
			rorZpXValue >>>= 1;
			rorZpXValue += flagCarry ? 128 : 0;
			
			setSignZeroFlags(rorZpXValue);
			writeMemory(rorZpXAddress, rorZpXValue);
			break;
		case RTI:
			byte pullFlags2 = pullStack();
			
			flagCarry = testBit(pullFlags2, 0);
			flagZero = testBit(pullFlags2, 1);
			flagInterrupt = testBit(pullFlags2, 2);
			flagDecimal = testBit(pullFlags2, 3);
			flagBreak = testBit(pullFlags2, 4);
			flagOverflow = testBit(pullFlags2, 6);
			flagSign = testBit(pullFlags2, 7);
			
			programCounter = pullImmAddress();
			break;
		case RTS:
			programCounter = pullImmAddress()+1;
			break;
		case SBC_ABS:
			performAddition(readImmAddress(programCounter+1), true);
			break;
		case SBC_ABS_X:
			performAddition(readImmAddress(programCounter+1)+(registerX&0xFF), true);
			break;
		case SBC_ABS_Y:
			performAddition(readImmAddress(programCounter+1)+(registerY&0xFF), true);
			break;
		case SBC_IMM:
			performAddition(programCounter+1, true);
			break;
		case SBC_IND_X:
			performAddition(readImmAddress(readMemory(programCounter+1)+registerX & 0xFF), true);
			break;
		case SBC_IND_Y:
			performAddition(readImmAddress(readMemory(programCounter+1))+(registerY&0xFF), true);
			break;
		case SBC_ZP:
			performAddition(readMemory(programCounter+1), true);
			break;
		case SBC_ZP_X:
			performAddition(readMemory(programCounter+1)+registerX & 0xFF, true);
			break;
		case SEC:
			flagCarry = true;
			break;
		case SED:
			flagDecimal = true;
			break;
		case SEI:
			flagInterrupt = true;
			break;
		case STA_ABS:
			writeMemory(readImmAddress(programCounter+1), registerA);
			break;
		case STA_ABS_X:
			writeMemory(readImmAddress(programCounter+1)+(registerX&0xFF), registerA);
			break;
		case STA_ABS_Y:
			writeMemory(readImmAddress(programCounter+1)+(registerY&0xFF), registerA);
			break;
		case STA_IND_X:
			writeMemory(readImmAddress(readMemory(programCounter+1)+registerX & 0xFF), registerA);
			break;
		case STA_IND_Y:
			writeMemory(readImmAddress(readMemory(programCounter+1))+(registerY&0xFF), registerA);
			break;
		case STA_ZP:
			writeMemory(readMemory(programCounter+1), registerA);
			break;
		case STA_ZP_X:
			writeMemory(readMemory(programCounter+1)+registerX & 0xFF, registerA);
			break;
		case STP:
			setState(StateComputer.HALT);
			break;
		case STX_ABS:
			writeMemory(readImmAddress(programCounter+1), registerX);
			break;
		case STX_ZP:
			writeMemory(readMemory(programCounter+1), registerX);
			break;
		case STX_ZP_Y:
			writeMemory(readMemory(programCounter+1)+registerY & 0xFF, registerX);
			break;
		case STY_ABS:
			writeMemory(readImmAddress(programCounter+1), registerY);
			break;
		case STY_ZP:
			writeMemory(readMemory(programCounter+1), registerY);
			break;
		case STY_ZP_X:
			writeMemory(readMemory(programCounter+1)+registerX & 0xFF, registerY);
			break;
		case TAX:
			registerX = registerA;
			setSignZeroFlags(registerA);
			break;
		case TAY:
			registerY = registerA;
			setSignZeroFlags(registerA);
			break;
		case TSX:
			registerX = stackPointer;
			setSignZeroFlags(stackPointer);
			break;
		case TXA:
			registerA = registerX;
			setSignZeroFlags(registerX);
			break;
		case TXS:
			stackPointer = registerX;
			setSignZeroFlags(registerX);
			break;
		case TYA:
			registerA = registerY;
			setSignZeroFlags(registerY);
			break;
		case UNUSED:
			setState(StateComputer.HALT);
			System.out.println("The computer has encountered an unknown instruction: " + String.format("%x", instruction.getHexValue()).toUpperCase());
			break;
		}
		
		programCounter += instruction.getLength();
		programCounter %= 65536;
	}
	
	/**
	 * Reads and returns a little-endian two byte address
	 * from the given location in memory.
	 * 
	 * @param addressToRead The location to read from
	 * @return An absolute address that was read from memory
	 */
	private int readImmAddress(int addressToRead) {
		byte low = readMemory(addressToRead);
		byte high = readMemory(addressToRead+1);
		return (high << 8) | low;
	}
	
	/**
	 * Pulls and returns a two-byte address from the stack.
	 * The low byte is pulled first, so when storing an
	 * address on the stack the high byte should be pushed
	 * first.
	 * 
	 * @return An absolute address that was pulled from the stack
	 */
	private int pullImmAddress() {
		byte low = pullStack();
		byte high = pullStack();
		return (high << 8) | low;
	}
	
	/**
	 * Sets the sign flag if the test value is negative
	 * and the zero flag if the test value is zero.
	 * 
	 * @param testValue The value to be tested
	 */
	private void setSignZeroFlags(byte testValue) {
		flagSign = testValue<0;
		flagZero = testValue==0;
	}
	
	/**
	 * Seeks a byte value at the address given and adds it
	 * to the accumulator while accounting for and setting
	 * flags appropriately. Subtraction can also be
	 * performed by specifying so in the second parameter.
	 * 
	 * @param addressOfValue The address of the value to add/subtract
	 * @param subtract Whether subtraction should be performed or not
	 */
	private void performAddition(int addressOfValue, boolean subtract) {
		byte addValue = readMemory(addressOfValue);
		int result = (flagCarry ? 256 : 0 + registerA) + (subtract ? -addValue : addValue);
		registerA = (byte) result;
		
		flagOverflow = result<-128 || result>127;
		flagCarry = result>255;
		setSignZeroFlags(registerA);
	}
	
	/**
	 * Seeks a byte value at the address given and compares
	 * it to the value given. This sets the overflow, carry,
	 * sign, and zero flags similarly as if a subtraction had
	 * occurred but no actual changes are made to the registers.
	 * 
	 * @param addressOfValue The address of the value to compare
	 * @param valueComparedTo The value to compare the first to
	 */
	private void performComparation(int addressOfValue, int valueComparedTo) {
		byte compareValue = readMemory(addressOfValue);
		int result = (256 + valueComparedTo) - compareValue;
		
		flagOverflow = result<-128 || result>127;
		flagCarry = result>255;
		setSignZeroFlags((byte) result);
	}
	
	/**
	 * Calculates how long this computer has existed for,
	 * in ticks by subtracting the current world time from
	 * the time when this computer was created.
	 * 
	 * @return The number of ticks that this computer has existed for
	 */
	private long getComputerTime() {
		return getWorldObj().getTotalWorldTime() - creationTime;
	}
	
}
