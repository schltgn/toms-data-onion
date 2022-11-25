package fs.tdo.layer5;

public class InstructionWithBytes {
    final Instruction instruction;
    final byte[] bytes;

    InstructionWithBytes(Instruction instruction, byte[] bytes) {
        if (instruction.length != bytes.length) {
            throw new IllegalArgumentException("Instruction "
                + instruction
                + " needs "
                + instruction.length
                + " bytes.");
        }
        this.instruction = instruction;
        this.bytes = bytes;
    }
}
