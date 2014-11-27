package com.github.blackjak34.compute.entity.tile;

import java.util.Arrays;

import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

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
	 * values temporarily, the processor flags are set and
	 * read by different instructions and affect their
	 * operation, and the stack pointer stores the address
	 * on page 1 where the next item on the stack may be
	 * stored. (The most recent value pushed to the stack
	 * may be found by reading the memory at location
	 * stackPointer - 1.
	 * 
	 * The processor flags are as follows:
	 * 0 - Carry
	 * 1 - Zero
	 * 3 - Decimal Mode
	 * 4 - Break
	 * 5 - unused (always logical 1)
	 * 6 - Overflow
	 * 7 - Sign (negative)
	 */
	private byte registerA, registerX, registerY, flags, stackPointer = (byte) 0xFF;
	
	/**
	 * The program counter. While at first glance this
	 * appears to be another register, the program counter
	 * is actually 16 bits instead of 8 to allow it to
	 * address up to 65536 bytes of memory. Every time the
	 * tile entity is updated (once per tick), the
	 * instruction at the location pointed to by the
	 * program counter is executed and the program counter
	 * is incremented by one (some instructions also
	 * change/increment the program counter during their
	 * operation, so the it may still change before it is
	 * incremented).
	 */
	private short programCounter;
	
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
	private StateComputer state = StateComputer.RESET;
	
	/**
	 * The amount of time that this tile entity has
	 * existed for. This is incremented every time the
	 * tile entity is updated, even if the computer is
	 * not in RUN mode. Because this is of 'long' type and
	 * it is incremented 20 times per second, it will roll
	 * over once every 14,623,560,433.9 years, which is
	 * roughly the same as the current age of the universe.
	 */
	private long time;
	
	/**
	 * Sets a specific bit to a specified value within a
	 * byte. Mainly used for setting/unsetting the
	 * processor flags.
	 * 
	 * @param originalByte The byte value to be modified
	 * @param bitIndex Which bit to set
	 * @param value The value to set the bit to
	 * @return The modified byte
	 */
	private static byte setBit(byte originalByte, int bitIndex, boolean value) {
		return (byte) (value ? (originalByte|(1<<bitIndex)) : (originalByte&~(1<<bitIndex)));
	}
	
	/**
	 * Tests a specific bit within a byte. Used primarily
	 * for determining the overflow/carry flags.
	 * 
	 * @param target The byte to be tested
	 * @param offset Which bit to test
	 * @return Whether or not that bit is set
	 */
	private static boolean testBit(byte target, int offset) {
		return (target&(1<<offset)) != 0;
	}
	
	/**
	 * A helper function for accessing memory through a
	 * signed short variable (programCounter). Converts the
	 * value to unsigned by adding 32768 to it (max value
	 * of the short type) and returns the byte stored at
	 * that address.
	 * 
	 * @param index The memory location to be read
	 * @return The byte at that location
	 */
	public byte readMemory(short index) {
		return memory[index + 32768];
	}
	
	/**
	 * Similar in function to writeMemory, but
	 * writes the specified value to the memory location
	 * instead of reading from it.
	 * 
	 * @param index The memory location to be written to
	 * @param value The value to write
	 */
	public void writeMemory(short index, byte value) {
		memory[index + 32768] = value;
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
	 * memory with #$FF and sets the unused flag to a logical
	 * 1.
	 */
	public TileEntityComputer() {
		Arrays.fill(memory, (byte) 0xFF);
		flags = setBit(flags, 5, true);
	}
	
	/**
	 * Sets the program counter to the specified short
	 * value. This will cause the computer to start
	 * executing whatever instructions are stored at that
	 * location.
	 * 
	 * @param value The address to jump to
	 */
	public void setProgramCounter(short value) {
		programCounter = value;
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
	 * well as the stack pointer, and the number of times
	 * that this entity has been updated.
	 * 
	 * @return A string representation of this emulator instance
	 */
	@Override
	public String toString() {
		StringBuilder string = new StringBuilder();
		string.append("A:");
		string.append(String.format("%x", registerA));
		string.append(" X:");
		string.append(String.format("%x", registerX));
		string.append(" Y:");
		string.append(String.format("%x", registerY));
		string.append(" S:");
		string.append(String.format("%x", stackPointer));
		string.append(" T:");
		string.append(time);
		
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
		data.setByte("flags", flags);
		data.setByte("stackPointer", stackPointer);
		data.setShort("programCounter", programCounter);
		
		data.setByteArray("memory", memory);
		
		data.setString("state", state.toString());
		data.setLong("time", time);
		
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
		flags = data.getByte("flags");
		stackPointer = data.getByte("stackPointer");
		programCounter = data.getShort("programCounter");
		
		memory = data.getByteArray("memory");
		
		state = StateComputer.valueOf(data.getString("state"));
		time = data.getLong("time");
		
		super.readFromNBT(data);
	}
	
	/**
	 * This function is automatically called by Forge once
	 * per tick for all loaded TileEntitys. This is the
	 * main work function of the emulator; it increments
	 * the time and executes an instruction every time the
	 * TileEntity is updated. You may also call this
	 * function manually if you want the emulator to
	 * asynchronously execute a command.
	 */
	@Override
	public void updateEntity() {
		markDirty();
		time++;
		
		if(state != StateComputer.RUN) {return;}
		
		byte instruction = readMemory(programCounter);
		switch(InstructionComputer.getInstruction(instruction)) {
		case ADC_ABS:
			break;
		case ADC_ABS_X:
			break;
		case ADC_ABS_Y:
			break;
		case ADC_IMM:
			break;
		case ADC_IND_X:
			break;
		case ADC_IND_Y:
			break;
		case ADC_ZP:
			break;
		case ADC_ZP_X:
			break;
		case AND_ABS:
			break;
		case AND_ABS_X:
			break;
		case AND_ABS_Y:
			break;
		case AND_IMM:
			break;
		case AND_IND_X:
			break;
		case AND_IND_Y:
			break;
		case AND_ZP:
			break;
		case AND_ZP_X:
			break;
		case ASL_A:
			flags = setBit(flags, 0, testBit(registerA, 7));
			
			registerA <<= 1;
			flags = setBit(flags, 7, registerA<0);
			flags = setBit(flags, 1, registerA==0);
			break;
		case ASL_ABS:
			break;
		case ASL_ABS_X:
			break;
		case ASL_ZP:
			break;
		case ASL_ZP_X:
			break;
		case BCC_REL:
			break;
		case BCS_REL:
			break;
		case BEQ_REL:
			break;
		case BIT_ABS:
			break;
		case BIT_ZP:
			break;
		case BMI_REL:
			break;
		case BNE_REL:
			break;
		case BPL_REL:
			break;
		case BRK:
			flags = setBit(flags, 4, true);
			++programCounter;
			break;
		case BVC_REL:
			break;
		case BVS_REL:
			break;
		case CLC:
			flags = setBit(flags, 0, false);
			break;
		case CLD:
			flags = setBit(flags, 3, false);
			break;
		case CLI:
			flags = setBit(flags, 2, false);
			break;
		case CLV:
			flags = setBit(flags, 6, false);
			break;
		case CMP_ABS:
			break;
		case CMP_ABS_X:
			break;
		case CMP_ABS_Y:
			break;
		case CMP_IMM:
			break;
		case CMP_IND_X:
			break;
		case CMP_IND_Y:
			break;
		case CMP_ZP:
			break;
		case CMP_ZP_X:
			break;
		case CPX_ABS:
			break;
		case CPX_IMM:
			break;
		case CPX_ZP:
			break;
		case CPY_ABS:
			break;
		case CPY_IMM:
			break;
		case CPY_ZP:
			break;
		case DEC_ABS:
			break;
		case DEC_ABS_X:
			break;
		case DEC_ZP:
			break;
		case DEC_ZP_X:
			break;
		case DEX:
			registerX--;
			flags = setBit(flags, 7, registerX<0);
			flags = setBit(flags, 1, registerX==0);
			break;
		case DEY:
			registerY--;
			flags = setBit(flags, 7, registerY<0);
			flags = setBit(flags, 1, registerY==0);
			break;
		case EOR_ABS:
			break;
		case EOR_ABS_X:
			break;
		case EOR_ABS_Y:
			break;
		case EOR_IMM:
			break;
		case EOR_IND_X:
			break;
		case EOR_IND_Y:
			break;
		case EOR_ZP:
			break;
		case EOR_ZP_X:
			break;
		case INC_ABS:
			break;
		case INC_ABS_X:
			break;
		case INC_ZP:
			break;
		case INC_ZP_X:
			break;
		case INX:
			registerX++;
			flags = setBit(flags, 7, registerX<0);
			flags = setBit(flags, 1, registerX==0);
			break;
		case INY:
			registerY++;
			flags = setBit(flags, 7, registerY<0);
			flags = setBit(flags, 1, registerY==0);
			break;
		case JMP_ABS:
			break;
		case JMP_IND:
			break;
		case JSR_ABS:
			break;
		case LDA_ABS:
			break;
		case LDA_ABS_X:
			break;
		case LDA_ABS_Y:
			break;
		case LDA_IMM:
			registerA = readMemory(++programCounter);
			flags = setBit(flags, 7, registerA<0);
			flags = setBit(flags, 1, registerA==0);
			break;
		case LDA_IND_X:
			break;
		case LDA_IND_Y:
			break;
		case LDA_ZP:
			registerA = memory[readMemory(++programCounter)];
			flags = setBit(flags, 7, registerA<0);
			flags = setBit(flags, 1, registerA==0);
			break;
		case LDA_ZP_X:
			break;
		case LDX_ABS:
			break;
		case LDX_ABS_Y:
			break;
		case LDX_IMM:
			registerX = readMemory(++programCounter);
			flags = setBit(flags, 7, registerX<0);
			flags = setBit(flags, 1, registerX==0);
			break;
		case LDX_ZP:
			registerX = memory[readMemory(++programCounter)];
			flags = setBit(flags, 7, registerX<0);
			flags = setBit(flags, 1, registerX==0);
			break;
		case LDX_ZP_Y:
			break;
		case LDY_ABS:
			break;
		case LDY_ABS_X:
			break;
		case LDY_IMM:
			registerY = readMemory(++programCounter);
			flags = setBit(flags, 7, registerY<0);
			flags = setBit(flags, 1, registerY==0);
			break;
		case LDY_ZP:
			registerY = memory[readMemory(++programCounter)];
			flags = setBit(flags, 7, registerY<0);
			flags = setBit(flags, 1, registerY==0);
			break;
		case LDY_ZP_X:
			break;
		case LSR_A:
			flags = setBit(flags, 0, testBit(registerA, 0));
			
			registerA >>= 1;
			flags = setBit(flags, 7, registerA<0);
			flags = setBit(flags, 1, registerA==0);
			break;
		case LSR_ABS:
			break;
		case LSR_ABS_X:
			break;
		case LSR_ZP:
			break;
		case LSR_ZP_X:
			break;
		case NOP:
			break;
		case ORA_ABS:
			break;
		case ORA_ABS_X:
			break;
		case ORA_ABS_Y:
			break;
		case ORA_IMM:
			break;
		case ORA_IND_X:
			break;
		case ORA_IND_Y:
			break;
		case ORA_ZP:
			break;
		case ORA_ZP_X:
			break;
		case PHA:
			pushStack(registerA);
			break;
		case PHP:
			pushStack(flags);
			break;
		case PLA:
			registerA = pullStack();
			break;
		case PLP:
			flags = pullStack();
			break;
		case ROL_A:
			break;
		case ROL_ABS:
			break;
		case ROL_ABS_X:
			break;
		case ROL_ZP:
			break;
		case ROL_ZP_X:
			break;
		case ROR_A:
			break;
		case ROR_ABS:
			break;
		case ROR_ABS_X:
			break;
		case ROR_ZP:
			break;
		case ROR_ZP_X:
			break;
		case RTI:
			flags = pullStack();
			byte low = pullStack();
			byte high = pullStack();
			programCounter = (short) ((high << 8) | low);
			break;
		case RTS:
			byte low2 = pullStack();
			byte high2 = pullStack();
			programCounter = (short) ((high2 << 8) + low2);
			++programCounter;
			break;
		case SBC_ABS:
			break;
		case SBC_ABS_X:
			break;
		case SBC_ABS_Y:
			break;
		case SBC_IMM:
			byte value = readMemory(++programCounter);
			short result = (short) (((setBit((byte) 0x00, 7, testBit(flags, 0)) << 1) + registerA) - value);
			registerA = (byte) (result & 0xFF);
			
			flags = setBit(flags, 6, (result<-128 || result>127));
			flags = setBit(flags, 0, result>255);
			flags = setBit(flags, 1, result==0);
			flags = setBit(flags, 7, result<0);
			break;
		case SBC_IND_X:
			break;
		case SBC_IND_Y:
			break;
		case SBC_ZP:
			break;
		case SBC_ZP_X:
			break;
		case SEC:
			flags = setBit(flags, 0, true);
			break;
		case SED:
			flags = setBit(flags, 3, true);
			break;
		case SEI:
			flags = setBit(flags, 2, true);
			break;
		case STA_ABS:
			byte low5 = readMemory(++programCounter);
			byte high5 = readMemory(++programCounter);
			short address = (short) ((high5 << 8) | low5);
			
			writeMemory(address, registerA);
			break;
		case STA_ABS_X:
			break;
		case STA_ABS_Y:
			break;
		case STA_IND_X:
			break;
		case STA_IND_Y:
			break;
		case STA_ZP:
			memory[readMemory(++programCounter)] = registerA;
			break;
		case STA_ZP_X:
			break;
		case STP:
			setState(StateComputer.HALT);
			World world = getWorldObj();
			int yPlusOne = yCoord + 1;
			if(world.isAirBlock(xCoord, yPlusOne, zCoord)) {
				world.setBlock(xCoord, yPlusOne, zCoord, Blocks.fire);
			}
			break;
		case STX_ABS:
			byte low4 = readMemory(++programCounter);
			byte high4 = readMemory(++programCounter);
			short address2 = (short) ((high4 << 8) | low4);
			
			writeMemory(address2, registerX);
			break;
		case STX_ZP:
			memory[readMemory(++programCounter)] = registerX;
			break;
		case STX_ZP_Y:
			break;
		case STY_ABS:
			byte low3 = readMemory(++programCounter);
			byte high3 = readMemory(++programCounter);
			short address3 = (short) ((high3 << 8) | low3);
			
			writeMemory(address3, registerY);
			break;
		case STY_ZP:
			memory[readMemory(++programCounter)] = registerY;
			break;
		case STY_ZP_X:
			break;
		case TAX:
			registerX = registerA;
			flags = setBit(flags, 7, registerA<0);
			flags = setBit(flags, 1, registerA==0);
			break;
		case TAY:
			registerY = registerA;
			flags = setBit(flags, 7, registerA<0);
			flags = setBit(flags, 1, registerA==0);
			break;
		case TSX:
			registerX = stackPointer;
			break;
		case TXA:
			registerA = registerX;
			flags = setBit(flags, 7, registerX<0);
			flags = setBit(flags, 1, registerX==0);
			break;
		case TXS:
			stackPointer = registerX;
			break;
		case TYA:
			registerA = registerY;
			flags = setBit(flags, 7, registerY<0);
			flags = setBit(flags, 1, registerY==0);
			break;
		case UNUSED:
			setState(StateComputer.HALT);
			System.out.println("The computer has encountered an unknown instruction: " + String.format("%x", instruction));
			break;
		}
		
		programCounter++;
	}
	
}
