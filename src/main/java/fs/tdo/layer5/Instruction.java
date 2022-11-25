package fs.tdo.layer5;

public enum Instruction {
    ADD((byte) 0xC2, (byte) 0xFF, 1),
    APTR((byte) 0xE1, (byte) 0xFF, 2),
    CMP((byte) 0xC1, (byte) 0xFF, 1),
    HALT((byte) 0x01, (byte) 0xFF, 1),
    JEZ((byte) 0x21, (byte) 0xFF, 5),
    JNZ((byte) 0x22, (byte) 0xFF, 5),
    MVI((byte) 0b01000000, (byte) 0b11000111, 2),
    MVI32((byte) 0b10000000, (byte) 0b11000111, 5),
    MV((byte) 0b01000000, (byte) 0b11000000, 1),
    MV32((byte) 0b10000000, (byte) 0b11000000, 1),
    OUT((byte) 0x02, (byte) 0xFF, 1),
    SUB((byte) 0xC3, (byte) 0xFF, 1),
    XOR((byte) 0xC4, (byte) 0xFF, 1);

    public final byte value;
    public final byte checkMask;

    public final int length;

    Instruction(byte value, byte checkMask, int length) {
        this.value = value;
        this.checkMask = checkMask;
        this.length = length;
    }

    public static Instruction valueOf(byte in) {
        for (Instruction inst : values()) {
            if ((in & inst.checkMask) == inst.value) {
                return inst;
            }
        }
        throw new IllegalArgumentException("Byte " + in + " is not an instruction byte");
    }
}
