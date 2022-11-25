package fs.tdo.layer5;

import fs.tdo.Utils;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TomtelVm {
    final byte[] ram;
    final ByteArrayOutputStream out;
    private byte a;
    private byte b;
    private byte c;
    private byte d;
    private byte e;
    private byte f;
    private int la;
    private int lb;
    private int lc;
    private int ld;
    private int ptr;
    private int pc;

    public TomtelVm(byte[] ram) {
        this.ram = ram;
        out = new ByteArrayOutputStream();
    }

    public byte[] run() {
        runInstructions();
        return out.toByteArray();
    }

    private void runInstructions() {
        while (true) {
            InstructionWithBytes iwp = readInstruction();
            switch (iwp.instruction) {
                case HALT:
                    return;
                case ADD:
                    add();
                    break;
                case APTR:
                    aptr(iwp.bytes);
                    break;
                case CMP:
                    cmp();
                    break;
                case JEZ:
                    jez(iwp.bytes);
                    break;
                case JNZ:
                    jnz(iwp.bytes);
                    break;
                case MV:
                    mv(iwp.bytes);
                    break;
                case MV32:
                    mv32(iwp.bytes);
                    break;
                case MVI:
                    mvi(iwp.bytes);
                    break;
                case MVI32:
                    mvi32(iwp.bytes);
                    break;
                case OUT:
                    out();
                    break;
                case SUB:
                    sub();
                    break;
                case XOR:
                    xor();
                    break;
                default:
                    throw new RuntimeException("Instruction " + iwp.instruction + " not implemented");
            }
        }
    }

    /**
     * Reads one instruction from memory, at the address stored in the `pc` register.
     * Adds the byte size of the instruction to the `pc` register.
     */
    private InstructionWithBytes readInstruction() {
        Instruction instruction = Instruction.valueOf(ram[pc]);
        byte[] bytes = Utils.subarray(ram, pc, pc + instruction.length);
        InstructionWithBytes iwp = new InstructionWithBytes(instruction, bytes);
        pc += instruction.length;
        return iwp;
    }

    /**
     * --[ ADD a <- b ]--------------------------------------------
     *
     * <p>8-bit addition<br>
     * Opcode: 0xC2 (1 byte)
     *
     * <p>Sets `a` to the sum of `a` and `b`, modulo 256.
     */
    private void add() {
        a = (byte) ((Byte.toUnsignedInt(a) + Byte.toUnsignedInt(b)) % 256);
    }

    /**
     * --[ APTR imm8 ]---------------------------------------------
     *
     * <p>Advance ptr<br>
     * Opcode: 0xE1 0x__ (2 bytes)
     *
     * <p>Sets `ptr` to the sum of `ptr` and `imm8`. Overflow behaviour is undefined.
     */
    private void aptr(byte[] bytes) {
        ptr += Byte.toUnsignedInt(bytes[1]);
    }

    /**
     * --[ CMP ]---------------------------------------------------
     *
     * <p>Compare<br>
     * Opcode: 0xC1 (1 byte)
     * <p>Sets `f` to zero if `a` and `b` are equal, otherwise sets `f` to 0x01.
     */
    private void cmp() {
        f = a == b ? (byte) 0 : 0x01;
    }

    /**
     * --[ JEZ imm32 ]---------------------------------------------
     *
     * <p>Jump if equals zero<br>
     * Opcode: 0x21 0x__ 0x__ 0x__ 0x__ (5 bytes)
     *
     * <p>If `f` is equal to zero, sets `pc` to `imm32`. Otherwise does nothing.
     */
    private void jez(byte[] bytes) {
        if (f == 0) {
            ByteBuffer bb = ByteBuffer.wrap(bytes, 1, 4);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            pc = bb.getInt();
        }
    }

    /**
     * --[ JNZ imm32 ]---------------------------------------------
     *
     * <p>Jump if not zero<br>
     * Opcode: 0x22 0x__ 0x__ 0x__ 0x__ (5 bytes)
     *
     * <p>If `f` is not equal to zero, sets `pc` to `imm32`. Otherwise does nothing.
     */
    private void jnz(byte[] bytes) {
        if (f != 0) {
            ByteBuffer bb = ByteBuffer.wrap(bytes, 1, 4);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            pc = bb.getInt();
        }
    }

    /**
     * --[ MV {dest} <- {src} ]------------------------------------
     *
     * <p>Move 8-bit value<br>
     * Opcode: 0b01DDDSSS (1 byte)
     *
     * <p>Sets `{dest}` to the value of `{src}`.
     *
     * <p>Both `{dest}` and `{src}` are 3-bit unsigned integers that
     * correspond to an 8-bit register or pseudo-register. In the
     * opcode format above, the "DDD" bits are `{dest}`, and the
     * "SSS" bits are `{src}`. Below are the possible valid
     * values (in decimal) and their meaning.
     *
     * <p>1 => `a`<br>
     * 2 => `b`<br>
     * 3 => `c`<br>
     * 4 => `d`<br>
     * 5 => `e`<br>
     * 6 => `f`<br>
     * 7 => `(ptr+c)`
     *
     * <p>A zero `{src}` indicates an MVI instruction, not MV.
     */
    private void mv(byte[] bytes) {
        byte dest = (byte) ((bytes[0] & 0b00111000) >> 3);
        byte src = (byte) (bytes[0] & 0b00000111);
        if (src != 0) {
            setByteRegisterValue(dest, getByteRegisterValue(src));
        } else {
            mvi(bytes);
        }
    }

    /**
     * --[ MV32 {dest} <- {src} ]----------------------------------
     *
     * <p>Move 32-bit value<br>
     * Opcode: 0b10DDDSSS (1 byte)
     *
     * <p>Sets `{dest}` to the value of `{src}`.
     *
     * <p>Both `{dest}` and `{src}` are 3-bit unsigned integers that
     * correspond to a 32-bit register. In the opcode format
     * above, the "DDD" bits are `{dest}`, and the "SSS" bits are
     * `{src}`. Below are the possible valid values (in decimal)
     * and their meaning.
     *
     * <p>1 => `la`<br>
     * 2 => `lb`<br>
     * 3 => `lc`<br>
     * 4 => `ld`<br>
     * 5 => `ptr`<br>
     * 6 => `pc`<br>
     */
    private void mv32(byte[] bytes) {
        byte dest = (byte) ((bytes[0] & 0b00111000) >> 3);
        byte src = (byte) (bytes[0] & 0b00000111);
        setIntRegisterValue(dest, getIntRegisterValue(src));
    }


    /**
     * --[ MVI {dest} <- imm8 ]------------------------------------
     *
     * <p>Move immediate 8-bit value<br>
     * Opcode: 0b01DDD000 0x__ (2 bytes)
     *
     * <p>Sets `{dest}` to the value of `imm8`.
     *
     * <p>`{dest}` is a 3-bit unsigned integer that corresponds to
     * an 8-bit register or pseudo-register. It is the "DDD" bits
     * in the opcode format above. Below are the possible valid
     * values (in decimal) and their meaning.
     *
     * <p>1 => `a`<br>
     * 2 => `b`<br>
     * 3 => `c`<br>
     * 4 => `d`<br>
     * 5 => `e`<br>
     * 6 => `f`<br>
     * 7 => `(ptr+c)`<br>
     */
    private void mvi(byte[] bytes) {
        byte dest = (byte) ((bytes[0] & 0b00111000) >> 3);
        setByteRegisterValue(dest, bytes[1]);
    }

    /**
     * --[ MVI32 {dest} <- imm32 ]---------------------------------
     *
     * <p>Move immediate 32-bit value<br>
     * Opcode: 0b10DDD000 0x__ 0x__ 0x__ 0x__ (5 bytes)
     *
     * <p>Sets `{dest}` to the value of `imm32`.
     *
     * <p>`{dest}` is a 3-bit unsigned integer that corresponds to a
     * 32-bit register. It is the "DDD" bits in the opcode format
     * above. Below are the possible valid values (in decimal)
     * and their meaning.
     *
     * <p>1 => `la`<br>
     * 2 => `lb`<br>
     * 3 => `lc`<br>
     * 4 => `ld`<br>
     * 5 => `ptr`<br>
     * 6 => `pc`<br>
     */
    private void mvi32(byte[] bytes) {
        byte dest = (byte) ((bytes[0] & 0b00111000) >> 3);
        ByteBuffer bb = ByteBuffer.wrap(bytes, 1, 4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        setIntRegisterValue(dest, bb.getInt());
    }

    /**
     * --[ OUT a ]-------------------------------------------------
     *
     * <p>Output byte<br>
     * Opcode: 0x02 (1 byte)
     *
     * <p>Appends the value of `a` to the output stream.
     */
    private void out() {
        out.write(a);
    }

    /**
     * --[ SUB a <- b ]--------------------------------------------
     *
     * <p>8-bit subtraction<br>
     * Opcode: 0xC3 (1 byte)
     *
     * <p>Sets `a` to the result of subtracting `b` from `a`. If
     * subtraction would result in a negative number, 256 is
     * added to ensure that the result is non-negative.
     */
    private void sub() {
        int sub = Byte.toUnsignedInt(a) - Byte.toUnsignedInt(b);
        if (sub < 0) {
            sub += 256;
        }
        a = (byte) sub;
    }

    /**
     * --[ XOR a <- b ]--------------------------------------------
     *
     * <p>8-bit bitwise exclusive OR<br>
     * Opcode: 0xC4 (1 byte)
     *
     * <p>Sets `a` to the bitwise exclusive OR of `a` and `b`.
     */
    private void xor() {
        a = (byte) (a ^ b);
    }

    /*
     * 1 => `a`
     * 2 => `b`
     * 3 => `c`
     * 4 => `d`br>
     * 5 => `e`
     * 6 => `f`
     * 7 => `(ptr+c)`
     */
    private byte getByteRegisterValue(byte source) {
        switch (source) {
            case 1:
                return a;
            case 2:
                return b;
            case 3:
                return c;
            case 4:
                return d;
            case 5:
                return e;
            case 6:
                return f;
            case 7:
                return ram[ptr + Byte.toUnsignedInt(c)];
            default:
                throw new IllegalArgumentException("Byte "
                    + Integer.toHexString(source & 0xff)
                    + " does not represent a valid byte register");
        }
    }

    /*
     * 1 => `a`
     * 2 => `b`
     * 3 => `c`
     * 4 => `d`
     * 5 => `e`
     * 6 => `f`
     * 7 => `(ptr+c)`
     */
    private void setByteRegisterValue(byte dest, byte value) {
        switch (dest) {
            case 1:
                a = value;
                break;
            case 2:
                b = value;
                break;
            case 3:
                c = value;
                break;
            case 4:
                d = value;
                break;
            case 5:
                e = value;
                break;
            case 6:
                f = value;
                break;
            case 7:
                ram[ptr + Byte.toUnsignedInt(c)] = value;
                break;
            default:
                throw new IllegalArgumentException("Byte "
                    + Integer.toHexString(dest & 0xff)
                    + " does not represent a valid register");
        }
    }

    /*
     * 1 => `la`
     * 2 => `lb`
     * 3 => `lc`
     * 4 => `ld`
     * 5 => `ptr`
     * 6 => `pc`
     */
    private int getIntRegisterValue(byte source) {
        switch (source) {
            case 1:
                return la;
            case 2:
                return lb;
            case 3:
                return lc;
            case 4:
                return ld;
            case 5:
                return ptr;
            case 6:
                return pc;
            default:
                throw new IllegalArgumentException("Byte "
                    + Integer.toHexString(source & 0xff)
                    + " does not represent a valid int register");
        }
    }

    /*
     * 1 => `la`
     * 2 => `lb`
     * 3 => `lc`
     * 4 => `ld`
     * 5 => `ptr`
     * 6 => `pc`
     */
    private void setIntRegisterValue(byte source, int value) {
        switch (source) {
            case 1:
                la = value;
                break;
            case 2:
                lb = value;
                break;
            case 3:
                lc = value;
                break;
            case 4:
                ld = value;
                break;
            case 5:
                ptr = value;
                break;
            case 6:
                pc = value;
                break;
            default:
                throw new IllegalArgumentException("Byte "
                    + Integer.toHexString(source & 0xff)
                    + " does not represent a valid int register");
        }
    }
}
